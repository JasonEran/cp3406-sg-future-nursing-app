@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateUp: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务分析") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.accessDenied -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "仅管理员可访问此页面。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            else -> {
                AnalyticsContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    uiState = uiState,
                )
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    modifier: Modifier,
    uiState: AnalyticsUiState,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "最近${uiState.lookbackDays}天护理任务概况",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        item {
            AnalyticsCard(
                title = "任务完成情况",
                subtitle = "已完成 / 未完成 / 待处理",
            ) {
                TaskStatusPieChart(
                    data = uiState.pieSegments,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(260.dp),
                )
            }
        }

        item {
            AnalyticsCard(
                title = "助手任务完成统计",
                subtitle = uiState.helperCountLabel.ifBlank { "统计结果" },
            ) {
                HelperBarChart(
                    data = uiState.helperTaskCounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .heightIn(min = 220.dp),
                )
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun TaskStatusPieChart(
    data: List<AnalyticsPieSegment>,
    modifier: Modifier = Modifier,
) {
    val total = data.sumOf { it.value }
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.tertiary,
        )

    if (total == 0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无任务数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val holeColor = MaterialTheme.colorScheme.background

        Canvas(modifier = Modifier.size(200.dp)) {
            val radius = min(size.width, size.height) / 2f
            var startAngle = -90f
            val center = Offset(size.width / 2f, size.height / 2f)

            data.forEachIndexed { index, segment ->
                val proportion = segment.value / total.toFloat()
                val sweep = 360f * proportion
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                )
                startAngle += sweep
            }

            drawCircle(
                color = holeColor,
                radius = radius * 0.55f,
                center = center,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            data.forEachIndexed { index, segment ->
                LegendRow(
                    indicatorColor = colors[index % colors.size],
                    label = segment.label,
                    value = segment.value,
                )
            }
        }
    }
}

@Composable
private fun HelperBarChart(
    data: List<HelperTaskCount>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无助手完成数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val maxValue = data.maxOf { it.completedCount }.coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
        ) {
            val chunk = size.width / (data.size * 2f + 1f)
            val baseLine = size.height

            data.forEachIndexed { index, helper ->
                val barHeightRatio = helper.completedCount / maxValue.toFloat()
                val barHeight = size.height * barHeightRatio
                val left = chunk * (index * 2 + 1)
                val right = left + chunk

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, baseLine - barHeight),
                    size = Size(right - left, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            data.forEach { helper ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.sizeIn(maxWidth = 120.dp),
                ) {
                    Text(
                        text = helper.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                    Text(
                        text = "${helper.completedCount} 次完成",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    indicatorColor: Color,
    label: String,
    value: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(14.dp)
                        .padding(1.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = indicatorColor)
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    CP3406SGFutureNursingAppTheme {
        AnalyticsContent(
            modifier = Modifier.fillMaxSize(),
            uiState =
                AnalyticsUiState(
                    isLoading = false,
                    pieSegments =
                        listOf(
                            AnalyticsPieSegment("已完成", 12),
                            AnalyticsPieSegment("未完成", 5),
                            AnalyticsPieSegment("待处理", 3),
                        ),
                    helperTaskCounts =
                        listOf(
                            HelperTaskCount("1", "helper1@example.com", 8),
                            HelperTaskCount("2", "helper2@example.com", 4),
                        ),
                    helperCountLabel = "统计范围：最近7天",
                    lookbackDays = 7,
                ),
        )
    }
}
