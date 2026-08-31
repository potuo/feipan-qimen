package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// 主题：古典金 / 紫微 / 玄墨 / 青花 / 赭石 各两态

/** 古典金 · 浅色（宣纸） */
val ClassicLightColors = lightColorScheme(
    primary = QimenColors.Cinnabar,
    onPrimary = Color.White,
    primaryContainer = QimenColors.Cinnabar.copy(alpha = 0.14f),
    onPrimaryContainer = QimenColors.Cinnabar,
    secondary = QimenColors.Gold,
    onSecondary = Color.White,
    secondaryContainer = QimenColors.Gold.copy(alpha = 0.18f),
    onSecondaryContainer = QimenColors.Gold,
    tertiary = QimenColors.Slate,
    onTertiary = Color.White,
    background = QimenColors.PaperLight,
    onBackground = QimenColors.InkText,
    surface = Color(0xFFFFFBF6),
    onSurface = QimenColors.InkText,
    surfaceVariant = Color(0xFFE8E2D8),
    onSurfaceVariant = Color(0xFF52595F),
    surfaceContainerLow = Color(0xFFF5F0E6),
    surfaceContainerHigh = Color(0xFFF0E9D8),
    outline = Color(0xFF8A8070),
    outlineVariant = Color(0xFFC9BFAC),
)

/** 古典金 · 暗色（墨底） */
val ClassicDarkColors = darkColorScheme(
    primary = Color(0xFFE0806F),
    onPrimary = Color(0xFF3A0D06),
    primaryContainer = Color(0xFF8C2E1F),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = QimenColors.Gold,
    onSecondary = Color(0xFF2E1F0A),
    secondaryContainer = Color(0xFF4A3A1C),
    onSecondaryContainer = Color(0xFFE8D5B0),
    tertiary = Color(0xFF9AA3AD),
    onTertiary = Color(0xFF1C1E24),
    background = Color(0xFF1A1C20),
    onBackground = Color(0xFFF2EDE3),
    surface = Color(0xFF23262B),
    onSurface = Color(0xFFF2EDE3),
    surfaceVariant = Color(0xFF2E3138),
    onSurfaceVariant = Color(0xFFB8B2A0),
    surfaceContainerLow = Color(0xFF1E2126),
    surfaceContainerHigh = Color(0xFF2A2D33),
    outline = Color(0xFF5C6570),
    outlineVariant = Color(0xFF3A3F47),
)

// ── 紫微（蓝紫 · 星象）—— 参考 JetSnack 蓝紫 #5A31F4 + 浅青 #4DD0E1 ──

/** 紫微 · 浅色（月白） */
val ZiweiLightColors = lightColorScheme(
    primary = Color(0xFF5B3FD4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DFFF),
    onPrimaryContainer = Color(0xFF1A0066),
    secondary = Color(0xFF00697A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFAEEFFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = Color(0xFF6E5BA6),
    onTertiary = Color.White,
    background = Color(0xFFFBFAFF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBFAFF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    surfaceContainerLow = Color(0xFFF4F1FA),
    surfaceContainerHigh = Color(0xFFEDEBF5),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC8C5D0),
)

/** 紫微 · 暗色（夜空） */
val ZiweiDarkColors = darkColorScheme(
    primary = Color(0xFFCBBEFF),
    onPrimary = Color(0xFF2E0E9A),
    primaryContainer = Color(0xFF4423C4),
    onPrimaryContainer = Color(0xFFE6DFFF),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004E5A),
    onSecondaryContainer = Color(0xFFAEEFFF),
    tertiary = Color(0xFFCFBFF0),
    onTertiary = Color(0xFF352B52),
    background = Color(0xFF14121A),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF14121A),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC8C5D0),
    surfaceContainerLow = Color(0xFF1B1821),
    surfaceContainerHigh = Color(0xFF26232C),
    outline = Color(0xFF919099),
    outlineVariant = Color(0xFF47464F),
)

// ── 玄墨（夜观星象 · 罗盘）：玄黑 + 金，朱砂点睛 ──

