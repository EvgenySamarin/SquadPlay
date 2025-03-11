package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eysamarin.squadplay.models.DialPickerTarget
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialPicker(
    target: DialPickerTarget,
    onTimeChange: (TimePickerState, DialPickerTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime[Calendar.HOUR_OF_DAY],
        initialMinute = currentTime[Calendar.MINUTE],
        is24Hour = true,
    )

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(timePickerState, target)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimePicker(
            state = timePickerState,
            colors = TimePickerDefaults.colors(
                clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                selectorColor = MaterialTheme.colorScheme.primary,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DialPickerPreview() {
    DialPicker(
        target = DialPickerTarget.FROM,
        onTimeChange = { _, _ -> },
    )
}