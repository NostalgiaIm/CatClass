package com.catclass.feature.vision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catclass.domain.model.CourseDraft
import com.catclass.domain.model.VisionJobStatus
import com.catclass.domain.usecase.CommitRecognizedDraftsUseCase
import com.catclass.domain.usecase.ImportTimetableImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 图片识别页 ViewModel：协调识别、人工校对和提交，UI 不依赖具体 OCR 实现。
class VisionViewModel(
    private val importTimetableImageUseCase: ImportTimetableImageUseCase,
    private val commitRecognizedDraftsUseCase: CommitRecognizedDraftsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    fun updateSourceUri(value: String) {
        _uiState.update { it.copy(sourceUri = value) }
    }

    fun importImage() {
        val sourceUri = uiState.value.sourceUri.trim()
        if (sourceUri.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入图片地址或文件路径") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, resultMessage = null) }
            runCatching { importTimetableImageUseCase(sourceUri) }
                .onSuccess { job ->
                    _uiState.update {
                        it.copy(
                            job = job,
                            loading = false,
                            selectedDraftIds = job.drafts.map { draft -> draft.id }.toSet(),
                            resultMessage = "已识别到 ${job.drafts.size} 条课程草稿",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = error.message ?: "识别失败",
                        )
                    }
                }
        }
    }

    fun toggleDraftSelection(draft: CourseDraft) {
        _uiState.update { state ->
            val next = state.selectedDraftIds.toMutableSet()
            if (next.contains(draft.id)) {
                next.remove(draft.id)
            } else {
                next.add(draft.id)
            }
            state.copy(selectedDraftIds = next)
        }
    }

    fun commit() {
        val job = uiState.value.job ?: run {
            _uiState.update { it.copy(errorMessage = "请先识别图片") }
            return
        }
        val selectedDrafts = job.drafts.filter { draft -> uiState.value.selectedDraftIds.contains(draft.id) }
        if (selectedDrafts.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请至少选择一条课程草稿") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, resultMessage = null) }
            runCatching { commitRecognizedDraftsUseCase(job.copy(drafts = selectedDrafts)) }
                .onSuccess { imported ->
                    _uiState.update {
                        it.copy(
                            job = job.copy(status = VisionJobStatus.Committed),
                            loading = false,
                            selectedDraftIds = emptySet(),
                            resultMessage = "已导入 ${imported.size} 条课程",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = error.message ?: "提交失败",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, resultMessage = null) }
    }
}
