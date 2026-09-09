package com.potuo.feipanqimen2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.ui.components.Badge
import com.potuo.feipanqimen2.data.CaseTags
import com.potuo.feipanqimen2.data.CaseEntity
import com.potuo.feipanqimen2.ui.components.EmptyState
import com.potuo.feipanqimen2.ui.components.MiniBoard
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseListScreen(
    viewModel: MainViewModel,
    onCaseClick: (Long) -> Unit,
    onGoToPan: () -> Unit,
) {
    val cases by viewModel.cases.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val feedbackFilter by viewModel.feedbackFilter.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val feedbackStats by viewModel.feedbackStats.collectAsState()
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    val context = LocalContext.current

    val feedbackedCount = feedbackStats.find { it.f == "已反馈" }?.c ?: 0
    val notFeedbackedCount = feedbackStats.find { it.f == "未反馈" }?.c ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(QimenDimens.spacingLg),
    ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("搜索四柱、局数、标签…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = QimenDimens.spacingSm),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
            ) {
                listOf("全部" to (feedbackedCount + notFeedbackedCount), "未反馈" to notFeedbackedCount, "已反馈" to feedbackedCount).forEach { (label, count) ->
                    FilterChip(
                        selected = feedbackFilter == label,
                        onClick = { viewModel.setFeedbackFilter(label) },
                        label = { Text("$label $count") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                items(listOf("全部") + CaseTags.read(context)) { c ->
                    FilterChip(
                        selected = categoryFilter == c,
                        onClick = { viewModel.setCategoryFilter(c) },
                        label = { Text(c) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Text(
                "共 ${cases.size} 例 · ${if (searchQuery.isBlank() && categoryFilter == "全部" && feedbackFilter == "全部") "全部案例" else "当前筛选"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = QimenDimens.spacingSm),
            )

            if (cases.isEmpty()) {
                if (categoryStats.isEmpty()) {
                    EmptyState(
                        message = "暂无案例，去排一盘吧",
                        actionLabel = "去起盘",
                        onAction = onGoToPan,
                    )
                } else {
                    EmptyState(
                        message = "没有符合当前筛选条件的案例",
                        actionLabel = "清除筛选",
                        onAction = {
                            viewModel.setSearchQuery("")
                            viewModel.setCategoryFilter("全部")
                            viewModel.setFeedbackFilter("全部")
                        },
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    contentPadding = PaddingValues(vertical = QimenDimens.spacingSm),
                    horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
                ) {
                    items(cases, key = { it.id }) { case ->
                        CaseCard(
                            case = case,
                            dateFormat = dateFormat,
                            onClick = { onCaseClick(case.id) },
                        )
                    }
                }
            }
        }
}

@Composable
private fun CaseCard(case: CaseEntity, dateFormat: SimpleDateFormat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(QimenDimens.spacingMd)) {
            // 迷你盘面缩略图
            MiniBoard(panJson = case.panJson)
            Text(
                case.siZhu,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = QimenDimens.spacingSm),
            )
            Text(
                "${case.dunType}${case.juNumber}局 · ${case.jieQi}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
                modifier = Modifier.padding(top = QimenDimens.spacingSm),
            ) {
                if (case.category.isNotBlank()) Badge(text = case.category)
                Badge(
                    text = if (case.feedback.isNotBlank()) "✓ 已反馈" else "○ 未反馈",
                    containerColor = if (case.feedback.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (case.feedback.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (case.tags.isNotBlank()) {
                Text(
                    case.tags,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                dateFormat.format(Date(case.createTime)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = QimenDimens.spacingSm),
            )
        }
    }
}
