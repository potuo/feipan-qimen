package com.potuo.feipanqimen2.qimen

data class PalaceInfo(
    val palace: Int,
    val direction: String,
    val god: String,
    val star: String,
    val heavenStem: String,
    val gate: String,
    val earthStem: String,
    val hiddenStem: String? = null,
    val earthGod: String = "",            // 地盘神（值符神从旬首宫起布）
    val state: String = "",               // 旺衰状态（旺相休囚死废没胎）
    val liuQinStar: String = "",          // 星六亲（行3左，以时干为我）
    val liuQinHeaven: String = "",        // 天盘干六亲（行3右）
    val liuQinGate: String = "",          // 门六亲（行5左）
    val liuQinEarth: String = "",         // 地盘干六亲（行5右）
    val marks: List<String> = emptyList(), // 宫角标记：马/迫/刑/墓
)

data class QimenResult(
    val yearPillar: String,
    val monthPillar: String,
    val dayPillar: String,
    val hourPillar: String,
    val siZhu: String,
    val jieQi: String,
    val yuan: String,
    val dunType: String,
    val juNumber: Int,
    val xunShou: String,
    val xunShouStem: String,
    val kongWang: String,
    val zhiFuStar: String,
    val zhiFuPalace: Int,
    val zhiShiGate: String,
    val zhiShiPalace: Int,
    val palaces: Map<Int, PalaceInfo>,
)
