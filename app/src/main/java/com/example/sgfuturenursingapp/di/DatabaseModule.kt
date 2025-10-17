package com.example.sgfuturenursingapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sgfuturenursingapp.ui.data.AppDatabase
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.data.TaskDao
import com.example.sgfuturenursingapp.ui.data.news.NewsDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

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
        databaseCallback: RoomDatabase.Callback,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sg_future_nursing_app.db",
        ).addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .addCallback(databaseCallback)
            .build()

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao = appDatabase.taskDao()

    @Provides
    fun provideNewsDao(appDatabase: AppDatabase): NewsDao = appDatabase.newsDao()

    @Provides
    @Singleton
    fun provideDatabaseCallback(
        @ApplicationContext context: Context,
        gson: Gson,
        taskDaoProvider: Provider<TaskDao>,
    ): RoomDatabase.Callback =
        object : RoomDatabase.Callback() {
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

            private fun loadSeedTasks(
                context: Context,
                gson: Gson,
            ): List<Task> =
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
