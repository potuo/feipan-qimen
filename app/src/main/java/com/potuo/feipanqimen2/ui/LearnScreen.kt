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

/** 教材轻量 Markdown 渲染：`#/##/###` 标题、`>` 口诀引用、`-` 列表、插图、等宽代码块、`**重点**`。 */
private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Quote(val lines: List<String>) : MdBlock
    data class ListBlock(val items: List<String>) : MdBlock
    data class ImageBlock(val alt: String, val path: String) : MdBlock
    data class CodeBlock(val lines: List<String>) : MdBlock
    data object Divider : MdBlock
}

private fun parseMd(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val quoteBuf = mutableListOf<String>()
    val listBuf = mutableListOf<String>()
    val codeBuf = mutableListOf<String>()
    var inCode = false

    fun flushQuote() {
        if (quoteBuf.isNotEmpty()) {
            blocks += MdBlock.Quote(quoteBuf.toList())
            quoteBuf.clear()
        }
    }

    fun flushList() {
        if (listBuf.isNotEmpty()) {
            blocks += MdBlock.ListBlock(listBuf.toList())
            listBuf.clear()
        }
    }

    text.lines().forEach { raw ->
        val line = raw.trimEnd()
        if (inCode) {
            if (line.trim() == "```") {
                blocks += MdBlock.CodeBlock(codeBuf.toList())
                codeBuf.clear()
                inCode = false
            } else {
                codeBuf += line
            }
            return@forEach
        }
        val trimmed = line.trim()
        when {
            trimmed == "```" -> { flushQuote(); flushList(); inCode = true }
            trimmed == "---" -> { flushQuote(); flushList(); blocks += MdBlock.Divider }
            trimmed.startsWith("# ") -> { flushQuote(); flushList(); blocks += MdBlock.Heading(1, trimmed.drop(2)) }
            trimmed.startsWith("## ") -> { flushQuote(); flushList(); blocks += MdBlock.Heading(2, trimmed.drop(3)) }
            trimmed.startsWith("### ") -> { flushQuote(); flushList(); blocks += MdBlock.Heading(3, trimmed.drop(4)) }
            trimmed.startsWith("> ") -> { flushList(); quoteBuf += trimmed.drop(2) }
            trimmed.startsWith("- ") -> { flushQuote(); listBuf += trimmed.drop(2) }
            trimmed.startsWith("![") -> {
                flushQuote(); flushList()
                val m = Regex("!\\[([^]]+)]\\(([^)]+)\\)").find(trimmed)
                if (m != null) blocks += MdBlock.ImageBlock(m.groupValues[1], m.groupValues[2])
            }
            trimmed.isEmpty() -> { flushQuote(); flushList() }
            else -> { flushQuote(); flushList(); blocks += MdBlock.Paragraph(trimmed) }
        }
    }
    flushQuote(); flushList()
    if (codeBuf.isNotEmpty()) blocks += MdBlock.CodeBlock(codeBuf.toList())
    return blocks
}

/** 行内 `**重点**` 解析为 AnnotatedString */
private fun parseInline(text: String): AnnotatedString = buildAnnotatedString {
    val re = Regex("\\*\\*(.+?)\\*\\*")
    var last = 0
    for (m in re.findAll(text)) {
        append(text.substring(last, m.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[1]) }
        last = m.range.last + 1
    }
    append(text.substring(last))
}

/** 教材图片全局缓存（LazyColumn 复用避免反复解码） */
private object BookImageCache {
    val map = mutableMapOf<String, ImageBitmap>()
}

@Composable
private fun MdImageBlock(block: MdBlock.ImageBlock, palette: QimenPalette) {
    val context = LocalContext.current
    val bitmap = remember(block.path) {
        BookImageCache.map.getOrPut(block.path) {
            runCatching {
                context.assets.open("qimen_book/${block.path}").use { BitmapFactory.decodeStream(it) }
                    ?.asImageBitmap()
            }.getOrNull() ?: ImageBitmap(1, 1)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = QimenDimens.spacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = block.alt,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.gridBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.padding(top = QimenDimens.spacingXs))
        Text(
            "▲ ${block.alt}",
            fontSize = 12.sp,
            color = palette.secondaryText,
            modifier = Modifier.padding(horizontal = QimenDimens.spacingMd),
        )
    }
}

@Composable
private fun BlockItem(block: MdBlock) {
    val palette = LocalQimenPalette.current
    when (block) {
        is MdBlock.Heading -> when (block.level) {
            1 -> Text(
                block.text,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = palette.cinnabar,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
            )
            2 -> Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(palette.cinnabar, RoundedCornerShape(2.dp)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    block.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.inkText,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                )
            }
            else -> Text(
                block.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.gold,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
            )
        }
        is MdBlock.Paragraph -> Text(
            parseInline(block.text),
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = palette.inkText,
        )
        is MdBlock.Quote -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.cinnabar.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                .border(
                    width = 2.dp,
                    color = palette.cinnabar.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            block.lines.forEach { line ->
                Text(
                    line,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.inkText,
                )
            }
        }
        is MdBlock.ListBlock -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            block.items.forEach { item ->
                Text(
                    parseInline(item),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = palette.inkText,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        is MdBlock.ImageBlock -> MdImageBlock(block, palette)
        is MdBlock.CodeBlock -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.slate.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            block.lines.forEach { line ->
                Text(
                    line.replace("\t", "    "),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    color = palette.inkText,
                )
            }
        }
        is MdBlock.Divider -> HorizontalDivider(
            color = palette.gridBorder.copy(alpha = 0.4f),
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

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
fun LearnScreen(onBack: () -> Unit) {
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
