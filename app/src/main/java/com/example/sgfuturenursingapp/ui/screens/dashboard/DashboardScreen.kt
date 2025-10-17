@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sgfuturenursingapp.ui.components.TaskItem
import com.example.sgfuturenursingapp.ui.data.DummyDataProvider
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@Composable
fun DashboardScreen(
    onTaskClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbar) {
        val snackbar = uiState.snackbar ?: return@LaunchedEffect
        val result =
            snackbarHostState.showSnackbar(
                message = snackbar.message,
                actionLabel = snackbar.actionLabel,
            )
        viewModel.onSnackbarResult(
            snackbar = snackbar,
            actionPerformed = result == SnackbarResult.ActionPerformed,
        )
    }

    DashboardScreenContent(
        uiState = uiState,
        onTaskClick = onTaskClick,
        onProfileClick = onProfileClick,
        onCompleteClick = viewModel::completeTask,
        onAddTaskClick = onAddTaskClick,
        onDismissTask = viewModel::onTaskDismissed,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun DashboardScreenContent(
    uiState: DashboardUiState,
    onTaskClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCompleteClick: (Int) -> Unit,
    onAddTaskClick: () -> Unit,
    onDismissTask: (Task) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val canAddTask = uiState.userRole in setOf("Admin", "Primary Caregiver")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Care Plan") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        floatingActionButton = {
            if (canAddTask) {
                FloatingActionButton(onClick = onAddTaskClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.tasks.isEmpty() -> {
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp),
                        )
                        Text(
                            text =
                                if (canAddTask) {
                                    "No tasks for today. Tap the '+' button to add one!"
                                } else {
                                    "No tasks assigned for today."
                                },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        item {
                            Text(
                                text = "Welcome back, ${uiState.userName}!",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }

                        items(
                            items = uiState.tasks,
                            key = { it.id },
                        ) { task ->
                            val dismissState =
                                rememberDismissState { value ->
                                    val dismissed =
                                        value == DismissValue.DismissedToEnd ||
                                            value == DismissValue.DismissedToStart
                                    if (dismissed) {
                                        onDismissTask(task)
                                    }
                                    dismissed
                                }

                            SwipeToDismiss(
                                state = dismissState,
                                directions =
                                    setOf(
                                        DismissDirection.StartToEnd,
                                        DismissDirection.EndToStart,
                                    ),
                                background = {
                                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                                    val alignment =
                                        if (direction == DismissDirection.StartToEnd) {
                                            Alignment.CenterStart
                                        } else {
                                            Alignment.CenterEnd
                                        }
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.errorContainer)
                                                .padding(horizontal = 24.dp),
                                        contentAlignment = alignment,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                },
                                dismissContent = {
                                    TaskItem(
                                        task = task,
                                        modifier = Modifier.clickable { onTaskClick(task.id) },
                                        onCompleteClick = onCompleteClick,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    val previewState =
        DashboardUiState(
            tasks = DummyDataProvider.tasks,
            userRole = "Admin",
            isLoading = false,
        )
    val snackbarHostState = SnackbarHostState()
    CP3406SGFutureNursingAppTheme {
        DashboardScreenContent(
            uiState = previewState,
            onTaskClick = {},
            onProfileClick = {},
            onCompleteClick = {},
            onAddTaskClick = {},
            onDismissTask = {},
            snackbarHostState = snackbarHostState,
        )
    }
}
