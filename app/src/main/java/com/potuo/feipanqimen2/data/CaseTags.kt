package com.potuo.feipanqimen2.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 案例标签（事项类别）管理：用户自定义标签，默认仅「未分类」。
 * 存 SharedPreferences，同步用于占断分类（保存案例时选类别）与案例库筛选。
 */
object CaseTags {
    private const val KEY = "case_tags"
    const val DEFAULT = "未分类"
    private val gson = Gson()

    fun read(context: Context): List<String> {
        val json = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return listOf(DEFAULT)
        return runCatching {
            gson.fromJson<List<String>>(json, object : TypeToken<List<String>>() {}.type)
        }.getOrDefault(listOf(DEFAULT)).distinct().filter { it.isNotBlank() }.ifEmpty { listOf(DEFAULT) }
    }

    fun save(context: Context, tags: List<String>) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit()
            .putString(KEY, gson.toJson(tags.distinct().filter { it.isNotBlank() })).apply()
    }
}
