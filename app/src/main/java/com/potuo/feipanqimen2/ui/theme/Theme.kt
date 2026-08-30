package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 主题：古典金（黄/红/金）浅色 / 暗色 两态
// 黑白切换按钮在侧滑栏底部，设置页默认古典金

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

@Composable
fun FeipanQimenTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isDark) ClassicDarkColors else ClassicLightColors,
        typography = QimenTypography,
        shapes = QimenShapes,
        content = content,
    )
}
