@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.ui.data.ResourceArticle
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    onNavigateUp: () -> Unit,
    onArticleClick: (ResourceArticle) -> Unit,
    viewModel: ResourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资源中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { paddingValues ->
        ResourcesContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            uiState = uiState,
            onCategorySelected = viewModel::onCategorySelected,
            onArticleClick = onArticleClick,
        )
    }
}

@Composable
private fun ResourcesContent(
    modifier: Modifier,
    uiState: ResourcesUiState,
    onCategorySelected: (ResourceCategory) -> Unit,
    onArticleClick: (ResourceArticle) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        CategorySelector(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "加载中…",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage,
                )
            }

            uiState.articles.isEmpty() -> {
                EmptyState()
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.articles, key = { it.id }) { article ->
                        ResourceCard(
                            article = article,
                            onClick = { onArticleClick(article) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<ResourceCategory>,
    selectedCategory: ResourceCategory,
    onCategorySelected: (ResourceCategory) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun ResourceCard(
    article: ResourceArticle,
    onClick: () -> Unit,
) {
    val categoryLabel =
        ResourceCategory.defaults.firstOrNull { it.id == article.category }?.label
            ?: article.category.ifBlank { "综合内容" }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            ChipLabel(text = categoryLabel)
            if (article.summary.isNotBlank()) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChipLabel(text: String) {
    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无相关资源，敬请期待更新。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResourcesScreenPreview() {
    val sampleArticles =
        listOf(
            ResourceArticle(
                id = "1",
                title = "日常护理关键步骤",
                summary = "了解如何在家中进行安全有效的日常护理，保障长者舒适。",
                content = "",
                category = "日常护理",
            ),
            ResourceArticle(
                id = "2",
                title = "正确服药的 5 个要点",
                summary = "掌握日常用药注意事项，降低药物副作用风险。",
                content = "",
                category = "用药知识",
            ),
        )
    CP3406SGFutureNursingAppTheme {
        ResourcesContent(
            modifier = Modifier.fillMaxSize(),
            uiState =
                ResourcesUiState(
                    articles = sampleArticles,
                    isLoading = false,
                    errorMessage = null,
                ),
            onCategorySelected = {},
            onArticleClick = {},
        )
    }
}
