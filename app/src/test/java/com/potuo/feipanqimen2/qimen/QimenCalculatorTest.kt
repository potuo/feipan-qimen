package com.potuo.feipanqimen2.qimen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class QimenCalculatorTest {

  @Test
  fun `2026-08-17 亥时 阴八局 值使伤门落震3`() {
    val dt = LocalDateTime.of(2026, 8, 17, 22, 0)
    val result = QimenCalculator.calculate(dt)

    assertEquals("阴遁", result.dunType)
    assertEquals(8, result.juNumber)
    assertEquals("甲寅", result.xunShou)
    assertEquals("癸", result.xunShouStem)
    assertEquals("伤", result.zhiShiGate)
    assertEquals(3, result.zhiShiPalace)
    assertEquals("震", result.palaces[3]!!.direction)

    // 阴遁八局地盘：戊8己7庚6辛5壬4癸3丁2丙1乙9
    assertEquals("戊", result.palaces[8]!!.earthStem)
    assertEquals("己", result.palaces[7]!!.earthStem)
    assertEquals("庚", result.palaces[6]!!.earthStem)
    assertEquals("辛", result.palaces[5]!!.earthStem)
    assertEquals("壬", result.palaces[4]!!.earthStem)
    assertEquals("癸", result.palaces[3]!!.earthStem)
    assertEquals("丁", result.palaces[2]!!.earthStem)
    assertEquals("丙", result.palaces[1]!!.earthStem)
    assertEquals("乙", result.palaces[9]!!.earthStem)

    // 值符天冲落震3
    assertEquals("天冲", result.zhiFuStar)
    assertEquals(3, result.zhiFuPalace)
    assertEquals("天冲", result.palaces[3]!!.star)

    // 伤门落震3
    assertEquals("伤", result.palaces[3]!!.gate)

    // 阴遁九神逆飞：值符3 螣蛇2 太阴1 六合9 勾陈8 太常7 朱雀6 九地5 九天4
    assertEquals("值符", result.palaces[3]!!.god)
    assertEquals("螣蛇", result.palaces[2]!!.god)
    assertEquals("太阴", result.palaces[1]!!.god)
    assertEquals("六合", result.palaces[9]!!.god)
    assertEquals("勾陈", result.palaces[8]!!.god)
    assertEquals("太常", result.palaces[7]!!.god)
    assertEquals("朱雀", result.palaces[6]!!.god)
    assertEquals("九地", result.palaces[5]!!.god)
    assertEquals("九天", result.palaces[4]!!.god)

    // 星顺飞：冲3辅4心6柱7任8英9蓬1芮2 禽居中5
    assertEquals("天辅", result.palaces[4]!!.star)
    assertEquals("天心", result.palaces[6]!!.star)
    assertEquals("天柱", result.palaces[7]!!.star)
    assertEquals("天任", result.palaces[8]!!.star)
    assertEquals("天英", result.palaces[9]!!.star)
    assertEquals("天蓬", result.palaces[1]!!.star)
    assertEquals("天芮", result.palaces[2]!!.star)
    assertEquals("天禽", result.palaces[5]!!.star)

    // 门顺飞：伤3杜4开6惊7生8景9休1死2 中居中5
    assertEquals("杜", result.palaces[4]!!.gate)
    assertEquals("开", result.palaces[6]!!.gate)
    assertEquals("惊", result.palaces[7]!!.gate)
    assertEquals("生", result.palaces[8]!!.gate)
    assertEquals("景", result.palaces[9]!!.gate)
    assertEquals("休", result.palaces[1]!!.gate)
    assertEquals("死", result.palaces[2]!!.gate)
    assertEquals("中", result.palaces[5]!!.gate)

    assertTrue(result.hourPillar.endsWith("亥"))
  }

  @Test
  fun `2025-10-22 卯时 阴六局 值使开门落震3`() {
    val dt = LocalDateTime.of(2025, 10, 22, 6, 0)
    val result = QimenCalculator.calculate(dt)

    assertEquals("阴遁", result.dunType)
    assertEquals(6, result.juNumber)
    assertEquals("甲子", result.xunShou)
    assertEquals("戊", result.xunShouStem)
    assertEquals("开", result.zhiShiGate)
    assertEquals(3, result.zhiShiPalace)
    assertEquals("震", result.palaces[3]!!.direction)
  }

  @Test
  fun `2026-08-30 亥时 阴七局 时柱旬首甲午 值符天辅落乾6 值使杜门落艮8`() {
    val dt = LocalDateTime.of(2026, 8, 30, 22, 0)
    val result = QimenCalculator.calculate(dt)

    // 日柱丙子(甲戌旬)≠时柱己亥(甲午旬)——回归：必须用时柱旬首！
    assertEquals("阴遁", result.dunType)
    assertEquals(7, result.juNumber)
    assertEquals("甲午", result.xunShou)
    assertEquals("辛", result.xunShouStem)
    assertEquals("辰巳", result.kongWang)

    // 遁辛地盘宫=巽4 → 值符星天辅、值使门杜门
    assertEquals("天辅", result.zhiFuStar)
    assertEquals("杜", result.zhiShiGate)

    // 值符随时干己(地盘乾6) → 天辅落乾6
    assertEquals(6, result.zhiFuPalace)
    assertEquals("天辅", result.palaces[6]!!.star)

    // 值使飞宫法：从巽4逆飞6步(甲午1..己亥6) → 艮8
    assertEquals(8, result.zhiShiPalace)
    assertEquals("杜", result.palaces[8]!!.gate)

    // 天禽居中、中门居中
    assertEquals("天禽", result.palaces[5]!!.star)
    assertEquals("中", result.palaces[5]!!.gate)
  }

  @Test
  fun flyPalace_yin_backward_from_3_by_9_steps_returns_3() {
    assertEquals(3, QimenCalculator.flyPalace(3, 9, forward = false))
  }

  @Test
  fun flyPalace_yin_backward_from_6_by_3_steps_returns_3() {
    assertEquals(3, QimenCalculator.flyPalace(6, 3, forward = false))
  }
}
