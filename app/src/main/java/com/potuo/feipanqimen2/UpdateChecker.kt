package com.potuo.feipanqimen2

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** 检测更新结果 */
data class UpdateInfo(
    val version: String,
    val apkUrl: String,
    val notes: String?,
)

/** 更新日志条目（联网拉取自仓库 changelog.json） */
data class ChangelogEntry(
    val version: String,
    val date: String,
    val items: List<String>,
)

/** 系统公告（联网拉取自仓库 notice.json） */
data class NoticeInfo(
    val text: String,
    val date: String? = null,
)

/**
 * 无服务器检测更新：GitHub Releases API 为主，jsDelivr / raw 静态 version.json 兜底。
 * 客户端只需拿远程最新版本号与本地比对，需要时下载 APK 安装。
 */
object UpdateChecker {

    private const val REPO = "potuo/feipan-qimen"
    private const val GITEE_RAW = "https://gitee.com/$REPO/raw/master"
    private const val GITHUB_API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val JS_DELIVR = "https://cdn.jsdelivr.net/gh/$REPO@master/version.json"
    private const val RAW_GITHUB_BASE = "https://raw.githubusercontent.com/$REPO/master"
    private const val RAW_GITHUB = "$RAW_GITHUB_BASE/version.json"
    private const val JS_DELIVR_CHANGELOG = "https://cdn.jsdelivr.net/gh/$REPO@master/changelog.json"
    private const val RAW_CHANGELOG = "$RAW_GITHUB_BASE/changelog.json"
    private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L

    /** 版本比较：a > b 返回正数，a < b 返回负数，相等返回 0。语义化分段比较，v 前缀忽略。 */
    fun compareVersions(a: String, b: String): Int {
        val pa = a.trimStart('v').split('.').mapNotNull { it.toIntOrNull() }
        val pb = b.trimStart('v').split('.').mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val diff = pa.getOrElse(i) { 0 } - pb.getOrElse(i) { 0 }
            if (diff != 0) return diff
        }
        return 0
    }

    /** 距上次静默检查是否已超过间隔（24h） */
    fun shouldAutoCheck(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return System.currentTimeMillis() - prefs.getLong("last_update_check", 0L) >= CHECK_INTERVAL_MS
    }

    fun markChecked(context: Context) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putLong("last_update_check", System.currentTimeMillis()).apply()
    }

    /** 检查最新版本：Gitee（国内主）→ GitHub API → jsDelivr → raw，全部失败返回 null（调用方静默处理） */
    suspend fun checkLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        fetchVersionFile("$GITEE_RAW/version.json")
            ?: fetchGitHubApi()
            ?: fetchVersionFile(JS_DELIVR)
            ?: fetchVersionFile(RAW_GITHUB)
    }

    private fun fetchGitHubApi(): UpdateInfo? {
        return runCatching {
            val conn = URL(GITHUB_API).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "feipan-qimen")
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val tag = json.getString("tag_name").trimStart('v')
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name").endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            }
            if (apkUrl == null) return null
            UpdateInfo(tag, apkUrl, json.optString("body").takeIf { it.isNotBlank() })
        }.getOrNull()
    }

    /** 静态 version.json：{"version":"x.y.z","apk_url":"...","notes":"..."} */
    private fun fetchVersionFile(url: String): UpdateInfo? {
        return runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "feipan-qimen")
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val version = json.getString("version").trimStart('v')
            val apkUrl = json.optString("apk_url").takeIf { it.isNotBlank() } ?: return null
            UpdateInfo(version, apkUrl, json.optString("notes").takeIf { it.isNotBlank() })
        }.getOrNull()
    }

    /** 拉取更新日志（Gitee 主，jsDelivr/raw 兜底），失败返回 null（调用方可回退本地缓存） */
    suspend fun fetchChangelog(): List<ChangelogEntry>? = withContext(Dispatchers.IO) {
        fetchChangelogFrom("$GITEE_RAW/changelog.json")
            ?: fetchChangelogFrom(JS_DELIVR_CHANGELOG)
            ?: fetchChangelogFrom(RAW_CHANGELOG)
    }

    private fun fetchChangelogFrom(url: String): List<ChangelogEntry>? {
        return runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "feipan-qimen")
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val arr = json.getJSONArray("logs")
            buildList {
                for (i in 0 until arr.length()) {
                    val e = arr.getJSONObject(i)
                    val itemsArr = e.getJSONArray("items")
                    add(
                        ChangelogEntry(
                            version = e.getString("version"),
                            date = e.getString("date"),
                            items = buildList { for (j in 0 until itemsArr.length()) add(itemsArr.getString(j)) },
                        ),
                    )
                }
            }
        }.getOrNull()
    }

    /** 拉取系统公告（Gitee 主，GitHub raw 兜底）；无公告或失败返回 null */
    suspend fun fetchNotice(): NoticeInfo? = withContext(Dispatchers.IO) {
        fetchNoticeFrom("$GITEE_RAW/notice.json") ?: fetchNoticeFrom("$RAW_GITHUB_BASE/notice.json")
    }

    private fun fetchNoticeFrom(url: String): NoticeInfo? {
        return runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "feipan-qimen")
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val text = json.optString("text").takeIf { it.isNotBlank() } ?: return null
            NoticeInfo(text, json.optString("date").takeIf { it.isNotBlank() })
        }.getOrNull()
    }

    /** 下载 APK 到目标文件，成功返回 true；onProgress 回传 0f~1f 进度 */
    suspend fun downloadApk(url: String, dest: File, onProgress: (Float) -> Unit = {}): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 30000
            conn.setRequestProperty("User-Agent", "feipan-qimen")
            if (conn.responseCode != 200) return@runCatching false
            val total = conn.contentLengthLong
            var downloaded = 0L
            conn.inputStream.use { input ->
                dest.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        downloaded += n
                        if (total > 0) onProgress(downloaded.toFloat() / total.toFloat())
                    }
                }
            }
            dest.length() > 0
        }.getOrDefault(false)
    }

    /** 更新日志缓存：成功拉取后存本地，离线时兜底展示 */
    fun saveChangelogCache(context: Context, entries: List<ChangelogEntry>) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("changelog_cache", Gson().toJson(entries)).apply()
    }

    fun loadChangelogCache(context: Context): List<ChangelogEntry>? {
        val json = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getString("changelog_cache", null) ?: return null
        val type = object : TypeToken<List<ChangelogEntry>>() {}.type
        return runCatching { Gson().fromJson<List<ChangelogEntry>>(json, type) }.getOrNull()
    }

    /** 通过 FileProvider + 系统安装器安装 APK（Android 8+ 需 REQUEST_INSTALL_PACKAGES 与未知来源授权） */
    fun installApk(context: Context, apkFile: File): Boolean {
        return runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
