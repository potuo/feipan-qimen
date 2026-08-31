package com.potuo.feipanqimen2.qimen

import com.nlf.calendar.EightChar
import com.nlf.calendar.JieQi
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDateTime

/**
 * 飞盘奇门排盘核心（鸣法体系）。
 * 值使门落宫采用飞宫法：从旬首宫按六十甲子序数阳顺阴逆数到当前时辰。
 */
object QimenCalculator {

    fun calculate(dateTime: LocalDateTime): QimenResult {
        val solar = Solar.fromYmdHms(
            dateTime.year, dateTime.monthValue, dateTime.dayOfMonth,
            dateTime.hour, dateTime.minute, 0,
        )
        val lunar = solar.lunar
        val eightChar = lunar.eightChar
        eightChar.sect = 1 // 日柱以子时(23:00)分界

        val yearPillar = eightChar.year
        val monthPillar = eightChar.month
        val dayPillar = eightChar.day
        val hourPillar = eightChar.time

        val jieQi = findCurrentJieQi(solar)
        val yangDun = isYangDun(solar)
        val yuan = determineYuan(solar, lunar)
        val juNumber = lookupJuNumber(jieQi, yuan, yangDun)
        val dunType = if (yangDun) "阳遁" else "阴遁"

        val xunShou = findXunShou(hourPillar)
        val xunShouStem = QimenConstants.XUN_SHOU_STEM[xunShou]!!
        val kongWang = findKongWang(xunShou)

        val earthPlate = buildEarthPlate(juNumber, yangDun)
        val hourStem = hourPillar[0].toString()
        val xunShouEarthPalace = findStemPalace(earthPlate, xunShouStem)
        // 时干为甲（甲隐遁）：值符加时干时落旬首宫（值符归位）
        val hourStemPalace = if (hourStem == "甲") xunShouEarthPalace
        else findStemPalace(earthPlate, hourStem)

        val heavenPlate = buildHeavenPlate(earthPlate, xunShouStem, hourStemPalace)
        val gods = buildGods(hourStemPalace, yangDun)
        // 地盘神：值符神从旬首遁仪地盘宫起布（阴遁逆飞/阳遁顺飞）
        val earthGods = buildGods(xunShouEarthPalace, yangDun)

        val zhiFuStar = QimenConstants.STAR_ORIGINAL[xunShouEarthPalace]!!
        val stars = buildStars(zhiFuStar, hourStemPalace)

        val zhiShiGate = QimenConstants.GATE_ORIGINAL[xunShouEarthPalace]!!
        val hourIndexInXun = findHourIndexInXun(hourPillar, xunShou)
        // 值使门落宫：从旬首遁仪地盘宫起，按六十甲子序数阳顺阴逆飞布（值使为中门时同样飞布，不固定居中）
        val zhiShiPalace = flyPalace(xunShouEarthPalace, hourIndexInXun - 1, yangDun)
        val gates = buildGates(zhiShiGate, zhiShiPalace)

        val hiddenStems = buildHiddenStems(hourPillar, zhiShiPalace, yangDun)

        // 月令五行（据当前节气）
        val monthElement = QimenPalaceEnhancer.monthElementOf(jieQi)

        val palaces = (1..9).associateWith { p ->
            val star = stars[p] ?: ""
            val gate = gates[p] ?: ""
            val heavenStem = heavenPlate[p] ?: ""
            val earthStem = earthPlate[p] ?: ""
            PalaceInfo(
                palace = p,
                direction = QimenConstants.PALACE_NAMES[p]!!,
                god = gods[p] ?: "",
                star = star,
                heavenStem = heavenStem,
                gate = gate,
                earthStem = earthStem,
                hiddenStem = hiddenStems[p],
                earthGod = earthGods[p] ?: "",
                state = QimenPalaceEnhancer.wangShuaiState(heavenStem, monthElement),
                liuQinStar = QimenPalaceEnhancer.liuQinOf(QimenConstants.STAR_ELEMENT[star], hourStem),
                liuQinHeaven = QimenPalaceEnhancer.liuQinOf(QimenConstants.STEM_ELEMENT[heavenStem], hourStem),
                liuQinGate = QimenPalaceEnhancer.liuQinOf(QimenConstants.GATE_ELEMENT[gate], hourStem),
                liuQinEarth = QimenPalaceEnhancer.liuQinOf(QimenConstants.STEM_ELEMENT[earthStem], hourStem),
                marks = QimenPalaceEnhancer.buildMarks(p, gate, heavenStem, hourPillar),
            )
        }

        val siZhu = "${yearPillar}年 ${monthPillar}月 ${dayPillar}日 ${hourPillar}时"

        return QimenResult(
            yearPillar = yearPillar,
            monthPillar = monthPillar,
            dayPillar = dayPillar,
            hourPillar = hourPillar,
            siZhu = siZhu,
            jieQi = jieQi,
            yuan = yuan,
            dunType = dunType,
            juNumber = juNumber,
            xunShou = xunShou,
            xunShouStem = xunShouStem,
            kongWang = kongWang,
            zhiFuStar = zhiFuStar,
            zhiFuPalace = hourStemPalace,
            zhiShiGate = zhiShiGate,
            zhiShiPalace = zhiShiPalace,
            palaces = palaces,
        )
    }

