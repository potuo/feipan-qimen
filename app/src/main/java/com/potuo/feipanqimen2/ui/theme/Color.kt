package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 中式盘面色板（仅盘面组件使用：宫格/印章/金边）
object QimenColors {
    val Cinnabar = Color(0xFFC0392B)          // 朱砂
    val Gold = Color(0xFFB08D57)              // 金
    val Slate = Color(0xFF5C6570)             // 青灰
    val InkText = Color(0xFF23262B)           // 墨色
    val PaperLight = Color(0xFFF2EDE3)        // 宣纸
    val PaperDark = Color(0xFF23262B)         // 深色底
    val CenterBgLight = Color(0xFFFFF8EC)     // 中宫浅金底
    val CenterBgDark = Color(0xFF2A2518)      // 中宫深金底
    val GridBorderLight = Color(0xFF5C6570)
    val GridBorderDark = Color(0xFF5C6570)
}

// Material 3 baseline 色板（Android 12 以下 fallback；12+ 由动态取色接管）
val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
)
