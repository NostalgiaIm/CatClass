package com.catclass.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 图片识别草稿实体：用于保存识别结果和待校对信息
@Entity(tableName = "course_drafts")
data class CourseDraftEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val title: String,
    val teacher: String?,
    val location: String?,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val periodCount: Int,
    val confidence: Float,
    val selected: Boolean = false,
)
