@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardScreen
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardViewModel
import com.example.sgfuturenursingapp.ui.screens.profile.ProfileScreen
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskScreen
import com.example.sgfuturenursingapp.ui.screens.task.TaskDetailScreen

// Define routing names for all screens
object ScreenRoutes {
    const val DASHBOARD = "dashboard"
    const val TASK_DETAIL = "task_detail"
    const val PROFILE = "profile"
    const val ADD_EDIT_TASK = "add_edit_task"
    const val TASK_ID = "taskId"
    const val RESULT_MESSAGE = "resultMessage"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ScreenRoutes.DASHBOARD) {
        composable(ScreenRoutes.DASHBOARD) { backStackEntry ->
            val dashboardViewModel = hiltViewModel<DashboardViewModel>()
            val message = backStackEntry.savedStateHandle.get<String>(ScreenRoutes.RESULT_MESSAGE)
            if (message != null) {
                LaunchedEffect(message) {
                    dashboardViewModel.showSnackbarMessage(message)
                    backStackEntry.savedStateHandle.remove<String>(ScreenRoutes.RESULT_MESSAGE)
                }
            }

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
                viewModel = dashboardViewModel,
            )
        }

        composable("${ScreenRoutes.TASK_DETAIL}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(
                taskId = taskId,
                onNavigateUp = { navController.navigateUp() },
                onEditTask = { id ->
                    navController.navigate(
                        "${ScreenRoutes.ADD_EDIT_TASK}?${ScreenRoutes.TASK_ID}=$id",
                    )
                },
            )
        }

        composable(ScreenRoutes.PROFILE) {
            ProfileScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(
            route = "${ScreenRoutes.ADD_EDIT_TASK}?${ScreenRoutes.TASK_ID}={${ScreenRoutes.TASK_ID}}",
            arguments =
                listOf(
                    navArgument(ScreenRoutes.TASK_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
        ) {
            AddEditTaskScreen(
                onNavigateUp = { navController.navigateUp() },
                onActionFinished = { message ->
                    navController.getBackStackEntry(ScreenRoutes.DASHBOARD)
                        .savedStateHandle
                        .set(ScreenRoutes.RESULT_MESSAGE, message)
                    navController.popBackStack(
                        route = ScreenRoutes.DASHBOARD,
                        inclusive = false,
                    )
                },
            )
        }
    }
}
