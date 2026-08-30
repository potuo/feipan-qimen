package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.potuo.feipanqimen2.QimenShareImage
import com.potuo.feipanqimen2.data.CASE_CATEGORIES
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenPatternDetector
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.components.SealBadge
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val result by viewModel.qimenResult.collectAsState()
    val huangLi by viewModel.huangLi.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()
    var huangLiExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = LocalQimenPalette.current

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
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = CardShape,
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
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
        }

        QimenBoard(result = r, animate = true)

        // ── 格局（据《奇门基础资料 2023版教》第三卷）──
        if (patterns.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = CardShape,
            ) {
                Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                    Text(
                        "格局",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(QimenDimens.spacingMd))
                    patterns.forEach { p ->
                        Column(modifier = Modifier.padding(bottom = QimenDimens.spacingMd)) {
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
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
        ) {
            CASE_CATEGORIES.forEach { c ->
                FilterChip(
                    selected = category == c,
                    onClick = { viewModel.setCategory(c) },
                    label = { Text(c) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
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
    }
}
