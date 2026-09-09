package com.potuo.feipanqimen2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.ui.theme.QimenShapeTokens

@Composable
fun QimenButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(QimenDimens.motionDurationShort),
        label = "buttonScale",
    )
    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        content = content,
    )
}

@Composable
fun QimenOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(QimenDimens.motionDurationShort),
        label = "outlinedButtonScale",
    )
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

/** 小标签（值符/值使标记）：Material 3 Surface 胶囊 */
@Composable
fun SealBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = QimenShapeTokens.badge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)) {
            Text(
                text = text,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 10.sp,
            )
        }
    }
}

/** 无内容时的统一状态；动作标签或回调缺省时显示纯空态。 */
@Composable
fun EmptyState(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(QimenDimens.spacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(QimenDimens.spacingLg))
            QimenButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** 一级内容分组标题；可用 [sealMark] 在标题旁展示简短朱砂印记。 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    sealMark: String? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(4.dp)
                .height(20.dp)
                .background(LocalQimenPalette.current.cinnabar, QimenShapeTokens.accentMark),
        )
        Spacer(Modifier.width(QimenDimens.spacingSm))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        sealMark?.let {
            Badge(
                text = it,
                containerColor = LocalQimenPalette.current.cinnabar.copy(alpha = 0.14f),
                contentColor = LocalQimenPalette.current.cinnabar,
            )
        }
    }
}

/** 用于类别、状态等短文本的小型胶囊标签。 */
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        modifier = modifier,
        shape = QimenShapeTokens.badge,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = QimenDimens.spacingSm, vertical = QimenDimens.spacingXs),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** 页面或内容区域正在加载时的统一状态。 */
@Composable
fun LoadingState(message: String = "加载中…", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(QimenDimens.spacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(QimenDimens.spacingLg))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 加载或操作失败时的统一状态；[onRetry] 为空时仅展示错误说明。 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String = "重试",
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(QimenDimens.spacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        onRetry?.let {
            Spacer(Modifier.height(QimenDimens.spacingLg))
            QimenOutlinedButton(onClick = it) { Text(retryLabel) }
        }
    }
}

/**
 * 古风对话框：纸感描边容器 + 标题朱砂竖条点缀 + 可滚动内容 + 古风按钮。
 * 替代 Material3 默认 AlertDialog，契合三主题配色。
 *
 * @param destructive 确认操作为破坏性（删除等）时置 true，确认按钮用 error 色。
 * @param dismissText 传 null 可隐藏取消按钮（纯提示型弹窗）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QimenDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    text: (@Composable () -> Unit)? = null,
    confirmText: String = "知道了",
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    dismissText: String? = "取消",
    onDismiss: (() -> Unit)? = null,
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
    destructive: Boolean = false,
    accentColor: Color = LocalQimenPalette.current.cinnabar,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Surface(
            shape = QimenShapeTokens.dialog,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                // 标题：朱砂竖条 + 标题
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(accentColor, QimenShapeTokens.accentMark),
                    )
                    Spacer(modifier = Modifier.width(QimenDimens.spacingSm))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(QimenDimens.spacingMd))

                // 内容（过长可滚动）
                text?.let {
                    Box(
                        modifier = Modifier
                            .heightIn(max = QimenDimens.dialogMaxHeight)
                            .verticalScroll(rememberScrollState()),
                    ) { it() }
                }
                Spacer(modifier = Modifier.height(QimenDimens.spacingLg))

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    neutralText?.let { label ->
                        QimenOutlinedButton(onClick = { onNeutral?.invoke() }) {
                            Text(label)
                        }
                    }
                    dismissText?.let { label ->
                        Spacer(modifier = Modifier.width(QimenDimens.spacingSm))
                        QimenOutlinedButton(onClick = { (onDismiss ?: onDismissRequest).invoke() }) {
                            Text(label)
                        }
                    }
                    onConfirm?.let {
                        Spacer(modifier = Modifier.width(QimenDimens.spacingSm))
                        QimenButton(
                            onClick = it,
                            enabled = confirmEnabled,
                            containerColor = if (destructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (destructive) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                        ) { Text(confirmText) }
                    }
                }
            }
        }
    }
}

/** 卡片视觉层级：无边框、描边或轻抬升。 */
enum class QimenCardLevel { Plain, Outlined, Elevated }

/**
 * 古风卡片：圆角 + 描边 + 可选朱砂竖条点缀。
 *
 * @param accentBar 为 true 时在左侧绘制一条朱砂竖条。
 * @param containerColor 自定义容器色；为空时按 [level] 选择语义容器色。
 * @param level 卡片视觉层级；默认 [QimenCardLevel.Outlined] 保持既有视觉。
 */
@Composable
fun QimenCard(
    modifier: Modifier = Modifier,
    accentBar: Boolean = false,
    accentColor: Color = LocalQimenPalette.current.cinnabar,
    containerColor: Color? = null,
    level: QimenCardLevel = QimenCardLevel.Outlined,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val resolvedContainerColor = containerColor ?: when (level) {
        QimenCardLevel.Plain -> MaterialTheme.colorScheme.surface
        QimenCardLevel.Outlined -> MaterialTheme.colorScheme.surfaceVariant
        QimenCardLevel.Elevated -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = QimenShapeTokens.card,
        colors = CardDefaults.cardColors(containerColor = resolvedContainerColor),
        border = if (level == QimenCardLevel.Outlined) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (level == QimenCardLevel.Elevated) 3.dp else 0.dp,
        ),
    ) {
        Row(modifier = Modifier.padding(QimenDimens.cardPadding)) {
            if (accentBar) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(accentColor, QimenShapeTokens.accentMark),
                )
                Spacer(modifier = Modifier.width(QimenDimens.spacingSm))
            }
            Column(
                modifier = Modifier.weight(1f),
                content = content,
            )
        }
    }
}
