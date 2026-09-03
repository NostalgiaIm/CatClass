package com.catclass.feature.timetable

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catclass.domain.model.CourseInstance

// 课表页：展示周课表概览、课程列表和快速新增入口
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableRoute(viewModel: TimetableViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课表") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.selectedSpaceName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("当前周次：第 ${state.activeWeek} 周")
                    Text("课程数量：${state.courses.size}")
                }
            }

            state.message?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.changeWeek(-1) }) {
                    Text("上一周")
                }
                OutlinedButton(onClick = { viewModel.changeWeek(1) }) {
                    Text("下一周")
                }
                Button(onClick = { viewModel.addQuickCourse() }) {
                    Text("示例课程")
                }
            }

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("周视图", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TimetableGrid(courses = state.weekInstances)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("本周课程", style = MaterialTheme.typography.titleMedium)
                }
                if (state.weekInstances.isEmpty()) {
                    item {
                        EmptyStateCard("本周还没有课程")
                    }
                } else {
                    items(state.weekInstances) { course ->
                        CourseInstanceCard(course)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("全部课程", style = MaterialTheme.typography.titleMedium)
                }
                if (state.courses.isEmpty()) {
                    item {
                        EmptyStateCard("当前课表还没有录入任何课程")
                    }
                } else {
                    items(state.courses) { course ->
                        CourseTemplateCard(course.title, course.teacher, course.location, course.color)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseInstanceCard(course: CourseInstance) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text("星期 ${course.dayOfWeek} · 第 ${course.startPeriod} 节起 · ${course.periodCount} 节")
            Text(course.teacher ?: "教师待补")
            Text(course.location ?: "地点待补")
        }
    }
}

@Composable
private fun CourseTemplateCard(
    title: String,
    teacher: String?,
    location: String?,
    color: Long,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(teacher?.takeIf { it.isNotBlank() } ?: "教师待补")
            Text(location?.takeIf { it.isNotBlank() } ?: "地点待补")
            Text("颜色：#${color.toString(16).uppercase()}")
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(message)
        }
    }
}
