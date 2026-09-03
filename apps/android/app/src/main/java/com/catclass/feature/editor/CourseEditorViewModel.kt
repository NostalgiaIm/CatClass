package com.catclass.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catclass.domain.model.CourseFormState
import com.catclass.domain.usecase.AddCourseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 课程编辑页 ViewModel：负责表单输入和保存动作
class CourseEditorViewModel(
    private val addCourseUseCase: AddCourseUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseEditorUiState())
    val uiState: StateFlow<CourseEditorUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) = updateForm { it.copy(title = value) }
    fun updateTeacher(value: String) = updateForm { it.copy(teacher = value) }
    fun updateLocation(value: String) = updateForm { it.copy(location = value) }
    fun updateDay(value: Int) = updateForm { it.copy(dayOfWeek = value.coerceIn(1, 7)) }
    fun updateStartPeriod(value: Int) = updateForm { form ->
        val startPeriod = value.coerceIn(1, MAX_PERIOD_COUNT)
        form.copy(
            startPeriod = startPeriod,
            periodCount = form.periodCount.coerceIn(1, MAX_PERIOD_COUNT - startPeriod + 1),
        )
    }

    fun updatePeriodCount(value: Int) = updateForm { form ->
        form.copy(
            periodCount = value.coerceIn(1, MAX_PERIOD_COUNT - form.startPeriod + 1),
        )
    }

    fun save() {
        if (uiState.value.form.title.isBlank()) {
            _uiState.update { it.copy(message = "请填写课程名称") }
            return
        }

        viewModelScope.launch {
            val form = uiState.value.form
            _uiState.update { it.copy(saving = true, message = null) }
            runCatching { addCourseUseCase(form) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            form = CourseFormState(),
                            saving = false,
                            message = "课程已保存",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            saving = false,
                            message = error.message ?: "保存失败",
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun updateForm(transform: (CourseFormState) -> CourseFormState) {
        _uiState.update { state -> state.copy(form = transform(state.form)) }
    }

    private companion object {
        // 当前周视图默认显示 8 节；后续会由课表空间的节次配置替代该常量。
        const val MAX_PERIOD_COUNT = 8
    }
}
