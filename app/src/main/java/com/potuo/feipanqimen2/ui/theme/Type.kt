package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.R

/**
 * 古风字体：霞鹜文楷（LXGW WenKai，SIL OFL 开源，子集化约 0.8MB）。
 * 仅供盘面、印章或品牌短标题使用；导航、表单和长文继续使用系统字体。
 */
val QimenFontFamily = FontFamily(Font(R.font.lxgw_wenkai, FontWeight.Normal))

/** 天禽的六级语义字阶，供非 Material 语义（尤其 [data]）直接引用。 */
object QimenTypeScale {
    val display = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold)
    val title = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
    val label = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val data = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
    )
    val caption = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
}

/** Material 3 排版映射。正文与辅助文字不低于 14sp。 */
val QimenTypography = Typography(
    displayLarge = QimenTypeScale.display,
    displayMedium = QimenTypeScale.display.copy(fontSize = 28.sp, lineHeight = 36.sp),
    displaySmall = QimenTypeScale.title.copy(fontSize = 24.sp, lineHeight = 32.sp),
    headlineLarge = QimenTypeScale.title.copy(fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = QimenTypeScale.title,
    headlineSmall = QimenTypeScale.title.copy(fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = QimenTypeScale.title,
    titleMedium = QimenTypeScale.title.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = QimenTypeScale.label.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = QimenTypeScale.body,
    bodyMedium = QimenTypeScale.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = QimenTypeScale.caption,
    labelLarge = QimenTypeScale.label,
    labelMedium = QimenTypeScale.label,
    labelSmall = QimenTypeScale.caption,
)
