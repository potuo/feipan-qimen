package com.potuo.feipanqimen2.qimen

/**
 * 格局检测结果
 * @param name 格局名
 * @param detail 具体落宫/干支说明
 * @param isAuspicious true=吉格 false=凶格 null=平格（提示性）
 */
data class PatternInfo(
    val name: String,
    val detail: String,
    val isAuspicious: Boolean?,
)

/**
 * 飞盘奇门格局自动检测（据《奇门鸣法》第三卷·占断法则）。
 * 覆盖：六仪击刑、入墓、门迫/受制/交和、伏吟/反吟、守门八格、空亡。
 */
object QimenPatternDetector {

    // ── 正格干支组合常量（据 vol3 第十五章·正格）──
    private val STEMS = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

    /** 天干五合：甲己、乙庚、丙辛、丁壬、戊癸 */
    private val STEM_HE = mapOf(
        "甲" to "己", "己" to "甲",
        "乙" to "庚", "庚" to "乙",
        "丙" to "辛", "辛" to "丙",
        "丁" to "壬", "壬" to "丁",
        "戊" to "癸", "癸" to "戊",
    )

    /** 支破格（双向）：庚癸、壬己、戊辛 */
    private val STEM_PO = setOf(
        setOf("庚", "癸"), setOf("壬", "己"), setOf("戊", "辛"),
    )

    /** 检测盘面格局，返回格局列表（空亡恒在最后） */
    fun detect(result: QimenResult): List<PatternInfo> {
        val patterns = mutableListOf<PatternInfo>()

        // 1. 六仪击刑（仅值符论）：值符遁仪干落宫犯刑
        val jiXingPalace = QimenConstants.JI_XING[result.xunShouStem]
        if (jiXingPalace != null && result.zhiFuPalace == jiXingPalace) {
            patterns.add(
                PatternInfo(
                    "六仪击刑",
                    "${result.xunShouStem}值符落${palaceName(jiXingPalace)}${jiXingPalace}宫，${jiXingDesc(result.xunShouStem)}",
                    false,
                ),
            )
        }

        // 2. 入墓：天盘干落其墓宫
        result.palaces.forEach { (palace, info) ->
            val tomb = QimenConstants.STEM_TOMB[info.heavenStem]
            if (tomb != null && tomb == palace) {
                patterns.add(
                    PatternInfo(
                        "入墓",
                        "${info.heavenStem}加${info.earthStem}落${palaceName(palace)}${palace}宫（${info.direction}），奇仪入墓主闭塞困顿",
                        false,
                    ),
                )
            }
        }

        // 3. 门迫 / 受制 / 交和（门与宫五行生克，中门不论）
        result.palaces.forEach { (palace, info) ->
            if (info.gate.isEmpty() || info.gate == "中") return@forEach
            val gateEl = QimenConstants.GATE_ELEMENT[info.gate] ?: return@forEach
            val palaceEl = QimenConstants.PALACE_ELEMENT[palace] ?: return@forEach
            when {
                QimenConstants.ELEMENT_CONTROLS[gateEl] == palaceEl -> patterns.add(
                    PatternInfo("门迫", "${info.gate}门（$gateEl）克${palaceName(palace)}宫（$palaceEl），门克宫主事有阻、被逼迫", false),
                )
                QimenConstants.ELEMENT_CONTROLS[palaceEl] == gateEl -> patterns.add(
                    PatternInfo("受制", "${info.gate}门（$gateEl）受${palaceName(palace)}宫（$palaceEl）之克，宫克门主事受制难行", false),
                )
                QimenConstants.ELEMENT_GENERATES[gateEl] == palaceEl -> patterns.add(
                    PatternInfo("交和", "${info.gate}门（$gateEl）生${palaceName(palace)}宫（$palaceEl），门生宫主和顺通达", true),
                )
            }
        }

        // 4. 伏吟 / 反吟（天盘干与地盘干同或相冲）
        result.palaces.forEach { (palace, info) ->
            if (info.heavenStem.isEmpty() || info.earthStem.isEmpty()) return@forEach
            if (info.heavenStem == info.earthStem) {
                patterns.add(
                    PatternInfo(
                        "伏吟",
                        "${info.heavenStem}加${info.earthStem}落${palaceName(palace)}宫，伏吟主事迟滞、原地打转",
                        false,
                    ),
                )
            } else if (QimenConstants.STEM_CONFLICT[info.heavenStem] == info.earthStem) {
                patterns.add(
                    PatternInfo(
                        "反吟",
                        "${info.heavenStem}加${info.earthStem}落${palaceName(palace)}宫，反吟主事反复、进退无常",
                        false,
                    ),
                )
            }
        }

        // 5. 守门八格（值使门落宫天盘干）
        result.palaces[result.zhiShiPalace]?.let { zhiShiPalaceInfo ->
            val shouMen = QimenConstants.SHOU_MEN[zhiShiPalaceInfo.heavenStem]
            if (shouMen != null) {
                patterns.add(
                    PatternInfo(
                        shouMen,
                        "值使${result.zhiShiGate}门加${zhiShiPalaceInfo.heavenStem}于${palaceName(result.zhiShiPalace)}宫",
                        null,
                    ),
                )
            }
        }

        // 6. 正格干支组合（进茹/退茹/前间/后间/五合/支破）
        result.palaces.forEach { (palace, info) ->
            val p = detectGanZhiPattern(info.heavenStem, info.earthStem, palace)
            if (p != null) patterns.add(p)
        }

        // 7. 空亡（提示性，恒最后）
        if (result.kongWang.isNotBlank()) {
            patterns.add(
                PatternInfo(
                    "空亡",
                    "旬空：${result.kongWang}。主虚而不实，逢空需待出空之期（填实/冲实）",
                    null,
                ),
            )
        }

        return patterns
    }

