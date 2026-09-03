package com.catclass.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catclass.data.local.entity.CourseDraftEntity
import com.catclass.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

// 课程 DAO：这里先放查询入口，后续再补完整写操作
@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses")
    suspend fun listAll(): List<CourseEntity>

    @Query("SELECT * FROM course_drafts WHERE jobId = :jobId")
    suspend fun listDrafts(jobId: String): List<CourseDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourses(items: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDrafts(items: List<CourseDraftEntity>)
}
