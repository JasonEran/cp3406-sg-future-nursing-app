package com.example.sgfuturenursingapp.ui.data.news

import com.example.sgfuturenursingapp.BuildConfig
import com.example.sgfuturenursingapp.network.NewsApiService
import com.example.sgfuturenursingapp.network.model.NewsArticle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository
    @Inject
    constructor(
        private val newsApiService: NewsApiService,
    ) {
        suspend fun getTopHeadlines(
            country: String = "us",
            pageSize: Int = 20,
            apiKey: String = BuildConfig.NEWS_API_KEY,
        ): Result<List<NewsArticle>> =
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
                response.articles
            }
    }

