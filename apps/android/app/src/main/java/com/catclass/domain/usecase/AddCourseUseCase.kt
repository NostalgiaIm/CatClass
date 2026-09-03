package com.catclass.domain.usecase

import com.catclass.domain.model.CourseFormState
import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.repository.TimetableRepository

// 新增课程用例：UI 只提交表单，领域层负责生成标准课程模型
class AddCourseUseCase(
    private val repository: TimetableRepository,
) {
    suspend operator fun invoke(form: CourseFormState): CourseTemplate {
        require(form.title.isNotBlank()) { "课程名称不能为空" }
        return repository.addCourse(form)
    }
}
