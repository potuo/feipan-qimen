package com.potuo.feipanqimen2.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.potuo.feipanqimen2.qimen.PalaceInfo
import com.potuo.feipanqimen2.qimen.QimenResult
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.LocalQimenPalette
import com.potuo.feipanqimen2.ui.theme.PalaceShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import kotlinx.coroutines.delay

private val gridOrder = listOf(4, 9, 2, 3, 5, 7, 8, 1, 6)

/** 动画出场顺序：值符宫最先 → 值使宫 → 其余按洛书序 */
private fun animationOrder(zhiFu: Int, zhiShi: Int): List<Int> = buildList {
    add(zhiFu)
    if (zhiShi != zhiFu) add(zhiShi)
    gridOrder.forEach { if (it != zhiFu && it != zhiShi) add(it) }
}

@Composable
private fun systemAnimationsOff(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

@Composable
fun QimenBoard(
    result: QimenResult,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val dark = isSystemInDarkTheme()
    val reduceMotion = systemAnimationsOff()
    val order = remember(result) { animationOrder(result.zhiFuPalace, result.zhiShiPalace) }
    var shownSet by remember {
        mutableStateOf(if (!animate || reduceMotion) gridOrder.toSet() else emptySet())
    }

    LaunchedEffect(animate, reduceMotion, result) {
        if (!animate || reduceMotion) {
            shownSet = gridOrder.toSet()
            return@LaunchedEffect
        }
        shownSet = emptySet()
        order.forEachIndexed { index, palace ->
            if (index > 0) delay(90)
            shownSet = shownSet + palace
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(QimenDimens.gridBorder, LocalQimenPalette.current.gold.copy(alpha = 0.6f), PalaceShape)
            .padding(QimenDimens.spacingXs),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.gridGap),
    ) {
        gridOrder.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QimenDimens.gridGap),
            ) {
                row.forEach { palaceNum ->
                    val info = result.palaces[palaceNum]!!
                    val isVisible = palaceNum in shownSet
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                            ) +
                            scaleIn(
                                initialScale = 0.94f,
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                            ),
                        modifier = Modifier.weight(1f),
                    ) {
                        PalaceCell(
                            info = info,
                            dark = dark,
                            isCenter = palaceNum == 5,
                            isZhiFu = palaceNum == result.zhiFuPalace,
                            isZhiShi = palaceNum == result.zhiShiPalace,
                        )
                    }
                    if (!isVisible) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PalaceCell(
    info: PalaceInfo,
    dark: Boolean,
    isCenter: Boolean,
    isZhiFu: Boolean,
    isZhiShi: Boolean,
    modifier: Modifier = Modifier,
) {
    val isSpecial = isZhiFu || isZhiShi
    val palette = LocalQimenPalette.current
    val borderColor = when {
        isCenter -> palette.gold
        isSpecial -> palette.cinnabar
        else -> palette.gridBorder.copy(alpha = if (dark) 0.6f else 0.5f)
    }
    val borderWidth = when {
        isCenter || isSpecial -> 1.5.dp
        else -> QimenDimens.gridBorder
    }
    val bgColor = when {
        isCenter -> palette.centerBg
        dark -> palette.palaceBg
        else -> palette.paper.copy(alpha = 0.55f)
    }
    val textColor = palette.inkText

    // 值符/值使宫：入场后朱砂光晕扩散一次
    var glowStage by remember { mutableStateOf(0) }
    LaunchedEffect(isSpecial) {
        if (isSpecial) {
            glowStage = 0
            delay(260)
            glowStage = 1
            delay(420)
            glowStage = 2
        }
    }
    val glow by animateFloatAsState(
        targetValue = when (glowStage) {
            1 -> 1f
            else -> 0f
        },
        animationSpec = tween(if (glowStage == 1) 420 else 720, easing = FastOutSlowInEasing),
        label = "zhiGlow",
    )
    val glowWidth = 2.5.dp * glow
    val glowColor = LocalQimenPalette.current.cinnabar.copy(alpha = 0.5f * glow)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, PalaceShape)
            .border(borderWidth + glowWidth, if (glow > 0.01f) glowColor else borderColor, PalaceShape)
            .padding(QimenDimens.spacingXs),
    ) {
        Text(
            text = "${info.direction}${info.palace}",
            fontSize = 7.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.55f),
            modifier = Modifier.align(Alignment.TopStart),
        )

        if (isZhiFu || isZhiShi) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (isZhiFu) SealBadge(text = "符")
                if (isZhiShi) SealBadge(text = "使")
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                info.god,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalQimenPalette.current.secondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
            )
            if (isCenter) {
                // 中宫：金色圆形天禽标记
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(1.dp, LocalQimenPalette.current.gold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        info.star,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    info.heavenStem,
                    fontSize = 8.sp,
                    color = LocalQimenPalette.current.gold,
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp,
                )
            } else {
                Text(
                    "${info.star}${info.heavenStem}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                )
            }
            Text(
                info.gate,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
            )
            info.hiddenStem?.let {
                Text(
                    it,
                    fontSize = 7.sp,
                    color = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp,
                )
            }
            Text(
                "(${info.earthStem})",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = LocalQimenPalette.current.gold,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
            )
        }
    }
}

/** 迷你盘面缩略图（案例卡片用）：3×3 只显示九星，值符宫朱砂底 */
@Composable
fun MiniBoard(
    panJson: String,
    modifier: Modifier = Modifier,
) {
    val result = remember(panJson) {
        runCatching { Gson().fromJson(panJson, QimenResult::class.java) }.getOrNull()
    } ?: return

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .border(0.5.dp, LocalQimenPalette.current.gold.copy(alpha = 0.4f), PalaceShape)
            .padding(2.dp),
    ) {
        gridOrder.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                row.forEach { palaceNum ->
                    val info = result.palaces[palaceNum]!!
                    val isZhiFu = palaceNum == result.zhiFuPalace
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isZhiFu) LocalQimenPalette.current.cinnabar.copy(alpha = 0.2f)
                                else Color.Transparent,
                            )
                            .border(0.3.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            info.star.removePrefix("天"),
                            fontSize = 8.sp,
                            fontWeight = if (isZhiFu) FontWeight.Bold else FontWeight.Normal,
                            color = if (isZhiFu) LocalQimenPalette.current.cinnabar
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HuangLiCard(
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = CardShape,
        onClick = onToggle,
    ) {
        Row(modifier = Modifier.padding(QimenDimens.spacingLg)) {
            Box(
                modifier = Modifier
                    .width(QimenDimens.spacingXs)
                    .height(if (expanded) 56.dp else 40.dp)
                    .background(LocalQimenPalette.current.cinnabar, CircleShape),
            )
            Column(modifier = Modifier.padding(start = QimenDimens.spacingMd)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "黄历",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        if (expanded) "收起" else "展开",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Crossfade(targetState = expanded, label = "huangli", animationSpec = tween(250)) { isExpanded ->
                    Text(
                        text = if (isExpanded) summary else summary.lines().take(2).joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = QimenDimens.spacingSm),
                    )
                }
            }
        }
    }
}
