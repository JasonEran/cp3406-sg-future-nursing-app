package com.example.sgfuturenursingapp.ui.data

object DummyDataProvider {
    val tasks = listOf(
        Task(1, "Morning Medication", "08:00 AM", "Medication", "medical_services", true),
        Task(2, "Check Blood Pressure", "08:30 AM", "Health Check", "monitor_heart", true),
        Task(3, "Doctor's Appointment", "11:00 AM", "Appointment", "event", false),
        Task(4, "Afternoon Medication", "01:00 PM", "Medication", "medical_services", false),
        Task(5, "Evening Walk", "05:00 PM", "Activity", "monitor_heart", false),
        Task(6, "Evening Medication", "08:00 PM", "Medication", "medical_services", false)
    )
}
