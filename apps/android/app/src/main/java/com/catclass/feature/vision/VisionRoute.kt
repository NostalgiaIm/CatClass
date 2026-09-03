package com.catclass.feature.vision

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catclass.domain.model.VisionJobStatus

// 图片课表识别页：串联“选择图片 -> OCR 识别 -> 人工校对 -> 提交导入”的完整链路
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionRoute(viewModel: VisionViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.updateSourceUri(it.toString()) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("图片识别") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.sourceUri,
                onValueChange = viewModel::updateSourceUri,
                label = { Text("图片路径 / Uri") },
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !state.loading,
                ) {
                    Text("选择图片")
                }
                Button(
                    onClick = { viewModel.importImage() },
                    enabled = !state.loading,
                ) {
                    Text("开始识别")
                }
                Button(
                    onClick = { viewModel.commit() },
                    enabled = !state.loading && state.job?.status == VisionJobStatus.Reviewing,
                ) {
                    Text("导入选中草稿")
                }
            }

            if (state.loading) {
                Text("处理中...", color = MaterialTheme.colorScheme.primary)
            }

            state.errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }
            state.resultMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            state.job?.let { job ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("识别结果", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("来源：${job.sourceUri}")
                        Text("状态：${job.status}")
                        Text("草稿数量：${job.drafts.size}")
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(job.drafts) { draft ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = state.selectedDraftIds.contains(draft.id),
                                    onCheckedChange = { viewModel.toggleDraftSelection(draft) },
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(draft.title, style = MaterialTheme.typography.titleMedium)
                                    Text("星期 ${draft.dayOfWeek} · 第 ${draft.startPeriod} 节")
                                    Text("地点：${draft.location ?: "待补"}")
                                    Text("置信度：${"%.0f".format(draft.confidence * 100)}%")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
