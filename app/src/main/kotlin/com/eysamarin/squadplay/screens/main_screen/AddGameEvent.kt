package com.eysamarin.squadplay.screens.main_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.PREVIEW_POLLING_DIALOG_UI
import com.eysamarin.squadplay.models.PollingDialogUI
import com.eysamarin.squadplay.ui.theme.Accent
import com.eysamarin.squadplay.ui.theme.OnAccent
import com.eysamarin.squadplay.ui.theme.PrimaryFont
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.utils.PhonePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@Composable
fun AddGameEvent(
    ui: PollingDialogUI,
    windowSize: WindowSizeClass,
    onAction: (MainScreenAction) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Create new game event polling for date: ${ui.selectedDate.dayOfMonth}",
            style = adaptiveBodyByHeight(windowSize),
            color = PrimaryFont,
        )
        Button(
            onClick = { onAction(MainScreenAction.OnPollingStartTap) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = OnAccent,
            )
        ) {
            Text(
                text = "Start polling",
                style = adaptiveBodyByHeight(windowSize),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PhonePreview
@Composable
private fun AddGameEventContentPreview() {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded
    )

    ModalBottomSheet(
        onDismissRequest = {}, sheetState = bottomSheetState
    ) {
        AddGameEvent(
            ui = PREVIEW_POLLING_DIALOG_UI,
            windowSize = WINDOWS_SIZE_MEDIUM,
            onAction = {},
        )
    }

}