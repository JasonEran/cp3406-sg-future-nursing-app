package com.example.sg_future_nursing_app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sg_future_nursing_app.ui.components.TaskItem
import com.example.sg_future_nursing_app.ui.data.DummyDataProvider
import com.example.sg_future_nursing_app.ui.theme.CP3406SGFutureNursingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val tasks = DummyDataProvider.tasks

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Care Plan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Welcome back, Mark!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(tasks) { task ->
                TaskItem(task = task)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    CP3406SGFutureNursingAppTheme {
        DashboardScreen()
    }
}