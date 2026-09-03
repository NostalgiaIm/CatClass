package com.catclass

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.catclass.core.di.AppContainer
import com.catclass.core.di.AppViewModelFactory
import com.catclass.core.navigation.AppDestinations
import com.catclass.feature.editor.CourseEditorRoute
import com.catclass.feature.editor.CourseEditorViewModel
import com.catclass.feature.settings.SettingsRoute
import com.catclass.feature.timetable.TimetableRoute
import com.catclass.feature.timetable.TimetableViewModel
import com.catclass.feature.vision.VisionRoute
import com.catclass.feature.vision.VisionViewModel

// 应用根布局：负责底部导航和页面切换
@Composable
fun AppRoot(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val factory = AppViewModelFactory(container)

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(AppDestinations.Timetable) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Timetable,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppDestinations.Timetable) {
                val viewModel: TimetableViewModel = viewModel(factory = factory)
                TimetableRoute(viewModel = viewModel)
            }
            composable(AppDestinations.Editor) {
                val viewModel: CourseEditorViewModel = viewModel(factory = factory)
                CourseEditorRoute(viewModel = viewModel)
            }
            composable(AppDestinations.Vision) {
                val viewModel: VisionViewModel = viewModel(factory = factory)
                VisionRoute(viewModel = viewModel)
            }
            composable(AppDestinations.Settings) {
                SettingsRoute()
            }
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomItems = listOf(
    BottomItem(AppDestinations.Timetable, "课表", Icons.Filled.CalendarMonth),
    BottomItem(AppDestinations.Editor, "编辑", Icons.Filled.Edit),
    BottomItem(AppDestinations.Vision, "识别", Icons.Filled.PhotoLibrary),
    BottomItem(AppDestinations.Settings, "设置", Icons.Filled.Settings),
)
