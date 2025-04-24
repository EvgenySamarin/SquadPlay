package com.eysamarin.squadplay.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.data.R
import com.eysamarin.squadplay.models.DialPickerTarget
import com.eysamarin.squadplay.models.EventDialogUI
import com.eysamarin.squadplay.models.PREVIEW_POLLING_DIALOG_UI
import com.eysamarin.squadplay.models.TimePickerUI
import com.eysamarin.squadplay.ui.DialPicker
import com.eysamarin.squadplay.ui.SquadPlayTimePicker
import com.eysamarin.squadplay.ui.button.PrimaryButton
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import java.text.DecimalFormat
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameEvent(
    ui: EventDialogUI,
    windowSize: WindowSizeClass,
    onStartPollingTap: (timeFrom: LocalDateTime, timeTo: LocalDateTime) -> Unit
) {
    var dateTimeFrom by remember { mutableStateOf<LocalDateTime?>(null) }
    var dateTimeTo by remember { mutableStateOf<LocalDateTime?>(null) }
    var dialPickerTarget by remember { mutableStateOf(DialPickerTarget.FROM) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val timePickerUI = TimePickerUI(
        currentTarget = dialPickerTarget,
        timeFrom = dateTimeFrom,
        timeTo = dateTimeTo,
        errorText = errorText,
    )

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val format = DecimalFormat("00")
        Text(
            text = stringResource(
                R.string.create_new_event,
                ui.yearMonth.year,
                format.format(ui.yearMonth.monthValue),
                format.format(ui.selectedDate.dayOfMonth),
            ),
            style = adaptiveBodyByHeight(windowSize),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceTint)
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.from_to_time_warning),
                style = adaptiveBodyByHeight(windowSize),
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }
        SquadPlayTimePicker(
            ui = timePickerUI,
            windowSize = windowSize,
            modifier = Modifier.fillMaxWidth(),
            onFromTap = { dialPickerTarget = DialPickerTarget.FROM },
            onToTap = { dialPickerTarget = DialPickerTarget.TO },
        )
        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
        ) {
            DialPicker(
                target = dialPickerTarget,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onTimeChange = { timeState, target ->
                    errorText = null
                    when (target) {
                        DialPickerTarget.FROM -> {
                            dateTimeFrom = LocalDateTime.of(
                                /* year = */ ui.yearMonth.year,
                                /* month = */ ui.yearMonth.month,
                                /* dayOfMonth = */ ui.selectedDate.dayOfMonth ?: 1,
                                /* hour = */ timeState.hour,
                                /* minute = */ timeState.minute
                            )
                        }

                        DialPickerTarget.TO -> {
                            dateTimeTo = LocalDateTime.of(
                                /* year = */ ui.yearMonth.year,
                                /* month = */ ui.yearMonth.month,
                                /* dayOfMonth = */ ui.selectedDate.dayOfMonth ?: 1,
                                /* hour = */ timeState.hour,
                                /* minute = */ timeState.minute
                            )
                        }
                    }
                },
            )
        }

        val fromNotSetErrorText = stringResource(R.string.time_from_not_set)
        val toNotSetErrorText = stringResource(R.string.time_to_not_set)
        PrimaryButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            windowSize = windowSize,
            text = stringResource(R.string.schedule_event),
            onTap = {
                val from = dateTimeFrom ?: run {
                    errorText = fromNotSetErrorText
                    return@PrimaryButton
                }
                val to = dateTimeTo ?: run {
                    errorText = toNotSetErrorText
                    return@PrimaryButton
                }

                val isHoursNextDay = from.hour > to.hour
                val isMinutesNextDay = from.hour == to.hour
                        && from.minute > (to.minute)

                onStartPollingTap(
                    from,
                    if (isHoursNextDay || isMinutesNextDay) to.plusDays(1) else to
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
private fun AddGameEventContentPreview() {
    SquadPlayTheme {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)) {
            Spacer(Modifier.padding(top = 24.dp))
            AddGameEvent(
                ui = PREVIEW_POLLING_DIALOG_UI,
                windowSize = WINDOWS_SIZE_MEDIUM,
                onStartPollingTap = { _, _ -> },
            )
        }
    }
}