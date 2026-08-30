package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.UpdateChecker
import com.potuo.feipanqimen2.UpdateInfo
import com.potuo.feipanqimen2.log.LogManager
import kotlinx.coroutines.launch
import java.io.File

import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ChangeLogEntry(val version: String, val date: String, val items: List<String>)

private val changeLogs = listOf(
    ChangeLogEntry("v2.5.1", "2026-08-31", listOf(
        "新增检查更新（GitHub 发布源，自动检测新版本）",
        "新增紫微主题配色（浅色 / 暗色两态）",
        "设置页新增关于信息（作者 / GitHub 链接）",
    )),
    ChangeLogEntry("v2.5", "2026-08-31", listOf(
        "新增黄历详情页（侧滑栏入口，完整宜忌/冲煞/彭祖/吉神凶煞/二十八宿）",
        "侧滑栏底部新增黑白主题切换按钮（图标动效）",
        "主题收敛为古典金（黄红金），设置页默认",
        "启动页与动画跟随主题明暗",
        "侧滑栏宽度调整为 1/2",
    )),
    ChangeLogEntry("v2.4", "2026-08-30", listOf(
        "设置页改为折叠分组（外观/数据/日志）",
        "新增更新日志",
    )),
    ChangeLogEntry("v2.3", "2026-08-30", listOf(
        "框架重构：底部导航改为左侧侧滑栏",
        "排盘盘面恢复中式古典风格（金边/印章/中宫金圆）",
        "新增启动动画（罗盘旋转）",
    )),
    ChangeLogEntry("v2.2", "2026-08-30", listOf(
        "UI 全面重构为 Material 3 现代风",
    )),
    ChangeLogEntry("v2.1", "2026-08-30", listOf(
        "修复排盘崩溃（值使中门/甲时值符）",
        "新增应用日志系统（设置页导出）",
    )),
    ChangeLogEntry("v2.0", "2026-08-30", listOf(
        "飞盘排盘 App 重构：值使飞宫法 + 符头定元",
        "新增黄历、案例库（保存/搜索/导入导出）",
    )),
)

/** 可折叠设置分组 */
@Composable
private fun CollapsibleSection(
    title: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
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
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    isDark: Boolean = false,
    themeName: String = "classic",
    onToggleDark: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var pendingUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    val localVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0"
        }.getOrDefault("0")
    }

    fun checkForUpdate() {
        if (checking || downloading) return
        scope.launch {
            checking = true
            val info = UpdateChecker.checkLatest()
            checking = false
            if (info == null) {
                Toast.makeText(context, "检查更新失败：网络不可用", Toast.LENGTH_SHORT).show()
            } else if (UpdateChecker.compareVersions(info.version, localVersion) > 0) {
                pendingUpdate = info
            } else {
                Toast.makeText(context, "已是最新版本 v$localVersion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadAndInstall(info: UpdateInfo) {
        if (downloading) return
        scope.launch {
            downloading = true
            val dir = File(context.cacheDir, "update").apply { mkdirs() }
            val apk = File(dir, "feipan-qimen-v${info.version}.apk")
            val ok = UpdateChecker.downloadApk(info.apkUrl, apk)
            downloading = false
            if (ok) {
                Toast.makeText(context, "下载完成，正在安装…", Toast.LENGTH_SHORT).show()
                UpdateChecker.installApk(context, apk)
            } else {
                Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportAll(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importCases(it) } }

    val exportLogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            val text = LogManager.exportAllLogs(context)
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                Toast.makeText(context, "日志已导出", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                LogManager.logException("导出日志", e)
                Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var logSizeKB by remember { mutableLongStateOf(LogManager.totalSizeKB(context)) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(QimenDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
        ) {
            // ── 外观 ──
            CollapsibleSection(title = "外观 · 配色", defaultExpanded = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleTheme)
                        .padding(vertical = QimenDimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (themeName == "ziwei") "紫微" else "古典金",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        if (themeName == "ziwei") " · 点击切换古典金" else "（默认） · 点击切换紫微",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        if (isDark) "暗色" else "浅色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── 数据管理 ──
            CollapsibleSection(title = "数据管理") {
                Text(
                    "导出全部案例为 JSON 文件，或从 JSON 文件导入案例。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QimenButton(
                    onClick = {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
                        exportLauncher.launch("feipan_qimen_cases_$date.json")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("导出全部案例") }
                QimenButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("导入案例") }
            }

            // ── 应用日志 ──
            CollapsibleSection(title = "应用日志") {
                Text(
                    "记录排盘请求、案例操作与崩溃信息。导出后提供给开发者检核。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "当前日志：${logSizeKB} KB",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingSm),
                )
                QimenButton(
                    onClick = {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
                        exportLogLauncher.launch("feipan_qimen_logs_$date.txt")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("导出日志") }
                QimenButton(
                    onClick = {
                        LogManager.clearLogs(context)
                        logSizeKB = LogManager.totalSizeKB(context)
                        Toast.makeText(context, "日志已清空", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("清空日志") }
            }

            // ── 检查更新 ──
            CollapsibleSection(title = "检查更新", defaultExpanded = true) {
                Text(
                    "当前版本：v$localVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                QimenButton(
                    onClick = { checkForUpdate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                    enabled = !checking && !downloading,
                ) {
                    Text(
                        when {
                            downloading -> "下载中…"
                            checking -> "检查中…"
                            else -> "检查更新"
                        },
                    )
                }
                Text(
                    "自动从 GitHub 发布源检测新版本，无需服务器。",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingSm),
                )
            }

            // ── 更新日志 ──
            CollapsibleSection(title = "更新日志") {
                changeLogs.forEach { log ->
                    Column(modifier = Modifier.padding(bottom = QimenDimens.spacingLg)) {
                        Text(
                            "${log.version}（${log.date}）",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        log.items.forEach { item ->
                            Text(
                                "· $item",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }

            // ── 关于 ──
            CollapsibleSection(title = "关于") {
                Text(
                    "飞盘奇门遁甲排盘",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "鸣法体系 · 值使飞宫法",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "版本 v$localVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "作者：Potuo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/potuo/feipan-qimen")),
                                )
                            }
                        }
                        .padding(top = QimenDimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "GitHub：potuo/feipan-qimen ↗",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "MIT License · 数据完全本地存储",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingSm),
                )
            }

            Text(
                "飞盘奇门遁甲 v$localVersion\n鸣法体系 · 值使飞宫法",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = QimenDimens.spacingLg),
            )
        }
    }

    pendingUpdate?.let { info ->
        AlertDialog(
            onDismissRequest = { pendingUpdate = null },
            title = { Text("发现新版本 v${info.version}") },
            text = {
                Column {
                    info.notes?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        "当前版本 v$localVersion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingUpdate = null
                        downloadAndInstall(info)
                    },
                ) { Text("下载并安装") }
            },
            dismissButton = {
                TextButton(onClick = { pendingUpdate = null }) { Text("以后再说") }
            },
        )
    }
}
