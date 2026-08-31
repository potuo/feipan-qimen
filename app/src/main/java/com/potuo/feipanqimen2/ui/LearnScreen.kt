package com.potuo.feipanqimen2.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.ui.components.BlockItem
import com.potuo.feipanqimen2.ui.components.MdBlock
import com.potuo.feipanqimen2.ui.components.parseMd
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.ui.theme.QimenPalette
import kotlinx.coroutines.launch

private data class VolumeInfo(
    val title: String,
    val fileName: String,
    val summary: String,
)

private val VOLUMES = listOf(
    VolumeInfo("第一卷 · 数术基础", "vol1.txt", "河图洛书（含原图）/ 先后天八卦 / 八卦万物类象 / 阴阳五行 / 天干地支 / 六十甲子 / 二十四节气"),
    VolumeInfo("第二卷 · 奇门排盘", "vol2.txt", "排列四柱 / 定阴阳局 / 布地盘天盘 / 九神九星八门 / 暗干支 / 置闰拆补（含排盘案例图）"),
    VolumeInfo("第三卷 · 占断法则", "vol3.txt", "六仪击刑 / 正格辅格 / 守门九遁 / 三诈五假 / 六亲断法 / 星门八卦对应"),
)

/** 教材轻量 Markdown 渲染已抽到 ui/components/MarkdownText.kt（MdBlock/parseMd/BlockItem 公共复用） */

private data class SearchHit(val index: Int, val section: String, val preview: String)

/** 章节目录项 */
private data class CatalogItem(val index: Int, val level: Int, val text: String)

/** 阅读器：LazyColumn 渲染 + 章节目录 + 关键词搜索 + 阅读位置记忆 */
@Composable
private fun ReaderView(vol: VolumeInfo, onBack: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalQimenPalette.current
    val content = remember(vol.fileName) {
        runCatching {
            context.assets.open("qimen_book/${vol.fileName}").bufferedReader().use { it.readText() }
        }.getOrDefault("（未能加载教材内容）")
    }
    val blocks = remember(content) { parseMd(content) }
    val listState = rememberLazyListState()
    var showCatalog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    // 恢复上次阅读位置
    LaunchedEffect(vol.fileName) {
        val saved = prefs.getInt("learn_pos_${vol.fileName}", 0)
        if (saved > 0 && saved < blocks.size) {
            listState.scrollToItem(saved)
        }
    }
    // 离开时保存阅读位置
    DisposableEffect(vol.fileName) {
        onDispose {
            prefs.edit().putInt("learn_pos_${vol.fileName}", listState.firstVisibleItemIndex).apply()
        }
    }

    // 章节目录（H2/H3）
    val catalog = remember(blocks) {
        blocks.mapIndexedNotNull { i, b ->
            if (b is MdBlock.Heading && b.level in 2..3) CatalogItem(i, b.level, b.text) else null
        }
    }

    // 搜索结果
    val hits = remember(query, blocks) {
        if (query.isBlank()) emptyList()
        else {
            val result = mutableListOf<SearchHit>()
            var section = "卷首"
            blocks.forEachIndexed { i, b ->
                when (b) {
                    is MdBlock.Heading -> if (b.level <= 2) section = b.text
                    is MdBlock.Paragraph -> if (b.text.contains(query)) {
                        result += SearchHit(i, section, b.text.take(70))
                    }
                    is MdBlock.Quote -> b.lines.firstOrNull { it.contains(query) }?.let {
                        result += SearchHit(i, section, it.take(70))
                    }
                    is MdBlock.ListBlock -> b.items.firstOrNull { it.contains(query) }?.let {
                        result += SearchHit(i, section, it.take(70))
                    }
                    else -> {}
                }
            }
            result
        }
    }

    if (showCatalog) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QimenDimens.spacingSm, vertical = QimenDimens.spacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showCatalog = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    Text("章节目录", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "共 ${catalog.size} 节",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = QimenDimens.spacingMd),
                    )
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(catalog) { item ->
                        Text(
                            if (item.level == 2) item.text else "　${item.text}",
                            fontSize = if (item.level == 2) 15.sp else 13.5.sp,
                            fontWeight = if (item.level == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (item.level == 2) palette.inkText else palette.secondaryText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { listState.scrollToItem(item.index) }
                                    showCatalog = false
                                }
                                .padding(horizontal = QimenDimens.spacingLg, vertical = 9.dp),
                        )
                    }
                }
            }
        }
        return
    }

    if (showSearch) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QimenDimens.spacingSm, vertical = QimenDimens.spacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showSearch = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜索教材内容…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                HorizontalDivider()
                if (query.isNotBlank() && hits.isEmpty()) {
                    Text(
                        "未找到「$query」相关内容",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(QimenDimens.spacingLg),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(hits) { hit ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { listState.scrollToItem(hit.index) }
                                        showSearch = false
                                    }
                                    .padding(horizontal = QimenDimens.spacingLg, vertical = 8.dp),
                            ) {
                                Text(
                                    "「${hit.section}」",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.cinnabar,
                                )
                                Text(
                                    hit.preview,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = palette.inkText,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = QimenDimens.spacingSm, vertical = QimenDimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(vol.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showSearch = true }) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
            IconButton(onClick = { showCatalog = true }) {
                Icon(Icons.Default.MenuBook, contentDescription = "目录")
            }
        }
        HorizontalDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = QimenDimens.spacingLg, vertical = QimenDimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(blocks) { _, block ->
                BlockItem(block)
            }
        }
    }
}

/** 飞盘总纲：教材《奇门基础资料 2023版教》分卷阅读（md 排版 + 原图 + 目录/搜索/位置记忆） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen() {
    var currentVolume by remember { mutableStateOf<VolumeInfo?>(null) }

    if (currentVolume == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(QimenDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
        ) {
            Text(
                "据《奇门基础资料 2023版教》鸣法体系",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "本栏目收录排盘所依据的教材原文，按卷阅读；已按卷/章/节重新排版，口诀用引用框标注，附教材原图。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
            ) {
                VOLUMES.forEach { vol ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentVolume = vol },
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                            Text(vol.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.padding(top = QimenDimens.spacingXs))
                            Text(
                                vol.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    } else {
        ReaderView(vol = currentVolume!!, onBack = { currentVolume = null })
    }
}
