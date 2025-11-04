package com.example.sgfuturenursingapp.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.network.model.NewsArticle
import com.example.sgfuturenursingapp.ui.data.news.NewsRepository
import com.example.sgfuturenursingapp.ui.demo.DemoContentProvider
import com.example.sgfuturenursingapp.ui.demo.DemoModeController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class NewsViewModel
    @Inject
    constructor(
        private val newsRepository: NewsRepository,
    ) : ViewModel() {
        private val isRefreshing = MutableStateFlow(true)
        private val errorState = MutableStateFlow<String?>(null)

        val uiState: StateFlow<NewsUiState> =
            combine(
                newsRepository.observeHealthNews(),
                isRefreshing,
                errorState,
                DemoModeController.isDemoModeEnabled,
            ) { articles, refreshing, error, isDemoMode ->
                if (isDemoMode) {
                    return@combine NewsUiState(
                        articles = DemoContentProvider.newsArticles,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                    )
                }
                NewsUiState(
                    articles = articles,
                    isLoading = refreshing && articles.isEmpty(),
                    isRefreshing = refreshing,
                    errorMessage = error,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsUiState(isLoading = true, isRefreshing = true),
            )

        init {
            refreshNews()
        }

        fun refreshNews(country: String = "us") {
            viewModelScope.launch {
                if (DemoModeController.isDemoModeEnabled.value) {
                    isRefreshing.value = false
                    errorState.value = null
                    return@launch
                }
                isRefreshing.value = true
                val result = newsRepository.refreshHealthNews(country = country)
                result
                    .onSuccess { errorState.value = null }
                    .onFailure { throwable ->
                        errorState.value =
                            throwable.message ?: "Unable to load health news right now."
                    }
                isRefreshing.value = false
            }
        }
    }
