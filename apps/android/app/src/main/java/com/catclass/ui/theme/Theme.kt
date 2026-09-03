package com.catclass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 应用主题：后续可扩展颜色、排版和暗色模式
@Composable
fun CatClassTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = CatClassPrimary,
        secondary = CatClassPrimary,
        background = CatClassBackground,
        surface = CatClassBackground,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
