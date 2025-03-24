package com.eysamarin.squadplay.screens.registration

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.PREVIEW_REGISTRATION_SCREEN_UI
import com.eysamarin.squadplay.models.RegistrationScreenAction
import com.eysamarin.squadplay.models.RegistrationScreenUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.button.SecondaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    state: UiState<RegistrationScreenUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    if (state !is UiState.Normal) return

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(RegistrationScreenAction.OnBackButtonTap) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
                title = {
                Text(state.data.title)
            })
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium,
                    WindowWidthSizeClass.Expanded -> RegistrationMediumLayout(
                        state, windowSize, onAction
                    )
                }
            }
        },
    )
}

@Composable
private fun RegistrationMediumLayout(
    state: UiState<RegistrationScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    if (state !is UiState.Normal) return

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

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            OutlinedTextField(
                value = email,
                isError = emailHasErrors,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                label = { Text("Email") },
                supportingText = {
                    if (emailHasErrors) {
                        Text("Incorrect email format.")
                    }
                }
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Password") },
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Confirm password") },
            )
        }
        item {
            Spacer(Modifier.width(24.dp))
            SecondaryButton(
                modifier = Modifier.width(OutlinedTextFieldDefaults.MinWidth),
                windowSize = windowSize,
                text = "Confirm",
                onTap = { onAction(RegistrationScreenAction.OnConfirmTap) },
            )
        }
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun RegistrationScreenPhonePreview() {
    SquadPlayTheme {
        RegistrationScreen(
            state = UiState.Normal(PREVIEW_REGISTRATION_SCREEN_UI),
            onAction = {},
        )
    }
}
//endregion