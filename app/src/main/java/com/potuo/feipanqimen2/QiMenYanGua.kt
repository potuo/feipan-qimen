package com.potuo.feipanqimen2

import com.potuo.feipanqimen2.qimen.QimenConstants

/**
 * 奇门演卦（据《奇门基础资料 2023版教》第三卷·第十四章）。
 *
 * 星门与八卦对应（教材原文）：
 *   天蓬↔休门↔坎、天任↔生门↔艮、天冲↔伤门↔震、天辅↔杜门↔巽、
 *   天英↔景门↔离、天芮↔死门↔坤、天柱↔惊门↔兑、天心↔开门↔乾、天禽↔中门↔坤。
 *
 * 演卦法（教材「主要应用星门演卦、门宫演卦」）：
 *   - 星门演卦：星为上卦、门为下卦，叠成六爻卦。
 *   - 门宫演卦：门为上卦、宫为下卦，叠成六爻卦。
 * ⚠️ 教材未明写上下卦顺序，此处「星上门下 / 门上宫下」为约定，可据实调整。
 */
object QiMenYanGua {

    // 八卦序数：乾1 兑2 离3 震4 巽5 坎6 艮7 坤8
    private const val QIAN = 1; private const val DUI = 2; private const val LI = 3
    private const val ZHEN = 4; private const val XUN = 5; private const val KAN = 6
    private const val GEN = 7; private const val KUN = 8

    private val GUA_NAME = mapOf(
        QIAN to "乾", DUI to "兑", LI to "离", ZHEN to "震",
        XUN to "巽", KAN to "坎", GEN to "艮", KUN to "坤",
    )

    private val STAR_GUA = mapOf(
        "天蓬" to KAN, "天芮" to KUN, "天冲" to ZHEN, "天辅" to XUN, "天禽" to KUN,
        "天心" to QIAN, "天柱" to DUI, "天任" to GEN, "天英" to LI,
    )

    private val GATE_GUA = mapOf(
        "休" to KAN, "死" to KUN, "伤" to ZHEN, "杜" to XUN, "中" to KUN,
        "开" to QIAN, "惊" to DUI, "生" to GEN, "景" to LI,
    )

    private val PALACE_GUA = mapOf(
        1 to KAN, 2 to KUN, 3 to ZHEN, 4 to XUN, 5 to KUN,
        6 to QIAN, 7 to DUI, 8 to GEN, 9 to LI,
    )

    /** 64 卦名表：key = "上卦序,下卦序" */
    private val HEXAGRAM = mapOf(
        // 上乾
        "1,1" to "乾为天", "1,2" to "天泽履", "1,3" to "天火同人", "1,4" to "天雷无妄",
        "1,5" to "天风姤", "1,6" to "天水讼", "1,7" to "天山遁", "1,8" to "天地否",
        // 上兑
        "2,1" to "泽天夬", "2,2" to "兑为泽", "2,3" to "泽火革", "2,4" to "泽雷随",
        "2,5" to "泽风大过", "2,6" to "泽水困", "2,7" to "泽山咸", "2,8" to "泽地萃",
        // 上离
        "3,1" to "火天大有", "3,2" to "火泽睽", "3,3" to "离为火", "3,4" to "火雷噬嗑",
        "3,5" to "火风鼎", "3,6" to "火水未济", "3,7" to "火山旅", "3,8" to "火地晋",
        // 上震
        "4,1" to "雷天大壮", "4,2" to "雷泽归妹", "4,3" to "雷火丰", "4,4" to "震为雷",
        "4,5" to "雷风恒", "4,6" to "雷水解", "4,7" to "雷山小过", "4,8" to "雷地豫",
        // 上巽
        "5,1" to "风天小畜", "5,2" to "风泽中孚", "5,3" to "风火家人", "5,4" to "风雷益",
        "5,5" to "巽为风", "5,6" to "风水涣", "5,7" to "风山渐", "5,8" to "风地观",
        // 上坎
        "6,1" to "水天需", "6,2" to "水泽节", "6,3" to "水火既济", "6,4" to "水雷屯",
        "6,5" to "水风井", "6,6" to "坎为水", "6,7" to "水山蹇", "6,8" to "水地比",
        // 上艮
        "7,1" to "山天大畜", "7,2" to "山泽损", "7,3" to "山火贲", "7,4" to "山雷颐",
        "7,5" to "山风蛊", "7,6" to "山水蒙", "7,7" to "艮为山", "7,8" to "山地剥",
        // 上坤
        "8,1" to "地天泰", "8,2" to "地泽临", "8,3" to "地火明夷", "8,4" to "地雷复",
        "8,5" to "地风升", "8,6" to "地水师", "8,7" to "地山谦", "8,8" to "坤为地",
    )

    data class YanGua(
        val hexagram: String,      // 六爻卦名，如「乾为天」
        val upperDesc: String,     // 上卦描述，如「天心(乾)」
        val lowerDesc: String,     // 下卦描述，如「开门(乾)」
    )

    /** 星门演卦：星为上卦、门为下卦 */
    fun xingMenYanGua(star: String, gate: String): YanGua? {
        val upper = STAR_GUA[star] ?: return null
        val lower = GATE_GUA[gate] ?: return null
        val name = HEXAGRAM["$upper,$lower"] ?: return null
        return YanGua(name, "$star(${GUA_NAME[upper]})", "${gate}门(${GUA_NAME[lower]})")
    }

    /** 门宫演卦：门为上卦、宫为下卦 */
    fun menGongYanGua(gate: String, palace: Int): YanGua? {
        val upper = GATE_GUA[gate] ?: return null
        val lower = PALACE_GUA[palace] ?: return null
        val name = HEXAGRAM["$upper,$lower"] ?: return null
        val palaceName = QimenConstants.PALACE_NAMES[palace] ?: ""
        return YanGua(name, "${gate}门(${GUA_NAME[upper]})", "${palaceName}${palace}宫(${GUA_NAME[lower]})")
    }
}