/** 玄墨 · 浅色（宣纸·墨金） */
val XuanMoLightColors = lightColorScheme(
    primary = Color(0xFFB8912A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8912A).copy(alpha = 0.14f),
    onPrimaryContainer = Color(0xFF8A6B1F),
    secondary = Color(0xFFC0392B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC0392B).copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF8C2E1F),
    tertiary = Color(0xFF6B6255),
    onTertiary = Color.White,
    background = Color(0xFFF2EFE6),
    onBackground = Color(0xFF1F1C16),
    surface = Color(0xFFFAF7F0),
    onSurface = Color(0xFF1F1C16),
    surfaceVariant = Color(0xFFE8E3D6),
    onSurfaceVariant = Color(0xFF57503F),
    surfaceContainerLow = Color(0xFFF5F2E8),
    surfaceContainerHigh = Color(0xFFEFEBDD),
    outline = Color(0xFFA89E8A),
    outlineVariant = Color(0xFFD0C8B4),
)

/** 玄墨 · 暗色（玄黑·金） */
val XuanMoDarkColors = darkColorScheme(
    primary = Color(0xFFC9A227),
    onPrimary = Color(0xFF2E2204),
    primaryContainer = Color(0xFF4A3A0A),
    onPrimaryContainer = Color(0xFFF2E3B0),
    secondary = Color(0xFFE0806F),
    onSecondary = Color(0xFF3A0D06),
    secondaryContainer = Color(0xFF8C2E1F),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFF9A9186),
    onTertiary = Color(0xFF1C1C1C),
    background = Color(0xFF161616),
    onBackground = Color(0xFFEDE8DC),
    surface = Color(0xFF1F1C16),
    onSurface = Color(0xFFEDE8DC),
    surfaceVariant = Color(0xFF2A2720),
    onSurfaceVariant = Color(0xFFB0A896),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF26221C),
    outline = Color(0xFF4A453C),
    outlineVariant = Color(0xFF3A362F),
)

// ── 青花（靛蓝 · 瓷器）：靛蓝 + 瓷白，朱砂点睛 ──

/** 青花 · 浅色（瓷白·靛蓝） */
val QingHuaLightColors = lightColorScheme(
    primary = Color(0xFF2B4A7A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B4A7A).copy(alpha = 0.14f),
    onPrimaryContainer = Color(0xFF1A3358),
    secondary = Color(0xFFC0392B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC0392B).copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF8C2E1F),
    tertiary = Color(0xFF5A6B7A),
    onTertiary = Color.White,
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF1A2233),
    surface = Color(0xFFFAFBFC),
    onSurface = Color(0xFF1A2233),
    surfaceVariant = Color(0xFFE4E9EF),
    onSurfaceVariant = Color(0xFF4A5A6A),
    surfaceContainerLow = Color(0xFFEEF1F5),
    surfaceContainerHigh = Color(0xFFE8EDF2),
    outline = Color(0xFF8A99AA),
    outlineVariant = Color(0xFFC0C8D4),
)

/** 青花 · 暗色（藏蓝·靛青） */
val QingHuaDarkColors = darkColorScheme(
    primary = Color(0xFF7A9CC8),
    onPrimary = Color(0xFF0E1A30),
    primaryContainer = Color(0xFF22374F),
    onPrimaryContainer = Color(0xFFD4E2F4),
    secondary = Color(0xFFE0806F),
    onSecondary = Color(0xFF3A0D06),
    secondaryContainer = Color(0xFF8C2E1F),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFF8A9BB0),
    onTertiary = Color(0xFF141B28),
    background = Color(0xFF1A2233),
    onBackground = Color(0xFFE8EDF4),
    surface = Color(0xFF202A3C),
    onSurface = Color(0xFFE8EDF4),
    surfaceVariant = Color(0xFF2A3548),
    onSurfaceVariant = Color(0xFFA8B4C4),
    surfaceContainerLow = Color(0xFF161E2C),
    surfaceContainerHigh = Color(0xFF242E40),
    outline = Color(0xFF4A5A70),
    outlineVariant = Color(0xFF3A4A60),
)

