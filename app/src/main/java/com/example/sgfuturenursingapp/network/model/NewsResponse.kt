package com.example.sgfuturenursingapp.network.model

data class NewsResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<NewsArticle> = emptyList(),
    val code: String? = null,
    val message: String? = null,
)

