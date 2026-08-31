package com.potuo.feipanqimen2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.qimen.HuangLiService
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 黄历详情页：前后切换日期 + 完整黄历信息 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuangLiScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val palette = LocalQimenPalette.current

    val info = remember(selectedDate) {
        HuangLiService.getHuangLi(selectedDate.atTime(12, 0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        // ── 日期切换：前一天 ‹ | 日期（点按选日）| › 后一天 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QimenOutlinedButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "前一天")
            }
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true }
                    .padding(vertical = QimenDimens.spacingSm),
            )
            QimenOutlinedButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "后一天")
            }
        }

        // ── 农历头部 ──
        QimenCard(accentBar = true) {
            Text(
                "${info.lunarDate} · ${info.shengXiao}年",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (info.festival.isNotBlank()) {
                Text(
                    "节日：${info.festival}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.cinnabar,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (info.jieQi.isNotBlank()) {
                Text(
                    "节气：${info.jieQi}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                "二十八宿：${info.xiu}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // ── 宜忌等详情 ──
        DetailRow(title = "宜", content = info.yi, isGood = true)
        DetailRow(title = "忌", content = info.ji, isGood = false)
        DetailRow(title = "冲煞", content = info.chongSha)
        DetailRow(title = "彭祖百忌", content = info.pengZu)
        DetailRow(title = "吉神宜趋", content = info.jiShen)
        DetailRow(title = "凶煞宜忌", content = info.xiongSha)

        Spacer(modifier = Modifier.height(QimenDimens.spacingLg))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DetailRow(title: String, content: String, isGood: Boolean? = null) {
    if (content.isBlank()) return
    val palette = LocalQimenPalette.current
    QimenCard(
        accentBar = true,
        accentColor = when (isGood) {
            true -> MaterialTheme.colorScheme.primary
            false -> MaterialTheme.colorScheme.error
            else -> palette.gold
        },
    ) {
        Row {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when (isGood) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.error
                    else -> palette.gold
                },
                modifier = Modifier.padding(end = QimenDimens.spacingLg),
            )
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
