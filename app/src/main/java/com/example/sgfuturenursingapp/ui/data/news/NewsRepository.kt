package com.example.sgfuturenursingapp.ui.data.news

import androidx.room.withTransaction
import com.example.sgfuturenursingapp.BuildConfig
import com.example.sgfuturenursingapp.network.NewsApiService
import com.example.sgfuturenursingapp.network.model.NewsArticle
import com.example.sgfuturenursingapp.network.model.NewsSource
import com.example.sgfuturenursingapp.ui.data.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class NewsRepository
    @Inject
    constructor(
        private val newsApiService: NewsApiService,
        private val newsDao: NewsDao,
        private val appDatabase: AppDatabase,
    ) {
        fun observeHealthNews(): Flow<List<NewsArticle>> =
            newsDao.observeArticles().map { entities ->
                entities.map { it.toDomain() }
            }

        suspend fun refreshHealthNews(
            country: String = "us",
            pageSize: Int = 20,
            apiKey: String = BuildConfig.NEWS_API_KEY,
        ): Result<Unit> =
            runCatching {
                require(apiKey.isNotBlank()) {
                    "Missing News API key. Set NEWS_API_KEY in your gradle.properties file."
                }
                val response =
                    newsApiService.getTopHealthHeadlines(
                        country = country,
                        pageSize = pageSize,
                        apiKey = apiKey,
                    )
                if (!response.status.equals("ok", ignoreCase = true)) {
                    val errorMessage = response.message ?: "Unexpected response: ${response.status}"
                    throw IllegalStateException(errorMessage)
                }
                val entities =
                    response.articles
                        .filter { !it.url.isNullOrBlank() }
                        .map { it.toEntity() }

                appDatabase.withTransaction {
                    newsDao.clearArticles()
                    if (entities.isNotEmpty()) {
                        newsDao.insertArticles(entities)
                    }
                }
            }

        private fun NewsArticleEntity.toDomain(): NewsArticle =
            NewsArticle(
                title = title,
                description = description,
                url = url,
                imageUrl = imageUrl,
                publishedAt = publishedAt,
                source =
                    if (sourceName.isNullOrBlank()) {
                        null
                    } else {
                        NewsSource(name = sourceName)
                    },
            )

        private fun NewsArticle.toEntity(): NewsArticleEntity =
            NewsArticleEntity(
                url = requireNotNull(url) { "News article URL is required to cache the item." },
                title = title,
                description = description,
                imageUrl = imageUrl,
                sourceName = source?.name,
                publishedAt = publishedAt,
            )
    }
