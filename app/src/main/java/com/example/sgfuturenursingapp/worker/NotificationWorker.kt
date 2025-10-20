package com.example.sgfuturenursingapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sgfuturenursingapp.MainActivity
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenBy

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                NotificationWorkerEntryPoint::class.java,
            )

        val currentUser = entryPoint.firebaseAuth().currentUser ?: return Result.success()
        val tasks = entryPoint.taskRepository().getTasks().firstOrNull().orEmpty()
        val pendingTasks = tasks.filter { !it.isCompleted && it.userId == currentUser.uid }

        if (pendingTasks.isEmpty()) {
            return Result.success()
        }

        val nextTask =
            pendingTasks
                .sortedWith(
                    compareByDescending<Task> { it.priority }.thenBy { it.time },
                ).first()

        showNotification(nextTask, pendingTasks.size)

        return Result.success()
    }

    private fun showNotification(task: Task, pendingCount: Int) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        createNotificationChannel(notificationManager)

        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val intent =
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val additionalTasksText =
            if (pendingCount > 1) {
                " - ${pendingCount - 1} more pending task(s)"
            } else {
                ""
            }

        val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(task.title)
                .setContentText("Scheduled at ${task.time} (${task.category})$additionalTasksText")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Task reminders",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Reminders for upcoming care tasks"
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "task_reminder_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationWorkerEntryPoint {
    fun taskRepository(): TaskRepository
    fun firebaseAuth(): FirebaseAuth
}
