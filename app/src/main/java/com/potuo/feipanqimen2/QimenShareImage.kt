package com.potuo.feipanqimen2

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenResult
import com.potuo.feipanqimen2.ui.theme.QimenPalette
import androidx.compose.ui.graphics.toArgb
import java.io.File
import java.io.FileOutputStream

/**
 * 盘面分享图：把飞盘盘面绘制成一张 PNG 图片（跟随当前主题配色）。
 * 布局：标题（四柱/局数）→ 九宫盘面 → 底部（值符值使/空亡 → 可选案例信息）。
 */
object QimenShareImage {

    private const val WIDTH = 1080

    /** 案例附加信息（分享案例盘面时携带：标签/备注/反馈结果） */
    data class CaseShareInfo(
        val tags: String = "",
        val note: String = "",
        val feedback: String = "",
    )

    fun create(result: QimenResult, palette: QimenPalette, dir: File, extra: CaseShareInfo? = null): File {
        val boardSize = 900
        val titleH = 260
        val footerH = 180
        // 案例信息区（仅在有内容时占用高度）
        val extraLines = buildList {
            if (!extra?.tags.isNullOrBlank()) add("标签：${extra!!.tags}")
            if (!extra?.note.isNullOrBlank()) add("备注：${extra!!.note}")
            if (!extra?.feedback.isNullOrBlank()) add("反馈：${extra!!.feedback}")
        }
        val extraH = if (extraLines.isEmpty()) 0 else 50 + extraLines.size * 58 + 30
        val height = titleH + boardSize + footerH + extraH

        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(palette.paper.toArgb())

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.inkText.toArgb()
            textSize = 52f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.slate.toArgb()
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        val cellText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.inkText.toArgb()
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        val cellSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.slate.toArgb()
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.gold.toArgb()
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        val goldSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.gold.toArgb()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.cinnabar.toArgb()
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = palette.gridBorder.toArgb()
        }
        val specialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = palette.cinnabar.toArgb()
        }

        // ── 标题区 ──
        canvas.drawText("飞盘奇门 · ${result.siZhu}", WIDTH / 2f, 110f, titlePaint)
        canvas.drawText(
            "${result.jieQi} · ${result.yuan} · ${result.dunType}${result.juNumber}局   旬首：${result.xunShou}（遁${result.xunShouStem}）",
            WIDTH / 2f,
            170f,
            subPaint,
        )
        canvas.drawText("据《奇门基础资料 2023版教》鸣法体系 · 值使飞宫法", WIDTH / 2f, 220f, subPaint)

        // ── 盘面 ──
        val left = (WIDTH - boardSize) / 2f
        val top = titleH.toFloat()
        val cell = boardSize / 3f
        val gridOrder = listOf(4, 9, 2, 3, 5, 7, 8, 1, 6)
        val cellBg = Paint(Paint.ANTI_ALIAS_FLAG)

        gridOrder.forEachIndexed { index, palaceNum ->
            val col = index % 3
            val row = index / 3
            val x = left + col * cell
            val y = top + row * cell
            val info = result.palaces[palaceNum] ?: return@forEachIndexed
            val isZhiFu = palaceNum == result.zhiFuPalace
            val isZhiShi = palaceNum == result.zhiShiPalace

            cellBg.color = palette.paper.toArgb()
            canvas.drawRect(x, y, x + cell, y + cell, cellBg)
            val rect = RectF(x, y, x + cell, y + cell)
            if (isZhiFu || isZhiShi) {
                canvas.drawRect(rect, specialPaint)
            } else {
                canvas.drawRect(rect, borderPaint)
            }

            val cx = x + cell / 2f
            val hiddenGan = info.hiddenStem?.firstOrNull()?.toString() ?: ""
            val hiddenZhi = info.hiddenStem?.lastOrNull()?.toString() ?: ""
            val starRed = info.star == result.zhiFuStar
            val gateRed = info.gate == result.zhiShiGate
            val heavenRed = info.heavenStem.isNotEmpty() &&
                (info.heavenStem == result.dayPillar.first().toString() ||
                    info.heavenStem == result.hourPillar.first().toString())
            val earthGodRed = info.earthGod == "值符"

            // ── 行1：宫名（左）＋ 天盘神（中）＋ 角标（右上：马/迫/刑/墓棕）──
            canvas.drawText("${info.direction}$palaceNum", x + 55f, y + 50f, cellSub)
            canvas.drawText(info.god, cx, y + 50f, cellSub)
            info.marks.forEachIndexed { idx, m ->
                canvas.drawText(m, x + cell - 45f - idx * 38f, y + 50f, goldSmallPaint)
            }

            // ── 行2：星行 = 暗干(棕) 星(黑/红) 天盘干(黑/红) ──
            val starPaint = if (starRed) redPaint else cellText
            if (hiddenGan.isNotEmpty()) {
                canvas.drawText(hiddenGan, cx - 100f, y + 100f, goldPaint)
            }
            canvas.drawText(info.star, cx, y + 100f, starPaint)
            if (info.heavenStem.isNotEmpty()) {
                canvas.drawText(info.heavenStem, cx + 100f, y + 100f, if (heavenRed) redPaint else cellText)
            }

            // ── 行3：六亲 = 星六亲 + 天盘干六亲 ──
            if (info.liuQinStar.isNotEmpty()) {
                canvas.drawText(info.liuQinStar, cx - 100f, y + 138f, cellSub)
                canvas.drawText(info.liuQinHeaven, cx + 100f, y + 138f, cellSub)
            }

            // ── 行4：门行 = 暗支(棕) 门(黑/红) 地盘干(黑) ──
            val gatePaint = if (gateRed) redPaint else cellText
            if (hiddenZhi.isNotEmpty()) {
                canvas.drawText(hiddenZhi, cx - 100f, y + 186f, goldPaint)
            }
            canvas.drawText(info.gate, cx, y + 186f, gatePaint)
            if (info.earthStem.isNotEmpty()) {
                canvas.drawText(info.earthStem, cx + 100f, y + 186f, cellText)
            }

            // ── 行5：六亲 = 门六亲 + 地盘干六亲 ──
            if (info.liuQinGate.isNotEmpty()) {
                canvas.drawText(info.liuQinGate, cx - 100f, y + 224f, cellSub)
                canvas.drawText(info.liuQinEarth, cx + 100f, y + 224f, cellSub)
            }

            // ── 行6：地盘神（左）+ 状态（右）──
            canvas.drawText(info.earthGod, x + 70f, y + 272f, if (earthGodRed) redPaint else cellSub)
            if (info.state.isNotEmpty()) {
                canvas.drawText(info.state, x + cell - 70f, y + 272f, goldSmallPaint)
            }
        }

        // ── 底部 ──
        val footerY = top + boardSize + 80f
        canvas.drawText(
            "值符：${result.zhiFuStar}·${QimenConstants.PALACE_NAMES[result.zhiFuPalace]}${result.zhiFuPalace}宫   值使：${result.zhiShiGate}门·${QimenConstants.PALACE_NAMES[result.zhiShiPalace]}${result.zhiShiPalace}宫",
            WIDTH / 2f,
            footerY,
            cellText,
        )
        canvas.drawText("空亡：${result.kongWang}", WIDTH / 2f, footerY + 60f, goldPaint)

        // ── 案例信息区（标签/备注/反馈，有内容才绘制）──
        if (extraLines.isNotEmpty()) {
            val infoTop = footerY + 120f
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.inkText.toArgb()
                textSize = 32f
                textAlign = Paint.Align.LEFT
            }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.cinnabar.toArgb()
                textSize = 32f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.LEFT
            }
            // 分隔线
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = palette.gridBorder.toArgb()
            }
            canvas.drawLine(80f, infoTop - 30f, WIDTH - 80f, infoTop - 30f, dividerPaint)

            extraLines.forEachIndexed { i, line ->
                val y = infoTop + i * 58f
                val (label, content) = if (line.contains("：")) {
                    line.substringBefore("：") to line.substringAfter("：")
                } else {
                    "" to line
                }
                if (label.isNotEmpty()) canvas.drawText("$label：", 80f, y, labelPaint)
                canvas.drawText(
                    content,
                    80f + (if (label.isNotEmpty()) labelPaint.measureText("$label：") else 0f),
                    y,
                    linePaint,
                )
            }
        }

        // ── 保存 ──
        dir.mkdirs()
        val file = File(dir, "feipan_qimen_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }
}
