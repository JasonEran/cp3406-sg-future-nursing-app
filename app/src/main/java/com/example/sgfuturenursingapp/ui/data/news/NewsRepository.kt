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
import retrofit2.HttpException

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
        ): Result<Unit> {
            if (apiKey.isBlank()) {
                return Result.success(Unit)
            }

            val result =
                runCatching {
                    val response =
                        newsApiService.getTopHealthHeadlines(
                            country = country,
                            pageSize = pageSize,
                            apiKey = apiKey,
                        )

                    if (!response.status.equals("ok", ignoreCase = true)) {
                        throw IllegalStateException(mapNewsApiError(response.code, response.message))
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

            return result.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { throwable ->
                    Result.failure(
                        IllegalStateException(
                            normalizeErrorMessage(throwable),
                            throwable,
                        ),
                    )
                },
            )
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

        private fun mapNewsApiError(code: String?, message: String?): String {
            val normalizedCode = code?.lowercase()
            if (normalizedCode != null && normalizedCode.contains("apikey")) {
                return GENERIC_NEWS_ERROR_MESSAGE
            }
            if (message != null && message.contains("api key", ignoreCase = true)) {
                return GENERIC_NEWS_ERROR_MESSAGE
            }
            return message ?: GENERIC_NEWS_ERROR_MESSAGE
        }

        private fun normalizeErrorMessage(throwable: Throwable): String {
            if (throwable is HttpException && throwable.code() == 401) {
                return GENERIC_NEWS_ERROR_MESSAGE
            }
            val message = throwable.message.orEmpty()
            return if (message.contains("api key", ignoreCase = true) || message.contains("apikey", ignoreCase = true)) {
                GENERIC_NEWS_ERROR_MESSAGE
            } else {
                message.ifBlank { GENERIC_NEWS_ERROR_MESSAGE }
            }
        }

        private companion object {
            const val GENERIC_NEWS_ERROR_MESSAGE = "Health news is currently unavailable. Please try again later."
        }
    }

