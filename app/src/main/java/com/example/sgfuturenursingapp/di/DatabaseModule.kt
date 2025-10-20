package com.example.sgfuturenursingapp.di

import android.content.Context
import androidx.room.Room
import com.example.sgfuturenursingapp.ui.data.AppDatabase
import com.example.sgfuturenursingapp.ui.data.news.NewsDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sg_future_nursing_app.db",
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNewsDao(appDatabase: AppDatabase): NewsDao = appDatabase.newsDao()
}