    fun flyPalace(start: Int, steps: Int, forward: Boolean): Int {
        var p = start
        val dir = if (forward) 1 else -1
        repeat(steps) {
            p += dir
            if (p > 9) p = 1
            if (p < 1) p = 9
        }
        return p
    }

    private fun Solar.sortKey(): Long =
        year.toLong() * 1_00_00_00_00_00L + month * 1_00_00_00_00L + day * 1_00_00_00L +
            hour * 1_00_00L + minute * 1_00L + second

    private fun findCurrentJieQi(solar: Solar): String {
        val lunar = solar.lunar
        val table = lunar.jieQiTable
        var current = "冬至"
        var latestTime = 0L
        val now = solar.sortKey()
        for ((name, jqSolar) in table) {
            val jqTime = jqSolar.sortKey()
            if (jqTime <= now && jqTime >= latestTime) {
                if (isJieQi(name)) {
                    latestTime = jqTime
                    current = name
                }
            }
        }
        return current
    }

    private fun isJieQi(name: String): Boolean =
        QimenConstants.YANG_JU_TABLE.containsKey(name) || QimenConstants.YIN_JU_TABLE.containsKey(name)

    private fun isYangDun(solar: Solar): Boolean {
        val table = solar.lunar.jieQiTable
        var lastDongZhi = Long.MIN_VALUE
        var lastXiaZhi = Long.MIN_VALUE
        val now = solar.sortKey()
        for ((name, jqSolar) in table) {
            val t = jqSolar.sortKey()
            if (t <= now) {
                when (name) {
                    "冬至" -> if (t > lastDongZhi) lastDongZhi = t
                    "夏至" -> if (t > lastXiaZhi) lastXiaZhi = t
                }
            }
        }
        return lastDongZhi > lastXiaZhi
    }

    private fun determineYuan(solar: Solar, lunar: Lunar): String {
        val fuTouSolar = findFuTouDay(solar)
        val fuTouLunar = fuTouSolar.lunar
        val branch = fuTouLunar.dayInGanZhi[1]
        return when {
            branch in QimenConstants.SHANG_YUAN_BRANCHES -> "上元"
            branch in QimenConstants.ZHONG_YUAN_BRANCHES -> "中元"
            else -> "下元"
        }
    }

    private fun findFuTouDay(solar: Solar): Solar {
        val jieQiStart = findJieQiStartSolar(solar)
        var candidate = solar
        var best: Solar? = null
        while (candidate.sortKey() >= jieQiStart.sortKey()) {
            val gz = candidate.lunar.dayInGanZhi
            val stem = gz[0]
            if (stem == '甲' || stem == '己') {
                best = candidate
                break
            }
            candidate = candidate.next(-1)
        }
        return best ?: solar
    }

    private fun findJieQiStartSolar(solar: Solar): Solar {
        val table = solar.lunar.jieQiTable
        var latest: Solar? = null
        var latestTime = 0L
        val now = solar.sortKey()
        for ((name, jqSolar) in table) {
            if (!isJieQi(name)) continue
            val t = jqSolar.sortKey()
            if (t <= now && t >= latestTime) {
                latestTime = t
                latest = jqSolar
            }
        }
        return latest ?: solar
    }

    private fun lookupJuNumber(jieQi: String, yuan: String, yangDun: Boolean): Int {
        val table = if (yangDun) QimenConstants.YANG_JU_TABLE else QimenConstants.YIN_JU_TABLE
        val juList = table[jieQi] ?: table.values.first()
        return when (yuan) {
            "上元" -> juList[0]
            "中元" -> juList[1]
            else -> juList[2]
        }
    }

    private fun buildEarthPlate(juNumber: Int, yangDun: Boolean): Map<Int, String> {
        val plate = mutableMapOf<Int, String>()
        var palace = juNumber
        for (stem in QimenConstants.STEMS_ORDER) {
            plate[palace] = stem
            palace = flyPalace(palace, 1, yangDun)
        }
        return plate
    }

    private fun findStemPalace(earthPlate: Map<Int, String>, stem: String): Int =
        earthPlate.entries.first { it.value == stem }.key

