package com.eysamarin.squadplay.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.data.R
import com.eysamarin.squadplay.models.EventDialogUI
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.PREVIEW_MAIN_SCREEN_UI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.ui.EmptyContent
import com.eysamarin.squadplay.ui.Event
import com.eysamarin.squadplay.ui.UserAvatar
import com.eysamarin.squadplay.ui.calendar.Calendar
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_COMPACT
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_EXPANDED
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.utils.TabletDarkModePreview
import com.eysamarin.squadplay.utils.TabletLightModePreview
import com.eysamarin.squadplay.utils.WearDarkModePreview
import com.eysamarin.squadplay.utils.WearLightModePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState<MainScreenUI>,
    eventDialogState: UiState<EventDialogUI> = UiState.Empty,
    confirmInviteDialogState: UiState<String> = UiState.Empty,
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (MainScreenAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { onAction(MainScreenAction.OnLogOutTap) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.content_description_log_out),
                        )
                    }
                })
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> MainScreenMediumLayout(
                        state, windowSize, onAction
                    )

                    WindowWidthSizeClass.Expanded -> MainScreenExpandedLayout(
                        state, windowSize, onAction
                    )
                }
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onAction(MainScreenAction.OnAddGameEventTap)
                },
                shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.content_description_add_game),
                )
                Text(text = stringResource(R.string.new_game_event))
            }
        }
    )


    if (eventDialogState is UiState.Normal<EventDialogUI>) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { onAction(MainScreenAction.OnDismissEventDialog) },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AddGameEvent(
                ui = eventDialogState.data,
                windowSize = windowSize,
                onStartPollingTap = { from, to ->
                    onAction(MainScreenAction.OnEventSaveTap(dateTimeFrom = from, dateTimeTo = to))
                    onAction(MainScreenAction.OnDismissEventDialog)
                }
            )
        }
    }

    if (confirmInviteDialogState is UiState.Normal<String>) {
        ConfirmationDialog(
            windowSize = windowSize,
            title = stringResource(R.string.invite_new_friend),
            text = confirmInviteDialogState.data,
            onDismiss = {
                onAction(MainScreenAction.OnJoinGroupDialogDismiss)
            },
            onConfirmTap = {
                onAction(MainScreenAction.OnJoinGroupDialogConfirm)
            }
        )
    }
}

@Composable
private fun MainScreenMediumLayout(
    state: UiState<MainScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (MainScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        GreetingBar(windowSize = windowSize, user = state.data.user, onAvatarTap = {
            onAction(MainScreenAction.OnAvatarTap)
        })
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Calendar(
                    ui = state.data.calendarUI,
                    windowSize = windowSize,
                    onPreviousMonthTap = { onAction(MainScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(MainScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(MainScreenAction.OnDateTap(it)) }
                )
            }
            item {
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            if (state.data.gameEventsOnDate.isEmpty()) {
                item { EmptyContent(windowSize, modifier = Modifier.fillMaxSize()) }
            } else {
                items(items = state.data.gameEventsOnDate) { item ->
                    Event(
                        windowSize = windowSize,
                        ui = item,
                        onDeleteEventTap = { onAction(MainScreenAction.OnDeleteEventTap(item.eventId)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreenExpandedLayout(
    state: UiState<MainScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (MainScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Calendar(
                    ui = state.data.calendarUI,
                    windowSize = windowSize,
                    onPreviousMonthTap = { onAction(MainScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(MainScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(MainScreenAction.OnDateTap(it)) }
                )
            }

        }
        Column(modifier = Modifier.weight(1f)) {
            GreetingBar(windowSize = windowSize, user = state.data.user, onAvatarTap = {
                onAction(MainScreenAction.OnAvatarTap)
            })
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = state.data.gameEventsOnDate) { item ->
                    Event(
                        windowSize = windowSize,
                        ui = item,
                        onDeleteEventTap = { onAction(MainScreenAction.OnDeleteEventTap(item.eventId)) },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GreetingBar(
    windowSize: WindowSizeClass,
    user: User,
    onAvatarTap: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
            text = stringResource(R.string.greeting_text, user.username),
            style = adaptiveHeadlineByHeight(windowSize),
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clickable {
                    onAvatarTap()
                }) {
            UserAvatar(imageUrl = user.photoUrl)
        }
    }
}


//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun MainScreenPhonePreview() {
    SquadPlayTheme {
        MainScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            onAction = {},
        )
    }
}

@TabletDarkModePreview
@TabletLightModePreview
@Composable
fun MainScreenTabletPreview() {
    SquadPlayTheme {
        MainScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            windowSize = WINDOWS_SIZE_EXPANDED,
            onAction = {},
        )
    }
}

@WearDarkModePreview
@WearLightModePreview
@Composable
fun MainScreenWearPreview() {
    SquadPlayTheme {
        MainScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            windowSize = WINDOWS_SIZE_COMPACT,
            onAction = {},
        )
    }
}
//endregion