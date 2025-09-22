package com.example.sg_future_nursing_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sg_future_nursing_app.ui.screens.dashboard.DashboardScreen
import com.example.sg_future_nursing_app.ui.screens.task.TaskDetailScreen
import com.example.sg_future_nursing_app.ui.screens.profile.ProfileScreen

// Define routing names for all screens
object ScreenRoutes {
    const val DASHBOARD = "dashboard"
    const val TASK_DETAIL = "task_detail"
    const val PROFILE = "profile"
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
                }
            )
        }

        composable("${ScreenRoutes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(
                taskId = taskId,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(ScreenRoutes.PROFILE) {
            ProfileScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}