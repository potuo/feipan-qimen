package com.potuo.feipanqimen2

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
 * 布局：标题（四柱/局数）→ 九宫盘面 → 底部（值符值使/空亡）。
 */
object QimenShareImage {

    private const val WIDTH = 1080

    fun create(result: QimenResult, palette: QimenPalette, dir: File): File {
        val boardSize = 900
        val titleH = 260
        val footerH = 180
        val height = titleH + boardSize + footerH

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
            val isCenter = palaceNum == 5

            cellBg.color = when {
                isCenter -> palette.centerBg.toArgb()
                else -> palette.paper.toArgb()
            }
            canvas.drawRect(x, y, x + cell, y + cell, cellBg)
            val rect = RectF(x, y, x + cell, y + cell)
            if (isZhiFu || isZhiShi) {
                canvas.drawRect(rect, specialPaint)
            } else {
                canvas.drawRect(rect, borderPaint)
            }

            // 宫名（左上角）
            canvas.drawText(
                "${info.direction}$palaceNum",
                x + 40f,
                y + 52f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.slate.toArgb(); textSize = 26f
                },
            )

            val cx = x + cell / 2f

            // 值符/值使标记
            val badge = when {
                isZhiFu && isZhiShi -> "符使"
                isZhiFu -> "符"
                isZhiShi -> "使"
                else -> null
            }
            if (badge != null) {
                canvas.drawText(badge, x + cell - 50f, y + 52f, goldPaint)
            }

            if (isCenter) {
                canvas.drawText("中", cx, y + cell / 2f + 10f, titlePaint)
            } else {
                // 星+天盘干（上）
                canvas.drawText("${info.star}${info.heavenStem}", cx, y + cell / 2f - 40f, cellText)
                // 门（中）
                canvas.drawText(info.gate, cx, y + cell / 2f + 22f, cellText)
                // 神（下左）
                canvas.drawText(info.god, cx - 40f, y + cell - 30f, cellSub)
                // 地盘干（下右）
                canvas.drawText("(${info.earthStem})", cx + 40f, y + cell - 30f, goldPaint)
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
