package com.catclass.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catclass.domain.model.CourseInstance

private val weekDayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private const val periodCount = 8

// 以固定高度单元格呈现周课表。课程跨节时仅在起始节显示名称，后续节保持同色块。
@Composable
fun TimetableGrid(courses: List<CourseInstance>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            GridHeaderCell("节次", modifier = Modifier.width(52.dp))
            weekDayLabels.forEach { day ->
                GridHeaderCell(day, modifier = Modifier.width(92.dp))
            }
        }

        (1..periodCount).forEach { period ->
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                GridHeaderCell("第 $period 节", modifier = Modifier.width(52.dp))
                (1..weekDayLabels.size).forEach { dayOfWeek ->
                    val startingCourse = courses.firstOrNull {
                        it.dayOfWeek == dayOfWeek && it.startPeriod == period
                    }
                    val continuingCourse = courses.firstOrNull {
                        it.dayOfWeek == dayOfWeek &&
                            period in (it.startPeriod + 1) until (it.startPeriod + it.periodCount)
                    }
                    CourseGridCell(
                        course = startingCourse,
                        continuesCourse = continuingCourse != null,
                    )
                }
            }
        }
    }
}

@Composable
private fun GridHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CourseGridCell(
    course: CourseInstance?,
    continuesCourse: Boolean,
) {
    val hasCourse = course != null || continuesCourse
    val backgroundColor = when {
        course != null -> Color(course.color)
        continuesCourse -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .width(92.dp)
            .height(64.dp)
            .background(backgroundColor)
            .padding(6.dp),
    ) {
        course?.let {
            Text(
                text = it.title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!hasCourse) {
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
