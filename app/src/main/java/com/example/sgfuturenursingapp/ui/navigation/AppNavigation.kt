@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sgfuturenursingapp.ui.screens.admin.AdminDashboardScreen
import com.example.sgfuturenursingapp.ui.screens.admin.HelperManagementScreen
import com.example.sgfuturenursingapp.ui.screens.auth.ForgotPasswordScreen
import com.example.sgfuturenursingapp.ui.screens.auth.LoginScreen
import com.example.sgfuturenursingapp.ui.screens.auth.RegisterScreen
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardScreen
import com.example.sgfuturenursingapp.ui.screens.dashboard.DashboardViewModel
import com.example.sgfuturenursingapp.ui.screens.main.MainScreen
import com.example.sgfuturenursingapp.ui.screens.news.HealthNewsScreen
import com.example.sgfuturenursingapp.ui.screens.profile.ProfileScreen
import com.example.sgfuturenursingapp.ui.screens.task.AddEditTaskScreen
import com.example.sgfuturenursingapp.ui.screens.task.TaskDetailScreen
import com.example.sgfuturenursingapp.ui.screens.auth.AuthViewModel

// Define routing names for all screens
object ScreenRoutes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val HELPER_MANAGEMENT = "helper_management"
    const val HEALTH_NEWS = "health_news"
    const val TASK_DETAIL = "task_detail"
    const val PROFILE = "profile"
    const val ADD_EDIT_TASK = "add_edit_task"
    const val TASK_ID = "taskId"
    const val RESULT_MESSAGE = "resultMessage"
}

@Composable
fun AppNavigation(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var initialDestination by rememberSaveable { mutableStateOf<String?>(null) }
    var previousAuthStatus by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(authUiState.isLoading, authUiState.isAuthenticated) {
        if (!authUiState.isLoading && initialDestination == null) {
            initialDestination =
                if (authUiState.isAuthenticated) {
                    ScreenRoutes.MAIN
                } else {
                    ScreenRoutes.LOGIN
                }
        }

        val lastStatus = previousAuthStatus
        if (lastStatus == true && !authUiState.isAuthenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != ScreenRoutes.LOGIN) {
                navController.navigate(ScreenRoutes.LOGIN) {
                    popUpTo(ScreenRoutes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        if (!authUiState.isLoading) {
            previousAuthStatus = authUiState.isAuthenticated
        }
    }

    val startDestination = initialDestination

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ScreenRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(ScreenRoutes.REGISTER) {
                        launchSingleTop = true
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(ScreenRoutes.FORGOT_PASSWORD) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    val currentRole =
                        authViewModel.uiState.value.userEmail?.let { email ->
                            when {
                                email.contains("admin", ignoreCase = true) -> "Admin"
                                email.contains("primary", ignoreCase = true) -> "Primary Caregiver"
                                else -> "Helper"
                            }
                        } ?: "Helper"

                    if (currentRole == "Admin") {
                        navController.navigate(ScreenRoutes.ADMIN_DASHBOARD) {
                            popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(ScreenRoutes.MAIN) {
                            popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                viewModel = authViewModel,
            )
        }

        composable(ScreenRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack(ScreenRoutes.LOGIN, inclusive = false)
                },
                onRegisterSuccess = {
                    navController.navigate(ScreenRoutes.MAIN) {
                        popUpTo(ScreenRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }

        composable(ScreenRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(ScreenRoutes.MAIN) {
            MainScreen(
                navController = navController,
                windowSizeClass = windowSizeClass,
            )
        }

        composable(ScreenRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen()
        }

        composable(ScreenRoutes.HELPER_MANAGEMENT) {
            HelperManagementScreen()
        }

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
                windowSizeClass = windowSizeClass,
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

        composable(ScreenRoutes.HEALTH_NEWS) {
            HealthNewsScreen()
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
