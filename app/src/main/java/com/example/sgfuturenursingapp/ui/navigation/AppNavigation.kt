@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardScreen
import com.example.sgfuturenursingapp.ui.screens.profile.ProfileScreen
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskScreen
import com.example.sgfuturenursingapp.ui.screens.task.TaskDetailScreen

// Define routing names for all screens
object ScreenRoutes {
    const val DASHBOARD = "dashboard"
    const val TASK_DETAIL = "task_detail"
    const val PROFILE = "profile"
    const val ADD_EDIT_TASK = "add_edit_task"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ScreenRoutes.DASHBOARD) {
        composable(ScreenRoutes.DASHBOARD) {
            DashboardScreen(
                onTaskClick = { taskId ->
                    // Navigate to the task details page and pass the task ID
                    navController.navigate("${ScreenRoutes.TASK_DETAIL}/$taskId")
                },
                onProfileClick = {
                    navController.navigate(ScreenRoutes.PROFILE)
                },
                onAddTaskClick = {
                    navController.navigate(ScreenRoutes.ADD_EDIT_TASK)
                },
            )
        }

        composable("${ScreenRoutes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(
                taskId = taskId,
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(ScreenRoutes.PROFILE) {
            ProfileScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(ScreenRoutes.ADD_EDIT_TASK) {
            AddEditTaskScreen(
                onNavigateUp = { navController.navigateUp() },
                onSaveClick = { _, _, _ ->
                    navController.navigateUp()
                },
            )
        }
    }
}
