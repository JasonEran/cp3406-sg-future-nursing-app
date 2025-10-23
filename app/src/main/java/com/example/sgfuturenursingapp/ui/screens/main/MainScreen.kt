@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.network.model.NewsArticle
import com.example.sgfuturenursingapp.network.model.NewsSource
import com.example.sgfuturenursingapp.ui.data.Task
import com.example.sgfuturenursingapp.ui.navigation.ScreenRoutes
import com.example.sgfuturenursingapp.ui.screens.news.NewsUiState
import com.example.sgfuturenursingapp.ui.screens.news.NewsViewModel
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@Composable
fun MainScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    mainViewModel: MainViewModel = hiltViewModel(),
    newsViewModel: NewsViewModel = hiltViewModel(),
) {
    val overviewUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()

    MainScreen(
        windowWidthSizeClass = windowSizeClass.widthSizeClass,
        overviewUiState = overviewUiState,
        newsUiState = newsUiState,
        onTaskClick = { taskId ->
            navController.navigate("${ScreenRoutes.TASK_DETAIL}/$taskId") {
                launchSingleTop = true
            }
        },
        onViewAllTasks = {
            navController.navigate(ScreenRoutes.DASHBOARD) {
                launchSingleTop = true
            }
        },
        onNewsClick = {
            navController.navigate(ScreenRoutes.HEALTH_NEWS) {
                launchSingleTop = true
            }
        },
        onResourcesClick = {
            navController.navigate(ScreenRoutes.RESOURCES) {
                launchSingleTop = true
            }
        },
        onProfileClick = {
            navController.navigate(ScreenRoutes.PROFILE) {
                launchSingleTop = true
            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    overviewUiState: MainUiState,
    newsUiState: NewsUiState,
    onTaskClick: (Int) -> Unit,
    onViewAllTasks: () -> Unit,
    onNewsClick: () -> Unit,
    onResourcesClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val horizontalPadding =
        when (windowWidthSizeClass) {
            WindowWidthSizeClass.Compact -> 20.dp
            WindowWidthSizeClass.Medium -> 32.dp
            WindowWidthSizeClass.Expanded -> 48.dp
            else -> 24.dp
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.main_home_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = stringResource(id = R.string.main_profile_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = 16.dp,
                        bottom = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    TodayOverviewCard(
                        uiState = overviewUiState,
                        onViewAllTasks = onViewAllTasks,
                    )
                }

                item {
                    UrgentTasksSection(
                        tasks = overviewUiState.urgentTasks,
                        isLoading = overviewUiState.isLoading,
                        errorMessage = overviewUiState.errorMessage,
                        onTaskClick = onTaskClick,
                        onViewAllTasks = onViewAllTasks,
                    )
                }

                item {
                    LatestNewsSection(
                        newsUiState = newsUiState,
                        onNewsClick = onNewsClick,
                        onResourcesClick = onResourcesClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayOverviewCard(
    uiState: MainUiState,
    onViewAllTasks: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.main_overview_card_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(id = R.string.main_overview_card_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
                TextButton(
                    onClick = onViewAllTasks,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                ) {
                    Text(stringResource(id = R.string.main_overview_view_schedule))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp),
                    )
                }
            }

            if (uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OverviewMetric(
                        label = stringResource(id = R.string.main_overview_total),
                        value = uiState.totalTasks,
                    )
                    OverviewMetric(
                        label = stringResource(id = R.string.main_overview_completed),
                        value = uiState.completedTasks,
                    )
                    OverviewMetric(
                        label = stringResource(id = R.string.main_overview_pending),
                        value = uiState.pendingTasks,
                        highlight = true,
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: Int,
    highlight: Boolean = false,
) {
    val textColor =
        if (highlight) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun UrgentTasksSection(
    tasks: List<Task>,
    isLoading: Boolean,
    errorMessage: String?,
    onTaskClick: (Int) -> Unit,
    onViewAllTasks: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.main_urgent_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            TextButton(onClick = onViewAllTasks) {
                Text(stringResource(id = R.string.main_urgent_view_all))
            }
        }

        when {
            isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
                }
            }

            tasks.isEmpty() -> {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.main_urgent_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(id = R.string.main_urgent_empty_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onViewAllTasks) {
                            Text(stringResource(id = R.string.main_urgent_empty_cta))
                        }
                    }
                }
            }

            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(tasks) { task ->
                        UrgentTaskCard(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                        )
                    }
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun UrgentTaskCard(
    task: Task,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = task.time.ifBlank { stringResource(id = R.string.main_urgent_time_unset) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LatestNewsSection(
    newsUiState: NewsUiState,
    onNewsClick: () -> Unit,
    onResourcesClick: () -> Unit,
) {
    val articles = newsUiState.articles.take(NEWS_ITEM_LIMIT)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.main_news_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onNewsClick) {
                    Text(stringResource(id = R.string.main_news_more))
                }
                TextButton(onClick = onResourcesClick) {
                    Text(stringResource(id = R.string.main_news_resources))
                }
            }
        }

        if (newsUiState.isLoading && articles.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (articles.isEmpty() && !newsUiState.isLoading) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.main_news_empty),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.main_news_empty_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                articles.forEach { article ->
                    NewsPreviewCard(
                        article = article,
                        onClick = onNewsClick,
                    )
                }
            }
        }

        newsUiState.errorMessage?.takeIf { articles.isNotEmpty() }?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun NewsPreviewCard(
    article: NewsArticle,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = article.title.orEmpty().ifBlank { stringResource(id = R.string.main_news_title_fallback) },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            article.source?.name?.takeUnless { it.isBlank() }?.let { source ->
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private const val NEWS_ITEM_LIMIT = 3

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    CP3406SGFutureNursingAppTheme {
        MainScreen(
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            overviewUiState =
                MainUiState(
                    totalTasks = 6,
                    completedTasks = 3,
                    pendingTasks = 3,
                    urgentTasks = previewTasks,
                    isLoading = false,
                ),
            newsUiState =
                NewsUiState(
                    articles = previewArticles,
                    isLoading = false,
                    isRefreshing = false,
                ),
            onTaskClick = {},
            onViewAllTasks = {},
            onNewsClick = {},
            onResourcesClick = {},
            onProfileClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 840)
@Composable
private fun MainScreenExpandedPreview() {
    CP3406SGFutureNursingAppTheme {
        MainScreen(
            windowWidthSizeClass = WindowWidthSizeClass.Expanded,
            overviewUiState =
                MainUiState(
                    totalTasks = 8,
                    completedTasks = 5,
                    pendingTasks = 3,
                    urgentTasks = previewTasks,
                    isLoading = false,
                ),
            newsUiState =
                NewsUiState(
                    articles = previewArticles,
                    isLoading = false,
                    isRefreshing = false,
                ),
            onTaskClick = {},
            onViewAllTasks = {},
            onNewsClick = {},
            onResourcesClick = {},
            onProfileClick = {},
        )
    }
}

private val previewTasks =
    listOf(
        Task(
            id = 1,
            title = "早间服药",
            time = "08:00",
            category = "Medication",
            iconName = "medical_services",
            isCompleted = false,
            priority = 3,
            userId = "admin",
        ),
        Task(
            id = 2,
            title = "血压监测",
            time = "10:30",
            category = "Monitoring",
            iconName = "monitor_heart",
            isCompleted = false,
            priority = 2,
            userId = "admin",
        ),
        Task(
            id = 3,
            title = "午后康复训练",
            time = "15:00",
            category = "Therapy",
            iconName = "fitness_center",
            isCompleted = false,
            priority = 1,
            userId = "admin",
        ),
    )

private val previewArticles =
    listOf(
        NewsArticle(
            title = "护理团队通过数字化协作提升效率",
            description = null,
            url = "https://example.com/article1",
            imageUrl = null,
            publishedAt = "2025-10-20T10:00:00Z",
            source = NewsSource(name = "Health Daily"),
        ),
        NewsArticle(
            title = "远程医疗助力社区护理覆盖更多居民",
            description = null,
            url = "https://example.com/article2",
            imageUrl = null,
            publishedAt = "2025-10-19T09:30:00Z",
            source = NewsSource(name = "Nurse Weekly"),
        ),
        NewsArticle(
            title = "新的营养指南强调个性化护理计划",
            description = null,
            url = "https://example.com/article3",
            imageUrl = null,
            publishedAt = "2025-10-18T08:45:00Z",
            source = NewsSource(name = "Caregiver News"),
        ),
    )
