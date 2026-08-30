package com.potuo.feipanqimen2.qimen

import org.junit.Test
import java.time.LocalDateTime

class CrashReproTest {

    @Test
    fun `2026-08-30 各时辰排盘不崩溃`() {
        // 指挥官反馈：寅时/卯时闪退（甲申旬遁庚落中宫5）
        val hours = mapOf(
            "子时" to 0, "丑时" to 2, "寅时" to 4, "卯时" to 6,
            "辰时" to 8, "巳时" to 10, "午时" to 12, "未时" to 14,
            "申时" to 16, "酉时" to 18, "戌时" to 20, "亥时" to 22,
        )
        for ((name, h) in hours) {
            try {
                val r = QimenCalculator.calculate(LocalDateTime.of(2026, 8, 30, h, 0))
                println("$name OK: ${r.siZhu} 旬首${r.xunShou}遁${r.xunShouStem} 值符${r.zhiFuStar}@${r.zhiFuPalace} 值使${r.zhiShiGate}@${r.zhiShiPalace}")
            } catch (e: Throwable) {
                println("$name 崩溃: ${e::class.simpleName}: ${e.message}")
            }
        }
    }
}
