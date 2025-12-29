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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.RegistrationScreenAction
import com.eysamarin.squadplay.ui.button.SecondaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(RegistrationScreenAction.OnBackButtonTap) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.registration_screen_title))
                }
            )
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
                        windowSize = windowSize, onAction = onAction
                    )
                }
            }
        },
        snackbarHost = snackbarHost,
    )
}

@Composable
private fun RegistrationMediumLayout(
    windowSize: WindowSizeClass,
    onAction: (RegistrationScreenAction) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val confirmPasswordHasErrors by remember {
        derivedStateOf {
            if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
                password != confirmPassword
            } else {
                false
            }
        }
    }
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
                label = { Text(stringResource(R.string.label_email)) },
                supportingText = {
                    if (emailHasErrors) {
                        Text(stringResource(R.string.incorrect_email))
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
                label = { Text(stringResource(R.string.label_password)) },
            )
        }
        item {
            OutlinedTextField(
                value = confirmPassword,
                isError = confirmPasswordHasErrors,
                onValueChange = { confirmPassword = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(stringResource(R.string.confirm_password)) },
                supportingText = {
                    if (confirmPasswordHasErrors) {
                        Text(stringResource(R.string.password_does_not_match))
                    }
                }
            )
        }
        item {
            Spacer(Modifier.width(24.dp))
            SecondaryButton(
                enabled = password.isNotEmpty()
                        && !confirmPasswordHasErrors
                        && email.isNotEmpty()
                        && !emailHasErrors,
                modifier = Modifier.width(OutlinedTextFieldDefaults.MinWidth),
                windowSize = windowSize,
                text = stringResource(R.string.confirm),
                onTap = { onAction(RegistrationScreenAction.OnConfirmTap(email, password)) },
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
        RegistrationScreen(onAction = {})
    }
}
//endregion