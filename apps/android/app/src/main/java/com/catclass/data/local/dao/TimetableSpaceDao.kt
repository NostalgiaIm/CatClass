package com.catclass.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catclass.data.local.entity.TimetableSpaceEntity
import kotlinx.coroutines.flow.Flow

// 课表空间 DAO：后续可补 insert / update / delete
@Dao
interface TimetableSpaceDao {
    @Query("SELECT * FROM timetable_spaces")
    fun observeAll(): Flow<List<TimetableSpaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TimetableSpaceEntity>)
}
