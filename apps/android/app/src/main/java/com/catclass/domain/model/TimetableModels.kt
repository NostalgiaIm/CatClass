package com.catclass.domain.model

// 领域模型遵循“共享契约优先”：字段命名尽量贴近 packages/contracts。

data class TimetableSpace(
    val id: String,
    val name: String,
    val activeWeek: Int = 1,
    val weekStartDay: Int = 1,
)

data class Period(
    val index: Int,
    val label: String,
    val startTime: String,
    val endTime: String,
)

data class CourseTemplate(
    val id: String,
    val title: String,
    val teacher: String? = null,
    val location: String? = null,
    val color: Long,
    val rules: List<ScheduleRule>,
)

data class ScheduleRule(
    val id: String,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val periodCount: Int,
    val weekMode: WeekMode = WeekMode.All,
    val weekSet: Set<Int> = emptySet(),
)

enum class WeekMode {
    All,
    Odd,
    Even,
    Custom,
}

data class CourseInstance(
    val id: String,
    val courseId: String,
    val title: String,
    val teacher: String?,
    val location: String?,
    val color: Long,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val periodCount: Int,
)

data class CourseDraft(
    val id: String,
    val title: String,
    val teacher: String?,
    val location: String?,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val periodCount: Int,
    val confidence: Float,
)

data class ImageImportJob(
    val id: String,
    val sourceUri: String,
    val status: VisionJobStatus,
    val drafts: List<CourseDraft>,
)

enum class VisionJobStatus {
    Pending,
    Processing,
    Reviewing,
    Committed,
    Failed,
}

data class CourseFormState(
    val title: String = "",
    val teacher: String = "",
    val location: String = "",
    val dayOfWeek: Int = 1,
    val startPeriod: Int = 1,
    val periodCount: Int = 2,
)
