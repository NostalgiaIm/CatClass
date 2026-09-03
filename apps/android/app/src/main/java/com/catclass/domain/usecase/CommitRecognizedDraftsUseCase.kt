package com.catclass.domain.usecase

import com.catclass.domain.model.CourseTemplate
import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.repository.TimetableRepository

// 将识别草稿写入正式课表；真实实现应在这里加入冲突检查和人工确认结果
class CommitRecognizedDraftsUseCase(
    private val repository: TimetableRepository,
) {
    suspend operator fun invoke(job: ImageImportJob): List<CourseTemplate> {
        return repository.importDrafts(job)
    }
}
