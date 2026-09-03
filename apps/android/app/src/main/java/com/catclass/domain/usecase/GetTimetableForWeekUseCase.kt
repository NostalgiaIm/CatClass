package com.catclass.domain.usecase

import com.catclass.domain.model.CourseInstance
import com.catclass.domain.repository.TimetableRepository

// 周课表查询用例：由 Repository 统一生成本周课程实例
class GetTimetableForWeekUseCase(
    private val repository: TimetableRepository,
) {
    suspend operator fun invoke(weekIndex: Int): List<CourseInstance> {
        return repository.listWeekInstances(weekIndex)
    }
}
