package com.catclass.domain.usecase

import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.repository.VisionRepository

// 图片课表识别用例：返回待校对的课程草稿
class ImportTimetableImageUseCase(
    private val repository: VisionRepository,
) {
    suspend operator fun invoke(sourceUri: String): ImageImportJob {
        require(sourceUri.isNotBlank()) { "图片地址不能为空" }
        return repository.recognizeTimetableImage(sourceUri)
    }
}
