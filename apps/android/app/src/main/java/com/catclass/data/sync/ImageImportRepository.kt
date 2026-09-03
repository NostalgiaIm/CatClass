package com.catclass.data.sync

import com.catclass.data.vision.TimetableImageRecognizer
import com.catclass.domain.model.ImageImportJob
import com.catclass.domain.model.VisionJobStatus
import com.catclass.domain.repository.VisionRepository
import java.util.UUID

// 图片识别导入仓库：聚合识别引擎输出，并将结果送入“人工校对”状态。
class ImageImportRepository(
    private val recognizer: TimetableImageRecognizer,
) : VisionRepository {
    override suspend fun recognizeTimetableImage(sourceUri: String): ImageImportJob {
        return ImageImportJob(
            id = UUID.randomUUID().toString(),
            sourceUri = sourceUri,
            status = VisionJobStatus.Reviewing,
            drafts = recognizer.recognize(sourceUri),
        )
    }
}
