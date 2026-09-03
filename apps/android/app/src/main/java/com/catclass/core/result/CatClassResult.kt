package com.catclass.core.result

// 简单结果封装，后续可替换为更完整的错误类型
sealed class CatClassResult<out T> {
    data class Success<T>(val data: T) : CatClassResult<T>()
    data class Error(val message: String) : CatClassResult<Nothing>()
}

