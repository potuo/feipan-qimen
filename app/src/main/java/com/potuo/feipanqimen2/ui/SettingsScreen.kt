package com.potuo.feipanqimen2.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.AiAssistant
import com.potuo.feipanqimen2.data.CaseTags
import com.potuo.feipanqimen2.ui.components.*
import com.potuo.feipanqimen2.ui.theme.*
import com.potuo.feipanqimen2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

private data class ThemePreview(val key: String, val name: String, val light: QimenPalette, val dark: QimenPalette)
private val themePreviews = listOf(
    ThemePreview("classic", "古典金", QimenPalettes.ClassicLight, QimenPalettes.ClassicDark),
    ThemePreview("ziwei", "紫微", QimenPalettes.ZiweiLight, QimenPalettes.ZiweiDark),
    ThemePreview("xuanmo", "玄墨", QimenPalettes.XuanMoLight, QimenPalettes.XuanMoDark),
    ThemePreview("qinghua", "青花", QimenPalettes.QingHuaLight, QimenPalettes.QingHuaDark),
    ThemePreview("zheshi", "赭石", QimenPalettes.ZheShiLight, QimenPalettes.ZheShiDark),
)

@Composable private fun PaletteStrip(p: QimenPalette, modifier: Modifier = Modifier) {
    Row(modifier.height(QimenDimens.spacingXl)) {
        listOf(p.paper, p.gold, p.cinnabar, p.inkText).forEach { Box(Modifier.weight(1f).fillMaxHeight().background(it)) }
    }
}

@Composable private fun ThemePreviewCard(theme: ThemePreview, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(QimenDimens.spacingSm), verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingXs)) {
                PaletteStrip(theme.light, Modifier.weight(1f)); PaletteStrip(theme.dark, Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(theme.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                if (selected) Box(Modifier.size(QimenDimens.spacingSm).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
            Text("浅色 / 暗色", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable fun SettingsScreen(viewModel: MainViewModel, isDark: Boolean = false, themeName: String = "classic", onSelectTheme: (String) -> Unit = {}) {
    val context = LocalContext.current
    var longitudeText by remember {
        val value = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).getFloat("longitude", 120f)
        mutableStateOf(if (value % 1f == 0f) value.toInt().toString() else value.toString())
    }
    var longitudeSaved by remember { mutableStateOf(false) }
    var aiEnabled by remember { mutableStateOf(AiAssistant.readConfig(context).enabled) }
    var caseTags by remember { mutableStateOf(CaseTags.read(context)) }
    var newTag by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editText by remember { mutableStateOf("") }
    var pendingDeleteTag by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val longitude = longitudeText.toFloatOrNull()
    val longitudeValid = longitude != null && longitude in 73f..136f
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(viewModel::exportAll) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(viewModel::importCases) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(QimenDimens.pageGutter), verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd)) {
        item { SectionHeader("外观", sealMark = themePreviews.firstOrNull { it.key == themeName }?.name) }
        item { QimenCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("主题与显示", style = MaterialTheme.typography.titleSmall); Text(if (isDark) "当前为暗色" else "当前为浅色", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("明暗由全局外观开关切换", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(QimenDimens.spacingMd))
            Column(verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) { themePreviews.chunked(2).forEach { themes -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) { themes.forEach { ThemePreviewCard(it, it.key == themeName, Modifier.weight(1f)) { onSelectTheme(it.key) } }; if (themes.size == 1) Spacer(Modifier.weight(1f)) } } }
        } }
        item { SectionHeader("排盘", sealMark = if (longitudeValid) "已配置" else "待修正") }
        item { QimenCard {
            Text("所在经度", style = MaterialTheme.typography.titleSmall)
            Text("东经 73°–136°，例如北京 116.4°；默认 120°。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(longitudeText, { input -> longitudeSaved = false; longitudeText = input.map { ch -> when { ch in '０'..'９' -> '0' + (ch - '０'); ch == '．' -> '.'; else -> ch } }.joinToString("").filter { it.isDigit() || it == '.' } }, label = { Text("东经度数") }, suffix = { Text("°E") }, supportingText = { Text(if (longitudeText.isBlank() || longitudeValid) "合法范围 73–136" else "请输入 73–136 之间的数值") }, isError = longitudeText.isNotBlank() && !longitudeValid, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm))
            QimenButton(onClick = { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().putFloat("longitude", longitude!!).apply(); longitudeSaved = true }, enabled = longitudeValid, modifier = Modifier.fillMaxWidth()) { Text(if (longitudeSaved) "已保存" else "保存经度") }
            if (longitudeSaved) Text("经度已保存到本机设置。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        } }
        item { SectionHeader("玄鉴", sealMark = if (aiEnabled) "已启用" else "已关闭") }
        item { QimenCard { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("启用玄鉴", style = MaterialTheme.typography.titleSmall); Text("意见仅供参考，不可尽信。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(aiEnabled, { enabled -> aiEnabled = enabled; AiAssistant.saveConfig(context, AiAssistant.readConfig(context).copy(enabled = enabled)) }) } } }
        item { SectionHeader("数据", sealMark = "JSON") }
        item { QimenCard {
            Text("案例文件", style = MaterialTheme.typography.titleSmall); Text("导出为 JSON 备份；导入会写入案例库，请先备份。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            QimenOutlinedButton(onClick = { val date = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date()); exportLauncher.launch("feipan_qimen_cases_$date.json") }, modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm)) { Text("导出全部案例（JSON）") }
            QimenButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }, modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm), containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) { Text("导入案例·谨慎") }
        } }
        item { SectionHeader("标签", sealMark = "${caseTags.size} 个") }
        itemsIndexed(caseTags, key = { _, tag -> tag }) { index, tag -> QimenCard {
            if (editingIndex == index) { OutlinedTextField(editText, { editText = it }, Modifier.fillMaxWidth(), label = { Text("标签名") }, singleLine = true); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton({ editingIndex = null }) { Text("取消") }; TextButton({ val value = editText.trim(); if (value.isNotEmpty()) { CaseTags.save(context, caseTags.toMutableList().apply { set(index, value) }); caseTags = CaseTags.read(context) }; editingIndex = null }) { Text("保存") } } }
            else Row(verticalAlignment = Alignment.CenterVertically) { Text(tag, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)); TextButton({ editingIndex = index; editText = tag }) { Text("编辑") }; TextButton({ if (caseTags.size <= 1) Toast.makeText(context, "至少保留一个标签", Toast.LENGTH_SHORT).show() else pendingDeleteTag = index to tag }) { Text("删除", color = MaterialTheme.colorScheme.error) } }
        } }
        item { QimenCard { Text("添加标签", style = MaterialTheme.typography.titleSmall); Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(newTag, { newTag = it }, Modifier.weight(1f), label = { Text("新标签名") }, singleLine = true); QimenButton({ val value = newTag.trim(); if (value.isNotEmpty() && value !in caseTags) { CaseTags.save(context, caseTags + value); caseTags = CaseTags.read(context); newTag = "" } }, Modifier.padding(start = QimenDimens.spacingSm)) { Text("添加") } } } }
    }
    pendingDeleteTag?.let { (index, tag) -> QimenDialog(onDismissRequest = { pendingDeleteTag = null }, title = "删除标签？", text = { Text("「$tag」将从标签列表移除。") }, confirmText = "删除", destructive = true, onConfirm = { CaseTags.save(context, caseTags.filterIndexed { i, _ -> i != index }); caseTags = CaseTags.read(context); pendingDeleteTag = null }, dismissText = "取消", onDismiss = { pendingDeleteTag = null }) }
}
