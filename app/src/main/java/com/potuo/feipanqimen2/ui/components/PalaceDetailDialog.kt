package com.potuo.feipanqimen2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.PalaceRef
import com.potuo.feipanqimen2.QiMenYanGua
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenResult
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.ui.theme.QimenShapeTokens

/**
 * 单宫详解弹窗：点按盘面宫格后弹出，展示该宫的星/门/神结构化释义
 * （五行/宫位/吉凶/定义/象意，联动 PalaceRef 速查表）＋ 奇仪/六亲/旺衰/暗干支/角标/演卦。
 */
@Composable
fun PalaceDetailDialog(
    result: QimenResult,
    palaceNum: Int,
    onDismiss: () -> Unit,
) {
    val info = result.palaces[palaceNum] ?: return
    val context = LocalContext.current
    val palette = LocalQimenPalette.current
    val palaceName = QimenConstants.PALACE_NAMES[palaceNum] ?: ""

    QimenDialog(
        onDismissRequest = onDismiss,
        title = "$palaceName${palaceNum}宫 · ${info.direction}",
        confirmText = "知道了",
        onConfirm = onDismiss,
        dismissText = null,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                // ── 星 ──
                EntityCard("星", info.star, info.star)

                // ── 门 ──
                EntityCard("门", info.gate, "${info.gate}门")

                // ── 神 ──
                EntityCard("神", info.god, info.god)

                // ── 奇仪 / 六亲 / 旺衰 ──
                DetailCard("奇仪·六亲·旺衰") {
                Text(
                    "天盘干：${info.heavenStem}" +
                        (if (info.liuQinHeaven.isNotEmpty()) "（${info.liuQinHeaven}）" else "") +
                        (if (info.state.isNotEmpty()) " · ${info.state}" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                )
                Text(
                    "地盘干：${info.earthStem}" +
                        (if (info.liuQinEarth.isNotEmpty()) "（${info.liuQinEarth}）" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                )
                if (!info.hiddenStem.isNullOrEmpty()) {
                    Text(
                        "暗干支：${info.hiddenStem}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.gold,
                        lineHeight = 20.sp,
                    )
                }
                if (info.marks.isNotEmpty()) {
                    Text(
                        "宫格标记：${info.marks.joinToString(" · ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.cinnabar,
                        lineHeight = 20.sp,
                    )
                }
                }

                // ── 奇门演卦（星门 / 门宫）──
                val xingMen = QiMenYanGua.xingMenYanGua(info.star, info.gate)
                val menGong = QiMenYanGua.menGongYanGua(info.gate, palaceNum)
                if (xingMen != null || menGong != null) {
                    DetailCard("演卦") {
                    xingMen?.let {
                        Text(
                            "星门演卦：${it.hexagram}（${it.upperDesc} 上 / ${it.lowerDesc} 下）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                        )
                    }
                    menGong?.let {
                        Text(
                            "门宫演卦：${it.hexagram}（${it.upperDesc} 上 / ${it.lowerDesc} 下）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp,
                        )
                    }
                    }
                }
            }
        },
    )
}

@Composable
private fun EntityCard(label: String, name: String, displayName: String) {
    DetailCard("$label · $displayName") { EntitySection(label, name, displayName, showLabel = false) }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = QimenShapeTokens.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(QimenDimens.cardPadding)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(QimenDimens.spacingSm))
            content()
        }
    }
}

/**
 * 星/门/神结构化释义区块：五行·宫位·吉凶 + 定义 + 象意要点。
 * 结构化速查表无数据时，回退到教材 vol2 原文。
 */
@Composable
private fun EntitySection(label: String, name: String, displayName: String, showLabel: Boolean = true) {
    val context = LocalContext.current
    if (showLabel) SectionLabel("$label · $displayName")
    val meta = PalaceRef.info(name)
    if (meta != null) {
        Text(
            "${meta.wuxing} · ${meta.gong} · ${meta.jixiong}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            meta.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        meta.xiangyi.forEach { item ->
            Text(
                "· $item",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
            )
        }
    } else {
        PalaceRef.lookup(context, name)?.let { Body(it) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 2.dp),
    )
}
