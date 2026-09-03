package com.catclass.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 本地课表空间实体：作为 Room 的基础表
@Entity(tableName = "timetable_spaces")
data class TimetableSpaceEntity(
    @PrimaryKey val id: String,
    val name: String,
)
