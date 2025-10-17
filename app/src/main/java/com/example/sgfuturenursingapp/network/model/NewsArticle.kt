package com.example.sgfuturenursingapp.network.model

import com.google.gson.annotations.SerializedName

data class NewsArticle(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    @SerializedName("urlToImage")
    val imageUrl: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    val source: NewsSource? = null,
)

data class NewsSource(
    val id: String? = null,
    val name: String? = null,
)

