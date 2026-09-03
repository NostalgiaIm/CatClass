package com.catclass.data.vision

import android.content.Context
import android.net.Uri
import com.catclass.domain.model.CourseDraft
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import kotlin.math.abs

// Android 端的真实 OCR 适配器：识别文本后，再根据文本坐标推断星期列和节次行。
class MlKitTimetableImageRecognizer(
    context: Context,
) : TimetableImageRecognizer {
    private val applicationContext = context.applicationContext

    override suspend fun recognize(sourceUri: String): List<CourseDraft> {
        val imageUri = sourceUri.toImageUri()
        val image = InputImage.fromFilePath(applicationContext, imageUri)
        val recognizer = TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build(),
        )

        return try {
            val result = recognizer.process(image).await()
            TimetableOcrParser().parse(result)
        } finally {
            recognizer.close()
        }
    }
}

// 轻量级表格解析器：保守地产生草稿，复杂布局交给审核页面修正。
private class TimetableOcrParser {
    private data class OcrLine(
        val text: String,
        val centerX: Float,
        val centerY: Float,
    )

    private data class HeaderColumn(
        val dayOfWeek: Int,
        val centerX: Float,
    )

    fun parse(result: Text): List<CourseDraft> {
        val lines = result.textBlocks
            .flatMap { block -> block.lines }
            .mapNotNull { line ->
                val bounds = line.boundingBox ?: return@mapNotNull null
                val text = normalize(line.text)
                if (text.isBlank()) {
                    return@mapNotNull null
                }
                OcrLine(
                    text = text,
                    centerX = bounds.centerX().toFloat(),
                    centerY = bounds.centerY().toFloat(),
                )
            }

        if (lines.isEmpty()) {
            return emptyList()
        }

        val candidates = lines.filterNot { isLayoutLabel(it.text) }
        val headers = inferHeaderColumns(lines)
        val rowCenters = inferRowCenters(candidates)
        val grouped = candidates.groupBy { line ->
            val day = nearestDay(line.centerX, headers)
            val row = nearestRow(line.centerY, rowCenters)
            day to row
        }

        return grouped
            .filterKeys { (day, row) -> day in 1..7 && row in 1..8 }
            .map { (key, cellLines) ->
                val title = cellLines
                    .joinToString(" ") { it.text }
                    .take(MAX_TITLE_LENGTH)
                CourseDraft(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    teacher = null,
                    location = null,
                    dayOfWeek = key.first,
                    startPeriod = key.second,
                    periodCount = 1,
                    confidence = confidence(headers, rowCenters, cellLines),
                )
            }
            .sortedWith(compareBy<CourseDraft> { it.dayOfWeek }.thenBy { it.startPeriod })
    }

    private fun inferHeaderColumns(lines: List<OcrLine>): List<HeaderColumn> {
        val result = lines.mapNotNull { line ->
            val day = dayFromText(line.text) ?: return@mapNotNull null
            HeaderColumn(day, line.centerX)
        }
        if (result.size >= 2) {
            return result.distinctBy { it.dayOfWeek }
        }

        // 没有识别出星期标题时，用文本横坐标聚类作为保守兜底。
        return lines
            .filterNot { isLayoutLabel(it.text) }
            .map { it.centerX }
            .distinct()
            .sorted()
            .take(7)
            .mapIndexed { index, centerX -> HeaderColumn(index + 1, centerX) }
    }

    private fun inferRowCenters(lines: List<OcrLine>): List<Float> {
        return lines
            .map { it.centerY }
            .sorted()
            .fold(mutableListOf<Float>()) { rows, centerY ->
                val previous = rows.lastOrNull()
                if (previous == null || abs(previous - centerY) > ROW_CLUSTER_DISTANCE) {
                    rows += centerY
                } else {
                    rows[rows.lastIndex] = (previous + centerY) / 2f
                }
                rows
            }
            .take(8)
    }

    private fun nearestDay(centerX: Float, headers: List<HeaderColumn>): Int {
        return headers.minByOrNull { abs(it.centerX - centerX) }?.dayOfWeek ?: 1
    }

    private fun nearestRow(centerY: Float, rows: List<Float>): Int {
        return rows.indexOfFirst { abs(it - centerY) <= ROW_CLUSTER_DISTANCE }
            .takeIf { it >= 0 }
            ?.plus(1)
            ?: 1
    }

    private fun confidence(
        headers: List<HeaderColumn>,
        rows: List<Float>,
        cellLines: List<OcrLine>,
    ): Float {
        val geometryScore = if (headers.size >= 2 && rows.size >= 2) 0.72f else 0.42f
        val textScore = if (cellLines.any { it.text.length >= 2 }) 0.12f else 0f
        return (geometryScore + textScore).coerceAtMost(0.95f)
    }

    private fun isLayoutLabel(text: String): Boolean {
        return dayFromText(text) != null ||
            text.matches(Regex("第?\\d{1,2}\\s*[节课]?")) ||
            text.matches(Regex("\\d{1,2}\\s*[-~至]\\s*\\d{1,2}"))
    }

    private fun dayFromText(text: String): Int? {
        val labels = listOf(
            "一" to 1,
            "二" to 2,
            "三" to 3,
            "四" to 4,
            "五" to 5,
            "六" to 6,
            "日" to 7,
            "天" to 7,
        )
        val normalized = text.replace(Regex("\\s+"), "")
        labels.firstOrNull { (label, _) -> normalized == label }?.let { return it.second }

        val match = Regex("^(?:周|星期|礼拜)([一二三四五六日天])$").find(normalized)
        return match?.groupValues?.getOrNull(1)?.let { dayLabel ->
            labels.firstOrNull { it.first == dayLabel }?.second
        }
    }

    private fun normalize(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun String.toImageUri(): Uri {
        return if (startsWith("content://") || startsWith("file://")) {
            Uri.parse(this)
        } else {
            Uri.fromFile(File(this))
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 80
        const val ROW_CLUSTER_DISTANCE = 36f
    }
}
