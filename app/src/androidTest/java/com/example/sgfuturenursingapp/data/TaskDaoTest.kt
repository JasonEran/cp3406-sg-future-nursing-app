package com.example.sgfuturenursingapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sgfuturenursingapp.ui.data.AppDatabase
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskDao
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTask_thenReadBackById() = runBlocking {
        val userId = "user-1"
        val task =
            Task(
                id = 1,
                title = "Test Task",
                time = "08:00 AM",
                category = "Medication",
                iconName = "medical_services",
                isCompleted = false,
                priority = 0,
                userId = userId,
            )

        taskDao.insertTask(task)
        val loaded = taskDao.getTaskById(task.id, userId)

        assertNotNull(loaded)
        assertEquals(task.title, loaded.title)
        assertEquals(task.userId, loaded.userId)
    }

    @Test
    fun updateTaskCompletion_reflectedInQueries() = runBlocking {
        val userId = "user-2"
        val task =
            Task(
                id = 2,
                title = "Follow-up",
                time = "10:00 AM",
                category = "Check-up",
                iconName = "monitor_heart",
                isCompleted = false,
                priority = 1,
                userId = userId,
            )
        taskDao.insertTask(task)

        taskDao.updateTaskCompletion(task.id, completed = true, userId = userId)

        val updated = taskDao.getTaskById(task.id, userId)
        assertNotNull(updated)
        assertTrue(updated.isCompleted)
        val tasks = taskDao.getTasks(userId).first()
        assertEquals(1, tasks.size)
        assertTrue(tasks.first().isCompleted)
    }
}
