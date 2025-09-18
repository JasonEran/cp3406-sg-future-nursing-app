package com.example.sg_future_nursing_app.ui.data

package com.jasoneran.cp3406_sg_future_nursing_app.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Event

object DummyDataProvider {
    val tasks = listOf(
        Task(1, "Morning Medication", "08:00 AM", "Medication", Icons.Default.MedicalServices, true),
        Task(2, "Check Blood Pressure", "08:30 AM", "Health Check", Icons.Default.MonitorHeart, true),
        Task(3, "Doctor's Appointment", "11:00 AM", "Appointment", Icons.Default.Event, false),
        Task(4, "Afternoon Medication", "01:00 PM", "Medication", Icons.Default.MedicalServices, false),
        Task(5, "Evening Walk", "05:00 PM", "Activity", Icons.Default.MonitorHeart, false),
        Task(6, "Evening Medication", "08:00 PM", "Medication", Icons.Default.MedicalServices, false)
    )
}