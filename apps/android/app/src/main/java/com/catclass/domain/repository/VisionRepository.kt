package com.catclass.domain.repository

import com.catclass.domain.model.ImageImportJob

// 图片识别仓库接口：后续可替换为 ML Kit、本地 OCR 或云端识别
interface VisionRepository {
    suspend fun recognizeTimetableImage(sourceUri: String): ImageImportJob
}
