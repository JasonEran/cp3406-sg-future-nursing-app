package com.example.sgfuturenursingapp.ui.screens.auth

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sgfuturenursingapp.R
import com.example.sgfuturenursingapp.ui.localization.AppLanguage

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onRegisterSuccess()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
        onRegister = viewModel::register,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val languageOptions = remember { AppLanguage.values() }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding(),
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
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.language_selector_label),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = MaterialTheme.typography.titleSmall.fontWeight),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    languageOptions.forEach { language ->
                        FilterChip(
                            selected = uiState.selectedLanguage == language,
                            onClick = { onLanguageSelected(language) },
                            label = {
                                Text(
                                    text = stringResource(id = language.displayNameRes),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            RegisterForm(
                uiState = uiState,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onSubmit = onRegister,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            val errorText =
                when {
                    uiState.errorMessageRes != null -> stringResource(id = uiState.errorMessageRes)
                    uiState.errorMessage.isNullOrBlank() -> null
                    else -> uiState.errorMessage
                }

            errorText?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                )
            } ?: Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(text = stringResource(id = R.string.register_nav_to_login))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RegisterForm(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(id = R.string.register_label_email)) },
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
            label = { Text(stringResource(id = R.string.register_label_password)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(
                    text = stringResource(id = R.string.register_password_hint),
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
            Text(
                text =
                    if (uiState.isLoading) {
                        stringResource(id = R.string.register_button_loading)
                    } else {
                        stringResource(id = R.string.register_button)
                    },
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreenContent(
        uiState = AuthUiState(),
        onEmailChanged = {},
        onPasswordChanged = {},
        onLanguageSelected = {},
        onRegister = {},
        onNavigateToLogin = {},
    )
}

