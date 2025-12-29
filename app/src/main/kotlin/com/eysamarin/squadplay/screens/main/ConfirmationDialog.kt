package com.eysamarin.squadplay.screens.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.button.PrimaryButton
import com.eysamarin.squadplay.ui.button.SecondaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.utils.DarkLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

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
            PrimaryButton(windowSize, stringResource(R.string.yes), onTap = onConfirmTap)
        },
        dismissButton = {
            SecondaryButton(windowSize, stringResource(R.string.no), onTap = onDismiss)
        },
    )
}

@DarkLightModePreview
@Composable
private fun ConfirmationDialogPreview() {
    SquadPlayTheme {
        ConfirmationDialog(
            windowSize = WINDOWS_SIZE_MEDIUM,
            title = "Title example",
            text = "Dialog example",
            onConfirmTap = {},
            onDismiss = {},
        )
    }
}