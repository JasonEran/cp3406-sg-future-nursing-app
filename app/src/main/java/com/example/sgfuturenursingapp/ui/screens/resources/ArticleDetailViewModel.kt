package com.example.sgfuturenursingapp.ui.screens.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.ui.data.ResourceArticle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ArticleDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class ArticleDetailViewModel
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val articleId: String =
            savedStateHandle.get<String>(ARTICLE_ID_KEY)
                ?: throw IllegalArgumentException("Article id is required.")

        private val _uiState = MutableStateFlow(ArticleDetailUiState())
        val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

        init {
            loadArticle()
        }

        fun reload() {
            loadArticle()
        }

        private fun loadArticle() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                runCatching {
                    val snapshot =
                        firestore
                            .collection(RESOURCES_COLLECTION)
                            .document(articleId)
                            .get()
                            .await()

                    val article =
                        snapshot.toObject(ResourceArticle::class.java)
                            ?.copy(id = snapshot.id)
                            ?: error("未找到指定的资源文章。")

                    _uiState.value =
                        ArticleDetailUiState(
                            isLoading = false,
                            title = article.title,
                            summary = article.summary,
                            content = article.content,
                        )
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapDetailError(throwable),
                        )
                    }
                }
            }
        }

        private fun mapDetailError(throwable: Throwable): String =
            when {
                throwable is FirebaseFirestoreException &&
                    throwable.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "无法读取该文章，请检查 Firestore 安全规则是否允许访问 resources 集合。"
                else -> throwable.message ?: "无法加载文章详情，请稍后再试。"
            }

        companion object {
            const val ARTICLE_ID_KEY = "resourceId"
            private const val RESOURCES_COLLECTION = "resources"
        }
    }
