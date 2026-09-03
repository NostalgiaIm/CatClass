package com.catclass.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// 课程编辑页：以表单方式创建或修改课程
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditorRoute(viewModel: CourseEditorViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = { TopAppBar(title = { Text("课程编辑") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("课程基础信息", style = MaterialTheme.typography.titleMedium)
                    LabeledTextField(
                        label = "课程名称",
                        value = state.form.title,
                        onValueChange = viewModel::updateTitle,
                    )
                    LabeledTextField(
                        label = "教师",
                        value = state.form.teacher,
                        onValueChange = viewModel::updateTeacher,
                    )
                    LabeledTextField(
                        label = "地点",
                        value = state.form.location,
                        onValueChange = viewModel::updateLocation,
                    )
                    LabeledIntField(
                        label = "星期几",
                        value = state.form.dayOfWeek,
                        onValueChange = viewModel::updateDay,
                    )
                    LabeledIntField(
                        label = "开始节次",
                        value = state.form.startPeriod,
                        onValueChange = viewModel::updateStartPeriod,
                    )
                    LabeledIntField(
                        label = "持续节数",
                        value = state.form.periodCount,
                        onValueChange = viewModel::updatePeriodCount,
                    )
                }
            }

            Button(
                onClick = { viewModel.save() },
                enabled = !state.saving,
            ) {
                Text(if (state.saving) "保存中..." else "保存课程")
            }

            state.message?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
    )
}

@Composable
private fun LabeledIntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        value = value.toString(),
        onValueChange = { text ->
            text.toIntOrNull()?.let(onValueChange)
        },
        singleLine = true,
    )
}
