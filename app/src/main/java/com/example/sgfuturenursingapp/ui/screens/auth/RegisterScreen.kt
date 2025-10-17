package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onRegister = { email, password -> viewModel.register(email, password) },
        onNavigateToLogin = onNavigateToLogin,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: AuthUiState,
    onRegister: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Create account",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign up to start tracking your health tasks with ease.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            RegisterForm(
                isLoading = uiState.isLoading,
                onSubmit = onRegister,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(text = "Already have an account? Sign in")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    onSubmit: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(
                    text = "At least 6 characters.",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (email.isNotBlank() && password.length >= 6 && !isLoading) {
                            focusManager.clearFocus()
                            onSubmit(email.trim(), password)
                        }
                    },
                ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && password.length >= 6 && !isLoading) {
                    focusManager.clearFocus()
                    onSubmit(email.trim(), password)
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(text = if (isLoading) "Creating account..." else "Create account")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreenContent(
        uiState = AuthUiState(),
        onRegister = { _, _ -> },
        onNavigateToLogin = {},
        snackbarHostState = SnackbarHostState(),
    )
}
