package com.example.sg_future_nursing_app.ui.data

package com.jasoneran.cp3406_sg_future_nursing_app.ui.data

import androidx.compose.ui.graphics.vector.ImageVector

data class Task(
    val id: Int,
    val title: String,
    val time: String,
    val category: String,
    val icon: ImageVector,
    val isCompleted: Boolean
)