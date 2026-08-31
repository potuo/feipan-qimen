package com.potuo.feipanqimen2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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

/**
 * 单宫详解弹窗：点按盘面宫格后弹出，展示该宫的星/门/神释义（联动教材 vol2）＋ 奇仪/六亲/旺衰/暗干支/角标。
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
            Column {
                // ── 星 ──
                SectionLabel("星 · ${info.star}")
                PalaceRef.lookup(context, info.star)?.let { Body(it) }

                // ── 门 ──
                SectionLabel("门 · ${info.gate}门")
                PalaceRef.lookup(context, info.gate)?.let { Body(it) }

                // ── 神 ──
                SectionLabel("神 · ${info.god}")
                PalaceRef.lookup(context, info.god)?.let { Body(it) }

                // ── 奇仪 / 六亲 / 旺衰 ──
                SectionLabel("奇仪")
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

                // ── 奇门演卦（星门 / 门宫）──
                val xingMen = QiMenYanGua.xingMenYanGua(info.star, info.gate)
                val menGong = QiMenYanGua.menGongYanGua(info.gate, palaceNum)
                if (xingMen != null || menGong != null) {
                    SectionLabel("演卦")
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
        },
    )
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
