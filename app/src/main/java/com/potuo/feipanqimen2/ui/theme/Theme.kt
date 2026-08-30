package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// 主题：古典金（黄/红/金）浅色 / 暗色 两态

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

@Composable
fun FeipanQimenTheme(
    isDark: Boolean = false,
    themeName: String = "classic",
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        themeName == "ziwei" && isDark -> ZiweiDarkColors
        themeName == "ziwei" -> ZiweiLightColors
        isDark -> ClassicDarkColors
        else -> ClassicLightColors
    }
    val qimenPalette = when {
        themeName == "ziwei" && isDark -> QimenPalettes.ZiweiDark
        themeName == "ziwei" -> QimenPalettes.ZiweiLight
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
