package com.potuo.feipanqimen2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens

private data class VolumeInfo(
    val title: String,
    val fileName: String,
    val summary: String,
)

private val VOLUMES = listOf(
    VolumeInfo("第一卷 · 数术基础", "vol1.txt", "河图洛书 / 先后天八卦 / 八卦万物类象 / 阴阳五行 / 天干地支 / 六十甲子"),
    VolumeInfo("第二卷 · 奇门排盘", "vol2.txt", "定阴阳局 / 符头定元 / 布地盘天盘 / 九神九星八门 / 暗干支 / 置闰拆补"),
    VolumeInfo("第三卷 · 占断法则", "vol3.txt", "六仪击刑 / 正格辅格 / 守门九遁 / 三诈五假 / 六亲断法"),
)

/** 飞盘总纲：教材《奇门基础资料 2023版教》分卷阅读 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(onBack: () -> Unit) {
    var currentVolume by remember { mutableStateOf<VolumeInfo?>(null) }

    if (currentVolume == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(QimenDimens.spacingLg),
            verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
        ) {
            Text(
                "据《奇门基础资料 2023版教》鸣法体系",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "本栏目收录排盘所依据的教材原文，按卷阅读。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VOLUMES.forEach { vol ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currentVolume = vol },
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                        Text(vol.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.padding(top = QimenDimens.spacingXs))
                        Text(
                            vol.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    } else {
        val vol = currentVolume!!
        val context = LocalContext.current
        val content = remember(vol.fileName) {
            runCatching {
                context.assets.open("qimen_book/${vol.fileName}").bufferedReader().use { it.readText() }
            }.getOrDefault("（未能加载教材内容）")
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = QimenDimens.spacingSm, vertical = QimenDimens.spacingXs),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                IconButton(onClick = { currentVolume = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text(vol.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider()
            Text(
                content,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(QimenDimens.spacingLg),
            )
        }
    }
}
