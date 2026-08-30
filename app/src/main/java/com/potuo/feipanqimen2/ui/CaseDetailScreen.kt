package com.potuo.feipanqimen2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.potuo.feipanqimen2.data.CaseEntity
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenTopBar
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            viewModel.loadCase(it)
        }
    }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { case?.let { c -> viewModel.exportOne(c, it) } }
    }

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
                val date = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
                exportLauncher.launch("feipan_qimen_cases_$date.json")
            }) {
                Icon(Icons.Default.Share, contentDescription = "导出")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
            Text(c.siZhu, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("${c.dunType}${c.juNumber}局 · ${c.jieQi} · ${c.yuan}")
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

            QimenButton(
                onClick = { viewModel.updateCase(c, tags, note) },
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
