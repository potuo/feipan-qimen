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
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 应用日志系统：写入内部存储 filesDir/logs/，按天滚动。
 * 记录：排盘请求/结果、案例操作、页面事件、未捕获异常（崩溃栈）。
 */
object LogManager {

    private const val TAG = "QimenLog"
    private var appContext: Context? = null
    private val lock = ReentrantLock()
    private val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val logDirName = "logs"

    fun init(context: Context) {
        appContext = context.applicationContext
        log("系统", "日志系统初始化，设备 ${Build.MODEL} Android ${Build.VERSION.SDK_INT}")
    }

    fun log(category: String, message: String) {
        val ctx = appContext ?: return
        val line = "${timeFmt.format(Date())} [${category}] $message"
        lock.withLock {
            try {
                val file = logFileForToday(ctx)
                file.parentFile?.mkdirs()
                file.appendText(line + "\n")
            } catch (e: Exception) {
                Log.e(TAG, "写日志失败", e)
            }
        }
        Log.d(TAG, line)
    }

    fun logException(where: String, e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        log("崩溃", "[$where] ${e::class.simpleName}: ${e.message}\n${sw.toString()}")
    }

    private fun logFileForToday(ctx: Context): File =
        File(ctx.filesDir, "$logDirName/app_${dayFmt.format(Date())}.log")

    /** 日志目录 */
    fun logDir(ctx: Context): File = File(ctx.filesDir, logDirName)

    /** 全部日志合并文本（导出用） */
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

    /** 日志总大小（KB） */
    fun totalSizeKB(ctx: Context): Long {
        val dir = logDir(ctx)
        if (!dir.exists()) return 0
        return dir.listFiles()?.sumOf { it.length() }?.div(1024) ?: 0
    }

    /** 清空日志 */
    fun clearLogs(ctx: Context) {
        lock.withLock {
            val dir = logDir(ctx)
            dir.listFiles()?.forEach { it.delete() }
        }
        log("系统", "日志已清空")
    }
}
