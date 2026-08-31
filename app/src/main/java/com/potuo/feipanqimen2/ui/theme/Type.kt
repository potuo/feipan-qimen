package com.potuo.feipanqimen2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.potuo.feipanqimen2.R

/**
 * 古风字体：霞鹜文楷（LXGW WenKai，SIL OFL 开源，子集化约 0.8MB）。
 * ⚠️ 仅盘面（PalaceCell）局部使用；全局排版仍用系统默认字体，避免楷体渲染拖慢滚动。
 */
val QimenFontFamily = FontFamily(
    Font(R.font.lxgw_wenkai, FontWeight.Normal),
)

// Material 3 默认排版（跟随系统字体）
val QimenTypography = Typography()