// ── 赭石（古籍 · 帛书）：赭石 + 秋香，朱砂点睛 ──

/** 赭石 · 浅色（米黄·赭石） */
val ZheShiLightColors = lightColorScheme(
    primary = Color(0xFFA0653A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA0653A).copy(alpha = 0.16f),
    onPrimaryContainer = Color(0xFF7A4A2A),
    secondary = Color(0xFFC0392B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC0392B).copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF8C2E1F),
    tertiary = Color(0xFF7A6A5A),
    onTertiary = Color.White,
    background = Color(0xFFF5EBDC),
    onBackground = Color(0xFF2A1F18),
    surface = Color(0xFFFBF4E8),
    onSurface = Color(0xFF2A1F18),
    surfaceVariant = Color(0xFFEBDFCC),
    onSurfaceVariant = Color(0xFF5A4A3A),
    surfaceContainerLow = Color(0xFFF2E9D8),
    surfaceContainerHigh = Color(0xFFEDE2CF),
    outline = Color(0xFF8A7A6A),
    outlineVariant = Color(0xFFC8B8A4),
)

/** 赭石 · 暗色（深赭·秋香） */
val ZheShiDarkColors = darkColorScheme(
    primary = Color(0xFFC9A86A),
    onPrimary = Color(0xFF2E1F0A),
    primaryContainer = Color(0xFF4A3A1C),
    onPrimaryContainer = Color(0xFFF0E0C0),
    secondary = Color(0xFFE0806F),
    onSecondary = Color(0xFF3A0D06),
    secondaryContainer = Color(0xFF8C2E1F),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFFA89A88),
    onTertiary = Color(0xFF1C160F),
    background = Color(0xFF2A1F18),
    onBackground = Color(0xFFF0E8DC),
    surface = Color(0xFF33261D),
    onSurface = Color(0xFFF0E8DC),
    surfaceVariant = Color(0xFF40332A),
    onSurfaceVariant = Color(0xFFB8A898),
    surfaceContainerLow = Color(0xFF241B14),
    surfaceContainerHigh = Color(0xFF3A2E24),
    outline = Color(0xFF5A4A3A),
    outlineVariant = Color(0xFF4A3D30),
)

@Composable
fun FeipanQimenTheme(
    isDark: Boolean = false,
    themeName: String = "classic",
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        themeName == "ziwei" && isDark -> ZiweiDarkColors
        themeName == "ziwei" -> ZiweiLightColors
        themeName == "xuanmo" && isDark -> XuanMoDarkColors
        themeName == "xuanmo" -> XuanMoLightColors
        themeName == "qinghua" && isDark -> QingHuaDarkColors
        themeName == "qinghua" -> QingHuaLightColors
        themeName == "zheshi" && isDark -> ZheShiDarkColors
        themeName == "zheshi" -> ZheShiLightColors
        isDark -> ClassicDarkColors
        else -> ClassicLightColors
    }
    val qimenPalette = when {
        themeName == "ziwei" && isDark -> QimenPalettes.ZiweiDark
        themeName == "ziwei" -> QimenPalettes.ZiweiLight
        themeName == "xuanmo" && isDark -> QimenPalettes.XuanMoDark
        themeName == "xuanmo" -> QimenPalettes.XuanMoLight
        themeName == "qinghua" && isDark -> QimenPalettes.QingHuaDark
        themeName == "qinghua" -> QimenPalettes.QingHuaLight
        themeName == "zheshi" && isDark -> QimenPalettes.ZheShiDark
        themeName == "zheshi" -> QimenPalettes.ZheShiLight
        isDark -> QimenPalettes.ClassicDark
        else -> QimenPalettes.ClassicLight
    }
    CompositionLocalProvider(LocalQimenPalette provides qimenPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QimenTypography,
            shapes = QimenShapes,
            content = content,
        )
    }
}
