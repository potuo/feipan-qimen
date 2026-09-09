package com.potuo.feipanqimen2.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 以用途命名的圆角入口，组件应优先使用这里的语义名称。 */
object QimenShapeTokens {
    val card: CornerBasedShape = RoundedCornerShape(16.dp)
    val input: CornerBasedShape = RoundedCornerShape(12.dp)
    val dialog: CornerBasedShape = RoundedCornerShape(20.dp)
    val palace: CornerBasedShape = RoundedCornerShape(12.dp)
    val badge: CornerBasedShape = RoundedCornerShape(percent = 50)
    val accentMark: CornerBasedShape = RoundedCornerShape(2.dp)
}

val QimenShapes = Shapes(
    extraSmall = QimenShapeTokens.badge,
    small = QimenShapeTokens.palace,
    medium = QimenShapeTokens.input,
    large = QimenShapeTokens.card,
    extraLarge = QimenShapeTokens.dialog,
)

// 对外兼容名称；新组件使用 QimenShapeTokens 的语义入口。
val CardShape: CornerBasedShape = QimenShapeTokens.card
val PalaceShape: CornerBasedShape = QimenShapeTokens.palace
