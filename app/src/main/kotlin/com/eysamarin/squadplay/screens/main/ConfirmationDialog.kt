package com.eysamarin.squadplay.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.ButtonStyle
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight

@Composable
fun ConfirmationDialog(
    windowSize: WindowSizeClass,
    title: String? = null,
    text: String? = null,
    onConfirmTap: () -> Unit,
    onDismiss: () -> Unit
) {
    val titleComposable: @Composable (() -> Unit)? = title?.let {
        { Text(it, style = adaptiveBodyByHeight(windowSize)) }
    }

    val textComposable: @Composable (() -> Unit)? = text?.let {
        { Text(it, style = adaptiveBodyByHeight(windowSize)) }
    }

    AlertDialog(
        title = titleComposable,
        text = textComposable,
        onDismissRequest = onDismiss,
        confirmButton = {
            DSButton(text = stringResource(R.string.yes), onTap = onConfirmTap)
        },
        dismissButton = {
            DSButton(text = stringResource(R.string.no), onTap = onDismiss, variant = ButtonStyle.Text)
        },
    )
}

@DarkLightModePreview
@Composable
private fun ConfirmationDialogPreview() {
    DesignSystemTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ConfirmationDialog(
                windowSize = WINDOWS_SIZE_MEDIUM,
                title = "Title example",
                text = "Dialog example",
                onConfirmTap = {},
                onDismiss = {},
            )
        }
    }
}