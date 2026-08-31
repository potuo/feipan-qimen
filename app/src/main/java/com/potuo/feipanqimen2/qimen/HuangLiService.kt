package com.potuo.feipanqimen2.qimen

import com.nlf.calendar.Solar
import java.time.LocalDateTime

data class HuangLiInfo(
    val lunarDate: String,
    val shengXiao: String,
    val yi: String,
    val ji: String,
    val chongSha: String,
    val pengZu: String,
    val jiShen: String,
    val xiongSha: String,
    val jieQi: String,
    val festival: String,
    val xiu: String,
    val summary: String,
)

object HuangLiService {
    fun getHuangLi(dateTime: LocalDateTime): HuangLiInfo {
        val solar = Solar.fromYmdHms(
            dateTime.year, dateTime.monthValue, dateTime.dayOfMonth,
            dateTime.hour, dateTime.minute, 0,
        )
        val lunar = solar.lunar

        val lunarDate = "农历${lunar.monthInChinese}月${lunar.dayInChinese}"
        val shengXiao = lunar.yearShengXiao
        val yi = lunar.dayYi.joinToString(" ")
        val ji = lunar.dayJi.joinToString(" ")
        val chongSha = "冲${lunar.dayChongDesc} 煞${lunar.daySha}"
        val pengZu = "${lunar.pengZuGan} ${lunar.pengZuZhi}"
        val jiShen = lunar.dayJiShen.joinToString(" ")
        val xiongSha = lunar.dayXiongSha.joinToString(" ")
        val jieQi = lunar.jieQi ?: ""
        val festival = buildList {
            solar.festivals?.let { addAll(it) }      // 公历节日（国庆、劳动节、儿童节等）
            lunar.festivals?.let { addAll(it) }      // 农历节日（春节、中秋、端午等）
            lunar.otherFestivals?.let { addAll(it) } // 其他农历节日（寒食、腊八等）
        }.joinToString(" ")
        val xiu = "${lunar.xiu}${lunar.zheng}${lunar.animal}"

        val summary = buildString {
            append(lunarDate)
            append(" · ")
            append(shengXiao)
            if (yi.isNotBlank()) append("\n宜：$yi")
            if (ji.isNotBlank()) append("\n忌：$ji")
            append("\n$chongSha")
        }

        return HuangLiInfo(
            lunarDate = lunarDate,
            shengXiao = shengXiao,
            yi = yi,
            ji = ji,
            chongSha = chongSha,
            pengZu = pengZu,
            jiShen = jiShen,
            xiongSha = xiongSha,
            jieQi = jieQi,
            festival = festival,
            xiu = xiu,
            summary = summary,
        )
    }
}
