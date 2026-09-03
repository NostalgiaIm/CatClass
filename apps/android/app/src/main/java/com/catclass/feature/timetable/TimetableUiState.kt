package com.catclass.feature.timetable

import com.catclass.domain.model.CourseInstance
import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.model.TimetableSpace

// 课表页状态：UI 只依赖这份状态对象，不直接接触 Repository
data class TimetableUiState(
    val spaces: List<TimetableSpace> = emptyList(),
    val courses: List<CourseTemplate> = emptyList(),
    val weekInstances: List<CourseInstance> = emptyList(),
    val activeWeek: Int = 1,
    val selectedSpaceName: String = "默认课表",
    val loading: Boolean = false,
    val message: String? = null,
)

