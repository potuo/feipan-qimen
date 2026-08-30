package com.potuo.feipanqimen2.qimen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class TrueSolarTimeTest {

    @Test
    fun `均时差 2月11日 约负14分钟`() {
        // dayOfYear(2/11) = 42，经典 EoT ≈ -14.2 min
        val eot = TrueSolarTime.equationOfTime(42)
        assertTrue("EoT=$eot 应在 -15~-13 之间", eot in -15.0..-13.0)
    }

    @Test
    fun `均时差 11月3日 约正16分钟`() {
        // dayOfYear(11/3) = 307，经典 EoT ≈ +16.4 min
        val eot = TrueSolarTime.equationOfTime(307)
        assertTrue("EoT=$eot 应在 15~17 之间", eot in 15.0..17.0)
    }

    @Test
    fun `东经120度 真太阳时只含均时差偏移`() {
        val dt = LocalDateTime.of(2026, 8, 17, 12, 0)
        val trueSolar = TrueSolarTime.toTrueSolar(dt, 120.0)
        // 8月17日 EoT 约 -4.7min
        assertTrue("偏移应在 -6~-3 分钟", trueSolar.minute in 54..57)
    }

    @Test
    fun `经度时差 每度4分钟`() {
        val dt = LocalDateTime.of(2026, 8, 17, 10, 0)
        // 东经 120+3=123°：+12 分钟（均时差被抵消）
        val t1 = TrueSolarTime.toTrueSolar(dt, 123.0)
        val t2 = TrueSolarTime.toTrueSolar(dt, 120.0)
        assertEquals(12L, java.time.Duration.between(t2, t1).toMinutes())
    }

    @Test
    fun `时辰索引 边界正确`() {
        assertEquals(0, TrueSolarTime.hourIndex(23)) // 子时
        assertEquals(0, TrueSolarTime.hourIndex(0))  // 子时
        assertEquals(1, TrueSolarTime.hourIndex(1))  // 丑时
        assertEquals(6, TrueSolarTime.hourIndex(11)) // 午时
        assertEquals(6, TrueSolarTime.hourIndex(12)) // 午时
        assertEquals(7, TrueSolarTime.hourIndex(13)) // 未时
        assertEquals(11, TrueSolarTime.hourIndex(21)) // 亥时
        assertEquals(11, TrueSolarTime.hourIndex(22)) // 亥时
    }

    @Test
    fun `跨时辰检测 北京时与真太阳时同辰不提示`() {
        val dt = LocalDateTime.of(2026, 8, 17, 10, 0)
        assertNull(TrueSolarTime.crossingHourHint(dt, 120.0))
    }

    @Test
    fun `跨时辰检测 大经度差触发提示`() {
        // 东经 150°：+120 分钟偏移 → 必跨时辰
        val dt = LocalDateTime.of(2026, 8, 17, 10, 0)
        val hint = TrueSolarTime.crossingHourHint(dt, 150.0)
        assertNotNull(hint)
        assertTrue(hint!!.contains("跨时辰"))
    }
}
