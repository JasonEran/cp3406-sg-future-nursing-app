@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sgfuturenursingapp.ui.navigation.ScreenRoutes
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@Composable
fun MainScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
) {
    MainScreen(
        windowWidthSizeClass = windowSizeClass.widthSizeClass,
        onCarePlanClick = {
            navController.navigate(ScreenRoutes.DASHBOARD) {
                launchSingleTop = true
            }
        },
        onHealthNewsClick = {
            navController.navigate(ScreenRoutes.HEALTH_NEWS) {
                launchSingleTop = true
            }
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    onCarePlanClick: () -> Unit,
    onHealthNewsClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User profile",
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            FeatureSection(
                windowWidthSizeClass = windowWidthSizeClass,
                onCarePlanClick = onCarePlanClick,
                onHealthNewsClick = onHealthNewsClick,
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 72.dp),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp,
            ) {
                IconContainer(iconContent)
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IconContainer(iconContent: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        iconContent()
    }
}

@Composable
private fun FeatureSection(
    windowWidthSizeClass: WindowWidthSizeClass,
    onCarePlanClick: () -> Unit,
    onHealthNewsClick: () -> Unit,
) {
    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FeatureCard(
                    title = "Today's Care Plan",
                    description = "Review and manage today's nursing tasks.",
                    iconContent = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar representing today's care plan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    onClick = onCarePlanClick,
                )

                FeatureCard(
                    title = "Health Insights",
                    description = "Browse the latest health news and practical tips.",
                    iconContent = {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Health insights and news",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    onClick = onHealthNewsClick,
                )
            }
        }

        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded,
        -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                FeatureCard(
                    title = "Today's Care Plan",
                    description = "Review and manage today's nursing tasks.",
                    iconContent = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar representing today's care plan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp),
                        )
                    },
                    onClick = onCarePlanClick,
                    modifier = Modifier.weight(1f),
                )

                FeatureCard(
                    title = "Health Insights",
                    description = "Browse the latest health news and practical tips.",
                    iconContent = {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Health insights and news",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp),
                        )
                    },
                    onClick = onHealthNewsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    CP3406SGFutureNursingAppTheme {
        MainScreen(
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            onCarePlanClick = {},
            onHealthNewsClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 840)
@Composable
private fun MainScreenExpandedPreview() {
    CP3406SGFutureNursingAppTheme {
        MainScreen(
            windowWidthSizeClass = WindowWidthSizeClass.Expanded,
            onCarePlanClick = {},
            onHealthNewsClick = {},
        )
    }
}
