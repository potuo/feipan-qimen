package com.potuo.feipanqimen2.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 盘面色板（跟随主题：古典金 / 紫微 / 玄墨 / 青花 / 赭石 × 浅色 / 暗色）。
 * 盘面组件（宫格/印章/金边/符使标记）统一从这里取色，不再写死 QimenColors。
 */
data class QimenPalette(
    val cinnabar: Color,        // 值符/值使边框、印章（强调色）
    val gold: Color,            // 金边、中宫标记
    val slate: Color,           // 次要文字（青灰 / 紫灰）
    val inkText: Color,         // 主文字
    val paper: Color,           // 宫格底色
    val palaceBg: Color,        // 暗色宫格底
    val gridBorder: Color,      // 宫格线
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
        secondaryText = Color(0xFFC8C5D0),
    )

    // ── 玄墨（夜观星象 · 罗盘）：玄黑 + 金，朱砂点睛 ──

    /** 玄墨 · 浅色（宣纸·墨金） */
    val XuanMoLight = QimenPalette(
        cinnabar = Color(0xFFC0392B),
        gold = Color(0xFFB8912A),
        slate = Color(0xFF6B6255),
        inkText = Color(0xFF1F1C16),
        paper = Color(0xFFF2EFE6),
        palaceBg = Color(0xFFF2EFE6),
        gridBorder = Color(0xFFA89E8A),
        secondaryText = Color(0xFF6B6255),
    )

    /** 玄墨 · 暗色（玄黑·金） */
    val XuanMoDark = QimenPalette(
        cinnabar = Color(0xFFE0806F),
        gold = Color(0xFFC9A227),
        slate = Color(0xFF9A9186),
        inkText = Color(0xFFEDE8DC),
        paper = Color(0xFF161616),
        palaceBg = Color(0xFF1F1C16),
        gridBorder = Color(0xFF4A453C),
        secondaryText = Color(0xFFB0A896),
    )

    // ── 青花（靛蓝 · 瓷器）：靛蓝 + 瓷白，朱砂点睛 ──

    /** 青花 · 浅色（瓷白·靛蓝） */
    val QingHuaLight = QimenPalette(
        cinnabar = Color(0xFFC0392B),
        gold = Color(0xFF2B4A7A),
        slate = Color(0xFF5A6B7A),
        inkText = Color(0xFF1A2233),
        paper = Color(0xFFF4F6F8),
        palaceBg = Color(0xFFF4F6F8),
        gridBorder = Color(0xFFB0BCC8),
        secondaryText = Color(0xFF5A6B7A),
    )

    /** 青花 · 暗色（藏蓝·靛青） */
    val QingHuaDark = QimenPalette(
        cinnabar = Color(0xFFE0806F),
        gold = Color(0xFF7A9CC8),
        slate = Color(0xFF8A9BB0),
        inkText = Color(0xFFE8EDF4),
        paper = Color(0xFF1A2233),
        palaceBg = Color(0xFF222C40),
        gridBorder = Color(0xFF3A4A60),
        secondaryText = Color(0xFFA8B4C4),
    )

    // ── 赭石（古籍 · 帛书）：赭石 + 秋香，朱砂点睛 ──

    /** 赭石 · 浅色（米黄·赭石） */
    val ZheShiLight = QimenPalette(
        cinnabar = Color(0xFFC0392B),
        gold = Color(0xFFA0653A),
        slate = Color(0xFF7A6A5A),
        inkText = Color(0xFF2A1F18),
        paper = Color(0xFFF5EBDC),
        palaceBg = Color(0xFFF5EBDC),
        gridBorder = Color(0xFFB8A898),
        secondaryText = Color(0xFF7A6A5A),
    )

    /** 赭石 · 暗色（深赭·秋香） */
    val ZheShiDark = QimenPalette(
        cinnabar = Color(0xFFE0806F),
        gold = Color(0xFFC9A86A),
        slate = Color(0xFFA89A88),
        inkText = Color(0xFFF0E8DC),
        paper = Color(0xFF2A1F18),
        palaceBg = Color(0xFF33261D),
        gridBorder = Color(0xFF5A4A3A),
        secondaryText = Color(0xFFB8A898),
    )
}

/** 当前主题对应的盘面色板（由 FeipanQimenTheme 注入） */
val LocalQimenPalette = staticCompositionLocalOf { QimenPalettes.ClassicLight }
