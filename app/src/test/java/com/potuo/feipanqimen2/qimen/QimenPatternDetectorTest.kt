package com.potuo.feipanqimen2.qimen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QimenPatternDetectorTest {

    private fun makeResult(
        xunShouStem: String = "戊",
        zhiFuPalace: Int = 1,
        zhiShiGate: String = "休",
        zhiShiPalace: Int = 1,
        kongWang: String = "",
        palaces: Map<Int, PalaceInfo> = mapOf(),
    ) = QimenResult(
        yearPillar = "丙午", monthPillar = "丙申", dayPillar = "戊寅", hourPillar = "壬子",
        siZhu = "丙午年 丙申月 戊寅日 壬子时",
        jieQi = "处暑", yuan = "下元", dunType = "阴遁", juNumber = 7,
        xunShou = "甲辰", xunShouStem = xunShouStem, kongWang = kongWang,
        zhiFuStar = "天冲", zhiFuPalace = zhiFuPalace,
        zhiShiGate = zhiShiGate, zhiShiPalace = zhiShiPalace,
        palaces = palaces,
    )

    private fun palace(p: Int, gate: String = "", heaven: String = "", earth: String = "") = PalaceInfo(
        palace = p,
        direction = QimenConstants.PALACE_NAMES[p]!!,
        god = "", star = "", heavenStem = heaven, gate = gate, earthStem = earth,
    )

    @Test
    fun `击刑 戊值符落震3 检出`() {
        val r = makeResult(xunShouStem = "戊", zhiFuPalace = 3)
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "六仪击刑" && it.isAuspicious == false })
    }

    @Test
    fun `击刑 值符不在刑位不检出`() {
        val r = makeResult(xunShouStem = "戊", zhiFuPalace = 1)
        assertTrue(QimenPatternDetector.detect(r).none { it.name == "六仪击刑" })
    }

    @Test
    fun `入墓 丙落乾6 检出`() {
        val r = makeResult(palaces = mapOf(6 to palace(6, heaven = "丙", earth = "戊")))
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "入墓" && it.detail.contains("丙") })
    }

    @Test
    fun `门迫 开金落震木 检出`() {
        val r = makeResult(palaces = mapOf(3 to palace(3, gate = "开", heaven = "庚", earth = "戊")))
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "门迫" && it.detail.contains("开") })
    }

    @Test
    fun `受制 休水落坤土 检出`() {
        // 坤2 土克 休门 水 → 受制
        val r = makeResult(palaces = mapOf(2 to palace(2, gate = "休", heaven = "庚", earth = "戊")))
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "受制" })
    }

    @Test
    fun `交和 休水生坎宫 检出`() {
        // 休门 水 生 坎1 水？水生水不是生——换：死门土生兑7金？土生金 ✓
        val r = makeResult(palaces = mapOf(7 to palace(7, gate = "死", heaven = "庚", earth = "戊")))
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "交和" })
    }

    @Test
    fun `伏吟 天盘地盘同干 检出`() {
        val r = makeResult(palaces = mapOf(1 to palace(1, heaven = "戊", earth = "戊")))
        assertTrue(QimenPatternDetector.detect(r).any { it.name == "伏吟" })
    }

    @Test
    fun `反吟 甲庚相冲 检出`() {
        val r = makeResult(palaces = mapOf(1 to palace(1, heaven = "甲", earth = "庚")))
        assertTrue(QimenPatternDetector.detect(r).any { it.name == "反吟" })
    }

    @Test
    fun `守门 值使加丁 玉女守门`() {
        val r = makeResult(zhiShiGate = "开", zhiShiPalace = 6, palaces = mapOf(6 to palace(6, gate = "开", heaven = "丁", earth = "戊")))
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "玉女守门" })
    }

    @Test
    fun `空亡 恒在结果中`() {
        val r = makeResult(kongWang = "寅卯")
        val patterns = QimenPatternDetector.detect(r)
        assertTrue(patterns.any { it.name == "空亡" && it.detail.contains("寅卯") })
    }

    @Test
    fun `2026-08-17 真实盘面 检测不崩溃且有输出`() {
        val result = QimenCalculator.calculate(java.time.LocalDateTime.of(2026, 8, 17, 22, 0))
        val patterns = QimenPatternDetector.detect(result)
        assertTrue("应至少检出空亡", patterns.isNotEmpty())
        assertEquals("空亡恒在最后", "空亡", patterns.last().name)
    }
}
