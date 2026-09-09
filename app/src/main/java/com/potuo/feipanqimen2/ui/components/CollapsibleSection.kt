package com.potuo.feipanqimen2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.ui.theme.QimenShapeTokens

/**
 * 可折叠内容分组。传入 [expanded] 与 [onExpandedChange] 时由调用方控制；
 * 不传 [expanded] 时使用内部可保存状态，兼容设置页、关于页等现有调用。
 */
@Composable
fun CollapsibleSection(
    title: String,
    defaultExpanded: Boolean = false,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var internalExpanded by rememberSaveable { mutableStateOf(defaultExpanded) }
    val isExpanded = expanded ?: internalExpanded
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(QimenDimens.motionDuration),
        label = "collapsibleArrowRotation",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = QimenShapeTokens.card,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        stateDescription = if (isExpanded) "已展开" else "已收起"
                    }
                    .clickable {
                        val next = !isExpanded
                        if (expanded == null) internalExpanded = next
                        onExpandedChange?.invoke(next)
                    }
                    .padding(QimenDimens.spacingLg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    modifier = Modifier.rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(QimenDimens.motionDuration)),
                exit = shrinkVertically(animationSpec = tween(QimenDimens.motionDuration)),
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = QimenDimens.spacingLg,
                        end = QimenDimens.spacingLg,
                        bottom = QimenDimens.spacingLg,
                    ),
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = QimenDimens.spacingLg))
                    content()
                }
            }
        }
    }
}
