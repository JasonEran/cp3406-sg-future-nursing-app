package com.example.sgfuturenursingapp.ui.screens.profile

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.ui.localization.AppLanguage
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

    val context = LocalContext.current
    val errorMessage = uiState.errorMessage
    val errorMessageRes = uiState.errorMessageRes

    LaunchedEffect(errorMessage, errorMessageRes) {
        when {
            errorMessageRes != null ->
                snackbarHostState.showSnackbar(context.getString(errorMessageRes))
            !errorMessage.isNullOrBlank() ->
                snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.profile_title),
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
            onLanguageSelected = viewModel::onLanguageSelected,
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
    onLanguageSelected: (AppLanguage) -> Unit,
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

        LanguageSection(
            selectedLanguage = uiState.selectedLanguage,
            isUpdating = uiState.isUpdatingLanguage,
            onLanguageSelected = onLanguageSelected,
        )

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
                contentDescription = stringResource(id = R.string.profile_title),
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .padding(end = 20.dp)
                        .size(56.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text =
                        uiState.email.ifBlank {
                            stringResource(id = R.string.profile_email_unknown)
                        },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text =
                        uiState.role.ifBlank {
                            stringResource(id = R.string.profile_role_unknown)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    isUpdating: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    val options = remember { AppLanguage.values() }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.profile_language_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = stringResource(id = R.string.profile_language_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text =
                    stringResource(
                        id = R.string.profile_language_current,
                        stringResource(id = selectedLanguage.displayNameRes),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                options.forEach { option ->
                    FilterChip(
                        selected = option == selectedLanguage,
                        onClick = { onLanguageSelected(option) },
                        label = { Text(text = stringResource(id = option.displayNameRes)) },
                    )
                }
            }

            if (isUpdating) {
                Text(
                    text = stringResource(id = R.string.profile_language_updating),
                    style = MaterialTheme.typography.bodySmall,
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
                    text = stringResource(id = R.string.profile_care_group_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                if (uiState.canManageTeam) {
                    TextButton(onClick = onManageTeam) {
                        Text(text = stringResource(id = R.string.profile_manage_team))
                    }
                }
            }

            val careGroupName =
                if (uiState.careGroupName.isBlank()) {
                    stringResource(id = R.string.profile_care_group_not_assigned)
                } else {
                    stringResource(id = R.string.profile_care_group_label, uiState.careGroupName)
                }

            Text(
                text = careGroupName,
                style = MaterialTheme.typography.bodyLarge,
            )

            HorizontalDivider()

            Text(
                text = stringResource(id = R.string.profile_members_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            if (uiState.members.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.profile_members_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.members.forEach { member ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text =
                                    member.email.ifBlank {
                                        stringResource(
                                            id = R.string.profile_care_group_unknown_member,
                                            member.uid,
                                        )
                                    },
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text =
                                    member.role.ifBlank {
                                        stringResource(id = R.string.profile_role_unknown)
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            TextButton(onClick = onRefresh) {
                Text(text = stringResource(id = R.string.profile_refresh))
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
                text = stringResource(id = R.string.profile_settings_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.profile_notifications_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(id = R.string.profile_notifications_description),
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
                Text(text = stringResource(id = R.string.profile_logout_button))
            }
        }
        Text(
            text = stringResource(id = R.string.profile_logout_hint),
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
                    careGroupName = "Care Group A",
                    members =
                        listOf(
                            CareGroupMemberUi(uid = "1", email = "admin@example.com", role = "Admin"),
                            CareGroupMemberUi(uid = "2", email = "helper@example.com", role = "Helper"),
                        ),
                    canManageTeam = true,
                    notificationEnabled = true,
                    selectedLanguage = AppLanguage.ENGLISH,
                ),
            paddingTop = 0.dp,
            paddingBottom = 0.dp,
            onManageTeam = {},
            onRefresh = {},
            onToggleNotification = {},
            onLanguageSelected = {},
            onLogout = {},
        )
    }
}
