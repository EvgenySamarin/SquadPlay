package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.ui.button.PrimaryButton
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@Composable
fun PermissionDialog(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    descriptionText: String,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onConfirmTap: () -> Unit,
    onGoToAppSettingsTap: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                HorizontalDivider()
                if (isPermanentlyDeclined) {
                    PrimaryButton(
                        windowSize = windowSize,
                        text = "Grant permission",
                        onTap = onGoToAppSettingsTap,
                    )
                } else {
                    PrimaryButton(windowSize = windowSize, text = "Ok", onTap = onConfirmTap)
                }
            }
        },
        title = {
            Text(text = "Permission required", style = adaptiveTitleByHeight(windowSize))
        },
        text = {
            Text(
                text = descriptionText,
                style = adaptiveBodyByHeight(windowSize)
            )
        },
    )
}


@Preview
@Composable
private fun PermissionDialogPreview() {
    PermissionDialog(
        windowSize = WINDOWS_SIZE_MEDIUM,
        descriptionText = LoremIpsum(25).values.joinToString(" "),
        isPermanentlyDeclined = false,
        onDismiss = { },
        onConfirmTap = { },
        onGoToAppSettingsTap = { }
    )
}
