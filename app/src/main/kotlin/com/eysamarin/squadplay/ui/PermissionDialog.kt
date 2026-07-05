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
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.Button
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight

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
                    Button(
                        text = "Grant permission",
                        onTap = onGoToAppSettingsTap,
                    )
                } else {
                    Button(text = "Ok", onTap = onConfirmTap)
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


@DarkLightModePreview
@Composable
private fun PermissionDialogPreview() {
    DesignSystemTheme {
        PermissionDialog(
            windowSize = WINDOWS_SIZE_MEDIUM,
            descriptionText = LoremIpsum(25).values.joinToString(" "),
            isPermanentlyDeclined = false,
            onDismiss = { },
            onConfirmTap = { },
            onGoToAppSettingsTap = { }
        )
    }
}
