@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sgfuturenursingapp.ui.data.DummyDataProvider
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme
import com.example.sgfuturenursingapp.ui.theme.SuccessGreen

@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCompleteClick: (Int) -> Unit,
) {
    val containerColor =
        if (isSelected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        }
    val border =
        if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        } else {
            null
        }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 6.dp else 2.dp,
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val displayIcon = iconForName(task.iconName)
            Icon(
                imageVector = displayIcon,
                contentDescription = task.category,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
                Text(text = task.time, style = MaterialTheme.typography.bodyMedium)
            }
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = SuccessGreen,
                )
            } else {
                OutlinedButton(
                    onClick = { onCompleteClick(task.id) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
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
        TaskItem(task = DummyDataProvider.tasks.first(), onCompleteClick = {})
    }
}

private fun iconForName(iconName: String) =
    when (iconName) {
        "medical_services" -> Icons.Filled.MedicalServices
        "monitor_heart" -> Icons.Filled.MonitorHeart
        "event" -> Icons.Filled.Event
        else -> Icons.Filled.CheckCircle
    }
