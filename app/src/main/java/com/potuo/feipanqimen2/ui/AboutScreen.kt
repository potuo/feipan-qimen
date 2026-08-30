package com.potuo.feipanqimen2.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.potuo.feipanqimen2.ui.theme.CardShape
import com.potuo.feipanqimen2.ui.theme.QimenDimens

/** 关于：作者 / GitHub / 开源协议 / 排盘口径 */
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        }.getOrDefault("?")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(QimenDimens.spacingLg),
        verticalArrangement = Arrangement.spacedBy(QimenDimens.spacingMd),
    ) {
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                Text("飞盘奇门遁甲排盘", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                Text(
                    "鸣法体系 · 值使飞宫法\n天禽居中 · 符使入中 · 星门顺飞",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(QimenDimens.spacingSm))
                Text("版本 v$versionName", style = MaterialTheme.typography.bodySmall)
                Text("作者：Potuo", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                Text("排盘口径", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(QimenDimens.spacingXs))
                Text(
                    "排盘规则依据《奇门基础资料 2023版教》（鸣法/飞盘体系）：符头定元、九星八门洛书顺飞、天禽居中、值使门飞宫法、暗干支飞宫法。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(QimenDimens.spacingLg)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/potuo/feipan-qimen")),
                                )
                            }
                        },
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "GitHub：potuo/feipan-qimen ↗",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = QimenDimens.spacingMd))
                Text("MIT License", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
