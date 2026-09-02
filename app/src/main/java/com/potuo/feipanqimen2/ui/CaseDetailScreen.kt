package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.potuo.feipanqimen2.QimenShareImage
import com.potuo.feipanqimen2.data.CaseEntity
import com.potuo.feipanqimen2.data.CaseTags
import com.potuo.feipanqimen2.log.LogManager
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenResult
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.MarkdownText
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.PalaceDetailDialog
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenDialog
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CaseDetailScreen(
    viewModel: MainViewModel,
    caseId: Long,
    onBack: () -> Unit,
) {
    var case by remember { mutableStateOf<CaseEntity?>(null) }
    var tags by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var huangLiExpanded by remember { mutableStateOf(false) }
    var selectedPalace by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<QimenResult?>(null) }

    BackHandler(onBack = onBack)

    LaunchedEffect(caseId) {
        val loaded = viewModel.getCaseById(caseId)
        case = loaded
        loaded?.let {
            tags = it.tags
            note = it.note
            feedback = it.feedback
            category = it.category
            result = viewModel.deserializePan(it.panJson)
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = LocalQimenPalette.current

    val c = case
    val r = result
    if (c == null || r == null) {
        Text("加载中…", modifier = Modifier.padding(QimenDimens.spacingLg))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        // 操作栏：导出 / 删除
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        val file = QimenShareImage.create(
                            r,
                            palette,
                            File(context.cacheDir, "share"),
                            QimenShareImage.CaseShareInfo(
                                tags = c.tags,
                                note = c.note,
                                feedback = c.feedback,
                            ),
                        )
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享盘面"))
                    }.onFailure { e ->
                        LogManager.logException("分享图", e)
                        Toast.makeText(context, "生成分享图失败：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Icon(Icons.Default.Share, contentDescription = "分享")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
            Text(c.siZhu, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("${c.dunType}${c.juNumber}局 · ${c.jieQi} · ${c.yuan}")
                Text(
                    if (feedback.isNotBlank()) "已反馈" else "未反馈",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (feedback.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .background(
                            if (feedback.isNotBlank()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
            Text("值符：${r.zhiFuStar}落${QimenConstants.PALACE_NAMES[r.zhiFuPalace]}宫")
            Text("值使：${r.zhiShiGate}门落${QimenConstants.PALACE_NAMES[r.zhiShiPalace]}宫")

            QimenBoard(result = r, animate = false, onPalaceClick = { selectedPalace = it })

            if (c.huangLi.isNotBlank()) {
                HuangLiCard(
                    summary = c.huangLi,
                    expanded = huangLiExpanded,
                    onToggle = { huangLiExpanded = !huangLiExpanded },
                )
            }

            Text(
                "事项分类",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                CaseTags.read(context).forEach { tagName ->
                    FilterChip(
                        selected = category == tagName,
                        onClick = { category = tagName },
                        label = { Text(tagName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("占断/备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("反馈结果（填写后该盘标记为已反馈）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            if (c.aiReading.isNotBlank()) {
                QimenCard(accentBar = true) {
                    Text(
                        "玄鉴",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(QimenDimens.spacingSm))

                    // 所问（用户输入给玄鉴的提示词）小字展示
                    if (c.aiPrompt.isNotBlank()) {
                        Text(
                            "所问：${c.aiPrompt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = QimenDimens.spacingSm),
                        )
                    }

                    // 拆分思考过程与结论（存库格式：【思考过程】…【结论】…）
                    val reasoningPart = if (c.aiReading.contains("【思考过程】")) {
                        c.aiReading.substringAfter("【思考过程】").substringBefore("【结论】").trim()
                    } else ""
                    val conclusionPart = if (c.aiReading.contains("【结论】")) {
                        c.aiReading.substringAfter("【结论】").trim()
                    } else c.aiReading

                    if (reasoningPart.isNotBlank()) {
                        var showReasoning by remember { mutableStateOf(false) }
                        Text(
                            if (showReasoning) "▾ 思考过程" else "▸ 思考过程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReasoning = !showReasoning },
                        )
                        if (showReasoning) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        RoundedCornerShape(QimenDimens.radiusSm),
                                    )
                                    .padding(QimenDimens.spacingMd),
                            ) {
                                MarkdownText(text = reasoningPart)
                            }
                        }
                        Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                    }
                    MarkdownText(text = conclusionPart)
                }
            }

            QimenButton(
                onClick = { viewModel.updateCase(c, category, tags, note, feedback) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("保存修改") }
    }

    if (showDeleteDialog) {
        QimenDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "确认删除",
            destructive = true,
            confirmText = "删除",
            onConfirm = {
                case?.let { viewModel.deleteCase(it) }
                showDeleteDialog = false
                onBack()
            },
            dismissText = "取消",
            onDismiss = { showDeleteDialog = false },
            text = { Text("删除后不可恢复，确定删除此案例？") },
        )
    }

    // ── 宫格详解 ──
    selectedPalace?.let { palaceNum ->
        PalaceDetailDialog(
            result = r,
            palaceNum = palaceNum,
            onDismiss = { selectedPalace = null },
        )
    }
}
