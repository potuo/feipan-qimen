package com.potuo.feipanqimen2.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.TrueSolarTime
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InputScreen(viewModel: MainViewModel, onCalculate: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedHourIndex by viewModel.selectedHourIndex.collectAsState()
    val note by viewModel.note.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // 真太阳时（据教材「抽时选局」：以卦师所在地地方时起卦）
    val context = LocalContext.current
    val longitude = remember {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getFloat("longitude", 120.0f)
    }
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
        Text("飞盘奇门排盘", style = MaterialTheme.typography.headlineMedium)
        Text(
            "选择日期与时辰，点击起盘",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        QimenOutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            )
        }

        Text("选择时辰", style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm),
        ) {
            QimenConstants.HOUR_NAMES.forEachIndexed { index, name ->
                val selected = selectedHourIndex == index
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setHourIndex(index) },
                    label = { Text(name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        Text(
            crossingHint
                ?: "真太阳时：$trueHourName（东经 ${longitude.toInt()}°，北京时间选${QimenConstants.HOUR_NAMES[selectedHourIndex]}）",
            style = MaterialTheme.typography.labelSmall,
            color = if (crossingHint != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = note,
            onValueChange = viewModel::setNote,
            label = { Text("备注/占断") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
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
}
