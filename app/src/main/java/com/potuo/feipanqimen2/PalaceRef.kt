package com.potuo.feipanqimen2

import android.content.Context

/**
 * 星门神 ↔ 教材联动：根据星/门/神名在《奇门基础资料 2023版教》第二卷「第二章 星门神」
 * 原文中定位对应释义段落（供「点宫格看详解」使用）。
 *
 * 定位规则：先按标题整行匹配（「天蓬星：」「开门：」「值符」），天心星缺标题行故退化为
 * 内容首行子串定位；命中后向后取到下一个星/门/神标题为止，过滤掉 markdown 标记与空行。
 */
object PalaceRef {

    /** 名称 -> 教材定位关键词（星门神名与教材用字有差异处已映射：螣蛇→腾蛇） */
    private val KEYWORDS = mapOf(
        // 九星（天心星缺标题，用内容首行定位）
        "天蓬" to "天蓬星：", "天芮" to "天芮星：", "天冲" to "天冲星：",
        "天辅" to "天辅星：", "天禽" to "天禽星：", "天心" to "原名武曲星",
        "天柱" to "天柱星：", "天任" to "天任星：", "天英" to "天英星：",
        // 八门 + 中门
        "休" to "休门：", "死" to "死门：", "伤" to "伤门：", "杜" to "杜门：",
        "中" to "中门", "开" to "开门：", "惊" to "惊门：", "生" to "生门：", "景" to "景门：",
        // 九神（螣蛇在教材写作「腾蛇」）
        "值符" to "值符", "螣蛇" to "腾蛇", "太阴" to "太阴",
        "六合" to "六合", "勾陈" to "勾陈", "太常" to "太常",
        "朱雀" to "朱雀", "九地" to "九地", "九天" to "九天",
    )

    @Volatile
    private var vol2: List<String>? = null

    private fun load(context: Context): List<String> {
        vol2?.let { return it }
        val text = context.assets.open("qimen_book/vol2.txt").bufferedReader().use { it.readText() }
        return text.lines().also { vol2 = it }
    }

    /** 查询星/门/神对应的教材释义；未收录返回 null */
    fun lookup(context: Context, name: String): String? {
        val kw = KEYWORDS[name] ?: return null
        val lines = load(context)

        // 先整行匹配（标题行），再子串匹配（天心星等缺标题的）
        var idx = lines.indexOfFirst { it.trim() == kw }
        val isExact = idx >= 0
        if (idx < 0) idx = lines.indexOfFirst { kw in it }
        if (idx < 0) return null

        // 标题行命中则从下一行取；内容行命中（天心星）则从该行取
        val start = if (isExact) idx + 1 else idx
        val seg = mutableListOf<String>()
        for (j in start until lines.size) {
            val raw = lines[j].trim()
            if (isTitle(raw)) break
            val t = raw.removePrefix("> ").trim()
            if (t.isEmpty() || t == "```" || t.startsWith("#")) continue
            seg += t
        }
        return if (seg.isEmpty()) null else seg.joinToString("\n")
    }

    /** 是否为星/门/神标题行（用于截断） */
    private fun isTitle(line: String): Boolean {
        if (line.endsWith("星：") || line.endsWith("门：") || line == "中门") return true
        return line in setOf("值符", "腾蛇", "太阴", "六合", "白虎", "玄武", "九地", "九天", "太常", "勾陈", "朱雀")
    }
}
