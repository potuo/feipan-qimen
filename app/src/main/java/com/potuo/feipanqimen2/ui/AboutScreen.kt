package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.ChangelogEntry
import com.potuo.feipanqimen2.NoticeInfo
import com.potuo.feipanqimen2.R
import com.potuo.feipanqimen2.UpdateChecker
import com.potuo.feipanqimen2.UpdateInfo
import com.potuo.feipanqimen2.log.LogManager
import com.potuo.feipanqimen2.ui.components.CollapsibleSection
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** 关于：应用信息 + 检查更新 + 应用日志 + 更新日志（联网拉取） */
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        }.getOrDefault("?")
    }

    // ── 检查更新状态 ──
    var checking by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var pendingUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    // ── 应用日志状态 ──
    var logSizeKB by remember { mutableLongStateOf(LogManager.totalSizeKB(context)) }

    // ── 更新日志状态（联网拉取 + 本地缓存兜底）──
    var changelog by remember { mutableStateOf<List<ChangelogEntry>?>(null) }
    var changelogLoading by remember { mutableStateOf(true) }
    var changelogFailed by remember { mutableStateOf(false) }

    // ── 系统公告状态（Gitee 拉取，无公告不显示）──
    var notice by remember { mutableStateOf<NoticeInfo?>(null) }

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

    fun checkForUpdate() {
        if (checking || downloading) return
        scope.launch {
            checking = true
            val info = UpdateChecker.checkLatest(versionName)
            checking = false
            if (info == null) {
                Toast.makeText(context, "检查更新失败：网络不可用", Toast.LENGTH_SHORT).show()
            } else if (UpdateChecker.compareVersions(info.version, versionName) > 0) {
                pendingUpdate = info
            } else {
                Toast.makeText(context, "已是最新版本 v$versionName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadAndInstall(info: UpdateInfo) {
        if (downloading) return
        scope.launch {
            downloading = true
            downloadProgress = 0f
            val dir = File(context.cacheDir, "update").apply { mkdirs() }
            val apk = File(dir, "feipan-qimen-v${info.version}.apk")
            val ok = UpdateChecker.downloadApk(info.apkUrl, apk) { p ->
                scope.launch { downloadProgress = p }
            }
            downloading = false
            if (ok) {
                Toast.makeText(context, "下载完成，正在安装…", Toast.LENGTH_SHORT).show()
                UpdateChecker.installApk(context, apk)
            } else {
                Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 进入页面即拉取更新日志 + 系统公告
    LaunchedEffect(Unit) {
        changelog = UpdateChecker.loadChangelogCache(context)
        val fresh = UpdateChecker.fetchChangelog()
        if (fresh != null) {
            changelog = fresh
            UpdateChecker.saveChangelogCache(context, fresh)
            changelogFailed = false
        } else if (changelog == null) {
            changelogFailed = true
        }
        changelogLoading = false
        notice = UpdateChecker.fetchNotice()
    }

    // 只显示当前版本及更早版本的更新日志（如当前 v2.6，看不到 v2.6.2 的日志）
    val visibleLogs = remember(changelog, versionName) {
        changelog?.filter { UpdateChecker.compareVersions(it.version, versionName) <= 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        // ── 应用信息 ──
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GithubAvatar(
                        url = "https://avatars.githubusercontent.com/potuo?s=120",
                        size = 56.dp,
                    )
                    Spacer(modifier = Modifier.width(QimenDimens.spacingMd))
                    Column {
                        Text("天禽", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "飞盘奇门 · 鸣法体系 · 值使飞宫法 · 天禽居中 · 星门顺飞",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                        Text("版本 v$versionName · 作者 Potuo", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                Text(
                    "排盘规则依据《奇门基础资料 2023版教》（符头定元 / 值使门飞宫法 / 暗干支飞宫法）。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .padding(top = QimenDimens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "GitHub：potuo/feipan-qimen ↗",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://gitee.com/potuo/feipan-qimen")),
                                )
                            }
                        }
                        .padding(top = QimenDimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_gitee),
                        contentDescription = "Gitee",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Gitee：potuo/feipan-qimen ↗",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = QimenDimens.spacingMd))
                Text("MIT License", style = MaterialTheme.typography.bodySmall)
            }
        }

        // ── 系统公告（Gitee 拉取；无公告不显示）──
        notice?.let { n ->
            CollapsibleSection(title = "系统公告", defaultExpanded = true) {
                Text(
                    n.text,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
                n.date?.let { d ->
                    Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                    Text(
                        "发布于 $d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── 检查更新 ──
        CollapsibleSection(title = "检查更新", defaultExpanded = true) {
            Text(
                "当前版本：v$versionName",
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
            // 下载进度条
            if (downloading) {
                LinearProgressIndicator(
                    progress = { downloadProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingMd),
                )
                Text(
                    "下载进度：${(downloadProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── 应用日志 ──
        CollapsibleSection(title = "应用日志") {
            Text(
                "当前日志：${logSizeKB} KB",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QimenButton(
                onClick = {
                    val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.CHINA)
                        .format(java.util.Date())
                    exportLogLauncher.launch("feipan_qimen_logs_$date.txt")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = QimenDimens.spacingSm),
            ) { Text("导出日志") }
            QimenOutlinedButton(
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

        // ── 更新日志（联网拉取）──
        CollapsibleSection(title = "更新日志", defaultExpanded = false) {
            when {
                changelogLoading -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QimenDimens.spacingMd),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(28.dp))
                }
                changelogFailed && visibleLogs.isNullOrEmpty() -> Text(
                    "更新日志加载失败（网络不可用）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                !visibleLogs.isNullOrEmpty() -> {
                    visibleLogs.forEach { log ->
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
            }
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
                        "当前版本 v$versionName",
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

/** GitHub 头像：网络加载（失败时显示首字母占位），圆形裁剪 */
@Composable
private fun GithubAvatar(url: String, size: androidx.compose.ui.unit.Dp) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "feipan-qimen")
                if (conn.responseCode == 200) {
                    conn.inputStream.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                } else null
            }.getOrNull()
        }
    }
    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = "GitHub 头像",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "P",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
