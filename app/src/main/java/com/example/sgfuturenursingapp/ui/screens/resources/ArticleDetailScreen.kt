@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.resources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title.ifBlank { "文章详情" }) },
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
        ArticleContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            uiState = uiState,
            onRetry = viewModel::reload,
        )
    }
}

@Composable
private fun ArticleContent(
    modifier: Modifier,
    uiState: ArticleDetailUiState,
    onRetry: () -> Unit,
) {
    when {
        uiState.isLoading -> LoadingState(modifier)
        uiState.errorMessage != null -> ErrorState(modifier, uiState.errorMessage, onRetry)
        else ->
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (uiState.summary.isNotBlank()) {
                    Text(
                        text = uiState.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = uiState.content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "加载中…",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text("重新加载")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleContentPreview() {
    CP3406SGFutureNursingAppTheme {
        ArticleContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            uiState =
                ArticleDetailUiState(
                    isLoading = false,
                    title = "正确护理床上患者的关键要点",
                    summary = "掌握体位变换与皮肤护理方法，有助于降低压疮风险。",
                    content = "1. 每两小时为患者翻身一次。\n2. 保持皮肤清洁干燥。\n3. 使用减压床垫。\n4. 注意营养和水分补充。",
                ),
            onRetry = {},
        )
    }
}
