package com.potuo.feipanqimen2.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.ui.theme.QimenPalette

/**
 * 轻量 Markdown 渲染器（自研，非第三方库）。
 * 支持：`#/##/###` 标题、`>` 引用、`-` 列表、`**重点**`、插图、```等宽代码块```、`---` 分隔线。
 * LearnScreen（教材阅读）与 AI 断局结果共用。
 */
sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Quote(val lines: List<String>) : MdBlock
    data class ListBlock(val items: List<String>) : MdBlock
    data class ImageBlock(val alt: String, val path: String) : MdBlock
    data class CodeBlock(val lines: List<String>) : MdBlock
    data object Divider : MdBlock
}

/** 轻量 Markdown 解析：按行归类为块 */
fun parseMd(text: String): List<MdBlock> {
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
fun parseInline(text: String): AnnotatedString = buildAnnotatedString {
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

/** 渲染单个 Markdown 块 */
@Composable
fun BlockItem(block: MdBlock) {
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

/** 渲染整段 Markdown 文本（AI 断局等场景直接调用） */
@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        parseMd(text).forEach { block -> BlockItem(block) }
    }
}
