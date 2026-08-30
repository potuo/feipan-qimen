package com.potuo.feipanqimen2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.potuo.feipanqimen2.log.LogManager
import com.potuo.feipanqimen2.ui.CaseDetailScreen
import com.potuo.feipanqimen2.ui.CaseListScreen
import com.potuo.feipanqimen2.ui.HuangLiScreen
import com.potuo.feipanqimen2.ui.InputScreen
import com.potuo.feipanqimen2.ui.ResultScreen
import com.potuo.feipanqimen2.ui.SettingsScreen
import com.potuo.feipanqimen2.ui.theme.FeipanQimenTheme
import com.potuo.feipanqimen2.ui.theme.QimenColors
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Section(val title: String) {
    PAN("飞盘排盘"),
    CASES("案例库"),
    HUANGLI("黄历"),
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
            FeipanQimenTheme(isDark = isDark) {
                MainApp(
                    isDark = isDark,
                    onToggleDark = {
                        isDark = !isDark
                        prefs.edit().putBoolean("is_dark", isDark).apply()
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
    onToggleDark: () -> Unit = {},
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

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(Unit) {
        if (showSplash) {
            delay(1900)
            showSplash = false
        }
    }

    if (showSplash) {
        SplashScreen(isDark = isDark)
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
                    "飞盘奇门遁甲 v2.5\n鸣法体系 · 值使飞宫法",
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
                when {
                    inDetail -> CaseDetailScreen(
                        viewModel = viewModel,
                        caseId = detailCaseId!!,
                        onBack = { detailCaseId = null },
                    )
                    section == Section.PAN && showResult -> ResultScreen(
                        viewModel = viewModel,
                        onBack = { showResult = false },
                    )
                    section == Section.PAN -> InputScreen(
                        viewModel = viewModel,
                        onCalculate = { showResult = true },
                    )
                    section == Section.CASES -> CaseListScreen(
                        viewModel = viewModel,
                        onCaseClick = { detailCaseId = it },
                        onSettingsClick = { section = Section.SETTINGS },
                        onGoToPan = {
                            section = Section.PAN
                            showResult = false
                        },
                    )
                    section == Section.HUANGLI -> HuangLiScreen()
                    else -> SettingsScreen(
                        viewModel = viewModel,
                        isDark = isDark,
                        onToggleDark = onToggleDark,
                        onBack = { section = Section.PAN },
                    )
                }
            }
        }
    }
}

/** 启动动画：罗盘金环旋转 + 标题浮现（跟随主题明暗） */
@Composable
private fun SplashScreen(isDark: Boolean) {
    val bg = if (isDark) Color(0xFF1A1C20) else QimenColors.PaperLight
    val titleColor = if (isDark) Color(0xFFF2EDE3) else QimenColors.InkText
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
        Box(
            modifier = Modifier
                .size(120.dp)
                .rotate(rotation)
                .border(2.dp, QimenColors.Gold.copy(alpha = ringAlpha), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .rotate(-rotation * 1.6f)
                    .border(1.dp, QimenColors.Gold.copy(alpha = ringAlpha * 0.7f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(QimenColors.Cinnabar, CircleShape),
                )
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
                            "飞盘奇门遁甲",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "据《奇门基础资料 2023版教》 · 鸣法体系",
                            fontSize = 12.sp,
                            color = QimenColors.Gold,
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
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(QimenColors.Cinnabar, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("飞", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text(
            "飞盘奇门",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
