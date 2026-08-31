package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.potuo.feipanqimen2.QimenShareImage
import com.potuo.feipanqimen2.data.CaseEntity
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var huangLiExpanded by remember { mutableStateOf(false) }
    val result by viewModel.qimenResult.collectAsState()

    BackHandler(onBack = onBack)

    LaunchedEffect(caseId) {
        val loaded = viewModel.getCaseById(caseId)
        case = loaded
        loaded?.let {
            tags = it.tags
            note = it.note
            feedback = it.feedback
            viewModel.loadCase(it)
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

            QimenBoard(result = r, animate = false)

            if (c.huangLi.isNotBlank()) {
                HuangLiCard(
                    summary = c.huangLi,
                    expanded = huangLiExpanded,
                    onToggle = { huangLiExpanded = !huangLiExpanded },
                )
            }

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("标签") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注/占断") },
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

            QimenButton(
                onClick = { viewModel.updateCase(c, tags, note, feedback) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("保存修改") }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除后不可恢复，确定删除此案例？") },
            confirmButton = {
                TextButton(onClick = {
                    case?.let { viewModel.deleteCase(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}
