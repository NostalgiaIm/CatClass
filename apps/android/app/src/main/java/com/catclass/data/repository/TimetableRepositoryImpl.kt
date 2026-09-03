package com.catclass.data.repository

import com.catclass.data.local.database.CatClassDatabase
import com.catclass.data.local.entity.CourseEntity
import com.catclass.data.mapper.toDomain
import com.catclass.data.mapper.toEntity
import com.catclass.domain.model.CourseFormState
import com.catclass.domain.model.CourseInstance
import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.model.TimetableSpace
import com.catclass.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 仓库实现：以 Room 作为本地唯一数据源，UI 不直接接触 DAO
class TimetableRepositoryImpl(
    private val database: CatClassDatabase,
) : TimetableRepository {
    private val spaceDao = database.timetableSpaceDao()
    private val courseDao = database.courseDao()

    override fun observeSpaces(): Flow<List<TimetableSpace>> {
        return spaceDao.observeAll().map { items -> items.map { it.toDomain() } }
    }

    override fun observeCourses(): Flow<List<CourseTemplate>> {
        return courseDao.observeAll().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun listWeekInstances(weekIndex: Int): List<CourseInstance> {
        // 当前 MVP 的课程默认每周重复；排除单周、双周和自定义周次将在规则表落库后使用 weekIndex。
        return courseDao.listAll().map { course ->
            CourseInstance(
                id = course.id,
                courseId = course.id,
                title = course.title,
                teacher = course.teacher,
                location = course.location,
                color = course.color,
                dayOfWeek = course.dayOfWeek,
                startPeriod = course.startPeriod,
                periodCount = course.periodCount,
            )
        }
    }

    override suspend fun addCourse(form: CourseFormState): CourseTemplate {
        val entity = form.toEntity(spaceId = "default-space", id = java.util.UUID.randomUUID().toString())
        courseDao.upsertCourses(listOf(entity))
        return entity.toDomain()
    }

    override suspend fun importDrafts(job: ImageImportJob): List<CourseTemplate> {
        val entities = job.drafts.map { draft ->
            val entity = CourseEntity(
                id = draft.id,
                title = draft.title,
                teacher = draft.teacher,
                location = draft.location,
                color = 0xFF2563EBL,
                dayOfWeek = draft.dayOfWeek,
                startPeriod = draft.startPeriod,
                periodCount = draft.periodCount,
                spaceId = "default-space",
            )
            entity
        }
        courseDao.upsertCourses(entities)
        return entities.map { it.toDomain() }
    }
}
