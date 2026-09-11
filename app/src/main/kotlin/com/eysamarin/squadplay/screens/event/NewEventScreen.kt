package com.eysamarin.squadplay.screens.event

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.ImageTopBar
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.Group
import com.eysamarin.squadplay.models.NewEventScreenAction
import com.eysamarin.squadplay.models.NewEventScreenUI
import com.eysamarin.squadplay.models.PREVIEW_NEW_EVENT_SCREEN_UI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.TimePicker
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEventScreen(
    state: UiState<NewEventScreenUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (NewEventScreenAction) -> Unit
) {
    if (state !is UiState.Normal) return

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            val originalAppearance = insetsController.isAppearanceLightStatusBars

            insetsController.isAppearanceLightStatusBars = false

            onDispose { insetsController.isAppearanceLightStatusBars = originalAppearance }
        }
    }

    val cornerRadius = 24.dp
    val headerHeight = 220.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystemTheme.colorScheme.background)
    ) {
        ImageTopBar(
            imageUrl = state.data.eventIconUrl,
            headerHeight = headerHeight,
            onBackTap = { onAction(NewEventScreenAction.OnBackButtonTap) }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight - cornerRadius),
            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
            color = DesignSystemTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
    }}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEventScreenMediumLayout(
    state: UiState.Normal<NewEventScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (NewEventScreenAction) -> Unit,
) {
    var dateTimeFrom by remember { mutableStateOf<LocalDateTime?>(null) }
    var dateTimeTo by remember { mutableStateOf<LocalDateTime?>(null) }
    var isGroupDropdownExpanded by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    LaunchedEffect(state.data.userGroups) {
        if (selectedGroup == null || state.data.userGroups.none { it.uid == selectedGroup?.uid }) {
            selectedGroup = state.data.userGroups.firstOrNull()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val format = DecimalFormat("00")
        val formattedDate = buildString {
            append(state.data.yearMonth.year)
            append(".")
            append(format.format(state.data.yearMonth.month.ordinal + 1))
            append(".")
            append(format.format(state.data.selectedDate.dayOfMonth))
        }

        item {
            Text(
                text = stringResource(R.string.create_new_event, formattedDate),
                style = adaptiveHeadlineByHeight(windowSize),
                color = DesignSystemTheme.colorScheme.onSurface,
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = isGroupDropdownExpanded,
                onExpandedChange = { isGroupDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedGroup?.title.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_group)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DesignSystemTheme.colorScheme.onSurface,
                        unfocusedTextColor = DesignSystemTheme.colorScheme.onSurface,
                    )
                )
                ExposedDropdownMenu(
                    expanded = isGroupDropdownExpanded,
                    onDismissRequest = { isGroupDropdownExpanded = false }
                ) {
                    state.data.userGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.title) },
                            onClick = {
                                selectedGroup = group
                                isGroupDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = state.data.gameTitle,
                onValueChange = { onAction(NewEventScreenAction.OnGameTitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Game Title") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DesignSystemTheme.colorScheme.onSurface,
                    unfocusedTextColor = DesignSystemTheme.colorScheme.onSurface,
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimePicker(
                    title = stringResource(R.string.from),
                    windowSize = windowSize,
                    onTimeChange = { timeState ->
                        dateTimeFrom = LocalDateTime(
                            /* year = */ state.data.yearMonth.year,
                            /* month = */ state.data.yearMonth.month,
                            /* dayOfMonth = */ state.data.selectedDate.dayOfMonth ?: 1,
                            /* hour = */ timeState.hour,
                            /* minute = */ timeState.minute
                        )
                    },
                )
                TimePicker(
                    title = stringResource(R.string.to),
                    windowSize = windowSize,
                    onTimeChange = { timeState ->
                        dateTimeTo = LocalDateTime(
                            /* year = */ state.data.yearMonth.year,
                            /* month = */ state.data.yearMonth.month,
                            /* dayOfMonth = */ state.data.selectedDate.dayOfMonth ?: 1,
                            /* hour = */ timeState.hour,
                            /* minute = */ timeState.minute
                        )
                    },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.from_to_time_warning),
                textAlign = TextAlign.Center,
                style = adaptiveBodyByHeight(windowSize),
                color = DesignSystemTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DSButton(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    text = stringResource(R.string.schedule_event),
                    onTap = {
                        val from = dateTimeFrom ?: return@DSButton
                        val to = dateTimeTo ?: return@DSButton

                        val isHoursNextDay = from.hour > to.hour
                        val isMinutesNextDay = from.hour == to.hour
                                && from.minute > (to.minute)

                        val targetGroupId = selectedGroup?.uid ?: state.data.userGroups.firstOrNull()?.uid.orEmpty()

                        onAction(
                            NewEventScreenAction.OnEventSaveTap(
                                title = state.data.gameTitle,
                                timeFrom = from,
                                timeTo = if (isHoursNextDay || isMinutesNextDay) {
                                    LocalDateTime(from.date.plus(1, DateTimeUnit.DAY), from.time)
                                } else to,
                                eventIconUrl = state.data.eventIconUrl,
                                groupId = targetGroupId,
                            )
                        )
                    },
                )
            }
        }
    }
}


//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun NewEventScreenPhonePreview() {
    DesignSystemTheme {
        NewEventScreen(
            state = UiState.Normal(PREVIEW_NEW_EVENT_SCREEN_UI),
            onAction = {}
        )
    }
}
//endregion