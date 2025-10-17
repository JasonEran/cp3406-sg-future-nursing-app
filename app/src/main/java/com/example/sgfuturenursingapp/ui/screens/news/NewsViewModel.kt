package com.example.sgfuturenursingapp.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.network.model.NewsArticle
import com.example.sgfuturenursingapp.ui.data.news.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<NewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class NewsViewModel
    @Inject
    constructor(
        private val newsRepository: NewsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NewsUiState(isLoading = true))
        val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

        init {
            refreshNews()
        }

        fun refreshNews(country: String = "us") {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                val result = newsRepository.getTopHeadlines(country = country)
                result.fold(
                    onSuccess = { articles ->
                        _uiState.update {
                            it.copy(
                                articles = articles.filter { article ->
                                    !article.title.isNullOrBlank()
                                },
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = throwable.message
                                    ?: "Unable to load health news right now.",
                            )
                        }
                    },
                )
            }
        }
    }

