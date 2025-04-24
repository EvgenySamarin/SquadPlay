package com.eysamarin.squadplay.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.utils.DarkLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

/**
 * @see <a href="https://developers.google.com/identity/branding-guidelines">
 *     Google Identity Guidelines</a>
 */
@Composable
fun GoogleButton(
    text: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        enabled = enabled,
        onClick = onTap,
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.inverseSurface
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_20),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text)
    }
}

@Composable
fun PrimaryButton(
    windowSize: WindowSizeClass,
    text: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier,
        onClick = onTap,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Text(
            text = text.uppercase(),
            style = adaptiveBodyByHeight(windowSize),
        )
    }
}

@Composable
fun SecondaryButton(
    windowSize: WindowSizeClass,
    text: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        enabled = enabled,
        modifier = modifier,
        onClick = onTap,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        )
    ) {
        Text(
            text = text.uppercase(),
            style = adaptiveBodyByHeight(windowSize),
        )
    }
}

@DarkLightModePreview
@Composable
fun PrimaryButtonPreview() {
    Column {
        PrimaryButton(windowSize = WINDOWS_SIZE_MEDIUM, text = "Primary button", onTap = {})
        PrimaryButton(
            windowSize = WINDOWS_SIZE_MEDIUM,
            text = "Primary disabled",
            enabled = false,
            onTap = {},
        )
        SecondaryButton(windowSize = WINDOWS_SIZE_MEDIUM, text = "Secondary button", onTap = {})
        SecondaryButton(
            windowSize = WINDOWS_SIZE_MEDIUM,
            text = "Secondary disabled",
            enabled = false,
            onTap = {},
        )
        GoogleButton(text = "Sign in with Google", {})
    }
}