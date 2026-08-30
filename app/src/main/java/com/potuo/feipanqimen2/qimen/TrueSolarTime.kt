package com.potuo.feipanqimen2.qimen

import java.time.LocalDateTime

/**
 * 真太阳时计算（据教材「抽时选局」：以卦师所在地的地方时起卦）。
 * 真太阳时 = 北京时间 + 经度时差(±4分/度) + 均时差(Equation of Time)。
 */
object TrueSolarTime {

    /** 均时差（分钟）：B = 2π(N-81)/365 */
    fun equationOfTime(dayOfYear: Int): Double {
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        return 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b)
    }

    /** 地方真太阳时：北京时间 + (经度-120°)×4 分钟 + 均时差 */
    fun toTrueSolar(dateTime: LocalDateTime, longitude: Double): LocalDateTime {
        val dayOfYear = dateTime.dayOfYear
        val eot = equationOfTime(dayOfYear)
        val offsetMinutes = (longitude - 120.0) * 4.0 + eot
        return dateTime.plusMinutes(Math.round(offsetMinutes).toLong())
    }

    /** 时辰索引（0=子时，23-1点）：(h+1)/2 % 12 */
    fun hourIndex(hour: Int): Int = ((hour + 1) / 2) % 12

    /** 时辰名 */
    fun hourName(hour: Int): String = QimenConstants.HOUR_NAMES[hourIndex(hour)]

    /**
     * 判断北京时与真太阳时是否落在不同时辰。
     * @return 若跨时辰返回「北京时辰 → 真太阳时辰」提示文本，否则 null
     */
    fun crossingHourHint(dateTime: LocalDateTime, longitude: Double): String? {
        val trueSolar = toTrueSolar(dateTime, longitude)
        val beijingIndex = hourIndex(dateTime.hour)
        val trueIndex = hourIndex(trueSolar.hour)
        if (beijingIndex != trueIndex) {
            return "真太阳时已跨时辰：${QimenConstants.HOUR_NAMES[beijingIndex]} → ${QimenConstants.HOUR_NAMES[trueIndex]}（请确认按哪个起盘）"
        }
        return null
    }
}
