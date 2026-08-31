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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
                            result = result,
                            dark = dark,
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
    result: QimenResult,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalQimenPalette.current
    val bgColor = when {
        dark -> palette.palaceBg
        else -> palette.paper.copy(alpha = 0.55f)
    }
    val textColor = palette.inkText
    val subColor = palette.secondaryText      // 神/六亲/地盘神（次级灰，同分享图 cellSub）
    val stemColor = palette.gold            // 天干地支/角标（棕金）
    val red = palette.cinnabar              // 朱砂（值符星/值使门/日时干/地盘值符）

    // 值符宫（值符星落宫）/ 值使宫（值使门落宫）：朱砂描边强调（中宫同普通灰边，以分享图样式为准）
    val isSpecial = info.star == result.zhiFuStar || info.gate == result.zhiShiGate
    val borderColor = when {
        isSpecial -> palette.cinnabar
        else -> palette.gridBorder.copy(alpha = if (dark) 0.6f else 0.5f)
    }
    val borderWidth = if (isSpecial) 1.5.dp else QimenDimens.gridBorder

    // 红色判定
    val starRed = info.star == result.zhiFuStar
    val gateRed = info.gate == result.zhiShiGate
    val heavenRed = info.heavenStem.isNotEmpty() &&
        (info.heavenStem == result.dayPillar.first().toString() ||
            info.heavenStem == result.hourPillar.first().toString())
    val earthGodRed = info.earthGod == "值符"

    // 暗干支拆分：干 → 星行左，支 → 门行左
    val hidden = info.hiddenStem.orEmpty()
    val hiddenGan = hidden.firstOrNull()?.toString() ?: ""
    val hiddenZhi = hidden.lastOrNull()?.toString() ?: ""

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
    val glowColor = palette.cinnabar.copy(alpha = 0.5f * glow)
    val actualBorder = if (isSpecial && glow > 0.01f) glowColor else borderColor

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor, PalaceShape)
            .border(borderWidth + glowWidth, actualBorder, PalaceShape)
            .padding(3.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── 行1：宫名（左）＋ 天盘神（中）＋ 角标（右上：马/迫/刑/墓棕）──
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${info.direction}${info.palace}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = subColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp),
                )
                Text(
                    info.god,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    info.marks.forEach { m ->
                        Text(
                            m,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = stemColor,
                        )
                    }
                }
            }

            // ── 行2：星行 = 暗干(棕,左列) 星(黑/红,中列) 天盘干(黑/红,右列)（三列对齐，同分享图）──
            Box(modifier = Modifier.fillMaxWidth()) {
                if (hiddenGan.isNotEmpty()) {
                    Text(
                        hiddenGan,
                        fontSize = 10.sp,
                        color = stemColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 3.dp),
                    )
                }
                Text(
                    info.star,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (starRed) red else textColor,
                    modifier = Modifier.align(Alignment.Center),
                )
                if (info.heavenStem.isNotEmpty()) {
                    Text(
                        info.heavenStem,
                        fontSize = 10.sp,
                        color = if (heavenRed) red else textColor,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 3.dp),
                    )
                }
            }

            // ── 行3：六亲 = 星六亲(左列) + 天盘干六亲(右列)（小字）──
            if (info.liuQinStar.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        info.liuQinStar,
                        fontSize = 7.sp,
                        color = subColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp),
                    )
                    Text(
                        info.liuQinHeaven,
                        fontSize = 7.sp,
                        color = subColor,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                    )
                }
            }

            // ── 行4：门行 = 暗支(棕,左列) 门(黑/红,中列) 地盘干(黑,右列)──
            Box(modifier = Modifier.fillMaxWidth()) {
                if (hiddenZhi.isNotEmpty()) {
                    Text(
                        hiddenZhi,
                        fontSize = 10.sp,
                        color = stemColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 3.dp),
                    )
                }
                Text(
                    info.gate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gateRed) red else textColor,
                    modifier = Modifier.align(Alignment.Center),
                )
                if (info.earthStem.isNotEmpty()) {
                    Text(
                        info.earthStem,
                        fontSize = 10.sp,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 3.dp),
                    )
                }
            }

            // ── 行5：六亲 = 门六亲(左列) + 地盘干六亲(右列)（小字）──
            if (info.liuQinGate.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        info.liuQinGate,
                        fontSize = 7.sp,
                        color = subColor,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp),
                    )
                    Text(
                        info.liuQinEarth,
                        fontSize = 7.sp,
                        color = subColor,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp),
                    )
                }
            }

            // ── 行6：地盘神（左，值符红）+ 状态（右，棕）──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (info.earthGod.isNotEmpty()) {
                    Text(
                        info.earthGod,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (earthGodRed) red else subColor,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (info.state.isNotEmpty()) {
                    Text(
                        info.state,
                        fontSize = 8.sp,
                        color = stemColor,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
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
