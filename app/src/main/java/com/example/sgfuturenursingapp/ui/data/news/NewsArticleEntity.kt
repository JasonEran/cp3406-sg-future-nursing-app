package com.example.sgfuturenursingapp.ui.data.news

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val sourceName: String?,
    val publishedAt: String?,
    val savedAt: Long = System.currentTimeMillis(),
)

