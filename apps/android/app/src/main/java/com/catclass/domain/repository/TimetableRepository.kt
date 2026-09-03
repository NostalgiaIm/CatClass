package com.catclass.domain.repository

import com.catclass.domain.model.CourseFormState
import com.catclass.domain.model.CourseInstance
import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.model.TimetableSpace
import kotlinx.coroutines.flow.Flow

// 领域层仓库接口：数据来源可替换，但领域层不关心实现
interface TimetableRepository {
    fun observeSpaces(): Flow<List<TimetableSpace>>
    fun observeCourses(): Flow<List<CourseTemplate>>
    suspend fun listWeekInstances(weekIndex: Int): List<CourseInstance>
    suspend fun addCourse(form: CourseFormState): CourseTemplate
    suspend fun importDrafts(job: ImageImportJob): List<CourseTemplate>
}
