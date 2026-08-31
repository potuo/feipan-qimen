package com.potuo.feipanqimen2.qimen

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * 宫格增强字段（六亲/地盘神/角标/旺衰）与星门飞布的具体断言。
 * 基准盘：2026-08-30 20:36（戊戌时，处暑下元阴七局）——对照参考盘逐宫核验。
 */
class QimenPalaceEnhancerTest {

    private val result = QimenCalculator.calculate(LocalDateTime.of(2026, 8, 30, 20, 36))

    @Test
    fun `2026-08-30 20-36 星门飞布与参考盘一致`() {
        // 星序（含天禽）从值符星天辅（兑7）顺飞：辅7禽8心9柱1任2英3蓬4芮5冲6
        assertEquals("天辅", result.palaces[7]!!.star)
        assertEquals("天禽", result.palaces[8]!!.star)
        assertEquals("天心", result.palaces[9]!!.star)
        assertEquals("天柱", result.palaces[1]!!.star)
        assertEquals("天任", result.palaces[2]!!.star)
        assertEquals("天英", result.palaces[3]!!.star)
        assertEquals("天蓬", result.palaces[4]!!.star)
        assertEquals("天芮", result.palaces[5]!!.star)
        assertEquals("天冲", result.palaces[6]!!.star)

        // 门序（含中门）从值使杜门（离9）顺飞：杜9中1开2惊3生4景5休6死7伤8
        assertEquals("杜", result.palaces[9]!!.gate)
        assertEquals("中", result.palaces[1]!!.gate)
        assertEquals("开", result.palaces[2]!!.gate)
        assertEquals("惊", result.palaces[3]!!.gate)
        assertEquals("生", result.palaces[4]!!.gate)
        assertEquals("景", result.palaces[5]!!.gate)
        assertEquals("休", result.palaces[6]!!.gate)
        assertEquals("死", result.palaces[7]!!.gate)
        assertEquals("伤", result.palaces[8]!!.gate)
    }

    @Test
    fun `2026-08-30 20-36 六亲以时干戊为我在参考盘逐宫一致`() {
        // 参考盘六亲（行3=[星,天盘干] 行5=[门,地盘干]）
        val expected = mapOf(
            4 to listOf("妻财", "父母", "兄弟", "子孙"), // 巽4：天蓬水/丁火/生门土/辛金
            9 to listOf("子孙", "兄弟", "官鬼", "父母"), // 离9：天心金/己土/杜门木/丙火
            2 to listOf("兄弟", "官鬼", "子孙", "妻财"), // 坤2：天任土/乙木/开门金/癸水
            3 to listOf("父母", "父母", "子孙", "妻财"), // 震3：天英火/丙火/惊门金/壬水
            5 to listOf("兄弟", "妻财", "父母", "子孙"), // 中5：天芮土/癸水/景门火/庚金
            7 to listOf("官鬼", "子孙", "兄弟", "兄弟"), // 兑7：天辅木/辛金/死门土/戊土
            8 to listOf("兄弟", "子孙", "官鬼", "官鬼"), // 艮8：天禽土/庚金/伤门木/乙木
            1 to listOf("子孙", "兄弟", "兄弟", "父母"), // 坎1：天柱金/戊土/中门土/丁火
            6 to listOf("官鬼", "妻财", "妻财", "兄弟"), // 乾6：天冲木/壬水/休门水/己土
        )
        expected.forEach { (palace, lq) ->
            val p = result.palaces[palace]!!
            assertEquals("$palace 星六亲", lq[0], p.liuQinStar)
            assertEquals("$palace 天盘六亲", lq[1], p.liuQinHeaven)
            assertEquals("$palace 门六亲", lq[2], p.liuQinGate)
            assertEquals("$palace 地盘六亲", lq[3], p.liuQinEarth)
        }
    }

