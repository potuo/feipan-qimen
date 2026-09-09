package com.potuo.feipanqimen2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.potuo.feipanqimen2.qimen.HuangLiService
import com.potuo.feipanqimen2.ui.components.Badge
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.QimenCardLevel
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.components.SectionHeader
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuangLiScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val info = remember(selectedDate) { HuangLiService.getHuangLi(selectedDate.atTime(12, 0)) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(QimenDimens.pageGutter),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.sectionGap),
    ) {
        QimenCard(level = QimenCardLevel.Elevated) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) { Icon(Icons.Default.ChevronLeft, "前一天") }
                Row(
                    Modifier.weight(1f).clickable { showDatePicker = true }.padding(vertical = QimenDimens.spacingMd),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = QimenDimens.spacingSm), textAlign = TextAlign.Center,
                    )
                }
                IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) { Icon(Icons.Default.ChevronRight, "后一天") }
            }
            if (selectedDate != today) {
                QimenOutlinedButton(onClick = { selectedDate = today }, modifier = Modifier.fillMaxWidth()) { Text("回到今天") }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            SectionHeader("黄历概览", sealMark = info.shengXiao)
            QimenCard(accentBar = true) {
                Text("${info.lunarDate} · ${info.shengXiao}年", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().padding(top = QimenDimens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
                ) {
                    if (info.festival.isNotBlank()) Badge(info.festival, containerColor = LocalQimenPalette.current.cinnabar.copy(alpha = 0.14f), contentColor = LocalQimenPalette.current.cinnabar)
                    if (info.jieQi.isNotBlank()) Badge(info.jieQi)
                }
                Text("二十八宿 · ${info.xiu}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = QimenDimens.spacingMd))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            SectionHeader("今日宜忌")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
                AuspiceCard("宜", info.yi, true, Modifier.weight(1f))
                AuspiceCard("忌", info.ji, false, Modifier.weight(1f))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            SectionHeader("参考信息")
            QimenCard(level = QimenCardLevel.Plain) {
                LowPriorityRow("冲煞", info.chongSha)
                LowPriorityRow("彭祖百忌", info.pengZu)
                LowPriorityRow("吉神宜趋", info.jiShen)
                LowPriorityRow("凶煞宜忌", info.xiongSha, divider = false)
            }
        }
        Spacer(Modifier.height(QimenDimens.spacingLg))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                state.selectedDateMillis?.let { selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                showDatePicker = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        ) { DatePicker(state) }
    }
}

@Composable
private fun AuspiceCard(title: String, content: String, good: Boolean, modifier: Modifier = Modifier) {
    val color = if (good) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    QimenCard(modifier, accentBar = true, accentColor = color, containerColor = color.copy(alpha = 0.07f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (good) Icons.Default.CheckCircle else Icons.Default.DoNotDisturbOn, null, tint = color)
            Text(title, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = QimenDimens.spacingSm))
        }
        Text(content.ifBlank { "无" }, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = QimenDimens.spacingMd))
    }
}

@Composable
private fun LowPriorityRow(title: String, content: String, divider: Boolean = true) {
    if (content.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = QimenDimens.spacingMd)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(QimenDimens.contentMaxWidth / 5))
        Text(content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
    if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
