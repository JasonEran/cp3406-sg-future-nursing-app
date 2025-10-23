package com.example.sgfuturenursingapp.di

import com.example.sgfuturenursingapp.BuildConfig
import com.example.sgfuturenursingapp.network.NewsApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNewsApiKeyInterceptor(): Interceptor =
        Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            val apiKey = BuildConfig.NEWS_API_KEY
            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("X-Api-Key", apiKey)
            }
            chain.proceed(requestBuilder.build())
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyInterceptor: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level =
                        if (BuildConfig.DEBUG) {
                            HttpLoggingInterceptor.Level.BASIC
                        } else {
                            HttpLoggingInterceptor.Level.NONE
                        }
                },
            ).build()

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.NEWS_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)
}

