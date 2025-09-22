package com.example.sg_future_nursing_app.ui.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?,
    onNavigateUp: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel()
) {
    // When taskId changes, let the VNet load data
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (task != null) {
                Text("Category: ${task!!.category}", style = MaterialTheme.typography.titleLarge)
                Text("Time: ${task!!.time}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (task!!.isCompleted) "Status: Completed" else "Status: Pending",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text("Task not found.")
            }
        }
    }
}