    private fun palaceName(palace: Int): String = QimenConstants.PALACE_NAMES[palace] ?: ""

    private fun jiXingDesc(stem: String): String = when (stem) {
        "戊" -> "子刑卯，主受欺辱、官非"
        "己" -> "戌刑未，主刑罚、斗争"
        "庚" -> "申刑寅，主官非、破财"
        "辛" -> "午自刑，主自寻烦恼、内耗"
        "壬" -> "辰自刑，主是非缠绕"
        "癸" -> "寅刑巳，主口舌、小人"
        else -> "主难受、欺辱、刑罚"
    }

    /** 正格干支组合检测：进茹/退茹/前间/后间/五合/支破，未命中返回 null */
    private fun detectGanZhiPattern(heaven: String, earth: String, palace: Int): PatternInfo? {
        if (heaven.isEmpty() || earth.isEmpty()) return null
        val hi = STEMS.indexOf(heaven)
        val ei = STEMS.indexOf(earth)
        if (hi < 0 || ei < 0) return null
        val loc = "${palaceName(palace)}${palace}宫"
        val gz = "${heaven}加$earth"

        // 支破（凶）
        if (STEM_PO.any { heaven in it && earth in it && heaven != earth }) {
            return PatternInfo("支破", "${gz}落$loc，谋为不就、诸事难成", false)
        }
        // 五合（吉）
        if (STEM_HE[heaven] == earth) {
            return PatternInfo("相合", "${gz}落$loc，天干相合主和合顺遂", true)
        }
        // 前间（隔一进，凶）
        if ((hi + 2) % 10 == ei && earth != "甲") {
            return PatternInfo("前间", "${gz}落$loc，前进中途有阻拦", false)
        }
        // 后间（隔一退，凶）
        if ((hi + 8) % 10 == ei && earth != "甲") {
            return PatternInfo("后间", "${gz}落$loc，后退当中犯艰难", false)
        }
        // 进茹（进一，中性提示）
        if ((hi + 1) % 10 == ei && earth != "甲") {
            return PatternInfo("进茹", "${gz}落$loc，宜于进步", null)
        }
        // 退茹（退一，中性提示）
        if ((hi + 9) % 10 == ei && earth != "甲") {
            return PatternInfo("退茹", "${gz}落$loc，宜退步为美", null)
        }
        return null
    }
}
