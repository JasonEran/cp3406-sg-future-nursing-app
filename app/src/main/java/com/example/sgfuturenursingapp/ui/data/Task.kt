package com.example.sgfuturenursingapp.ui.data

data class Task(
    val id: Int,
    val title: String,
    val time: String,
    val category: String,
    val iconName: String,
    val isCompleted: Boolean,
    val priority: Int = 0,
    val userId: String = "",
)
