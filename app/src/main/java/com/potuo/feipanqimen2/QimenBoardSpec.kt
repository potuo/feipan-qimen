package com.potuo.feipanqimen2

import com.potuo.feipanqimen2.qimen.PalaceInfo
import com.potuo.feipanqimen2.qimen.QimenResult

/**
 * 盘面渲染规格（单一数据源）：App 盘面（Compose）与分享图（Canvas）共用。
 *
 * 背景：盘面曾在 `QimenComponents.PalaceCell`（Compose）与 `QimenShareImage`（Canvas）
 * 各写一份「宫格顺序 / 红色判定 / 暗干支拆分」，改一处容易忘同步另一处。
 * 此处收拢为唯一实现，两处一律引用，杜绝两套逻辑漂移。
 *
 * 注意：红色/高亮是 UI 层规则（数据层只存纯数据），故本文件放在 UI 层而非 qimen/。
 */
object QimenBoardSpec {

    /** 洛书九宫格顺序（左上→右下按行读取的宫号序列） */
    val gridOrder = listOf(4, 9, 2, 3, 5, 7, 8, 1, 6)

    /** 一宫的高亮/红色判定结果（UI 只读不写） */
    data class Highlights(
        val isSpecial: Boolean,      // 值符星宫 或 值使门宫 → 朱砂描边
        val starRed: Boolean,        // 值符星红
        val gateRed: Boolean,        // 值使门红
        val heavenRed: Boolean,      // 天盘干 = 日干 / 时干 红
        val earthGodRed: Boolean,    // 地盘神 = 值符 红
    )

    /** 判定一宫的高亮规则（App 盘面与分享图共用，勿在 UI 层重复实现） */
    fun highlights(info: PalaceInfo, result: QimenResult): Highlights {
        val starRed = info.star == result.zhiFuStar
        val gateRed = info.gate == result.zhiShiGate
        val heavenRed = info.heavenStem.isNotEmpty() &&
            (info.heavenStem == result.dayPillar.first().toString() ||
                info.heavenStem == result.hourPillar.first().toString())
        val earthGodRed = info.earthGod == "值符"
        return Highlights(
            isSpecial = starRed || gateRed,
            starRed = starRed,
            gateRed = gateRed,
            heavenRed = heavenRed,
            earthGodRed = earthGodRed,
        )
    }

    /** 暗干支拆「干」（星行左列） */
    fun hiddenGan(info: PalaceInfo): String = info.hiddenStem?.firstOrNull()?.toString() ?: ""

    /** 暗干支拆「支」（门行左列） */
    fun hiddenZhi(info: PalaceInfo): String = info.hiddenStem?.lastOrNull()?.toString() ?: ""
}
