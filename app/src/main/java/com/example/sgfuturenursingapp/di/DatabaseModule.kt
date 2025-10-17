package com.example.sgfuturenursingapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sgfuturenursingapp.ui.data.AppDatabase
import com.example.sgfuturenursingapp.ui.data.TaskDao
import com.example.sgfuturenursingapp.ui.data.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        databaseCallback: RoomDatabase.Callback
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "sg_future_nursing_app.db"
    ).fallbackToDestructiveMigration()
        .addCallback(databaseCallback)
        .build()

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao = appDatabase.taskDao()

    @Provides
    @Singleton
    fun provideDatabaseCallback(
        @ApplicationContext context: Context,
        gson: Gson,
        taskDaoProvider: Provider<TaskDao>
    ): RoomDatabase.Callback = object : RoomDatabase.Callback() {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                val tasks = loadSeedTasks(context, gson)
                if (tasks.isNotEmpty()) {
                    taskDaoProvider.get().insertTasks(tasks)
                }
            }
        }

        private fun loadSeedTasks(context: Context, gson: Gson): List<Task> =
            runCatching {
                context.assets.open("tasks.json").use { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        val type = object : TypeToken<List<Task>>() {}.type
                        gson.fromJson<List<Task>>(reader, type) ?: emptyList()
                    }
                }
            }.getOrElse { emptyList() }
    }
}
