package com.potuo.feipanqimen2

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** 玄鉴（AI 辅助）配置（存 SharedPreferences，key 仅本地） */
data class AiConfig(
    val enabled: Boolean = false,
    val provider: String = "DeepSeek",
    val baseUrl: String = "",
    val model: String = "",
    val apiKey: String = "",
)

/**
 * 玄鉴 skill（喂给 AI 的资料）。
 * builtin = 内置（预设，不能删除）；locked = 锁定（不能开关，强制启用）。
 */
data class XuanJianSkill(
    val name: String,
    val content: String,
    val enabled: Boolean = true,
    val builtin: Boolean = false,
    val locked: Boolean = false,
)

/** 玄鉴断局结果：思考过程 + 最终结论 */
data class AiResult(
    val reasoning: String,
    val content: String,
)

/**
 * 玄鉴（AI 断局）客户端（OpenAI 兼容 chat/completions）。
 * 排盘交给确定性算法，AI 只做「参考性断语」。system prompt 由内置 skill + 用户 skill 构成，防幻觉。
 */
object AiAssistant {

    /** 预设供应商：每个供应商带多个可选模型。supportsThinking=是否支持思考过程 */
    data class Provider(val name: String, val baseUrl: String, val models: List<String>, val supportsThinking: Boolean = false)

    val PROVIDERS = listOf(
        Provider("DeepSeek", "https://api.deepseek.com", listOf("deepseek-chat", "deepseek-reasoner"), supportsThinking = false),
        Provider("Kimi", "https://api.moonshot.cn/v1", listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"), supportsThinking = true),
        Provider("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", listOf("qwen-plus", "qwen-turbo", "qwen-max"), supportsThinking = false),
        Provider("小米 MiMo", "https://api.xiaomimimo.com/v1", listOf("mimo-v2.5-pro", "mimo-v2.5", "mimo-v2-flash", "mimo-v2-omni"), supportsThinking = true),
    )
    const val CUSTOM = "自定义"

    private const val SKILLS_KEY = "xuanjian_user_skills"

    /** 内置 skill 元数据（内容从 assets/xuanjian/ 读，写入仓库） */
    private data class BuiltinSpec(val asset: String, val name: String, val locked: Boolean)

    private val BUILTIN_SPECS = listOf(
        BuiltinSpec("xuanjian/qimen-divination-discipline.md", "占断思维纪律", locked = true),
        BuiltinSpec("xuanjian/feipan-qimen-spec.md", "飞盘奇门规格", locked = false),
    )

    fun readConfig(context: Context): AiConfig {
        val p = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val provider = p.getString("ai_provider", "DeepSeek") ?: "DeepSeek"
        val preset = PROVIDERS.firstOrNull { it.name == provider }
        return AiConfig(
            enabled = p.getBoolean("ai_enabled", false),
            provider = provider,
            baseUrl = p.getString("ai_base_url", preset?.baseUrl ?: "") ?: "",
            model = p.getString("ai_model", preset?.models?.firstOrNull() ?: "") ?: "",
            apiKey = p.getString("ai_api_key", "") ?: "",
        )
    }

    fun saveConfig(context: Context, config: AiConfig) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putBoolean("ai_enabled", config.enabled)
            .putString("ai_provider", config.provider)
            .putString("ai_base_url", config.baseUrl)
            .putString("ai_model", config.model)
            .putString("ai_api_key", config.apiKey)
            .apply()
    }

    private fun readAsset(context: Context, asset: String): String =
        runCatching {
            context.assets.open(asset).bufferedReader().use { it.readText() }
        }.getOrDefault("")

    /** 读取全部 skill：内置（预设）在前，用户导入的在后 */
    fun readSkills(context: Context): List<XuanJianSkill> {
        val p = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val builtin = BUILTIN_SPECS.map { spec ->
            XuanJianSkill(
                name = spec.name,
                content = readAsset(context, spec.asset),
                enabled = if (spec.locked) true else p.getBoolean("builtin_enabled_${spec.name}", true),
                builtin = true,
                locked = spec.locked,
            )
        }
        return builtin + readUserSkills(context)
    }

    private fun readUserSkills(context: Context): List<XuanJianSkill> {
        val p = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val json = p.getString(SKILLS_KEY, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<XuanJianSkill>>() {}.type
            Gson().fromJson<List<XuanJianSkill>>(json, type)
        }.getOrDefault(emptyList()).map { it.copy(builtin = false, locked = false) }
    }

    /** 保存用户 skill（内置 skill 不在此保存，开关状态单独存） */
    fun saveSkills(context: Context, skills: List<XuanJianSkill>) {
        val userSkills = skills.filter { !it.builtin }
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putString(SKILLS_KEY, Gson().toJson(userSkills))
            .apply()
    }

    /** 切换内置 skill 开关（仅非锁定内置 skill） */
    fun toggleBuiltinSkill(context: Context, name: String, enabled: Boolean) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putBoolean("builtin_enabled_$name", enabled)
            .apply()
    }

    /** 调玄鉴断局：喂盘面 JSON + 用户情况 + 开启的 skill，返回思考过程 + 参考性意见 */
    suspend fun ask(context: Context, panJson: String, situation: String): Result<AiResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = readConfig(context)
                if (!config.enabled) return@runCatching AiResult("", "玄鉴未开启（设置页开启并填 API Key）")
                if (config.apiKey.isBlank()) return@runCatching AiResult("", "未填写 API Key，请到玄鉴配置页设置")
                if (config.baseUrl.isBlank()) return@runCatching AiResult("", "未配置接口地址")

                val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
                val userContent = buildString {
                    append("【用户情况】\n")
                    append(if (situation.isBlank()) "（未填写，请综合判断）" else situation)
                    append("\n\n【盘面数据 JSON】\n")
                    append(panJson)
                }

                // system prompt = 开启的 skill 内容（内置锁定项恒启用）
                val systemContent = readSkills(context)
                    .filter { it.enabled }
                    .joinToString("\n\n") { "【${it.name}】\n${it.content}" }

                val preset = PROVIDERS.firstOrNull { it.name == config.provider }
                val body = JSONObject().apply {
                    put("model", config.model)
                    put("temperature", 0.4)
                    if (preset?.supportsThinking == true) {
                        put("thinking", JSONObject().put("type", "enabled"))
                    }
                    put("messages", JSONArray().apply {
                        put(JSONObject().put("role", "system").put("content", systemContent))
                        put(JSONObject().put("role", "user").put("content", userContent))
                    })
                }

                val conn = URL(endpoint).openConnection() as HttpURLConnection
                conn.connectTimeout = 30000
                conn.readTimeout = 90000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                if (conn.responseCode != 200) {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    return@runCatching AiResult("", "请求失败（${conn.responseCode}）：${err.take(200)}")
                }
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(resp)
                val message = obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                val content = message.optString("content").trim()
                val reasoning = message.optString("reasoning_content").trim()
                if (content.isBlank()) return@runCatching AiResult(reasoning, "AI 返回为空，请重试")
                AiResult(reasoning, content)
            }
        }
}
