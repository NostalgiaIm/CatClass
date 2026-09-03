package com.catclass

import android.app.Application
import com.catclass.core.di.AppContainer

// 应用入口：统一持有跨页面共享的依赖容器
class CatClassApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
