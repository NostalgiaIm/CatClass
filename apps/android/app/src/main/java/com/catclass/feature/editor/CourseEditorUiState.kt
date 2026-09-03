package com.catclass.feature.editor

import com.catclass.domain.model.CourseFormState

// 课程编辑页状态
data class CourseEditorUiState(
    val form: CourseFormState = CourseFormState(),
    val saving: Boolean = false,
    val message: String? = null,
)

