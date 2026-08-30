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
}
