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
            lunar.festivals?.let { addAll(it) }
            lunar.otherFestivals?.let { addAll(it) }
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
