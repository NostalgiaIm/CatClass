package com.catclass.data.vision

import com.catclass.domain.model.CourseDraft
import java.util.UUID

// 图片识别引擎边界。接入 ML Kit、云端 OCR 或离线模型时只需实现此接口。
interface TimetableImageRecognizer {
    suspend fun recognize(sourceUri: String): List<CourseDraft>
}

// 开发期实现：保证“选择图片 -> 校对草稿 -> 导入课表”链路可以离线演示和验证。
class DemoTimetableImageRecognizer : TimetableImageRecognizer {
    override suspend fun recognize(sourceUri: String): List<CourseDraft> {
        return listOf(
            CourseDraft(
                id = UUID.randomUUID().toString(),
                title = "示例课程",
                teacher = "待识别",
                location = "待确认",
                dayOfWeek = 1,
                startPeriod = 1,
                periodCount = 2,
                confidence = 0.72f,
            )
        )
    }
}