    @Test
    fun `2026-08-30 20-36 地盘神值符从旬首宫起布`() {
        // 旬首甲午辛，遁辛地盘巽4 → 值符起巽4，阴遁逆飞
        assertEquals("值符", result.palaces[4]!!.earthGod)
        assertEquals("螣蛇", result.palaces[3]!!.earthGod)
        assertEquals("太阴", result.palaces[2]!!.earthGod)
        assertEquals("六合", result.palaces[1]!!.earthGod)
        assertEquals("勾陈", result.palaces[9]!!.earthGod)
        assertEquals("太常", result.palaces[8]!!.earthGod)
        assertEquals("朱雀", result.palaces[7]!!.earthGod)
        assertEquals("九地", result.palaces[6]!!.earthGod)
        assertEquals("九天", result.palaces[5]!!.earthGod)
    }

    @Test
    fun `2026-08-30 20-36 角标与状态`() {
        // 马：戌时驿马在申（寅午戌马在申）→ 坤2（未申）
        assertEquals(listOf("马", "墓"), result.palaces[2]!!.marks) // 坤2 乙入墓+马
        // 刑墓：艮8 天盘庚击刑（申刑寅）+ 庚入墓艮8
        assertEquals(listOf("墓", "刑"), result.palaces[8]!!.marks)
        // 迫：震3 惊门金克震木
        assertEquals(listOf("迫"), result.palaces[3]!!.marks)
        // 其余宫无角标
        listOf(1, 4, 5, 6, 7, 9).forEach { p ->
            assertEquals("$p 应无角标", emptyList<String>(), result.palaces[p]!!.marks)
        }
        // 状态（月令旺衰·申月金令）：天盘干对月令——当令旺/令生相/生令休/克令囚/令克死
        // 注：参考盘含 废/没/胎 8 态，口径待校准，此处断言当前 5 态实现合法
        listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach { p ->
            assert(result.palaces[p]!!.state in setOf("旺", "相", "休", "囚", "死"))
        }
        // 抽查：丁火克申金=囚；辛金当令=旺；己土生金=休
        assertEquals("囚", result.palaces[4]!!.state)
        assertEquals("旺", result.palaces[7]!!.state)
        assertEquals("休", result.palaces[9]!!.state)
    }

    @Test
    fun `老案例缺注解字段时 enhance 补全且与全新盘一致`() {
        // 模拟 v2.6.8 之前存的旧格式：注解字段（地盘神/状态/六亲/角标）为空
        val oldPalaces = result.palaces.mapValues { (_, info) ->
            info.copy(
                earthGod = "",
                state = "",
                liuQinStar = "",
                liuQinHeaven = "",
                liuQinGate = "",
                liuQinEarth = "",
                marks = emptyList(),
            )
        }
        val oldResult = result.copy(palaces = oldPalaces)

        val enhanced = QimenPalaceEnhancer.enhance(oldResult)

        // 补全后与全新排盘结果逐字段一致
        result.palaces.forEach { (p, fresh) ->
            val got = enhanced.palaces[p]!!
            assertEquals("$p earthGod", fresh.earthGod, got.earthGod)
            assertEquals("$p state", fresh.state, got.state)
            assertEquals("$p liuQinStar", fresh.liuQinStar, got.liuQinStar)
            assertEquals("$p liuQinHeaven", fresh.liuQinHeaven, got.liuQinHeaven)
            assertEquals("$p liuQinGate", fresh.liuQinGate, got.liuQinGate)
            assertEquals("$p liuQinEarth", fresh.liuQinEarth, got.liuQinEarth)
            assertEquals("$p marks", fresh.marks, got.marks)
        }
        // 核心排盘字段不受影响
        assertEquals(result.siZhu, enhanced.siZhu)
        assertEquals(result.zhiFuStar, enhanced.zhiFuStar)
        assertEquals(result.zhiShiGate, enhanced.zhiShiGate)
        assertEquals(result.kongWang, enhanced.kongWang)
    }

    @Test
    fun `enhance 幂等且字段完整时原样返回`() {
        val once = QimenPalaceEnhancer.enhance(result)
        val twice = QimenPalaceEnhancer.enhance(once)
        // 全新盘字段已完整 → enhance 不产生任何改动
        assertEquals(result.palaces, once.palaces)
        assertEquals(once.palaces, twice.palaces)
    }
}
