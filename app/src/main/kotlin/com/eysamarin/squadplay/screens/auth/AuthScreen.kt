package com.eysamarin.squadplay.screens.auth

import android.util.Patterns
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.AuthScreenAction
import com.eysamarin.squadplay.models.AuthScreenUI
import com.eysamarin.squadplay.models.PREVIEW_AUTH_SCREEN_UI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.button.GoogleButton
import com.eysamarin.squadplay.ui.button.PrimaryButton
import com.eysamarin.squadplay.ui.button.SecondaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: UiState<AuthScreenUI>,
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (AuthScreenAction) -> Unit,
) {
    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .imePadding()
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> AuthScreenMediumLayout(
                        state, windowSize, onAction
                    )

                    WindowWidthSizeClass.Expanded -> AuthScreenExpandedLayout(
                        state, windowSize, onAction
                    )
                }
            }
        },
        snackbarHost = snackbarHost,
    )
}

@Composable
private fun AuthScreenMediumLayout(
    state: UiState<AuthScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (AuthScreenAction) -> Unit
) {
    if (state !is UiState.Normal) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Unspecified
                },
            )
            Text(text = state.data.title, style = adaptiveHeadlineByHeight(windowSize))
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            EmailPasswordSignIn(windowSize, onAction)
        }
        item {
            if (state.data.isSignButtonVisible) {
                Spacer(modifier = Modifier.height(24.dp))
                GoogleButton(
                    onTap = { onAction(AuthScreenAction.OnSignInWithGoogleTap) },
                )
            }
        }
    }
}

@Composable
private fun AuthScreenExpandedLayout(
    state: UiState<AuthScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (AuthScreenAction) -> Unit
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = state.data.title, style = adaptiveHeadlineByHeight(windowSize))
        EmailPasswordSignIn(windowSize, onAction)
        GoogleButton(
            onTap = { onAction(AuthScreenAction.OnSignInWithGoogleTap) },
        )
    }
}

@Composable
private fun EmailPasswordSignIn(
    windowSize: WindowSizeClass,
    onAction: (AuthScreenAction) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val emailHasErrors by remember {
        derivedStateOf {
            if (email.isNotEmpty()) {
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            } else {
                false
            }
        }
    }

    OutlinedTextField(
        value = email,
        isError = emailHasErrors,
        onValueChange = { email = it },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        label = { Text("Email") },
        supportingText = {
            if (emailHasErrors) {
                Text("Incorrect email format.")
            }
        }
    )

    OutlinedTextField(
        value = password,
        maxLines = 1,
        onValueChange = { password = it },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        label = { Text("Password") },
    )
    Spacer(Modifier.height(16.dp))

    val isEmailValid = email.isNotEmpty() && !emailHasErrors
    Row(
        modifier = Modifier.width(OutlinedTextFieldDefaults.MinWidth),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryButton(
            modifier = Modifier.weight(1f),
            enabled = isEmailValid && password.isNotEmpty(),
            windowSize = windowSize,
            text = "Sign in",
            onTap = { onAction(AuthScreenAction.OnSignInTap(email, password)) },
        )
        SecondaryButton(
            windowSize = windowSize,
            text = "Sign Up",
            onTap = { onAction(AuthScreenAction.OnSignUpTap) },
        )
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun MainScreenPhonePreview() {
    SquadPlayTheme {
        AuthScreen(
            state = UiState.Normal(PREVIEW_AUTH_SCREEN_UI),
            onAction = {},
        )
    }
}
//endregion