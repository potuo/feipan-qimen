package com.potuo.feipanqimen2.qimen

object QimenConstants {
    val PALACE_NAMES = mapOf(
        1 to "坎", 2 to "坤", 3 to "震", 4 to "巽", 5 to "中",
        6 to "乾", 7 to "兑", 8 to "艮", 9 to "离",
    )

    val STEMS_ORDER = listOf("戊", "己", "庚", "辛", "壬", "癸", "丁", "丙", "乙")

    val GODS_ORDER = listOf("值符", "螣蛇", "太阴", "六合", "勾陈", "太常", "朱雀", "九地", "九天")

    val STARS_ORDER = listOf("天蓬", "天芮", "天冲", "天辅", "天禽", "天心", "天柱", "天任", "天英")

    val GATES_ORDER = listOf("休", "死", "伤", "杜", "中", "开", "惊", "生", "景")

    val STAR_ORIGINAL = mapOf(
        1 to "天蓬", 2 to "天芮", 3 to "天冲", 4 to "天辅", 5 to "天禽",
        6 to "天心", 7 to "天柱", 8 to "天任", 9 to "天英",
    )

    val GATE_ORIGINAL = mapOf(
        1 to "休", 2 to "死", 3 to "伤", 4 to "杜", 5 to "中",
        6 to "开", 7 to "惊", 8 to "生", 9 to "景",
    )

    val XUN_SHOU_STEM = mapOf(
        "甲子" to "戊", "甲戌" to "己", "甲申" to "庚",
        "甲午" to "辛", "甲辰" to "壬", "甲寅" to "癸",
    )

    val JIA_ZI_60 = buildList {
        val stems = listOf('甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸')
        val branches = listOf('子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥')
        for (i in 0 until 60) {
            add("${stems[i % 10]}${branches[i % 12]}")
        }
    }

    val SHANG_YUAN_BRANCHES = setOf('子', '午', '卯', '酉')
    val ZHONG_YUAN_BRANCHES = setOf('寅', '申', '巳', '亥')
    val XIA_YUAN_BRANCHES = setOf('辰', '戌', '丑', '未')

    val YANG_JU_TABLE = mapOf(
        "冬至" to listOf(1, 7, 4),
        "小寒" to listOf(2, 8, 5),
        "大寒" to listOf(3, 9, 6),
        "立春" to listOf(8, 5, 2),
        "雨水" to listOf(9, 6, 3),
        "惊蛰" to listOf(1, 7, 4),
        "春分" to listOf(3, 9, 6),
        "清明" to listOf(4, 1, 7),
        "谷雨" to listOf(5, 2, 8),
        "立夏" to listOf(4, 1, 7),
        "小满" to listOf(5, 2, 8),
        "芒种" to listOf(6, 3, 9),
    )

    val YIN_JU_TABLE = mapOf(
        "夏至" to listOf(9, 3, 6),
        "小暑" to listOf(8, 2, 5),
        "大暑" to listOf(7, 1, 4),
        "立秋" to listOf(2, 5, 8),
        "处暑" to listOf(1, 4, 7),
        "白露" to listOf(9, 3, 6),
        "秋分" to listOf(7, 1, 4),
        "寒露" to listOf(6, 9, 3),
        "霜降" to listOf(5, 8, 2),
        "立冬" to listOf(6, 9, 3),
        "小雪" to listOf(5, 8, 2),
        "大雪" to listOf(4, 7, 1),
    )

    val HOUR_NAMES = listOf(
        "子时", "丑时", "寅时", "卯时", "辰时", "巳时",
        "午时", "未时", "申时", "酉时", "戌时", "亥时",
    )

    val HOUR_RANGES = listOf(
        23 to 1, 1 to 3, 3 to 5, 5 to 7, 7 to 9, 9 to 11,
        11 to 13, 13 to 15, 15 to 17, 17 to 19, 19 to 21, 21 to 23,
    )

    // ── 格局检测所需常量（据《奇门基础资料 2023版教》第三卷）──

    /** 宫位五行：1坎水 2坤土 3震木 4巽木 5中土 6乾金 7兑金 8艮土 9离火 */
    val PALACE_ELEMENT = mapOf(
        1 to "水", 2 to "土", 3 to "木", 4 to "木", 5 to "土",
        6 to "金", 7 to "金", 8 to "土", 9 to "火",
    )

    /** 八门五行（中门同土） */
    val GATE_ELEMENT = mapOf(
        "休" to "水", "死" to "土", "伤" to "木", "杜" to "木", "中" to "土",
        "开" to "金", "惊" to "金", "生" to "土", "景" to "火",
    )

