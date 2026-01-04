package com.eysamarin.squadplay.screens.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.DialPickerTarget
import com.eysamarin.squadplay.models.NewEventScreenAction
import com.eysamarin.squadplay.models.NewEventScreenUI
import com.eysamarin.squadplay.models.PREVIEW_NEW_EVENT_SCREEN_UI
import com.eysamarin.squadplay.models.TimePickerUI
import com.eysamarin.squadplay.models.UiState
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
fun NewEventScreen(
    state: UiState<NewEventScreenUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (NewEventScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(NewEventScreenAction.OnBackButtonTap) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Expanded,
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> NewEventScreenMediumLayout(
                        state, windowSize, onAction
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEventScreenMediumLayout(
    state: UiState<NewEventScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (NewEventScreenAction) -> Unit,
) {
    if (state !is UiState.Normal) return


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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val format = DecimalFormat("00")
        val formattedDate = buildString {
            append(state.data.yearMonth.year)
            append(".")
            append(format.format(state.data.yearMonth.monthValue))
            append(".")
            append(format.format(state.data.selectedDate.dayOfMonth))
        }
        Text(
            text = stringResource(R.string.create_new_event, formattedDate),
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
                                /* year = */ state.data.yearMonth.year,
                                /* month = */ state.data.yearMonth.month,
                                /* dayOfMonth = */ state.data.selectedDate.dayOfMonth ?: 1,
                                /* hour = */ timeState.hour,
                                /* minute = */ timeState.minute
                            )
                        }

                        DialPickerTarget.TO -> {
                            dateTimeTo = LocalDateTime.of(
                                /* year = */ state.data.yearMonth.year,
                                /* month = */ state.data.yearMonth.month,
                                /* dayOfMonth = */ state.data.selectedDate.dayOfMonth ?: 1,
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

                onAction(NewEventScreenAction.OnEventSaveTap(
                    timeFrom = from,
                    timeTo = if (isHoursNextDay || isMinutesNextDay) to.plusDays(1) else to
                ))
            },
        )
    }
}


//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun NewEventScreenPhonePreview() {
    SquadPlayTheme {
        NewEventScreen(
            state = UiState.Normal(PREVIEW_NEW_EVENT_SCREEN_UI),
            onAction = {}
        )
    }
}
//endregion