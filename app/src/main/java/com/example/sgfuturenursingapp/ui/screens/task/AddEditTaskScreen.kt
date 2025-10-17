@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onNavigateUp: () -> Unit,
    onActionFinished: (String) -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        when {
            uiState.isSaved -> {
                viewModel.onActionConsumed()
                onActionFinished("任务已保存")
            }
            uiState.isDeleted -> {
                viewModel.onActionConsumed()
                onActionFinished("任务已删除")
            }
        }
    }

    AddEditTaskScreenContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onTitleChanged = viewModel::onTitleChanged,
        onTimeChanged = viewModel::onTimeChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onPriorityChanged = viewModel::onPriorityChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onDeleteClicked = viewModel::onDeleteClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskScreenContent(
    uiState: AddEditTaskUiState,
    onNavigateUp: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onPriorityChanged: (Int) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.taskId == null) "Add Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.taskId != null) {
                        IconButton(
                            onClick = onDeleteClicked,
                            enabled = !uiState.isSaving,
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                label = { Text("Title") },
                singleLine = true,
                isError = uiState.titleError != null,
                modifier = Modifier.fillMaxWidth(),
            )
            uiState.titleError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedTextField(
                value = uiState.time,
                onValueChange = onTimeChanged,
                label = { Text("Time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.category,
                onValueChange = onCategoryChanged,
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.priority.toString(),
                onValueChange = { input ->
                    input.toIntOrNull()?.let(onPriorityChanged)
                },
                label = { Text("Priority") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSaveClicked,
                enabled = uiState.isSaveEnabled && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AddEditTaskScreenPreview() {
    CP3406SGFutureNursingAppTheme {
        AddEditTaskScreenContent(
            uiState =
                AddEditTaskUiState(
                    title = "Sample Task",
                    time = "09:00",
                    category = "General",
                    priority = 1,
                    titleError = null,
                    isSaveEnabled = true,
                ),
            onNavigateUp = {},
            onTitleChanged = {},
            onTimeChanged = {},
            onCategoryChanged = {},
            onPriorityChanged = {},
            onSaveClicked = {},
            onDeleteClicked = {},
        )
    }
}
