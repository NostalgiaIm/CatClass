package com.catclass.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 设置页：当前先提供本地开关，后续可接入 DataStore 持久化
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute() {
    var compactMode by remember { mutableStateOf(false) }
    var showWeekend by remember { mutableStateOf(true) }
    var syncEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingCard(
                title = "紧凑模式",
                subtitle = "适合课表信息较多的场景",
                checked = compactMode,
                onCheckedChange = { compactMode = it },
            )
            SettingCard(
                title = "显示周末",
                subtitle = "控制课表视图中是否展示周六和周日",
                checked = showWeekend,
                onCheckedChange = { showWeekend = it },
            )
            SettingCard(
                title = "同步服务",
                subtitle = "预留同步入口，后续可接入云端或局域网同步",
                checked = syncEnabled,
                onCheckedChange = { syncEnabled = it },
            )
            Text(
                text = "这些开关当前只保存在页面状态中，后续会接入 DataStore 和真正的同步配置。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
