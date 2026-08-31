package com.potuo.feipanqimen2.qimen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * 排盘自洽性检核：跨节气 × 12 时辰批量扫描，断言所有盘面不变量。
 * 覆盖边界：值使中门（旬首遁仪落中宫）、时干为甲（值符归位）、阳遁/阴遁交替。
 */
class QimenCalculatorSelfConsistencyTest {

    /** 跨年选点：阳遁（冬至~夏至）/ 阴遁（夏至~冬至）各节气段 */
    private val probeDates = listOf(
        LocalDateTime.of(2026, 1, 10, 0, 0),   // 小寒·阳遁
        LocalDateTime.of(2026, 3, 25, 0, 0),   // 春分·阳遁
        LocalDateTime.of(2026, 6, 15, 0, 0),   // 芒种·阳遁
        LocalDateTime.of(2026, 8, 17, 0, 0),   // 立秋·阴遁
        LocalDateTime.of(2026, 8, 30, 0, 0),   // 处暑·阴遁
        LocalDateTime.of(2026, 10, 10, 0, 0),  // 寒露·阴遁
        LocalDateTime.of(2026, 12, 15, 0, 0),  // 大雪·阴遁（冬至前）
    )

    private val allStems = QimenConstants.STEMS_ORDER.toSet()
    private val allGods = QimenConstants.GODS_ORDER.toSet()
    private val liuQinSet = setOf("父母", "官鬼", "妻财", "子孙", "兄弟")

    @Test
    fun `全时段扫描-排盘不变量`() {
        var total = 0
        probeDates.forEach { base ->
            for (hour in listOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22)) {
                val r = QimenCalculator.calculate(base.withHour(hour))
                assertInvariants(r, base.withHour(hour))
                total++
            }
        }
        // 至少扫出一次「值使=中门」的边界（阴七局+甲申旬 必现）
        assertTrue("应至少遇到一次值使中门边界", midGateHit)
        assertTrue("扫描了 $total 个时辰", total >= 80)
    }

    private var midGateHit = false

    private fun assertInvariants(r: QimenResult, dt: LocalDateTime) {
        val tag = "$dt -> ${r.dunType}${r.juNumber}局 ${r.hourPillar}"

        // 1. 地盘 9 干 = 戊己庚辛壬癸丁丙乙 各一次（无重复无遗漏）
        assertEquals(tag, allStems, r.palaces.values.map { it.earthStem }.toSet())

        // 2. 天盘 9 干同上（平移不变量）
        assertEquals(tag, allStems, r.palaces.values.map { it.heavenStem }.toSet())

        // 3. 九神 9 个无重复，且值符神落值符宫
        assertEquals(tag, allGods, r.palaces.values.map { it.god }.toSet())
        assertEquals(tag, "值符", r.palaces[r.zhiFuPalace]!!.god)

        // 4. 星：9 宫 9 星无重复（天禽参与飞布，不固定居中）；值符星落值符宫
        assertEquals(tag, r.zhiFuStar, r.palaces[r.zhiFuPalace]!!.star)
        val stars = r.palaces.values.map { it.star }
        assertEquals(tag, 9, stars.toSet().size)

        // 5. 门：9 宫 9 门无重复（中门参与飞布）；值使门落值使宫
        assertEquals(tag, r.zhiShiGate, r.palaces[r.zhiShiPalace]!!.gate)
        val gates = r.palaces.values.map { it.gate }
        assertEquals(tag, 9, gates.toSet().size)

        // 6. 值符星/值使门 = 旬首遁仪地盘宫的本位星/本位门
        val xunShouPalace = r.palaces.entries.first { it.value.earthStem == r.xunShouStem }.key
        assertEquals(tag, QimenConstants.STAR_ORIGINAL[xunShouPalace], r.zhiFuStar)
        assertEquals(tag, QimenConstants.GATE_ORIGINAL[xunShouPalace], r.zhiShiGate)

        // 7. 值使=中门时：值使宫按飞宫法（不固定中宫），中门落值使宫
        if (r.zhiShiGate == "中") {
            assertEquals(tag, "中", r.palaces[r.zhiShiPalace]!!.gate)
            midGateHit = true
        }

        // 8. 地盘神：值符从旬首宫起布（9 神无重复）
        assertEquals(tag, allGods, r.palaces.values.map { it.earthGod }.toSet())
        assertEquals(tag, "值符", r.palaces[xunShouPalace]!!.earthGod)

        // 9. 六亲字段合法（不空、属于五亲）
        r.palaces.values.forEach { p ->
            listOf(p.liuQinStar, p.liuQinHeaven, p.liuQinGate, p.liuQinEarth).forEach { lq ->
                assertTrue("$tag ${p.direction}${p.palace} 六亲[$lq]非法", lq in liuQinSet)
            }
            // 状态合法（月令旺衰 5 态）
            assertTrue("$tag ${p.direction}${p.palace} 状态[${p.state}]非法", p.state in setOf("旺", "相", "休", "囚", "死"))
            // 角标合法
            p.marks.forEach { m -> assertTrue("$tag 角标[$m]非法", m in setOf("马", "迫", "刑", "墓")) }
        }

        // 10. 空亡 = 旬首后 +10/+11 位地支
        val kongExpected = expectedKongWang(r.xunShou)
        assertEquals(tag, kongExpected, r.kongWang)
    }

    private fun expectedKongWang(xunShou: String): String {
        val idx = QimenConstants.JIA_ZI_60.indexOf(xunShou)
        val branches = "子丑寅卯辰巳午未申酉戌亥"
        return "${branches[(idx + 10) % 12]}${branches[(idx + 11) % 12]}"
    }

    @Test
    fun `单日12时辰全排不崩溃`() {
        val base = LocalDateTime.of(2026, 8, 30, 0, 0)
        for (hour in 0..23) {
            val r = QimenCalculator.calculate(base.withHour(hour))
            assertTrue(r.hourPillar.isNotBlank())
        }
    }
}
