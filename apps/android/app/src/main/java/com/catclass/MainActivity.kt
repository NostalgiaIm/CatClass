package com.catclass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.catclass.CatClassApp
import com.catclass.ui.theme.CatClassTheme

// 主 Activity：承载 Compose 界面
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatClassTheme {
                AppRoot((application as CatClassApp).container)
            }
        }
    }
}