    /** 九星五行 */
    val STAR_ELEMENT = mapOf(
        "天蓬" to "水", "天芮" to "土", "天冲" to "木", "天辅" to "木", "天禽" to "土",
        "天心" to "金", "天柱" to "金", "天任" to "土", "天英" to "火",
    )

    /** 五行相生：木→火→土→金→水→木 */
    val ELEMENT_GENERATES = mapOf("木" to "火", "火" to "土", "土" to "金", "金" to "水", "水" to "木")

    /** 五行相克：木→土→水→火→金→木 */
    val ELEMENT_CONTROLS = mapOf("木" to "土", "土" to "水", "水" to "火", "火" to "金", "金" to "木")

    /** 天干相冲：甲庚、乙辛、丙壬、丁癸（戊己居中不论冲） */
    val STEM_CONFLICT = mapOf(
        "甲" to "庚", "庚" to "甲", "乙" to "辛", "辛" to "乙",
        "丙" to "壬", "壬" to "丙", "丁" to "癸", "癸" to "丁",
    )

    /** 六仪击刑（仅值符论）：值符遁仪干 -> 击刑宫 */
    val JI_XING = mapOf(
        "戊" to 3, "己" to 2, "庚" to 8, "辛" to 9, "壬" to 4, "癸" to 4,
    )

    /** 入墓：天盘干 -> 墓宫（丙丁乾6 / 甲乙坤2 / 癸壬巽4 / 庚辛艮8 / 戊己同壬癸） */
    val STEM_TOMB = mapOf(
        "丙" to 6, "丁" to 6, "甲" to 2, "乙" to 2,
        "壬" to 4, "癸" to 4, "庚" to 8, "辛" to 8, "戊" to 4, "己" to 4,
    )

    /** 守门八格：值使宫天盘干 -> 格局名（据教材·奇格守门） */
    val SHOU_MEN = mapOf(
        "丁" to "玉女守门", "乙" to "日照门", "己" to "地户闭门", "戊" to "青龙绻户",
        "庚" to "太白入门", "辛" to "白虎入门", "壬" to "玄武守门", "癸" to "螣蛇守门",
    )

    // ── 盘面宫格增强字段所需常量（六亲 / 状态 / 角标）──

    /** 天干五行 */
    val STEM_ELEMENT = mapOf(
        "甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火", "戊" to "土",
        "己" to "土", "庚" to "金", "辛" to "金", "壬" to "水", "癸" to "水",
    )

    /** 地支五行 */
    val BRANCH_ELEMENT = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木", "辰" to "土", "巳" to "火",
        "午" to "火", "未" to "土", "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水",
    )

    /** 二十四节气 -> 月令五行（节与中气同属一月）：
     *  立春/雨水=寅木、惊蛰/春分=卯木、清明/谷雨=辰土、立夏/小满=巳火、
     *  芒种/夏至=午火、小暑/大暑=未土、立秋/处暑=申金、白露/秋分=酉金、
     *  寒露/霜降=戌土、立冬/小雪=亥水、大雪/冬至=子水、小寒/大寒=丑土 */
    val JIE_QI_MONTH_ELEMENT = mapOf(
        "立春" to "木", "雨水" to "木", "惊蛰" to "木", "春分" to "木",
        "清明" to "土", "谷雨" to "土",
        "立夏" to "火", "小满" to "火", "芒种" to "火", "夏至" to "火",
        "小暑" to "土", "大暑" to "土",
        "立秋" to "金", "处暑" to "金", "白露" to "金", "秋分" to "金",
        "寒露" to "土", "霜降" to "土",
        "立冬" to "水", "小雪" to "水", "大雪" to "水", "冬至" to "水",
        "小寒" to "土", "大寒" to "土",
    )

    /** 驿马：地支（日/时支）-> 马星地支。申子辰马在寅、寅午戌马在申、巳酉丑马在亥、亥卯未马在巳 */
    val YI_MA = mapOf(
        "申" to "寅", "子" to "寅", "辰" to "寅",
        "寅" to "申", "午" to "申", "戌" to "申",
        "巳" to "亥", "酉" to "亥", "丑" to "亥",
        "亥" to "巳", "卯" to "巳", "未" to "巳",
    )

    /** 洛书九宫传统地支（马星/宫位标记定位用） */
    val PALACE_BRANCHES = mapOf(
        1 to listOf("子"), 2 to listOf("未", "申"), 3 to listOf("卯"),
        4 to listOf("辰", "巳"), 5 to emptyList(), 6 to listOf("戌", "亥"),
        7 to listOf("酉"), 8 to listOf("丑", "寅"), 9 to listOf("午"),
    )
}
