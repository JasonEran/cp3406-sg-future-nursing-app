@file:Suppress("ktlint:standard:function-naming")

package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.BuildConfig
import com.example.sgfuturenursingapp.ui.demo.DemoModeController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onNavigateBack: () -> Unit,
    onAdminLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var roleError by remember { mutableStateOf<String?>(null) }
    var hasPrefilled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    LaunchedEffect(uiState.isAuthenticated, uiState.userEmail) {
        if (uiState.isAuthenticated) {
            val role = resolveRoleForEmail(uiState.userEmail.orEmpty())
            if (role == ADMIN_ROLE) {
                roleError = null
                onAdminLoginSuccess()
            } else {
                roleError = "The signed-in account does not have administrator access."
                viewModel.logout()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPrefilled) {
            if (BuildConfig.ADMIN_DEMO_EMAIL.isNotBlank()) {
                viewModel.onEmailChanged(BuildConfig.ADMIN_DEMO_EMAIL)
            }
            if (BuildConfig.ADMIN_DEMO_PASSWORD.isNotBlank()) {
                viewModel.onPasswordChanged(BuildConfig.ADMIN_DEMO_PASSWORD)
            }
            hasPrefilled = true
        }
    }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Administrator Sign In") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AdminLoginContent(
            uiState = uiState,
            roleError = roleError,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLogin = viewModel::login,
            onSkipLogin = {
                DemoModeController.enableDemoMode()
                onAdminLoginSuccess()
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}

@Composable
private fun AdminLoginContent(
    uiState: AuthUiState,
    roleError: String?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onSkipLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier =
            modifier
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Admin Access",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in with your administrator credentials to manage care teams and helpers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AdminLoginForm(
            uiState = uiState,
            onEmailChanged = onEmailChanged,
            onPasswordChanged = onPasswordChanged,
            onSubmit = onLogin,
            onSkipLogin = onSkipLogin,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val combinedError = roleError ?: uiState.errorMessage
        combinedError?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun AdminLoginForm(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkipLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = { Text("Admin Email") },
            singleLine = true,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (uiState.isFormValid && !uiState.isLoading) {
                            focusManager.clearFocus()
                            onSubmit()
                        }
                    },
                ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (uiState.isFormValid && !uiState.isLoading) {
                    focusManager.clearFocus()
                    onSubmit()
                }
            },
            enabled = !uiState.isLoading && uiState.isFormValid,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(text = if (uiState.isLoading) "Signing in..." else "Sign in")
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    onSkipLogin()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
            ) {
                Text(text = "Skip login (development only)")
            }
        }
    }
}

private fun resolveRoleForEmail(email: String): String =
    when {
        email.contains("admin", ignoreCase = true) -> "Admin"
        email.contains("primary", ignoreCase = true) -> "Primary Caregiver"
        else -> "Helper"
    }

private const val ADMIN_ROLE = "Admin"
