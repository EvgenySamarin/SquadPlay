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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.HomeScreenUI
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
fun HomeScreen(
    state: UiState<HomeScreenUI>,
    confirmInviteDialogState: UiState<String> = UiState.Empty,
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (HomeScreenAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { onAction(HomeScreenAction.OnLogOutTap) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_exit_to_app_24),
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
                    WindowWidthSizeClass.Medium -> HomeScreenMediumLayout(
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
                    onAction(HomeScreenAction.OnAddGameEventTap)
                },
                shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_24),
                    contentDescription = stringResource(R.string.content_description_add_game),
                )
                Text(text = stringResource(R.string.new_game_event))
            }
        }
    )

    if (confirmInviteDialogState is UiState.Normal<String>) {
        ConfirmationDialog(
            windowSize = windowSize,
            title = stringResource(R.string.invite_new_friend),
            text = confirmInviteDialogState.data,
            onDismiss = {
                onAction(HomeScreenAction.OnJoinGroupDialogDismiss)
            },
            onConfirmTap = {
                onAction(HomeScreenAction.OnJoinGroupDialogConfirm)
            }
        )
    }
}

@Composable
private fun HomeScreenMediumLayout(
    state: UiState<HomeScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (HomeScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        GreetingBar(windowSize = windowSize, user = state.data.user, onAvatarTap = {
            onAction(HomeScreenAction.OnAvatarTap)
        })
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Calendar(
                    ui = state.data.calendarUI,
                    windowSize = windowSize,
                    onPreviousMonthTap = { onAction(HomeScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(HomeScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(HomeScreenAction.OnDateTap(it)) }
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
                        onDeleteEventTap = { onAction(HomeScreenAction.OnDeleteEventTap(item.eventId)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreenExpandedLayout(
    state: UiState<HomeScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (HomeScreenAction) -> Unit = {},
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
                    onPreviousMonthTap = { onAction(HomeScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(HomeScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(HomeScreenAction.OnDateTap(it)) }
                )
            }

        }
        Column(modifier = Modifier.weight(1f)) {
            GreetingBar(windowSize = windowSize, user = state.data.user, onAvatarTap = {
                onAction(HomeScreenAction.OnAvatarTap)
            })
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = state.data.gameEventsOnDate) { item ->
                    Event(
                        windowSize = windowSize,
                        ui = item,
                        onDeleteEventTap = { onAction(HomeScreenAction.OnDeleteEventTap(item.eventId)) },
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
fun HomeScreenPhonePreview() {
    SquadPlayTheme {
        HomeScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            onAction = {},
        )
    }
}

@TabletDarkModePreview
@TabletLightModePreview
@Composable
fun HomeScreenTabletPreview() {
    SquadPlayTheme {
        HomeScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            windowSize = WINDOWS_SIZE_EXPANDED,
            onAction = {},
        )
    }
}

@WearDarkModePreview
@WearLightModePreview
@Composable
fun HomeScreenWearPreview() {
    SquadPlayTheme {
        HomeScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            windowSize = WINDOWS_SIZE_COMPACT,
            onAction = {},
        )
    }
}
//endregion