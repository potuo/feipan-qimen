package com.potuo.feipanqimen2.log

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 应用日志系统：写入内部存储 filesDir/logs/，按天滚动。
 * - 级别分级（DEBUG/INFO/WARN/ERROR）
 * - 异步单线程写（不阻塞主线程）
 * - 轮转清理：保留 7 天 + 总大小 10MB 上限
 * - 敏感信息脱敏（API key 打码）
 * - 崩溃计数（下次启动提示）
 */
object LogManager {

    enum class Level { DEBUG, INFO, WARN, ERROR }

    private const val TAG = "QimenLog"
    private var appContext: Context? = null
    private val lock = ReentrantLock()
    private val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private const val logDirName = "logs"

    // 异步写：单线程守护线程
    private val writer = Executors.newSingleThreadExecutor { r ->
        Thread(r, "qimen-log-writer").apply { isDaemon = true }
    }

    // 轮转参数：保留 7 天 + 总大小 10MB
    private const val MAX_DAYS = 7
    private const val MAX_TOTAL_BYTES = 10L * 1024 * 1024

    fun init(context: Context) {
        appContext = context.applicationContext
        cleanup()
        log(Level.INFO, "系统", "日志系统初始化，设备 ${Build.MODEL} Android ${Build.VERSION.SDK_INT}")
    }

    // ── 写入入口 ──

    /** 默认 INFO 级别（保持旧调用兼容） */
    fun log(category: String, message: String) = log(Level.INFO, category, message)

    fun d(category: String, message: String) = log(Level.DEBUG, category, message)
    fun w(category: String, message: String) = log(Level.WARN, category, message)
    fun e(category: String, message: String) = log(Level.ERROR, category, message)

    fun log(level: Level, category: String, message: String) {
        val ctx = appContext ?: return
        val line = "${timeFmt.format(Date())} [${level.name}] [${category}] ${redact(message)}"
        writer.execute {
            lock.withLock {
                try {
                    val file = logFileForToday(ctx)
                    file.parentFile?.mkdirs()
                    file.appendText(line + "\n")
                } catch (ex: Exception) {
                    Log.e(TAG, "写日志失败", ex)
                }
            }
        }
        when (level) {
            Level.ERROR -> Log.e(TAG, line)
            Level.WARN -> Log.w(TAG, line)
            Level.DEBUG -> Log.d(TAG, line)
            else -> Log.i(TAG, line)
        }
    }

    fun logException(where: String, e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        log(Level.ERROR, "崩溃", "[$where] ${e::class.simpleName}: ${e.message}\n${sw}")
    }

    // ── 文件 ──

    private fun logFileForToday(ctx: Context): File =
        File(ctx.filesDir, "$logDirName/app_${dayFmt.format(Date())}.log")

    private fun logDir(ctx: Context): File = File(ctx.filesDir, logDirName)

    /** 日志文件列表（新→旧） */
    fun listLogFiles(ctx: Context): List<File> =
        logDir(ctx).listFiles { f -> f.name.endsWith(".log") }?.sortedByDescending { it.name } ?: emptyList()

    /** 读单个日志文件内容 */
    fun readLogFile(ctx: Context, fileName: String): String =
        File(logDir(ctx), fileName).takeIf { it.exists() }?.readText() ?: ""

    // ── 脱敏 ──

    /** 打码 API key（sk- 前缀）等敏感串 */
    private fun redact(s: String): String =
        s.replace(Regex("sk-[A-Za-z0-9]{6,}"), "sk-***")

    // ── 轮转清理 ──

    private fun cleanup() {
        val ctx = appContext ?: return
        writer.execute {
            lock.withLock {
                try {
                    val dir = logDir(ctx)
                    if (!dir.exists()) return@withLock
                    val cutoff = dayFmt.format(Date(System.currentTimeMillis() - MAX_DAYS * 24L * 3600 * 1000))
                    dir.listFiles { f -> f.name.endsWith(".log") }
                        ?.filter { it.nameWithoutExtension.substringAfter("app_") < cutoff }
                        ?.forEach { it.delete() }
                    // 超总大小则删最旧（保留至少 1 个）
                    var files = dir.listFiles { f -> f.name.endsWith(".log") }?.sortedBy { it.name }?.toMutableList() ?: mutableListOf()
                    var total = files.sumOf { it.length() }
                    while (total > MAX_TOTAL_BYTES && files.size > 1) {
                        val oldest = files.removeAt(0)
                        total -= oldest.length()
                        oldest.delete()
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "日志清理失败", ex)
                }
            }
        }
    }

    // ── 导出 / 大小 / 清空 ──

    fun exportAllLogs(ctx: Context): String {
        val dir = logDir(ctx)
        if (!dir.exists()) return "（无日志）"
        val sb = StringBuilder()
        sb.append("飞盘奇门排盘 App 日志导出\n")
        sb.append("导出时间：${timeFmt.format(Date())}\n")
        sb.append("设备：${Build.MODEL} / Android ${Build.VERSION.SDK_INT}\n")
        sb.append("=".repeat(60)).append("\n\n")
        val files = dir.listFiles { f -> f.name.endsWith(".log") }?.sortedBy { it.name } ?: emptyList()
        for (f in files) {
            sb.append("───── ${f.name}（${f.length()} 字节）─────\n")
            sb.append(f.readText())
            sb.append("\n")
        }
        return sb.toString()
    }

    fun totalSizeKB(ctx: Context): Long {
        val dir = logDir(ctx)
        if (!dir.exists()) return 0
        return dir.listFiles()?.sumOf { it.length() }?.div(1024) ?: 0
    }

    fun clearLogs(ctx: Context) {
        lock.withLock {
            logDir(ctx).listFiles()?.forEach { it.delete() }
        }
        log(Level.INFO, "系统", "日志已清空")
    }

    // ── 崩溃计数 ──

    private const val CRASH_PREFS = "qimen_log"
    private const val CRASH_KEY = "crash_count"

    fun recordCrash(ctx: Context) {
        val p = ctx.getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE)
        p.edit().putInt(CRASH_KEY, p.getInt(CRASH_KEY, 0) + 1).apply()
    }

    fun crashCount(ctx: Context): Int =
        ctx.getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE).getInt(CRASH_KEY, 0)

    fun clearCrashCount(ctx: Context) =
        ctx.getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE).edit().putInt(CRASH_KEY, 0).apply()
}
