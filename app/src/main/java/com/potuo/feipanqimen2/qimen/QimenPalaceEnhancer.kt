package com.potuo.feipanqimen2.qimen

/**
 * 宫格注解计算（与排盘核心解耦）：
 * 六亲（以时干为「我」）、旺衰状态（月令旺衰）、宫角标记（马/迫/刑/墓）。
 * 纯函数，无状态，便于单测。
 */
object QimenPalaceEnhancer {

    /** 节令（十二节）-> 月令五行：立春寅木、惊蛰卯木、清明辰土、立夏巳火、芒种午火、
     *  小暑未土、立秋申金、白露酉金、寒露戌土、立冬亥水、大雪子水、小寒丑土 */
    fun monthElementOf(jieQi: String): String =
        QimenConstants.JIE_QI_MONTH_ELEMENT[jieQi] ?: "土"

    /**
     * 六亲取法（以时干为「我」）：生我=父母、克我=官鬼、我克=妻财、我生=子孙、比和=兄弟。
     * @param element 元素五行（星/天盘干/门/地盘干）
     * @param woStem 时干
     */
    fun liuQinOf(element: String?, woStem: String): String {
        if (element.isNullOrEmpty()) return ""
        val wo = QimenConstants.STEM_ELEMENT[woStem] ?: return ""
        return when {
            QimenConstants.ELEMENT_GENERATES[element] == wo -> "父母"
            QimenConstants.ELEMENT_CONTROLS[element] == wo -> "官鬼"
            QimenConstants.ELEMENT_CONTROLS[wo] == element -> "妻财"
            QimenConstants.ELEMENT_GENERATES[wo] == element -> "子孙"
            else -> "兄弟"
        }
    }

    /**
     * 旺衰状态（月令旺衰）：以天盘干五行对月令五行论——当令者旺、令生者相、生令者休、克令者囚、令克者死。
     * 注：参考盘含 废/没/胎 8 态，判定口径待校准（需参考盘多组数据反推），当前以 5 态兜底。
     */
    fun wangShuaiState(heavenStem: String, monthElement: String): String {
        val el = QimenConstants.STEM_ELEMENT[heavenStem] ?: return ""
        return when {
            el == monthElement -> "旺"
            QimenConstants.ELEMENT_GENERATES[monthElement] == el -> "相"
            QimenConstants.ELEMENT_GENERATES[el] == monthElement -> "休"
            QimenConstants.ELEMENT_CONTROLS[el] == monthElement -> "囚"
            else -> "死"
        }
    }

    /**
     * 宫角标记：
     * - 马：时支驿马落宫（绿色马字）
     * - 迫：门克宫（凶门克宫，且无刑墓时显示）
     * - 刑：天盘干犯六仪击刑本宫
     * - 墓：天盘干入墓本宫
     */
    fun buildMarks(
        palace: Int,
        gate: String,
        heavenStem: String,
        hourPillar: String,
    ): List<String> {
        val marks = mutableListOf<String>()

        // 马：时支三合驿马（如戌时 -> 马在申 -> 坤2）
        val hourBranch = hourPillar.getOrNull(1)?.toString() ?: ""
        val yiMaBranch = QimenConstants.YI_MA[hourBranch] ?: ""
        if (yiMaBranch.isNotEmpty() && yiMaBranch in (QimenConstants.PALACE_BRANCHES[palace] ?: emptyList())) {
            marks.add("马")
        }

        // 墓：天盘干入本宫墓
        if (QimenConstants.STEM_TOMB[heavenStem] == palace) marks.add("墓")

        // 刑：天盘干六仪击刑落本宫
        if (QimenConstants.JI_XING[heavenStem] == palace) marks.add("刑")

        // 迫：门克宫（中门不论），已有刑/墓时不再标迫
        if (gate.isNotEmpty() && gate != "中" && marks.none { it == "刑" || it == "墓" }) {
            val gateEl = QimenConstants.GATE_ELEMENT[gate] ?: ""
            val palaceEl = QimenConstants.PALACE_ELEMENT[palace] ?: ""
            if (gateEl.isNotEmpty() && QimenConstants.ELEMENT_CONTROLS[gateEl] == palaceEl) {
                marks.add("迫")
            }
        }

        return marks
    }
}
