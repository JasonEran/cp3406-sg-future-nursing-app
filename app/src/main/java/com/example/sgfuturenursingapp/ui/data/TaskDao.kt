package com.example.sgfuturenursingapp.ui.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, completed: Boolean)
}
