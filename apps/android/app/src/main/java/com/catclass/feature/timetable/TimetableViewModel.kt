package com.catclass.feature.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catclass.domain.model.CourseFormState
import com.catclass.domain.repository.TimetableRepository
import com.catclass.domain.usecase.GetTimetableForWeekUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 课表页 ViewModel：管理空间、课程和周视图状态
class TimetableViewModel(
    private val getTimetableForWeekUseCase: GetTimetableForWeekUseCase,
    private val repository: TimetableRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    init {
        observeSpaces()
        observeCourses()
        refreshWeek()
    }

    fun changeWeek(delta: Int) {
        _uiState.update { state ->
            val nextWeek = (state.activeWeek + delta).coerceAtLeast(1)
            state.copy(activeWeek = nextWeek, loading = true)
        }
        refreshWeek()
    }

    fun addQuickCourse() {
        viewModelScope.launch {
            repository.addCourse(
                CourseFormState(
                    title = "高等数学",
                    teacher = "示例教师",
                    location = "A-101",
                    dayOfWeek = 1,
                    startPeriod = 1,
                    periodCount = 2,
                )
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun observeSpaces() {
        viewModelScope.launch {
            repository.observeSpaces().collectLatest { spaces ->
                _uiState.update { state ->
                    val firstName = spaces.firstOrNull()?.name ?: "默认课表"
                    state.copy(
                        spaces = spaces,
                        selectedSpaceName = firstName,
                    )
                }
            }
        }
    }

    private fun observeCourses() {
        viewModelScope.launch {
            repository.observeCourses().collectLatest { courses ->
                _uiState.update { it.copy(courses = courses) }
                refreshWeek()
            }
        }
    }

    private fun refreshWeek() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val week = uiState.value.activeWeek
            val instances = getTimetableForWeekUseCase(week)
            _uiState.update {
                it.copy(
                    weekInstances = instances,
                    loading = false,
                    message = "第 $week 周已刷新",
                )
            }
        }
    }
}
