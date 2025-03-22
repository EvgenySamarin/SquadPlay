package com.eysamarin.squadplay.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.models.DialPickerTarget
import com.eysamarin.squadplay.models.PREVIEW_TIME_PICKER_UI
import com.eysamarin.squadplay.models.TimePickerUI
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveLabelByHeight
import com.eysamarin.squadplay.utils.DarkLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import java.util.Locale

@Composable
fun SquadPlayTimePicker(
    ui: TimePickerUI,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    onFromTap: () -> Unit = {},
    onToTap: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(text = "Select time", style = adaptiveBodyByHeight(windowSize), color = MaterialTheme.colorScheme.onSurface)
        Card(
            modifier = Modifier.padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            border = if (ui.errorText != null) BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error) else null,
        ) {
            Row(
                modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeUnit(
                    windowSize = windowSize,
                    label = "From",
                    timeString = ui.timeFrom?.let { buildTimeString(it.hour, it.minute) }
                        ?: "--:--",
                    selected = ui.currentTarget == DialPickerTarget.FROM,
                    onTap = onFromTap,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "next",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                TimeUnit(
                    windowSize = windowSize,
                    label = "To",
                    timeString = ui.timeTo?.let { buildTimeString(it.hour, it.minute) }
                        ?: "--:--",
                    selected = ui.currentTarget == DialPickerTarget.TO,
                    onTap = onToTap,
                )
            }
        }
        ui.errorText?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                text = it,
                style = adaptiveBodyByHeight(windowSize),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun buildTimeString(hour: Int, minute: Int): String = buildString {
    append(String.format(Locale.getDefault(), "%02d", hour))
    append(":")
    append(String.format(Locale.getDefault(), "%02d", minute))
}


@Composable
private fun TimeUnit(
    selected: Boolean = false,
    windowSize: WindowSizeClass,
    label: String,
    timeString: String,
    enabled: Boolean = true,
    onTap: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .clickable(enabled = enabled) {
                onTap()
            },
    ) {
        Text(
            text = label,
            style = adaptiveLabelByHeight(windowSize),
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = timeString,
            style = adaptiveHeadlineByHeight(windowSize),
            color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@DarkLightModePreview
@Composable
fun TimePickerPreview() {
    Column {
        Spacer(Modifier.padding(top = 24.dp))
        SquadPlayTimePicker(
            ui = PREVIEW_TIME_PICKER_UI,
            windowSize = WINDOWS_SIZE_MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
        SquadPlayTimePicker(
            ui = PREVIEW_TIME_PICKER_UI.copy(errorText = "Time from cannot be more then time to"),
            windowSize = WINDOWS_SIZE_MEDIUM,
            modifier = Modifier.fillMaxWidth()
        )
    }
}