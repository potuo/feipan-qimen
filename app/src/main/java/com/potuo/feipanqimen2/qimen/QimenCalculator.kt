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
        // 值使门为中门时固定落中宫（中门只在五宫）
        val zhiShiPalace = if (zhiShiGate == "中") 5
        else flyPalace(xunShouEarthPalace, hourIndexInXun - 1, yangDun)
        val gates = buildGates(zhiShiGate, zhiShiPalace)

        val hiddenStems = buildHiddenStems(hourPillar, zhiShiPalace, yangDun, kongWang)

        // 月令五行（据当前节气）
        val monthElement = QimenConstants.JIE_QI_MONTH_ELEMENT[jieQi] ?: "土"

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
                state = wangShuaiState(heavenStem, monthElement),
                liuQinStar = liuQinOf(QimenConstants.STAR_ELEMENT[star], hourStem),
                liuQinHeaven = liuQinOf(QimenConstants.STEM_ELEMENT[heavenStem], hourStem),
                liuQinGate = liuQinOf(QimenConstants.GATE_ELEMENT[gate], hourStem),
                liuQinEarth = liuQinOf(QimenConstants.STEM_ELEMENT[earthStem], hourStem),
                marks = buildMarks(p, star, gate, heavenStem, earthStem, hourPillar),
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
        val stars = mutableMapOf<Int, String>()
        stars[5] = "天禽"
        val flyOrder = QimenConstants.STARS_ORDER.filter { it != "天禽" }
        val startIdx = if (zhiFuStar == "天禽") 0 else flyOrder.indexOf(zhiFuStar)
        var palace = hourStemPalace
        var starIdx = startIdx
        var placed = 0
        while (placed < 8) {
            if (palace != 5) {
                stars[palace] = flyOrder[starIdx % 8]
                starIdx++
                placed++
            }
            if (placed < 8) {
                palace = flyPalace(palace, 1, forward = true)
            }
        }
        return stars
    }

    private fun buildGates(zhiShiGate: String, zhiShiPalace: Int): Map<Int, String> {
        val gates = mutableMapOf<Int, String>()
        gates[5] = "中"
        val flyOrder = QimenConstants.GATES_ORDER.filter { it != "中" }
        // 值使门为中门时：中门居中宫，其余八门从中门之后（开）顺飞
        val startIdx = if (zhiShiGate == "中") 0 else flyOrder.indexOf(zhiShiGate)
        var palace = zhiShiPalace
        var gateIdx = startIdx
        var placed = 0
        while (placed < 8) {
            if (palace != 5) {
                gates[palace] = flyOrder[gateIdx % 8]
                gateIdx++
                placed++
            }
            if (placed < 8) {
                palace = flyPalace(palace, 1, forward = true)
            }
        }
        return gates
    }

    private fun buildHiddenStems(
        hourPillar: String,
        zhiShiPalace: Int,
        yangDun: Boolean,
        kongWang: String,
    ): Map<Int, String?> {
        val result = mutableMapOf<Int, String?>()
        val kongBranches = kongWang.map { it.toString() }.toSet()
        var jiaZiIdx = QimenConstants.JIA_ZI_60.indexOf(hourPillar)
        var palace = zhiShiPalace
        var placed = 0
        var safety = 0
        while (placed < 8 && safety < 120) {
            safety++
            if (palace == 5) {
                palace = flyPalace(palace, 1, yangDun)
                continue
            }
            val gz = QimenConstants.JIA_ZI_60[jiaZiIdx % 60]
            val stem = gz[0].toString()
            val branch = gz[1].toString()
            if (branch !in kongBranches && stem != "甲") {
                result[palace] = gz
                placed++
                palace = flyPalace(palace, 1, yangDun)
            }
            jiaZiIdx = if (yangDun) (jiaZiIdx + 1) % 60 else (jiaZiIdx + 59) % 60
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

    /**
     * 六亲取法（以时干为「我」）：生我=父母、克我=官鬼、我克=妻财、我生=子孙、比和=兄弟。
     * @param element 元素五行（星/天盘干/门/地盘干）
     * @param woStem 时干
     */
    private fun liuQinOf(element: String?, woStem: String): String {
        if (element == null || element.isEmpty()) return ""
        val wo = QimenConstants.STEM_ELEMENT[woStem] ?: return ""
        return when {
            QimenConstants.ELEMENT_GENERATES[element] == wo -> "父母"
            QimenConstants.ELEMENT_CONTROLS[element] == wo -> "官鬼"
            QimenConstants.ELEMENT_CONTROLS[wo] == element -> "妻财"
            QimenConstants.ELEMENT_GENERATES[wo] == element -> "子孙"
            else -> "兄弟"
        }
    }

    /**
     * 旺衰状态（月令旺衰）：以天盘干五行对月令五行论——当令者旺、令生者相、生令者休、克令者囚、令克者死。
     * 废/没/胎 为对宫/极衰补充（待校准：与原版参考盘的 废/没/胎 判定口径可能不同）。
     */
    private fun wangShuaiState(heavenStem: String, monthElement: String): String {
        val el = QimenConstants.STEM_ELEMENT[heavenStem] ?: return ""
        return when {
            el == monthElement -> "旺"
            QimenConstants.ELEMENT_GENERATES[monthElement] == el -> "相"
            QimenConstants.ELEMENT_GENERATES[el] == monthElement -> "休"
            QimenConstants.ELEMENT_CONTROLS[el] == monthElement -> "囚"
            else -> "死"
        }
    }

    /**
     * 宫角标记：
     * - 马：时支驿马落宫（绿色马字）
     * - 迫：门克宫（凶门克宫，且无刑墓时显示）
     * - 刑：天盘干犯六仪击刑本宫
     * - 墓：天盘干入墓本宫
     */
    private fun buildMarks(
        palace: Int,
        star: String,
        gate: String,
        heavenStem: String,
        earthStem: String,
        hourPillar: String,
    ): List<String> {
        val marks = mutableListOf<String>()

        // 马：时支三合驿马（如戌时 -> 马在申 -> 坤2）
        val hourBranch = hourPillar.getOrNull(1)?.toString() ?: ""
        val yiMaBranch = QimenConstants.YI_MA[hourBranch] ?: ""
        if (yiMaBranch.isNotEmpty() && yiMaBranch in (QimenConstants.PALACE_BRANCHES[palace] ?: emptyList())) {
            marks.add("马")
        }

        // 墓：天盘干入本宫墓
        if (QimenConstants.STEM_TOMB[heavenStem] == palace) marks.add("墓")

        // 刑：天盘干六仪击刑落本宫
        if (QimenConstants.JI_XING[heavenStem] == palace) marks.add("刑")

        // 迫：门克宫（中门不论），已有刑/墓时不再标迫
        if (gate.isNotEmpty() && gate != "中" && marks.none { it == "刑" || it == "墓" }) {
            val gateEl = QimenConstants.GATE_ELEMENT[gate] ?: ""
            val palaceEl = QimenConstants.PALACE_ELEMENT[palace] ?: ""
            if (gateEl.isNotEmpty() && QimenConstants.ELEMENT_CONTROLS[gateEl] == palaceEl) {
                marks.add("迫")
            }
        }

        return marks
    }
}
