package com.jasoneran.cp3406_sg_future_nursing_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jasoneran.cp3406_sg_future_nursing_app.ui.data.DummyDataProvider
import com.jasoneran.cp3406_sg_future_nursing_app.ui.data.Task
import com.jasoneran.cp3406_sg_future_nursing_app.ui.theme.CP3406SGFutureNursingAppTheme
import com.jasoneran.cp3406_sg_future_nursing_app.ui.theme.SuccessGreen

@Composable
fun TaskItem(task: Task, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = task.icon,
                contentDescription = task.category,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
                Text(text = task.time, style = MaterialTheme.typography.bodyMedium)
            }
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = SuccessGreen
                )
            } else {
                OutlinedButton(onClick = { /*  */ }) {
                    Text("Complete")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    CP3406SGFutureNursingAppTheme {
        TaskItem(task = DummyDataProvider.tasks.first())
    }
}