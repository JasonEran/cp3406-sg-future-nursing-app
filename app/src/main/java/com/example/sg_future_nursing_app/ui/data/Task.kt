package com.example.sg_future_nursing_app.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: Int,
    val title: String,
    val time: String,
    val category: String,
    val iconName: String,
    val isCompleted: Boolean
)
