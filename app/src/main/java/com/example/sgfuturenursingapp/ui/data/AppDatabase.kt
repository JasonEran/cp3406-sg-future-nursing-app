package com.example.sgfuturenursingapp.ui.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sgfuturenursingapp.ui.data.news.NewsArticleEntity
import com.example.sgfuturenursingapp.ui.data.news.NewsDao

@Database(entities = [Task::class, NewsArticleEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun newsDao(): NewsDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS news_articles (
                            url TEXT NOT NULL,
                            title TEXT,
                            description TEXT,
                            imageUrl TEXT,
                            sourceName TEXT,
                            publishedAt TEXT,
                            savedAt INTEGER NOT NULL,
                            PRIMARY KEY(url)
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
