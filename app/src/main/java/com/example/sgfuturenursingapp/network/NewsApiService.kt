package com.example.sgfuturenursingapp.network

import com.example.sgfuturenursingapp.network.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHealthHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "health",
        @Query("pageSize") pageSize: Int = 20,
    ): NewsResponse
}

