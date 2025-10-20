package com.example.sgfuturenursingapp.ui.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sgfuturenursingapp.ui.data.news.NewsArticleEntity
import com.example.sgfuturenursingapp.ui.data.news.NewsDao

@Database(entities = [NewsArticleEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}
