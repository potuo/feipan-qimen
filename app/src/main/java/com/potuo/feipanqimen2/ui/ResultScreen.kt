package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.potuo.feipanqimen2.AiAssistant
import com.potuo.feipanqimen2.PatternBook
import com.potuo.feipanqimen2.QimenShareImage
import com.potuo.feipanqimen2.data.CASE_CATEGORIES
import com.potuo.feipanqimen2.qimen.PatternInfo
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenPatternDetector
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.MarkdownText
import com.potuo.feipanqimen2.ui.components.PalaceDetailDialog
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.QimenDialog
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.components.SealBadge
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val result by viewModel.qimenResult.collectAsState()
    val huangLi by viewModel.huangLi.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    val aiReading by viewModel.aiReading.collectAsState()
    val aiReasoning by viewModel.aiReasoning.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiElapsed by viewModel.aiElapsed.collectAsState()
    var huangLiExpanded by remember { mutableStateOf(false) }
    var patternDetail by remember { mutableStateOf<PatternInfo?>(null) }
    var selectedPalace by remember { mutableStateOf<Int?>(null) }
    var boardAnimate by remember { mutableStateOf(true) }
    var showAiDialog by remember { mutableStateOf(false) }
    var aiSituation by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = LocalQimenPalette.current
    val aiEnabled = AiAssistant.readConfig(context).enabled

    BackHandler(onBack = onBack)

    if (result == null) {
        Text("无排盘结果", modifier = Modifier.padding(QimenDimens.spacingLg))
        return
    }

    val r = result!!
    val patterns = remember(r) { QimenPatternDetector.detect(r) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        QimenCard(accentBar = true) {
            Text(
                r.siZhu,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
            Text(
                "${r.jieQi} · ${r.yuan} · ${r.dunType}${r.juNumber}局",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                "旬首：${r.xunShou}（遁${r.xunShouStem}）· 空亡：${r.kongWang}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SealBadge(text = "符")
                Text(
                    "${r.zhiFuStar}·${QimenConstants.PALACE_NAMES[r.zhiFuPalace]}${r.zhiFuPalace}宫",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = QimenDimens.spacingXs),
                )
                Spacer(modifier = Modifier.width(QimenDimens.spacingLg))
                SealBadge(text = "使")
                Text(
                    "${r.zhiShiGate}门·${QimenConstants.PALACE_NAMES[r.zhiShiPalace]}${r.zhiShiPalace}宫",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = QimenDimens.spacingXs),
                )
            }
        }

        // ── 时辰快捷对比 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
        ) {
            QimenOutlinedButton(
                onClick = {
                    boardAnimate = false
                    viewModel.shiftHour(-1)
                },
                modifier = Modifier.weight(1f),
            ) { Text("‹ 上一时辰") }
            QimenOutlinedButton(
                onClick = {
                    boardAnimate = false
                    viewModel.shiftHour(1)
                },
                modifier = Modifier.weight(1f),
            ) { Text("下一时辰 ›") }
        }

        QimenBoard(result = r, animate = boardAnimate, onPalaceClick = { selectedPalace = it })

        // ── 格局（据《奇门基础资料 2023版教》第三卷，可折叠）──
        if (patterns.isNotEmpty()) {
            QimenCard(accentBar = true) {
                var patternsExpanded by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { patternsExpanded = !patternsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "格局",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        if (patternsExpanded) "收起" else "展开",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (patternsExpanded) {
                    Spacer(modifier = Modifier.height(QimenDimens.spacingMd))
                    patterns.forEach { p ->
                        Column(
                            modifier = Modifier
                                .padding(bottom = QimenDimens.spacingMd)
                                .clickable { patternDetail = p },
                        ) {
                            Text(
                                p.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = when (p.isAuspicious) {
                                    true -> MaterialTheme.colorScheme.primary
                                    false -> MaterialTheme.colorScheme.error
                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                p.detail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp,
                            )
                            Text(
                                "📖 点按查看教材原文",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }

        huangLi?.let {
            HuangLiCard(
                summary = it.summary,
                expanded = huangLiExpanded,
                onToggle = { huangLiExpanded = !huangLiExpanded },
            )
        }

        OutlinedTextField(
            value = tags,
            onValueChange = viewModel::setTags,
            label = { Text("标签（逗号分隔）") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "事项类别",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        // 事项类别：4 + 3 两行，间距宽松
        CASE_CATEGORIES.chunked(4).forEach { rowNames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
            ) {
                rowNames.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { viewModel.setCategory(c) },
                        label = { Text(c) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            QimenButton(
                onClick = { viewModel.saveCase() },
                modifier = Modifier.weight(1f),
            ) {
                Text("保存案例")
            }
            QimenOutlinedButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        runCatching {
                            val file = QimenShareImage.create(r, palette, File(context.cacheDir, "share"))
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享盘面"))
                        }.onFailure { e ->
                            Toast.makeText(context, "生成分享图失败：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("分享盘面")
            }
        }

        // ── AI 辅助断局（仅 AI 开关开启时显示）──
        if (aiEnabled) {
            QimenButton(
                onClick = { showAiDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !aiLoading,
            ) {
                if (aiLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(QimenDimens.spacingSm))
                        Text("参断中 ${aiElapsed}s")
                    }
                } else {
                    Text("玄鉴参断")
                }
            }

            if (aiReading.isNotBlank()) {
                QimenCard(accentBar = true) {
                    var showReasoning by remember { mutableStateOf(false) }
                    Text(
                        "玄鉴",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                    if (aiReasoning.isNotBlank()) {
                        Text(
                            if (showReasoning) "▾ 思考过程" else "▸ 思考过程",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showReasoning = !showReasoning },
                        )
                        if (showReasoning) {
                            MarkdownText(text = aiReasoning)
                        }
                        Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                    }
                    MarkdownText(text = aiReading)
                }
            }
        }
    }

    // ── AI 辅助输入 ──
    if (showAiDialog) {
        QimenDialog(
            onDismissRequest = { showAiDialog = false },
            title = "玄鉴参断",
            confirmText = "开始断局",
            onConfirm = {
                viewModel.askAi(aiSituation)
                showAiDialog = false
            },
            dismissText = "取消",
            onDismiss = { showAiDialog = false },
            text = {
                OutlinedTextField(
                    value = aiSituation,
                    onValueChange = { aiSituation = it },
                    label = { Text("目前的情况（可留空）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            },
        )
    }

    // ── 格局详情：点按弹出教材原文 ──
    patternDetail?.let { p ->
        val bookText = remember(p.name) { PatternBook.lookup(context, p.name) }
        QimenDialog(
            onDismissRequest = { patternDetail = null },
            title = p.name,
            accentColor = when (p.isAuspicious) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.error
                null -> palette.cinnabar
            },
            confirmText = "知道了",
            onConfirm = { patternDetail = null },
            dismissText = null,
            text = {
                Column {
                    Text(
                        p.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(QimenDimens.spacingMd))
                    Text(
                        "《奇门基础资料 2023版教》· 第三卷 原文",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        bookText ?: "（教材未收录此格局的单独原文，以上为盘面判定说明）",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                }
            },
        )
    }

    // ── 宫格详解：点按宫格弹出星门神释义 ──
    selectedPalace?.let { palaceNum ->
        PalaceDetailDialog(
            result = r,
            palaceNum = palaceNum,
            onDismiss = { selectedPalace = null },
        )
    }
}