    private fun buildHeavenPlate(
        earthPlate: Map<Int, String>,
        xunShouStem: String,
        hourStemPalace: Int,
    ): Map<Int, String> {
        val xunShouPalace = findStemPalace(earthPlate, xunShouStem)
        val shift = calcForwardSteps(xunShouPalace, hourStemPalace)
        return (1..9).associateWith { p ->
            val sourcePalace = flyPalace(p, shift, forward = false)
            earthPlate[sourcePalace]!!
        }
    }

    private fun calcForwardSteps(from: Int, to: Int): Int {
        var steps = 0
        var p = from
        while (p != to && steps < 9) {
            p = flyPalace(p, 1, forward = true)
            steps++
        }
        return steps
    }

    private fun buildGods(valueFuPalace: Int, yangDun: Boolean): Map<Int, String> {
        val gods = mutableMapOf<Int, String>()
        var palace = valueFuPalace
        for (god in QimenConstants.GODS_ORDER) {
            gods[palace] = god
            palace = flyPalace(palace, 1, yangDun)
        }
        return gods
    }

    private fun buildStars(zhiFuStar: String, hourStemPalace: Int): Map<Int, String> {
        // 教材（鸣法）：星序含天禽，值符星加时干宫后按星序顺飞 9 宫（天禽参与飞布，不固定居中）
        val stars = mutableMapOf<Int, String>()
        val startIdx = QimenConstants.STARS_ORDER.indexOf(zhiFuStar)
        var palace = hourStemPalace
        for (i in 0 until 9) {
            stars[palace] = QimenConstants.STARS_ORDER[(startIdx + i) % 9]
            palace = flyPalace(palace, 1, forward = true)
        }
        return stars
    }

    private fun buildGates(zhiShiGate: String, zhiShiPalace: Int): Map<Int, String> {
        // 教材（鸣法）：门序含中门，值使门落宫后按门序顺飞 9 宫（中门参与飞布，不固定居中）
        val gates = mutableMapOf<Int, String>()
        val startIdx = QimenConstants.GATES_ORDER.indexOf(zhiShiGate)
        var palace = zhiShiPalace
        for (i in 0 until 9) {
            gates[palace] = QimenConstants.GATES_ORDER[(startIdx + i) % 9]
            palace = flyPalace(palace, 1, forward = true)
        }
        return gates
    }

    /**
     * 布暗干支（据《暗干排法》：旬内回绕飞宫法）
     * 时干支加值使门落宫；暗干序列 = 本旬十个干支从时干支起正序循环（回绕过旬首甲，即「遇甲不排」）；
     * 宫位阳遁顺飞、阴遁逆飞；中宫也参与飞布（不跳过）。
     * 注：旬内十支天然不含本旬空亡地支（空亡=旬首后第10/11位地支，必在旬外），故「空亡不排」自动满足。
     */
    private fun buildHiddenStems(
        hourPillar: String,
        zhiShiPalace: Int,
        yangDun: Boolean,
    ): Map<Int, String?> {
        val result = mutableMapOf<Int, String?>()
        result[zhiShiPalace] = hourPillar
        // 本旬十干支（甲子旬~癸亥旬，每旬10个）
        val hourIdx = QimenConstants.JIA_ZI_60.indexOf(hourPillar)
        val xunStart = (hourIdx / 10) * 10
        val start = hourIdx - xunStart // 时干支在旬内位置（0=旬首甲）
        var palace = zhiShiPalace
        var placed = 0
        var step = 1
        while (placed < 8 && step < 12) {
            val gz = QimenConstants.JIA_ZI_60[xunStart + (start + step) % 10]
            if (gz[0] != '甲') {
                palace = flyPalace(palace, 1, yangDun)
                result[palace] = gz
                placed++
            }
            step++
        }
        return result
    }

    private fun findXunShou(dayPillar: String): String {
        val idx = QimenConstants.JIA_ZI_60.indexOf(dayPillar)
        val xunStart = (idx / 10) * 10
        return QimenConstants.JIA_ZI_60[xunStart]
    }

    private fun findKongWang(xunShou: String): String {
        val idx = QimenConstants.JIA_ZI_60.indexOf(xunShou)
        val branches = "子丑寅卯辰巳午未申酉戌亥"
        val b1 = branches[(idx + 10) % 12]
        val b2 = branches[(idx + 11) % 12]
        return "$b1$b2"
    }

    private fun findHourIndexInXun(hourPillar: String, xunShou: String): Int {
        val xunStart = QimenConstants.JIA_ZI_60.indexOf(xunShou)
        val hourIdx = QimenConstants.JIA_ZI_60.indexOf(hourPillar)
        return hourIdx - xunStart + 1
    }
}
