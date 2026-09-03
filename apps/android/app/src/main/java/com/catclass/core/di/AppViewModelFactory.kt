package com.catclass.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.catclass.feature.editor.CourseEditorViewModel
import com.catclass.feature.timetable.TimetableViewModel
import com.catclass.feature.vision.VisionViewModel

// ViewModel 工厂：统一从容器里创建页面级状态对象
class AppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TimetableViewModel::class.java) -> {
                TimetableViewModel(container.getTimetableForWeekUseCase, container.timetableRepository) as T
            }
            modelClass.isAssignableFrom(CourseEditorViewModel::class.java) -> {
                CourseEditorViewModel(container.addCourseUseCase) as T
            }
            modelClass.isAssignableFrom(VisionViewModel::class.java) -> {
                VisionViewModel(container.importTimetableImageUseCase, container.commitRecognizedDraftsUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

