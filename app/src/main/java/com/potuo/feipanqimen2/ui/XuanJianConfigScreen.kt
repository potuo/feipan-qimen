package com.potuo.feipanqimen2.ui

import android.content.Context
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.AiAssistant
import com.potuo.feipanqimen2.ApiKeyEntry
import com.potuo.feipanqimen2.XuanJianSkill
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenCard
import com.potuo.feipanqimen2.ui.components.QimenDialog
import com.potuo.feipanqimen2.ui.components.QimenOutlinedButton
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import kotlinx.coroutines.launch

/** 从 Uri 读取文本内容 */
private fun readTextFromUri(context: Context, uri: android.net.Uri): String =
    runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
    }.getOrDefault("")

/** 从 Uri 获取文件名（去掉扩展名，作为默认 skill 名） */
private fun fileNameFromUri(context: Context, uri: android.net.Uri): String {
    var name = "未命名"
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
    }
    return name.substringBeforeLast('.')
}

/** 玄鉴配置页：模型/apikey + 资料 skill 管理（开关/删除/导入） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XuanJianConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var aiConfig by remember { mutableStateOf(AiAssistant.readConfig(context)) }
    var skills by remember { mutableStateOf(AiAssistant.readSkills(context)) }
    var pendingSkillContent by remember { mutableStateOf<String?>(null) }
    var newSkillName by remember { mutableStateOf("") }
    var showThinkingWarn by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var apiKeys by remember { mutableStateOf(AiAssistant.readApiKeys(context)) }
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val importSkillLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            val content = readTextFromUri(context, it)
            if (content.isNotBlank()) {
                newSkillName = fileNameFromUri(context, it)
                pendingSkillContent = content
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        // ── 模型 ──
        QimenCard(accentBar = true) {
            Text(
                "模型",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
            Row(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                // 候选框 1：供应商
                Box(modifier = Modifier.weight(1f)) {
                    var providerMenu by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { providerMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(aiConfig.provider, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = providerMenu, onDismissRequest = { providerMenu = false }) {
                        (AiAssistant.PROVIDERS.map { it.name } + AiAssistant.CUSTOM).forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    val preset = AiAssistant.PROVIDERS.firstOrNull { it.name == name }
                                    val supports = preset != null && preset.thinkingStyle != AiAssistant.ThinkingStyle.NONE
                                    aiConfig = aiConfig.copy(
                                        provider = name,
                                        baseUrl = preset?.baseUrl ?: aiConfig.baseUrl,
                                        model = preset?.models?.firstOrNull() ?: aiConfig.model,
                                        thinkingEnabled = if (supports) aiConfig.thinkingEnabled else false,
                                    )
                                    AiAssistant.saveConfig(context, aiConfig)
                                    providerMenu = false
                                },
                            )
                        }
                    }
                }
                // 候选框 2：模型
                Box(modifier = Modifier.weight(1f)) {
                    var modelMenu by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { modelMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(aiConfig.model, maxLines = 1, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }) {
                        val preset = AiAssistant.PROVIDERS.firstOrNull { it.name == aiConfig.provider }
                        val models = if (aiConfig.provider == AiAssistant.CUSTOM) {
                            listOf(aiConfig.model.ifBlank { "自定义模型" })
                        } else {
                            preset?.models ?: listOf(aiConfig.model)
                        }
                        models.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    aiConfig = aiConfig.copy(model = m)
                                    AiAssistant.saveConfig(context, aiConfig)
                                    modelMenu = false
                                },
                            )
                        }
                    }
                }
            }

            if (aiConfig.provider == AiAssistant.CUSTOM) {
                OutlinedTextField(
                    value = aiConfig.baseUrl,
                    onValueChange = { v ->
                        aiConfig = aiConfig.copy(baseUrl = v)
                        AiAssistant.saveConfig(context, aiConfig)
                    },
                    label = { Text("接口地址（base URL）") },
                    modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = aiConfig.model,
                    onValueChange = { v ->
                        aiConfig = aiConfig.copy(model = v)
                        AiAssistant.saveConfig(context, aiConfig)
                    },
                    label = { Text("模型名") },
                    modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm),
                    singleLine = true,
                )
            }

            // ── 思考模式 ──
            val preset = AiAssistant.PROVIDERS.firstOrNull { it.name == aiConfig.provider }
            val thinkingSupported = preset != null && preset.thinkingStyle != AiAssistant.ThinkingStyle.NONE
            HorizontalDivider(modifier = Modifier.padding(vertical = QimenDimens.spacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "思考模式",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = aiConfig.thinkingEnabled,
                    onCheckedChange = { on ->
                        if (on && !thinkingSupported) {
                            showThinkingWarn = true
                        } else {
                            aiConfig = aiConfig.copy(thinkingEnabled = on)
                            AiAssistant.saveConfig(context, aiConfig)
                        }
                    },
                )
            }
            if (!thinkingSupported) {
                Text(
                    "当前模型不支持思考过程",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (aiConfig.thinkingEnabled && preset.thinkingLevels.isNotEmpty()) {
                Text(
                    "思考强度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QimenDimens.spacingXs),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                    preset.thinkingLevels.forEach { level ->
                        FilterChip(
                            selected = aiConfig.thinkingLevel == level,
                            onClick = {
                                aiConfig = aiConfig.copy(thinkingLevel = level)
                                AiAssistant.saveConfig(context, aiConfig)
                            },
                            label = { Text(level) },
                        )
                    }
                }
            }

            // API Key 存档选择
            Box {
                var keyMenu by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { keyMenu = true },
                    modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm),
                ) {
                    Text(aiConfig.apiKeyName.ifBlank { "选择 Key 存档" }, maxLines = 1, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = keyMenu, onDismissRequest = { keyMenu = false }) {
                    if (apiKeys.isEmpty()) {
                        DropdownMenuItem(text = { Text("暂无存档，点「新建 Key」") }, onClick = { keyMenu = false })
                    } else {
                        apiKeys.forEach { entry ->
                            DropdownMenuItem(text = { Text(entry.name) }, onClick = {
                                aiConfig = aiConfig.copy(apiKey = entry.key, apiKeyName = entry.name)
                                AiAssistant.saveConfig(context, aiConfig)
                                keyMenu = false
                            })
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingSm)) {
                QimenOutlinedButton(
                    onClick = {
                        if (apiKeys.size >= 10) {
                            Toast.makeText(context, "最多保存 10 个 Key 存档", Toast.LENGTH_SHORT).show()
                        } else {
                            showAddKeyDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f).padding(top = QimenDimens.spacingSm),
                ) { Text("新建 Key") }
                QimenOutlinedButton(
                    onClick = {
                        if (aiConfig.apiKeyName.isNotBlank()) {
                            val updated = apiKeys.filterNot { it.name == aiConfig.apiKeyName }
                            AiAssistant.saveApiKeys(context, updated)
                            apiKeys = updated
                            aiConfig = aiConfig.copy(apiKey = "", apiKeyName = "")
                            AiAssistant.saveConfig(context, aiConfig)
                        }
                    },
                    modifier = Modifier.weight(1f).padding(top = QimenDimens.spacingSm),
                ) { Text("删除") }
            }
            Text(
                "Key 仅存本机不上传。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = QimenDimens.spacingXs),
            )
            QimenOutlinedButton(
                onClick = {
                    if (testing) return@QimenOutlinedButton
                    scope.launch {
                        testing = true
                        val result = AiAssistant.test(context)
                        testing = false
                        val msg = result.getOrElse { it.message ?: "未知错误" }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = QimenDimens.spacingSm),
            ) { Text(if (testing) "测试中…" else "测试模型") }
        }

        // ── 资料 skill ──
        QimenCard(accentBar = true) {
            Text(
                "资料 skill",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
            Text(
                "开启的 skill 会随盘面一并呈与玄鉴参断。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
            if (skills.isEmpty()) {
                Text(
                    "暂无资料，点下方「导入 skill」添加。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = QimenDimens.spacingMd),
                )
            } else {
                skills.forEachIndexed { index, skill ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = QimenDimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    skill.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (skill.builtin) {
                                    Text(
                                        if (skill.locked) "· 固定" else "· 内置",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = QimenDimens.spacingXs),
                                    )
                                }
                            }
                            if (skill.content.isNotBlank()) {
                                Text(
                                    skill.content.take(40) + if (skill.content.length > 40) "…" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                        if (!skill.locked) {
                            Switch(
                                checked = skill.enabled,
                                onCheckedChange = { on ->
                                    if (on) {
                                        // 开启时限额：除「占断思维纪律」（locked）外，最多同时开 5 个
                                        val enabledCount = skills.count { it.enabled && !it.locked }
                                        if (enabledCount >= 5) {
                                            Toast.makeText(context, "最多同时开启 5 个 skill（占断思维纪律不计入）", Toast.LENGTH_SHORT).show()
                                            skills = AiAssistant.readSkills(context)
                                        } else {
                                            if (skill.builtin) {
                                                AiAssistant.toggleBuiltinSkill(context, skill.name, true)
                                            } else {
                                                skills = skills.toMutableList().apply { set(index, skill.copy(enabled = true)) }
                                                AiAssistant.saveSkills(context, skills)
                                            }
                                            skills = AiAssistant.readSkills(context)
                                        }
                                    } else {
                                        if (skill.builtin) {
                                            AiAssistant.toggleBuiltinSkill(context, skill.name, false)
                                        } else {
                                            skills = skills.toMutableList().apply { set(index, skill.copy(enabled = false)) }
                                            AiAssistant.saveSkills(context, skills)
                                        }
                                        skills = AiAssistant.readSkills(context)
                                    }
                                },
                            )
                        }
                        if (!skill.builtin) {
                            Text(
                                "删除",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable {
                                        skills = skills.toMutableList().apply { removeAt(index) }
                                        AiAssistant.saveSkills(context, skills)
                                    }
                                    .padding(start = QimenDimens.spacingMd),
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = QimenDimens.spacingXs))
                }
            }

            QimenButton(
                onClick = { importSkillLauncher.launch(arrayOf("text/*", "application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm),
            ) { Text("导入 skill") }
        }

        QimenOutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("返回") }
    }

    // ── 思考警告弹窗 ──
    if (showThinkingWarn) {
        QimenDialog(
            onDismissRequest = { showThinkingWarn = false },
            title = "不支持思考",
            text = { Text("当前模型不支持思考过程，无法开启。") },
            confirmText = "知道了",
            onConfirm = { showThinkingWarn = false },
            dismissText = null,
        )
    }

    // ── 新建 API Key 弹窗 ──
    if (showAddKeyDialog) {
        QimenDialog(
            onDismissRequest = { showAddKeyDialog = false },
            title = "新建 API Key 存档",
            confirmText = "保存",
            onConfirm = {
                val name = newKeyName.trim()
                val value = newKeyValue.trim()
                if (name.isNotBlank() && value.isNotBlank() && apiKeys.size < 10) {
                    val updated = apiKeys + ApiKeyEntry(name, value)
                    AiAssistant.saveApiKeys(context, updated)
                    apiKeys = updated
                    aiConfig = aiConfig.copy(apiKey = value, apiKeyName = name)
                    AiAssistant.saveConfig(context, aiConfig)
                }
                showAddKeyDialog = false
                newKeyName = ""
                newKeyValue = ""
            },
            dismissText = "取消",
            onDismiss = { showAddKeyDialog = false },
            text = {
                Column {
                    OutlinedTextField(
                        value = newKeyName,
                        onValueChange = { newKeyName = it },
                        label = { Text("名称（如：我的 MiMo）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newKeyValue,
                        onValueChange = { newKeyValue = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth().padding(top = QimenDimens.spacingSm),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
            },
        )
    }

    // ── 导入 skill 弹窗 ──
    pendingSkillContent?.let { content ->
        QimenDialog(
            onDismissRequest = { pendingSkillContent = null },
            title = "导入 skill",
            confirmText = "加入",
            onConfirm = {
                val skill = XuanJianSkill(newSkillName.ifBlank { "未命名" }, content, enabled = true)
                skills = skills + skill
                AiAssistant.saveSkills(context, skills)
                pendingSkillContent = null
            },
            dismissText = "取消",
            onDismiss = { pendingSkillContent = null },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSkillName,
                        onValueChange = { newSkillName = it },
                        label = { Text("skill 名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                    Text(
                        "内容预览（${content.length} 字）：",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        content.take(150) + if (content.length > 150) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            },
        )
    }
}
