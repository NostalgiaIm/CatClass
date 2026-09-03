package com.catclass.feature.vision

import com.catclass.domain.model.CourseDraft
import com.catclass.domain.model.ImageImportJob

// 图片识别页状态
data class VisionUiState(
    val sourceUri: String = "",
    val job: ImageImportJob? = null,
    val selectedDraftIds: Set<String> = emptySet(),
    val resultMessage: String? = null,
    val errorMessage: String? = null,
    val loading: Boolean = false,
) {
    val drafts: List<CourseDraft>
        get() = job?.drafts.orEmpty()
}

