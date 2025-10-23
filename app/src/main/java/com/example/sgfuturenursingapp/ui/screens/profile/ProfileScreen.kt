@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.ui.theme.CP3406SGFutureNursingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateUp: () -> Unit,
    onManageTeam: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            viewModel.consumeLogoutSuccess()
            onLogoutSuccess()
        }
    }

    LaunchedEffect(uiState.logoutError) {
        uiState.logoutError?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage
            ?.takeIf { !uiState.isLoading }
            ?.let { message -> snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u4e2a\u4eba\u8d44\u6599") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "\u8fd4\u56de",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        ProfileScreenContent(
            uiState = uiState,
            paddingTop = paddingValues.calculateTopPadding(),
            paddingBottom = paddingValues.calculateBottomPadding(),
            onManageTeam = onManageTeam,
            onRefresh = viewModel::refreshProfile,
            onToggleNotification = viewModel::setNotificationsEnabled,
            onLogout = viewModel::logout,
        )
    }
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    paddingTop: Dp,
    paddingBottom: Dp,
    onManageTeam: () -> Unit,
    onRefresh: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = paddingTop + 24.dp,
                    bottom = paddingBottom + 24.dp,
                ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        ProfileHeaderCard(uiState = uiState)

        CareGroupCard(
            uiState = uiState,
            onManageTeam = onManageTeam,
            onRefresh = onRefresh,
        )

        SettingsCard(
            notificationEnabled = uiState.notificationEnabled,
            onToggleNotification = onToggleNotification,
        )

        LogoutSection(
            isLoggingOut = uiState.isLoggingOut,
            onLogout = onLogout,
        )
    }
}

@Composable
private fun ProfileHeaderCard(uiState: ProfileUiState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "\u5934\u50cf",
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(64.dp)
                        .padding(end = 20.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = uiState.email.ifBlank { "\u672a\u77e5\u90ae\u7bb1" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "\u89d2\u8272\uff1a${uiState.role.ifBlank { "\u672a\u5b9a\u4e49" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CareGroupCard(
    uiState: ProfileUiState,
    onManageTeam: () -> Unit,
    onRefresh: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "\u62a4\u7406\u7ec4\u4fe1\u606f",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                if (uiState.canManageTeam) {
                    TextButton(onClick = onManageTeam) {
                        Text("\u7ba1\u7406\u56e2\u961f")
                    }
                }
            }

            Text(
                text = "\u62a4\u7406\u7ec4\uff1a${uiState.careGroupName.ifBlank { "\u672a\u52a0\u5165\u62a4\u7406\u7ec4" }}",
                style = MaterialTheme.typography.bodyLarge,
            )

            HorizontalDivider()

            Text(
                text = "\u6210\u5458\u5217\u8868",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            if (uiState.members.isEmpty()) {
                Text(
                    text = "\u6682\u65e0\u6210\u5458\u4fe1\u606f\u3002",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.members.forEach { member ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = member.email,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "\u89d2\u8272\uff1a${member.role.ifBlank { "\u672a\u5b9a\u4e49" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            TextButton(onClick = onRefresh) {
                Text("\u5237\u65b0\u4fe1\u606f")
            }
        }
    }
}

@Composable
private fun SettingsCard(
    notificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "\u8bbe\u7f6e",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "\u901a\u77e5\u8bbe\u7f6e",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "\u5f00\u542f\u540e\u5c06\u53ca\u65f6\u6536\u5230\u62a4\u7406\u4efb\u52a1\u63d0\u9192\u3002",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = onToggleNotification,
                )
            }
        }
    }
}

@Composable
private fun LogoutSection(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            enabled = !isLoggingOut,
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onError,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("\u9000\u51fa\u767b\u5f55")
            }
        }
        Text(
            text = "\u9000\u51fa\u540e\u5c06\u8fd4\u56de\u767b\u5f55\u9875\u9762\uff0c\u53ef\u91cd\u65b0\u5207\u6362\u5e10\u6237\u3002",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenContentPreview() {
    CP3406SGFutureNursingAppTheme {
        ProfileScreenContent(
            uiState =
                ProfileUiState(
                    isLoading = false,
                    email = "admin@example.com",
                    role = "Admin",
                    careGroupName = "\u5171\u62a4\u56e2\u961f A",
                    members =
                        listOf(
                            CareGroupMemberUi(uid = "1", email = "admin@example.com", role = "Admin"),
                            CareGroupMemberUi(uid = "2", email = "helper@example.com", role = "Helper"),
                        ),
                    canManageTeam = true,
                    notificationEnabled = true,
                ),
            paddingTop = 0.dp,
            paddingBottom = 0.dp,
            onManageTeam = {},
            onRefresh = {},
            onToggleNotification = {},
            onLogout = {},
        )
    }
}
