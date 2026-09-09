package com.potuo.feipanqimen2.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.TrueSolarTime
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.components.SectionHeader
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.time.*
import java.time.format.DateTimeFormatter

private fun hourToShiChenIndex(hour: Int): Int = when (hour) {
    23, 0 -> 0; 1, 2 -> 1; 3, 4 -> 2; 5, 6 -> 3; 7, 8 -> 4; 9, 10 -> 5
    11, 12 -> 6; 13, 14 -> 7; 15, 16 -> 8; 17, 18 -> 9; 19, 20 -> 10; 21, 22 -> 11
    else -> 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(viewModel: MainViewModel, onCalculate: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedHourIndex by viewModel.selectedHourIndex.collectAsState()
    val note by viewModel.note.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(LocalTime.now().hour) }
    var selectedMinute by remember { mutableIntStateOf(LocalTime.now().minute) }

    val context = LocalContext.current
    val longitude = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).getFloat("longitude", 120.0f)
    val hourRange = QimenConstants.HOUR_RANGES[selectedHourIndex]
    val hour = if (hourRange.first == 23) 23 else hourRange.first
    val beijingDt = LocalDateTime.of(selectedDate.year, selectedDate.month, selectedDate.dayOfMonth, hour, 0)
    val trueSolarDt = TrueSolarTime.toTrueSolar(beijingDt, longitude.toDouble())
    val trueHourName = TrueSolarTime.hourName(trueSolarDt.hour)
    val crossingHint = TrueSolarTime.crossingHourHint(beijingDt, longitude.toDouble())
    val calculate = { viewModel.calculate(); onCalculate() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(QimenDimens.pageGutter),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.sectionGap),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("天禽 · 鸣法飞盘", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text("以时为纲，以事为问", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            SectionHeader("起局时间", sealMark = QimenConstants.HOUR_NAMES[selectedHourIndex])
            QimenCard {
                TimeFieldRow("日期", selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")), Icons.Default.CalendarMonth) { showDatePicker = true }
                Spacer(Modifier.height(QimenDimens.spacingSm))
                TimeFieldRow("时刻", String.format("%02d:%02d · %s", selectedHour, selectedMinute, QimenConstants.HOUR_NAMES[selectedHourIndex]), Icons.Default.AccessTime) { showTimePicker = true }
            }
            SolarTimeStatus(
                beijingDt.format(DateTimeFormatter.ofPattern("HH:mm")),
                trueSolarDt.format(DateTimeFormatter.ofPattern("HH:mm")),
                trueHourName, longitude.toInt(), crossingHint,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
            SectionHeader("所占事项", sealMark = "可选")
            OutlinedTextField(
                value = note, onValueChange = viewModel::setNote, modifier = Modifier.fillMaxWidth(),
                label = { Text("标题") }, placeholder = { Text("留空将按当前局势自动命名") },
                supportingText = { Text("已输入 ${note.length} 字 · 可选") }, minLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { calculate() }),
            )
        }
        QimenButton(onClick = calculate, modifier = Modifier.fillMaxWidth()) { Text("起盘") }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                state.selectedDateMillis?.let { viewModel.setDate(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()) }
                showDatePicker = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        ) { DatePicker(state) }
    }
    if (showTimePicker) {
        val state = rememberTimePickerState(selectedHour, selectedMinute, true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = {
                selectedHour = state.hour; selectedMinute = state.minute
                viewModel.setHourIndex(hourToShiChenIndex(state.hour)); showTimePicker = false
            }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } },
            text = { TimePicker(state) },
        )
    }
}

@Composable
private fun TimeFieldRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    QimenOutlinedButton(onClick, Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = QimenDimens.spacingMd)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SolarTimeStatus(beijingTime: String, trueSolarTime: String, hourName: String, longitude: Int, crossingHint: String?) {
    val warning = crossingHint != null
    val accent = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    QimenCard(accentBar = true, accentColor = accent, containerColor = accent.copy(alpha = 0.08f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (warning) Icons.Default.WarningAmber else Icons.Default.Schedule, null, tint = accent, modifier = Modifier.size(QimenDimens.spacingXl))
            Column(Modifier.padding(start = QimenDimens.spacingMd)) {
                Text("地方真太阳时校时", style = MaterialTheme.typography.titleSmall, color = accent)
                Text("北京时间 $beijingTime  →  地方时 $trueSolarTime · $hourName", style = MaterialTheme.typography.bodyMedium)
                Text(crossingHint ?: "东经 $longitude° · 校时后未跨时辰", style = MaterialTheme.typography.labelMedium, color = if (warning) accent else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
