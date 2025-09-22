package com.example.sg_future_nursing_app.ui.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sg_future_nursing_app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?,
    onNavigateUp: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    val task by viewModel.task.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (task != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Category: ${task!!.category}", style = MaterialTheme.typography.titleLarge)
                    Text("Time: ${task!!.time}", style = MaterialTheme.typography.bodyLarge)
                    val statusText = if (task!!.isCompleted) "Status: Completed" else "Status: Pending"
                    val statusColor = if (task!!.isCompleted) SuccessGreen else LocalContentColor.current
                    Text(statusText, style = MaterialTheme.typography.bodyLarge, color = statusColor)
                }

                // 如果任务未完成，显示按钮
                if (!task!!.isCompleted) {
                    Button(
                        onClick = { viewModel.completeTask() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Text("Mark as Complete")
                    }
                }
            } else {
                Text("Task not found.")
            }
        }
    }
}