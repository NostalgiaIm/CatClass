package com.catclass.data.mapper

import com.catclass.data.local.entity.CourseDraftEntity
import com.catclass.data.local.entity.CourseEntity
import com.catclass.data.local.entity.TimetableSpaceEntity
import com.catclass.domain.model.CourseDraft
import com.catclass.domain.model.CourseFormState
import com.catclass.domain.model.CourseInstance
import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.model.TimetableSpace

// 数据映射器：负责数据库实体与领域模型之间的转换
fun TimetableSpaceEntity.toDomain(): TimetableSpace = TimetableSpace(
    id = id,
    name = name,
)

fun CourseEntity.toDomain(): CourseTemplate = CourseTemplate(
    id = id,
    title = title,
    teacher = teacher,
    location = location,
    color = color,
    rules = emptyList(),
)

fun CourseDraftEntity.toDomain(): CourseDraft = CourseDraft(
    id = id,
    title = title,
    teacher = teacher,
    location = location,
    dayOfWeek = dayOfWeek,
    startPeriod = startPeriod,
    periodCount = periodCount,
    confidence = confidence,
)

fun CourseFormState.toEntity(spaceId: String, id: String): CourseEntity = CourseEntity(
    id = id,
    title = title,
    teacher = teacher.ifBlank { null },
    location = location.ifBlank { null },
    color = 0xFF2563EBL,
    dayOfWeek = dayOfWeek,
    startPeriod = startPeriod,
    periodCount = periodCount,
    spaceId = spaceId,
)

fun ImageImportJob.toDraftEntities(): List<CourseDraftEntity> = drafts.map { draft ->
    CourseDraftEntity(
        id = draft.id,
        jobId = id,
        title = draft.title,
        teacher = draft.teacher,
        location = draft.location,
        dayOfWeek = draft.dayOfWeek,
        startPeriod = draft.startPeriod,
        periodCount = draft.periodCount,
        confidence = draft.confidence,
    )
}
