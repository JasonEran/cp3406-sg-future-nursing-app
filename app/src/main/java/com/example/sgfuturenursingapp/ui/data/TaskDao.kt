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

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    fun getTasks(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun getTaskById(
        taskId: Int,
        userId: String,
    ): Task?

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId AND userId = :userId")
    suspend fun updateTaskCompletion(
        taskId: Int,
        completed: Boolean,
        userId: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT MAX(id) FROM tasks WHERE userId = :userId")
    suspend fun getMaxTaskId(userId: String): Int?

    @Query("DELETE FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun deleteTask(
        taskId: Int,
        userId: String,
    )
}
