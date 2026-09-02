package com.potuo.feipanqimen2.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.TrueSolarTime
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun hourToShiChenIndex(hour: Int): Int = when (hour) {
    23, 0 -> 0
    1, 2 -> 1
    3, 4 -> 2
    5, 6 -> 3
    7, 8 -> 4
    9, 10 -> 5
    11, 12 -> 6
    13, 14 -> 7
    15, 16 -> 8
    17, 18 -> 9
    19, 20 -> 10
    21, 22 -> 11
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

    // 真太阳时（据教材「抽时选局」：以卦师所在地地方时起卦）
    // 每次组合直接读设置，避免 remember 缓存旧经度（设置页改完后回输入页立即可见）
    val context = LocalContext.current
    val longitude = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        .getFloat("longitude", 120.0f)
    val hourRange = QimenConstants.HOUR_RANGES[selectedHourIndex]
    val hour = if (hourRange.first == 23) 23 else hourRange.first
    val beijingDt = LocalDateTime.of(selectedDate.year, selectedDate.month, selectedDate.dayOfMonth, hour, 0)
    val trueSolarDt = TrueSolarTime.toTrueSolar(beijingDt, longitude.toDouble())
    val trueHourName = TrueSolarTime.hourName(trueSolarDt.hour)
    val crossingHint = TrueSolarTime.crossingHourHint(beijingDt, longitude.toDouble())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingXl),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingLg),
    ) {
        Text(
            "鸣法飞盘起卦",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            "选择日期与时辰，点击起盘",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        QimenOutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        QimenOutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                String.format(
                    "%02d:%02d · %s",
                    selectedHour,
                    selectedMinute,
                    QimenConstants.HOUR_NAMES[selectedHourIndex],
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            crossingHint
                ?: "真太阳时：$trueHourName（东经 ${longitude.toInt()}°，北京时间选${QimenConstants.HOUR_NAMES[selectedHourIndex]}）",
            style = MaterialTheme.typography.labelSmall,
            color = if (crossingHint != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = note,
            onValueChange = viewModel::setNote,
            label = { Text("标题") },
            placeholder = { Text("留空默认「xx月xx日 阴/阳遁x局」") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
        )

        Spacer(modifier = Modifier.height(QimenDimens.spacingSm))

        QimenButton(
            onClick = {
                viewModel.calculate()
                onCalculate()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("起盘")
        }
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
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDate(date)
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    viewModel.setHourIndex(hourToShiChenIndex(timePickerState.hour))
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}
