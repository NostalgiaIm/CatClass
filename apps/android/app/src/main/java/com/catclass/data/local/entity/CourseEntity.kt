package com.catclass.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 本地课程实体：保持扁平，便于 Room 存取
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val teacher: String?,
    val location: String?,
    val color: Long,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val periodCount: Int,
    val spaceId: String,
)
