package com.potuo.feipanqimen2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.ui.components.HuangLiCard
import com.potuo.feipanqimen2.ui.components.QimenBoard
import com.potuo.feipanqimen2.ui.components.QimenButton
import com.potuo.feipanqimen2.ui.components.QimenTopBar
import com.potuo.feipanqimen2.ui.components.SealBadge
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens
import com.potuo.feipanqimen2.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val result by viewModel.qimenResult.collectAsState()
    val huangLi by viewModel.huangLi.collectAsState()
    val tags by viewModel.tags.collectAsState()
    var huangLiExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    if (result == null) {
        Text("无排盘结果", modifier = Modifier.padding(QimenDimens.spacingLg))
        return
    }

    val r = result!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = CardShape,
            ) {
                Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                    Text(r.siZhu, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                    Text("${r.jieQi} · ${r.yuan} · ${r.dunType}${r.juNumber}局")
                    Text("旬首：${r.xunShou}（遁${r.xunShouStem}）· 空亡：${r.kongWang}")
                    Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SealBadge(text = "符")
                        Text(
                            "${r.zhiFuStar}·${QimenConstants.PALACE_NAMES[r.zhiFuPalace]}${r.zhiFuPalace}宫",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SealBadge(text = "使")
                        Text(
                            "${r.zhiShiGate}门·${QimenConstants.PALACE_NAMES[r.zhiShiPalace]}${r.zhiShiPalace}宫",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            QimenBoard(result = r, animate = true)

            huangLi?.let {
                HuangLiCard(
                    summary = it.summary,
                    expanded = huangLiExpanded,
                    onToggle = { huangLiExpanded = !huangLiExpanded },
                )
            }

            OutlinedTextField(
                value = tags,
                onValueChange = viewModel::setTags,
                label = { Text("标签（逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
            )

            QimenButton(
                onClick = { viewModel.saveCase() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("保存案例")
            }
    }
}
