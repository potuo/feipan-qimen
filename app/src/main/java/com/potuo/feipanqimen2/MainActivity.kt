package com.potuo.feipanqimen2

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.potuo.feipanqimen2.UpdateChecker
import com.potuo.feipanqimen2.log.LogManager
import com.potuo.feipanqimen2.ui.AboutScreen
import com.potuo.feipanqimen2.ui.CaseDetailScreen
import com.potuo.feipanqimen2.ui.CaseListScreen
import com.potuo.feipanqimen2.ui.HuangLiScreen
import com.potuo.feipanqimen2.ui.InputScreen
import com.potuo.feipanqimen2.ui.LearnScreen
import com.potuo.feipanqimen2.ui.ResultScreen
import com.potuo.feipanqimen2.ui.SettingsScreen
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenDialog
import com.potuo.feipanqimen2.ui.theme.FeipanQimenTheme
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

private enum class Section(val title: String) {
    PAN("飞盘排盘"),
    CASES("案例库"),
    HUANGLI("黄历"),
    LEARN("飞盘总纲"),
    ABOUT("关于"),
    SETTINGS("设置"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogManager.init(this)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogManager.logException("未捕获异常@${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        enableEdgeToEdge()
        setContent {
            val prefs = remember { getSharedPreferences("app_settings", MODE_PRIVATE) }
            var isDark by remember { mutableStateOf(prefs.getBoolean("is_dark", false)) }
            var themeName by remember { mutableStateOf(prefs.getString("theme_name", "classic") ?: "classic") }
            FeipanQimenTheme(isDark = isDark, themeName = themeName) {
                MainApp(
                    isDark = isDark,
                    themeName = themeName,
                    onToggleDark = {
                        isDark = !isDark
                        prefs.edit().putBoolean("is_dark", isDark).apply()
                    },
                    onSelectTheme = { name ->
                        themeName = name
                        prefs.edit().putString("theme_name", name).apply()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
    isDark: Boolean = false,
    themeName: String = "classic",
    onToggleDark: () -> Unit = {},
    onSelectTheme: (String) -> Unit = {},
) {
    var showSplash by remember { mutableStateOf(true) }
    var section by remember { mutableStateOf(Section.PAN) }
    var showResult by remember { mutableStateOf(false) }
    var detailCaseId by remember { mutableStateOf<Long?>(null) }
    var lastBackPress by remember { mutableLongStateOf(0L) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appPrefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var changelogDialog by remember { mutableStateOf<ChangelogDialogState?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // 启动：闪屏 → 更新日志弹窗（新版本首次）→ 静默检查更新（24h 一次）
    LaunchedEffect(Unit) {
        delay(1900)
        showSplash = false

        val localVer = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0"
        }.getOrDefault("0")

        val lastSeen = appPrefs.getString("last_seen_version", null)
        if (lastSeen != localVer) {
            var entries = UpdateChecker.loadChangelogCache(context) ?: emptyList()
            if (entries.isEmpty()) {
                UpdateChecker.fetchChangelog()?.let { fresh ->
                    entries = fresh
                    UpdateChecker.saveChangelogCache(context, fresh)
                }
            }
            changelogDialog = ChangelogDialogState(localVer, entries)
            snapshotFlow { changelogDialog }.first { it == null }
        }

        if (UpdateChecker.shouldAutoCheck(context)) {
            val info = UpdateChecker.checkLatest(localVer)
            UpdateChecker.markChecked(context)
            val ignored = appPrefs.getString("ignored_update_version", null)
            if (info != null &&
                UpdateChecker.compareVersions(info.version, localVer) > 0 &&
                (ignored == null || UpdateChecker.compareVersions(info.version, ignored) > 0)) {
                updateInfo = info
            }
        }
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    val inDetail = detailCaseId != null

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerState.isOpen && inDetail) {
        detailCaseId = null
    }
    BackHandler(enabled = !drawerState.isOpen && !inDetail && showResult) {
        showResult = false
    }
    BackHandler(enabled = !drawerState.isOpen && !inDetail && !showResult) {
        val now = System.currentTimeMillis()
        if (now - lastBackPress < 2000) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPress = now
            Toast.makeText(context, "再按一次返回键退出", Toast.LENGTH_SHORT).show()
        }
    }

    val title = when {
        inDetail -> "案例详情"
        section == Section.PAN && showResult -> "已起盘"
        else -> section.title
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.5f),
            ) {
                DrawerHeader()
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                NavigationDrawerItem(
                    label = { Text("飞盘排盘") },
                    selected = section == Section.PAN,
                    onClick = {
                        section = Section.PAN
                        showResult = false
                        detailCaseId = null
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                )
                NavigationDrawerItem(
                    label = { Text("案例库") },
                    selected = section == Section.CASES,
                    onClick = {
                        section = Section.CASES
                        detailCaseId = null
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                )
                NavigationDrawerItem(
                    label = { Text("黄历") },
                    selected = section == Section.HUANGLI,
                    onClick = {
                        section = Section.HUANGLI
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                )
                NavigationDrawerItem(
                    label = { Text("飞盘总纲") },
                    selected = section == Section.LEARN,
                    onClick = {
                        section = Section.LEARN
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                )
                NavigationDrawerItem(
                    label = { Text("关于") },
                    selected = section == Section.ABOUT,
                    onClick = {
                        section = Section.ABOUT
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                )
                NavigationDrawerItem(
                    label = { Text("设置") },
                    selected = section == Section.SETTINGS,
                    onClick = {
                        section = Section.SETTINGS
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                )
                Spacer(modifier = Modifier.weight(1f))
                // 黑白切换按钮（仅图标，带动效）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleDark)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Crossfade(
                        targetState = isDark,
                        animationSpec = tween(350),
                        label = "themeToggle",
                    ) { dark ->
                        Icon(
                            imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (dark) "切换到白色" else "切换到暗色",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    "天禽 v${runCatching {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
                    }.getOrDefault("?")}\n飞盘奇门 · 鸣法体系 · 值使飞宫法",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        if (inDetail || (section == Section.PAN && showResult)) {
                            IconButton(onClick = {
                                if (inDetail) detailCaseId = null else showResult = false
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "菜单")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AnimatedContent(
                    targetState = NavTarget(section, showResult, detailCaseId),
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInHorizontally(tween(240)) { it / 14 })
                            .togetherWith(fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -it / 14 })
                    },
                    label = "screenTransition",
                ) { target ->
                    when {
                        target.detailCaseId != null -> CaseDetailScreen(
                            viewModel = viewModel,
                            caseId = target.detailCaseId!!,
                            onBack = { detailCaseId = null },
                        )
                        target.section == Section.PAN && target.showResult -> ResultScreen(
                            viewModel = viewModel,
                            onBack = { showResult = false },
                        )
                        target.section == Section.PAN -> InputScreen(
                            viewModel = viewModel,
                            onCalculate = { showResult = true },
                        )
                        target.section == Section.CASES -> CaseListScreen(
                            viewModel = viewModel,
                            onCaseClick = { detailCaseId = it },
                            onGoToPan = {
                                section = Section.PAN
                                showResult = false
                            },
                        )
                        target.section == Section.HUANGLI -> HuangLiScreen()
                        target.section == Section.LEARN -> LearnScreen()
                        target.section == Section.ABOUT -> AboutScreen()
                        else -> SettingsScreen(
                            viewModel = viewModel,
                            isDark = isDark,
                            themeName = themeName,
                            onSelectTheme = onSelectTheme,
                        )
                    }
                }
            }
        }
    }

    changelogDialog?.let { state ->
        val currentEntry = state.entries.firstOrNull {
            UpdateChecker.compareVersions(it.version, state.version) == 0
        }
        QimenDialog(
            onDismissRequest = {},
            title = "更新日志 · v${state.version}",
            confirmText = "知道了",
            onConfirm = {
                appPrefs.edit().putString("last_seen_version", state.version).apply()
                changelogDialog = null
            },
            dismissText = null,
            text = {
                Column {
                    if (currentEntry != null) {
                        currentEntry.items.forEach { item ->
                            Text(
                                "· $item",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    } else {
                        Text(
                            "暂无更新日志内容",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                        )
                    }
                }
            },
        )
    }

    updateInfo?.let { info ->
        QimenDialog(
            onDismissRequest = { if (!downloading) updateInfo = null },
            title = "发现新版本 v${info.version}",
            confirmText = if (downloading) "下载中…" else "更新",
            onConfirm = {
                if (downloading) return@QimenDialog
                scope.launch {
                    downloading = true
                    downloadProgress = 0f
                    val apkFile = File(context.cacheDir, "update.apk")
                    val ok = UpdateChecker.downloadApk(info.apkUrl, apkFile) { p ->
                        scope.launch { downloadProgress = p }
                    }
                    downloading = false
                    if (ok) {
                        UpdateChecker.installApk(context, apkFile)
                        updateInfo = null
                    } else {
                        Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            confirmEnabled = !downloading,
            dismissText = "取消",
            onDismiss = { if (!downloading) updateInfo = null },
            neutralText = "不再提醒",
            onNeutral = {
                appPrefs.edit().putString("ignored_update_version", info.version).apply()
                updateInfo = null
            },
            text = {
                Column {
                    Text(
                        info.notes?.takeIf { it.isNotBlank() } ?: "修复与优化内容请见更新日志",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (downloading) {
                        LinearProgressIndicator(
                            progress = { downloadProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                    }
                }
            },
        )
    }
}

private data class ChangelogDialogState(
    val version: String,
    val entries: List<ChangelogEntry>,
)

/** 导航目标（供 AnimatedContent 转场比较） */
private data class NavTarget(
    val section: Section,
    val showResult: Boolean,
    val detailCaseId: Long?,
)

/** 启动动画：罗盘金环旋转 + 标题浮现（跟随主题配色与明暗） */
@Composable
private fun SplashScreen() {
    val palette = LocalQimenPalette.current
    val bg = palette.paper
    val titleColor = palette.inkText
    val transition = rememberInfiniteTransition(label = "splash")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "splashRotation",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "splashAlpha",
    )

    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        titleVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        // 八卦金点（固定环绕）
        Canvas(modifier = Modifier.size(132.dp)) {
            val ring = size.minDimension * 0.46f
            repeat(8) { i ->
                val a = Math.toRadians(i * 45.0)
                drawCircle(
                    color = palette.gold.copy(alpha = 0.85f),
                    radius = size.minDimension * 0.035f,
                    center = Offset(center.x + (ring * cos(a)).toFloat(), center.y + (ring * sin(a)).toFloat()),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(120.dp)
                .rotate(rotation)
                .border(2.dp, palette.gold.copy(alpha = ringAlpha), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .rotate(-rotation * 1.6f)
                    .border(1.dp, palette.gold.copy(alpha = ringAlpha * 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                // 几何天禽（圆头 + 双三角翼 + 身 + 尾 + 朱砂眼）
                Canvas(modifier = Modifier.size(54.dp)) {
                    val c = center
                    val u = size.minDimension
                    val gold = palette.gold
                    drawCircle(color = gold, radius = u * 0.13f, center = Offset(c.x, c.y - u * 0.26f))
                    drawCircle(
                        color = palette.cinnabar,
                        radius = u * 0.06f,
                        center = Offset(c.x + u * 0.065f, c.y - u * 0.235f),
                    )
                    drawPath(
                        Path().apply {
                            moveTo(c.x - u * 0.38f, c.y + u * 0.08f)
                            lineTo(c.x - u * 0.05f, c.y + u * 0.08f)
                            lineTo(c.x - u * 0.21f, c.y - u * 0.26f)
                            close()
                        },
                        color = gold,
                    )
                    drawPath(
                        Path().apply {
                            moveTo(c.x + u * 0.38f, c.y + u * 0.08f)
                            lineTo(c.x + u * 0.05f, c.y + u * 0.08f)
                            lineTo(c.x + u * 0.21f, c.y - u * 0.26f)
                            close()
                        },
                        color = gold,
                    )
                    drawPath(
                        Path().apply {
                            moveTo(c.x - u * 0.06f, c.y + u * 0.12f)
                            lineTo(c.x + u * 0.06f, c.y + u * 0.12f)
                            lineTo(c.x, c.y + u * 0.30f)
                            close()
                        },
                        color = gold,
                    )
                    drawPath(
                        Path().apply {
                            moveTo(c.x, c.y + u * 0.30f)
                            lineTo(c.x - u * 0.05f, c.y + u * 0.42f)
                            lineTo(c.x + u * 0.05f, c.y + u * 0.42f)
                            close()
                        },
                        color = gold,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 190.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Crossfade(targetState = titleVisible, animationSpec = tween(700), label = "splashTitle") { visible ->
                if (visible) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "天禽",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "飞盘奇门 · 据《奇门基础资料 2023版教》鸣法体系",
                            fontSize = 12.sp,
                            color = palette.gold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "天禽",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Text(
            "天禽",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
