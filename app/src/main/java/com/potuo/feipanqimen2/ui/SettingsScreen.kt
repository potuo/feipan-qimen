package com.potuo.feipanqimen2.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.AiAssistant
import com.potuo.feipanqimen2.ui.components.CollapsibleSection
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 配色单选行 */
@Composable
private fun ThemeOptionRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = QimenDimens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    isDark: Boolean = false,
    themeName: String = "classic",
    onSelectTheme: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var longitudeText by remember {
        val v = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getFloat("longitude", 120.0f)
        mutableStateOf(if (v % 1f == 0f) v.toInt().toString() else v.toString())
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportAll(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importCases(it) } }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(QimenDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
        ) {
            // ── 外观 ──
            CollapsibleSection(title = "外观 · 配色", defaultExpanded = true) {
                Column {
                    ThemeOptionRow(
                        name = "古典金",
                        selected = themeName == "classic",
                        onClick = { onSelectTheme("classic") },
                    )
                    ThemeOptionRow(
                        name = "紫微",
                        selected = themeName == "ziwei",
                        onClick = { onSelectTheme("ziwei") },
                    )
                    ThemeOptionRow(
                        name = "玄墨",
                        selected = themeName == "xuanmo",
                        onClick = { onSelectTheme("xuanmo") },
                    )
                    ThemeOptionRow(
                        name = "青花",
                        selected = themeName == "qinghua",
                        onClick = { onSelectTheme("qinghua") },
                    )
                    ThemeOptionRow(
                        name = "赭石",
                        selected = themeName == "zheshi",
                        onClick = { onSelectTheme("zheshi") },
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = QimenDimens.spacingSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "明暗",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        if (isDark) "暗色" else "浅色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── 玄鉴 ──
            CollapsibleSection(title = "玄鉴") {
                var aiEnabled by remember {
                    mutableStateOf(AiAssistant.readConfig(context).enabled)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "启用玄鉴",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = aiEnabled,
                        onCheckedChange = { on ->
                            aiEnabled = on
                            val cfg = AiAssistant.readConfig(context).copy(enabled = on)
                            AiAssistant.saveConfig(context, cfg)
                        },
                    )
                }
                Text(
                    "玄鉴以飞盘奇门断法为纲，佐以自备资料，为盘面参断吉凶。意见仅供参考，不可尽信。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingSm),
                )
            }

            // ── 排盘设置 ──
            CollapsibleSection(title = "排盘设置") {
                Text(
                    "所在经度（东经）：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = longitudeText,
                    onValueChange = { input ->
                        // 全角数字归一为半角，再过滤非数字/小数点，保证 toFloatOrNull 必然可解析
                        val half = input.map { ch ->
                            when {
                                ch in '０'..'９' -> ('0' + (ch - '０'))
                                ch == '．' -> '.'
                                else -> ch
                            }
                        }.joinToString("")
                        val filtered = half.filter { it in '0'..'9' || it == '.' }
                        longitudeText = filtered
                        filtered.toFloatOrNull()?.let { v ->
                            if (v in 73.0f..136.0f) {
                                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                    .edit().putFloat("longitude", v).apply()
                            }
                        }
                    },
                    label = { Text("东经度数（默认 120）") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Text(
                    "输入后自动保存（东经 73°~136°，默认 120°）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingXs),
                )
            }

            // ── 数据管理 ──
            CollapsibleSection(title = "数据管理") {
                QimenButton(
                    onClick = {
                        val date = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
                        exportLauncher.launch("feipan_qimen_cases_$date.json")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("导出全部案例") }
                QimenButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = QimenDimens.spacingSm),
                ) { Text("导入案例") }
            }
        }
    }
}
