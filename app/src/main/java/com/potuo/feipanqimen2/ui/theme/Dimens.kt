package com.potuo.feipanqimen2.ui.theme

import androidx.compose.ui.unit.dp

/** 天禽 UI 的尺寸与节奏 token。 */
object QimenDimens {
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacingXxl = 32.dp

    /** 平板及横屏中正文内容的最大宽度。 */
    val contentMaxWidth = 600.dp
    /** 页面内容到窗口边缘的默认安全留白。 */
    val pageGutter = 16.dp
    /** 页面一级分组之间的默认垂直间距。 */
    val sectionGap = 24.dp
    /** 卡片内容的默认内边距。 */
    val cardPadding = 16.dp
    /** 通用对话框中可滚动正文的最大高度。 */
    val dialogMaxHeight = 480.dp

    /** 常规界面过渡时长，单位为毫秒。 */
    const val motionDuration = 200
    /** 按压等即时反馈的短动效时长，单位为毫秒。 */
    const val motionDurationShort = 100
    /** 低层级边框在主题轮廓色上的建议透明度。 */
    const val borderAlpha = 0.6f

    val gridGap = 4.dp
    val gridBorder = 0.5.dp

    // 旧半径名称保留，供尚未迁移到语义 Shape 的调用方兼容使用。
    val radiusSm = 8.dp
    val radiusMd = 12.dp
}
