package com.catclass.core.di

import android.content.Context
import androidx.room.Room
import com.catclass.data.repository.TimetableRepositoryImpl
import com.catclass.data.local.database.CatClassDatabase
import com.catclass.data.local.entity.TimetableSpaceEntity
import com.catclass.data.sync.ImageImportRepository
import com.catclass.data.vision.MlKitTimetableImageRecognizer
import com.catclass.domain.usecase.AddCourseUseCase
import com.catclass.domain.usecase.CommitRecognizedDraftsUseCase
import com.catclass.domain.usecase.GetTimetableForWeekUseCase
import com.catclass.domain.usecase.ImportTimetableImageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// 简单依赖容器：先用手写装配，后续可替换 Hilt/Koin
class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database: CatClassDatabase = Room.databaseBuilder(
        context,
        CatClassDatabase::class.java,
        "catclass.db",
    ).build()

    val timetableRepository = TimetableRepositoryImpl(database)
    val visionRepository = ImageImportRepository(MlKitTimetableImageRecognizer(context))

    val getTimetableForWeekUseCase = GetTimetableForWeekUseCase(timetableRepository)
    val addCourseUseCase = AddCourseUseCase(timetableRepository)
    val importTimetableImageUseCase = ImportTimetableImageUseCase(visionRepository)
    val commitRecognizedDraftsUseCase = CommitRecognizedDraftsUseCase(timetableRepository)

    init {
        // 首次安装时种入一个默认课表空间，后续由用户在设置中创建更多空间
        applicationScope.launch {
            database.timetableSpaceDao().upsertAll(
                listOf(TimetableSpaceEntity(id = "default-space", name = "默认课表"))
            )
        }
    }
}
