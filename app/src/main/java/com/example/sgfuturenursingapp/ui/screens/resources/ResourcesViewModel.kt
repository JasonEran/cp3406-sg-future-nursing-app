package com.example.sgfuturenursingapp.ui.screens.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgfuturenursingapp.ui.data.ResourceArticle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.snapshots
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ResourcesUiState(
    val categories: List<ResourceCategory> = ResourceCategory.defaults,
    val selectedCategory: ResourceCategory = ResourceCategory.defaults.first(),
    val articles: List<ResourceArticle> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ResourceCategory(
    val id: String,
    val label: String,
) {
    companion object {
        val defaults =
            listOf(
                ResourceCategory(id = "daily_care", label = "日常护理"),
                ResourceCategory(id = "medication", label = "用药知识"),
                ResourceCategory(id = "first_aid", label = "急救常识"),
            )
    }
}

@HiltViewModel
class ResourcesViewModel
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ResourcesUiState())
        val uiState: StateFlow<ResourcesUiState> = _uiState.asStateFlow()

        private var articlesJob: Job? = null

        init {
            loadArticlesForCategory(_uiState.value.selectedCategory)
        }

        fun onCategorySelected(category: ResourceCategory) {
            if (category == _uiState.value.selectedCategory) return
            _uiState.updateCategory(category)
            loadArticlesForCategory(category)
        }

        private fun loadArticlesForCategory(category: ResourceCategory) {
            articlesJob?.cancel()
            _uiState.setLoading()
            articlesJob =
                viewModelScope.launch {
                    firestore
                        .collection(RESOURCES_COLLECTION)
                        .whereEqualTo("category", category.id)
                        .snapshots()
                        .map { snapshot ->
                            snapshot.documents.mapNotNull { doc ->
                                doc.toObject(ResourceArticle::class.java)?.copy(id = doc.id)
                            }
                        }.catch { throwable ->
                            _uiState.updateError(mapResourcesError(throwable))
                        }.collect { articles ->
                            _uiState.updateArticles(articles)
                        }
                }
        }

        private fun MutableStateFlow<ResourcesUiState>.updateCategory(category: ResourceCategory) {
            value =
                value.copy(
                    selectedCategory = category,
                    isLoading = true,
                    errorMessage = null,
                )
        }

        private fun MutableStateFlow<ResourcesUiState>.setLoading() {
            value = value.copy(isLoading = true, errorMessage = null, articles = emptyList())
        }

        private fun MutableStateFlow<ResourcesUiState>.updateArticles(articles: List<ResourceArticle>) {
            value = value.copy(isLoading = false, articles = articles, errorMessage = null)
        }

        private fun MutableStateFlow<ResourcesUiState>.updateError(message: String) {
            value = value.copy(isLoading = false, errorMessage = message, articles = emptyList())
        }

        private fun mapResourcesError(throwable: Throwable): String =
            when {
                throwable is FirebaseFirestoreException &&
                    throwable.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "无法访问资源中心，请确认数据库安全规则允许当前用户读取 resources 集合。"
                else -> throwable.message ?: "无法加载资源，请稍后再试。"
            }

        companion object {
            private const val RESOURCES_COLLECTION = "resources"
        }
    }
