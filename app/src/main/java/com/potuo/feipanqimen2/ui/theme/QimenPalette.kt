package com.potuo.feipanqimen2.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 盘面色板（跟随主题：古典金 / 紫微 × 浅色 / 暗色）。
 * 盘面组件（宫格/印章/金边/符使标记）统一从这里取色，不再写死 QimenColors。
 */
data class QimenPalette(
    val cinnabar: Color,        // 值符/值使边框、印章（古典金=朱砂 / 紫微=亮紫）
    val gold: Color,            // 金边、中宫标记（古典金=金 / 紫微=星青）
    val slate: Color,           // 次要文字（青灰 / 紫灰）
    val inkText: Color,         // 主文字
    val paper: Color,           // 宫格底色
    val palaceBg: Color,        // 暗色宫格底
    val gridBorder: Color,      // 宫格线
    val centerBg: Color,        // 中宫底
    val secondaryText: Color,   // 神名等次级文字
)

object QimenPalettes {

    /** 古典金 · 浅色（宣纸） */
    val ClassicLight = QimenPalette(
        cinnabar = Color(0xFFC0392B),
        gold = Color(0xFFB08D57),
        slate = Color(0xFF5C6570),
        inkText = Color(0xFF23262B),
        paper = Color(0xFFF2EDE3),
        palaceBg = Color(0xFFF2EDE3),
        gridBorder = Color(0xFF5C6570),
        centerBg = Color(0xFFFFF8EC),
        secondaryText = Color(0xFF5C6570),
    )

    /** 古典金 · 暗色（墨底） */
    val ClassicDark = QimenPalette(
        cinnabar = Color(0xFFE0806F),
        gold = Color(0xFFB08D57),
        slate = Color(0xFF9AA3AD),
        inkText = Color(0xFFF2EDE3),
        paper = Color(0xFF1A1C20),
        palaceBg = Color(0xFF2A2D33),
        gridBorder = Color(0xFF5C6570),
        centerBg = Color(0xFF2A2518),
        secondaryText = Color(0xFFB8B2A0),
    )

    /** 紫微 · 浅色（月白） */
    val ZiweiLight = QimenPalette(
        cinnabar = Color(0xFF5B3FD4),
        gold = Color(0xFF00697A),
        slate = Color(0xFF625B71),
        inkText = Color(0xFF1B1B21),
        paper = Color(0xFFFBFAFF),
        palaceBg = Color(0xFFFBFAFF),
        gridBorder = Color(0xFFC8C5D0),
        centerBg = Color(0xFFF4F1FA),
        secondaryText = Color(0xFF625B71),
    )

    /** 紫微 · 暗色（夜空） */
    val ZiweiDark = QimenPalette(
        cinnabar = Color(0xFFCBBEFF),
        gold = Color(0xFF4DD0E1),
        slate = Color(0xFFC8C5D0),
        inkText = Color(0xFFE6E1E9),
        paper = Color(0xFF14121A),
        palaceBg = Color(0xFF1B1821),
        gridBorder = Color(0xFF47464F),
        centerBg = Color(0xFF26232C),
        secondaryText = Color(0xFFC8C5D0),
    )

    /** 青玉 · 浅色（绢素·晴山，青绿山水） */
    val JadeLight = QimenPalette(
        cinnabar = Color(0xFF1E7A5C),
        gold = Color(0xFFB08D57),
        slate = Color(0xFF5C6B66),
        inkText = Color(0xFF1E2422),
        paper = Color(0xFFF2F5F1),
        palaceBg = Color(0xFFF2F5F1),
        gridBorder = Color(0xFF9AA89F),
        centerBg = Color(0xFFFFF9EC),
        secondaryText = Color(0xFF6B7A72),
    )

    /** 青玉 · 暗色（夜山·松烟） */
    val JadeDark = QimenPalette(
        cinnabar = Color(0xFF6FD4AC),
        gold = Color(0xFFC9A96A),
        slate = Color(0xFFA8B4AC),
        inkText = Color(0xFFE8F0EC),
        paper = Color(0xFF121815),
        palaceBg = Color(0xFF1C2420),
        gridBorder = Color(0xFF3E4A44),
        centerBg = Color(0xFF1E2A22),
        secondaryText = Color(0xFFA9B8AF),
    )
}

/** 当前主题对应的盘面色板（由 FeipanQimenTheme 注入） */
val LocalQimenPalette = staticCompositionLocalOf { QimenPalettes.ClassicLight }
