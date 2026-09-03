package com.catclass.core.time

// 日期工具：后续统一处理周次、节次和时区
object CatClassDateTime {
    fun nowIso(): String = java.time.OffsetDateTime.now().toString()
}
