@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.sgfuturenursingapp.ui.theme.SuccessGreen

@Composable
fun DashboardScreen(
    onTaskClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val windowWidthSizeClass = windowSizeClass.widthSizeClass
    var selectedTaskId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(windowWidthSizeClass, uiState.tasks) {
        if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
            selectedTaskId =
                when {
                    uiState.tasks.isEmpty() -> null
                    selectedTaskId != null && uiState.tasks.any { it.id == selectedTaskId } -> selectedTaskId
                    else -> uiState.tasks.first().id
                }
        } else {
            selectedTaskId = null
        }
    }

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

    val handleTaskSelection: (Int) -> Unit = { taskId ->
        if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
            selectedTaskId = taskId
        } else {
            onTaskClick(taskId)
        }
    }

    DashboardScreenContent(
        uiState = uiState,
        windowWidthSizeClass = windowWidthSizeClass,
        selectedTaskId = selectedTaskId,
        onTaskSelected = handleTaskSelection,
        onTaskOpenDetail = onTaskClick,
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
    windowWidthSizeClass: WindowWidthSizeClass,
    selectedTaskId: Int?,
    onTaskSelected: (Int) -> Unit,
    onTaskOpenDetail: (Int) -> Unit,
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
                            contentDescription = "No tasks available",
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
                    when (windowWidthSizeClass) {
                        WindowWidthSizeClass.Expanded -> {
                            val selectedTask =
                                uiState.tasks.firstOrNull { it.id == selectedTaskId }
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                            ) {
                                TaskList(
                                    tasks = uiState.tasks,
                                    userName = uiState.userName,
                                    selectedTaskId = selectedTaskId,
                                    onTaskSelected = onTaskSelected,
                                    onCompleteClick = onCompleteClick,
                                    onDismissTask = onDismissTask,
                                    showSelection = true,
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                )
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                )
                                TaskDetailPane(
                                    task = selectedTask,
                                    onMarkComplete = onCompleteClick,
                                    onOpenTask = onTaskOpenDetail,
                                    modifier =
                                        Modifier
                                            .weight(1.2f)
                                            .fillMaxHeight(),
                                )
                            }
                        }

                        else -> {
                            TaskList(
                                tasks = uiState.tasks,
                                userName = uiState.userName,
                                selectedTaskId = selectedTaskId,
                                onTaskSelected = onTaskSelected,
                                onCompleteClick = onCompleteClick,
                                onDismissTask = onDismissTask,
                                showSelection = false,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TaskList(
    tasks: List<Task>,
    userName: String,
    selectedTaskId: Int?,
    onTaskSelected: (Int) -> Unit,
    onCompleteClick: (Int) -> Unit,
    onDismissTask: (Task) -> Unit,
    showSelection: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Text(
                text = "Welcome back, $userName!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(
            items = tasks,
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
                            contentDescription = "Delete task",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                },
                dismissContent = {
                    TaskItem(
                        task = task,
                        modifier =
                            Modifier
                                .clickable { onTaskSelected(task.id) }
                                .sizeIn(minHeight = 64.dp),
                        onCompleteClick = onCompleteClick,
                        isSelected = showSelection && task.id == selectedTaskId,
                    )
                },
            )
        }
    }
}

@Composable
private fun TaskDetailPane(
    task: Task?,
    onMarkComplete: (Int) -> Unit,
    onOpenTask: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
    ) {
        if (task == null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Select a task to view details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select a task to view its details.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Category: ${task.category}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Scheduled: ${task.time}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Priority: ${task.priority}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                val statusText = if (task.isCompleted) "Completed" else "Pending"
                val statusColor =
                    if (task.isCompleted) {
                        SuccessGreen
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                Text(
                    text = "Status: $statusText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor,
                )
                Spacer(modifier = Modifier.weight(1f, fill = true))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { onOpenTask(task.id) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                    ) {
                        Text("Open Details")
                    }
                    if (!task.isCompleted) {
                        Button(
                            onClick = { onMarkComplete(task.id) },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                        ) {
                            Text("Mark Complete")
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
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            selectedTaskId = null,
            onTaskSelected = {},
            onTaskOpenDetail = {},
            onProfileClick = {},
            onCompleteClick = {},
            onAddTaskClick = {},
            onDismissTask = {},
            snackbarHostState = snackbarHostState,
        )
    }
}

@Preview(showSystemUi = true, widthDp = 1000, heightDp = 700)
@Composable
fun DashboardScreenExpandedPreview() {
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
            windowWidthSizeClass = WindowWidthSizeClass.Expanded,
            selectedTaskId = previewState.tasks.firstOrNull()?.id,
            onTaskSelected = {},
            onTaskOpenDetail = {},
            onProfileClick = {},
            onCompleteClick = {},
            onAddTaskClick = {},
            onDismissTask = {},
            snackbarHostState = snackbarHostState,
        )
    }
}
