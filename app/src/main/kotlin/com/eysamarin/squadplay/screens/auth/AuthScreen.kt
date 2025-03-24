package com.eysamarin.squadplay.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: UiState<AuthScreenUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (AuthScreenAction) -> Unit
) {
    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
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
        }
    )
}

@Composable
private fun AuthScreenMediumLayout(
    state: UiState<AuthScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (AuthScreenAction) -> Unit
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null
        )
        Text(text = state.data.title, style = adaptiveHeadlineByHeight(windowSize))
        Spacer(modifier = Modifier.height(24.dp))

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            label = { Text("Email") },
        )
        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            label = { Text("Password") },
        )
        PrimaryButton(
            windowSize = windowSize,
            text = "Sign in",
            onTap = { onAction(AuthScreenAction.OnSignInTap(email, password)) },
        )

        if (state.data.isSignButtonVisible) {
            GoogleButton(
                onTap = { onAction(AuthScreenAction.OnSignUpTap) },
            )
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
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = state.data.title, style = adaptiveHeadlineByHeight(windowSize))
        PrimaryButton(windowSize, text = "Sign UP with Google", onTap = {
            onAction(AuthScreenAction.OnSignUpTap)
        })
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