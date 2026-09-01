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
    val apiKeyName: String = "",
    val thinkingEnabled: Boolean = true,
    val thinkingLevel: String = "high",
)

/** API Key 存档：自定义名称 + Key（最多 10 个） */
data class ApiKeyEntry(val name: String, val key: String)

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

    /** 各模型的「思考」请求方案（依据各官网） */
    enum class ThinkingStyle {
        NONE,               // 不支持思考（自定义供应商）
        REASONING_EFFORT,   // DeepSeek V4 / Kimi K3：顶层 reasoning_effort（low/high/max）
        ENABLE_THINKING,    // 通义千问 Qwen3：enable_thinking（true/false）
        THINKING_TYPE,      // 小米 MiMo V2.5：thinking.type（enabled/disabled）
    }

    /** 预设供应商：每个供应商带多个可选模型 + 思考方案 */
    data class Provider(
        val name: String,
        val baseUrl: String,
        val models: List<String>,
        val thinkingStyle: ThinkingStyle = ThinkingStyle.NONE,
        val thinkingLevels: List<String> = emptyList(),
    )

    val PROVIDERS = listOf(
        Provider(
            "DeepSeek", "https://api.deepseek.com",
            listOf("deepseek-v4-flash", "deepseek-v4-pro"),
            thinkingStyle = ThinkingStyle.REASONING_EFFORT,
            thinkingLevels = listOf("low", "high", "max"),
        ),
        Provider(
            "Kimi", "https://api.moonshot.cn/v1",
            listOf("kimi-k3", "kimi-k2.6"),
            thinkingStyle = ThinkingStyle.REASONING_EFFORT,
            thinkingLevels = listOf("low", "high", "max"),
        ),
        Provider(
            "通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1",
            listOf("qwen-max", "qwen-plus", "qwen-turbo"),
            thinkingStyle = ThinkingStyle.ENABLE_THINKING,
        ),
        Provider(
            "小米 MiMo", "https://api.xiaomimimo.com/v1",
            listOf("mimo-v2.5-pro", "mimo-v2.5"),
            thinkingStyle = ThinkingStyle.NONE,
        ),
    )
    const val CUSTOM = "自定义"

    private const val SKILLS_KEY = "xuanjian_user_skills"
    private const val API_KEYS_KEY = "xuanjian_api_keys"

    /** 内置 skill 元数据（内容从 assets/xuanjian/ 读，写入仓库） */
    private data class BuiltinSpec(
        val asset: String,
        val name: String,
        val locked: Boolean,
        val defaultEnabled: Boolean = true,
    )

    private val BUILTIN_SPECS = listOf(
        BuiltinSpec("xuanjian/qimen-divination-discipline.md", "占断思维纪律", locked = true),
        BuiltinSpec("xuanjian/feipan-qimen-spec.md", "飞盘奇门规格", locked = false, defaultEnabled = false),
        BuiltinSpec("xuanjian/qimen-beginner-guide.md", "初学者思路引导", locked = false, defaultEnabled = true),
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
            apiKeyName = p.getString("ai_api_key_name", "") ?: "",
            thinkingEnabled = p.getBoolean("ai_thinking_enabled", true),
            thinkingLevel = p.getString("ai_thinking_level", "high") ?: "high",
        )
    }

    fun saveConfig(context: Context, config: AiConfig) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putBoolean("ai_enabled", config.enabled)
            .putString("ai_provider", config.provider)
            .putString("ai_base_url", config.baseUrl)
            .putString("ai_model", config.model)
            .putString("ai_api_key", config.apiKey)
            .putString("ai_api_key_name", config.apiKeyName)
            .putBoolean("ai_thinking_enabled", config.thinkingEnabled)
            .putString("ai_thinking_level", config.thinkingLevel)
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
                enabled = if (spec.locked) true else p.getBoolean("builtin_enabled_${spec.name}", spec.defaultEnabled),
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

    /** 读取 API Key 存档列表 */
    fun readApiKeys(context: Context): List<ApiKeyEntry> {
        val p = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val json = p.getString(API_KEYS_KEY, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ApiKeyEntry>>() {}.type
            Gson().fromJson<List<ApiKeyEntry>>(json, type)
        }.getOrDefault(emptyList())
    }

    /** 保存 API Key 存档（最多 10 个） */
    fun saveApiKeys(context: Context, keys: List<ApiKeyEntry>) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putString(API_KEYS_KEY, Gson().toJson(keys.take(10)))
            .apply()
    }

    /** 按供应商的思考方案构造思考参数（返回 null = 不加） */
    private fun thinkingBody(config: AiConfig, preset: Provider?): Any? {
        if (!config.thinkingEnabled) return null
        return when (preset?.thinkingStyle) {
            ThinkingStyle.REASONING_EFFORT -> config.thinkingLevel
            ThinkingStyle.ENABLE_THINKING -> true
            ThinkingStyle.THINKING_TYPE -> JSONObject().put("type", "enabled")
            else -> null
        }
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
                    when (val t = thinkingBody(config, preset)) {
                        null -> {}
                        is String -> put("reasoning_effort", t)
                        is Boolean -> put("enable_thinking", t)
                        is JSONObject -> put("thinking", t)
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

    /** 测试当前配置的模型是否可用（发一个极简请求，不涉及盘面） */
    suspend fun test(context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val config = readConfig(context)
                if (!config.enabled) throw IllegalStateException("玄鉴未开启")
                if (config.apiKey.isBlank()) throw IllegalStateException("未填写 API Key")
                if (config.baseUrl.isBlank()) throw IllegalStateException("未配置接口地址")

                val endpoint = config.baseUrl.trimEnd('/') + "/chat/completions"
                val preset = PROVIDERS.firstOrNull { it.name == config.provider }
                val body = JSONObject().apply {
                    put("model", config.model)
                    put("max_tokens", 16)
                    when (val t = thinkingBody(config, preset)) {
                        null -> {}
                        is String -> put("reasoning_effort", t)
                        is Boolean -> put("enable_thinking", t)
                        is JSONObject -> put("thinking", t)
                    }
                    put("messages", JSONArray().apply {
                        put(JSONObject().put("role", "user").put("content", "回复「测试成功」四个字"))
                    })
                }

                val conn = URL(endpoint).openConnection() as HttpURLConnection
                conn.connectTimeout = 20000
                conn.readTimeout = 30000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                if (conn.responseCode != 200) {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    throw IllegalStateException("测试失败（${conn.responseCode}）：${err.take(200)}")
                }
                "测试成功"
            }
        }
}
