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
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.RegistrationScreenAction


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
                    WindowWidthSizeClass.Expanded -> RegistrationMediumLayout(onAction = onAction)
                }
            }
        },
        snackbarHost = snackbarHost,
        containerColor = DesignSystemTheme.colorScheme.surface,
    )
}

@Composable
private fun RegistrationMediumLayout(
    onAction: (RegistrationScreenAction) -> Unit,
) {
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    val confirmPasswordHasErrors by remember {
        derivedStateOf {
            val passwordText = passwordState.text
            val confirmPasswordText = confirmPasswordState.text
            if (passwordText.isNotEmpty() || confirmPasswordText.isNotEmpty()) {
                passwordText.toString() != confirmPasswordText.toString()
            } else {
                false
            }
        }
    }
    val emailState = rememberTextFieldState()
    val emailHasErrors by remember {
        derivedStateOf {
            val emailText = emailState.text.toString()
            if (emailText.isNotEmpty()) {
                !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()
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
                state = emailState,
                isError = emailHasErrors,
                lineLimits = TextFieldLineLimits.SingleLine,
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
            OutlinedSecureTextField(
                state = passwordState,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(stringResource(R.string.label_password)) },
            )
        }
        item {
            OutlinedSecureTextField(
                state = confirmPasswordState,
                isError = confirmPasswordHasErrors,
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
            DSButton(
                enabled = passwordState.text.isNotEmpty()
                    && !confirmPasswordHasErrors
                    && emailState.text.isNotEmpty()
                    && !emailHasErrors,
                text = stringResource(R.string.confirm),
                onTap = { onAction(RegistrationScreenAction.OnConfirmTap(emailState.text.toString(), passwordState.text.toString())) },
            )
        }
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun RegistrationScreenPhonePreview() {
    DesignSystemTheme {
        RegistrationScreen(onAction = {})
    }
}
//endregion