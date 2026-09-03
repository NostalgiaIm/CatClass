package com.catclass.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.catclass.data.local.dao.CourseDao
import com.catclass.data.local.dao.TimetableSpaceDao
import com.catclass.data.local.entity.CourseDraftEntity
import com.catclass.data.local.entity.CourseEntity
import com.catclass.data.local.entity.TimetableSpaceEntity

// Room 数据库入口：后续可继续补 migration 和同步表
@Database(
    entities = [
        TimetableSpaceEntity::class,
        CourseEntity::class,
        CourseDraftEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CatClassDatabase : RoomDatabase() {
    abstract fun timetableSpaceDao(): TimetableSpaceDao
    abstract fun courseDao(): CourseDao
}
