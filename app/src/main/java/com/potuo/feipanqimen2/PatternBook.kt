package com.potuo.feipanqimen2

import android.content.Context

/**
 * 格局 ↔ 教材联动：根据格局名在《奇门鸣法》第三卷原文中定位对应段落。
 * 教材 vol3.txt 为 markdown 排版，口诀为连续引用块；提取采用「关键词行 ± 窗口」，
 * 避免整个大引用块被拖出。未收录的格局返回 null（调用方显示盘面说明）。
 */
object PatternBook {

    /** 格局名 -> 定位关键词（候选按优先级；教材用字可能与规范名不同，如 返吟/腾蛇/絻户） */
    private val KEYWORDS = mapOf(
        "六仪击刑" to listOf("只有值符才算击刑", "击刑、相刑代表难受"),
        "入墓" to listOf("又有一般入墓诀", "入墓诀"),
        "门迫" to listOf("被迫开门"),
        "受制" to listOf("受制休加"),
        "交和" to listOf("交和原来"),
        "伏吟" to listOf("戊加戊名伏吟", "伏吟名"),
        "反吟" to listOf("名反吟格", "返吟过"),
        "玉女守门" to listOf("玉女守门扉", "玉女守门"),
        "日照门" to listOf("日照门兮", "日照门"),
        "地户闭门" to listOf("地户闭门"),
        "青龙绻户" to listOf("青龙絻户", "青龙财户"),
        "太白入门" to listOf("太白入门"),
        "白虎入门" to listOf("值使加地盘辛", "白虎入门"),
        "玄武守门" to listOf("玄武守门"),
        "螣蛇守门" to listOf("腾蛇守门", "守门腾蛇"),
        "空亡" to listOf("主空主人心虚诈", "甲子旬中空甲戌"),
    )

    @Volatile
    private var vol3: List<String>? = null

    private fun load(context: Context): List<String> {
        vol3?.let { return it }
        val text = context.assets.open("qimen_book/vol3.txt").bufferedReader().use { it.readText() }
        return text.lines().also { vol3 = it }
    }

    /** 查询格局对应的教材原文片段；未收录返回 null */
    fun lookup(context: Context, patternName: String): String? {
        val lines = load(context)
        val kws = KEYWORDS[patternName] ?: return null
        for (kw in kws) {
            val idx = lines.indexOfFirst { kw in it }
            if (idx < 0) continue
            val start = (idx - 2).coerceAtLeast(0)
            val end = (idx + 7).coerceAtMost(lines.size)
            val seg = mutableListOf<String>()
            for (j in start until end) {
                val t = lines[j].trim().removePrefix("> ").trim()
                if (t.isEmpty() || t == "```" || t.startsWith("#")) continue
                seg += t
            }
            if (seg.isNotEmpty()) return seg.joinToString("\n")
        }
        return null
    }
}
