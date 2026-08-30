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
