package com.eysamarin.squadplay.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.GameEventUI
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.PREVIEW_MAIN_SCREEN_UI
import com.eysamarin.squadplay.models.PollingDialogUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.EmptyContent
import com.eysamarin.squadplay.ui.UserAvatar
import com.eysamarin.squadplay.ui.calendar.Calendar
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
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
    pollingDialogState: UiState<PollingDialogUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (MainScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { onAction(MainScreenAction.OnLogOutTap) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "log out",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onAction(MainScreenAction.OnAddGameEventTap)
                },
                shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add game event")
                Text(text = "New game event")
            }
        }
    )


    if (pollingDialogState is UiState.Normal<PollingDialogUI>) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { onAction(MainScreenAction.OnDismissPolingDialog) },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AddGameEvent(
                ui = pollingDialogState.data,
                windowSize = windowSize,
                onStartPollingTap = { from, to ->
                    onAction(MainScreenAction.OnPollingStartTap(from, to))
                    onAction(MainScreenAction.OnDismissPolingDialog)
                }
            )
        }
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
        GreetingBar(windowSize, state)
        Calendar(
            ui = state.data.calendarUI,
            windowSize = windowSize,
            onPreviousMonthTap = { onAction(MainScreenAction.OnPrevMonthTap(it)) },
            onNextMonthTap = { onAction(MainScreenAction.OnNextMonthTap(it)) }
        ) { onAction(MainScreenAction.OnDateTap(it)) }
        HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        EventLists(events = state.data.gameEventsOnDate, windowSize = windowSize)
    }
}

@Composable
private fun MainScreenExpandedLayout(
    state: UiState<MainScreenUI>,
    windowSize: WindowSizeClass,
    onAction: (MainScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Column(modifier = Modifier.padding(16.dp)) {
        GreetingBar(windowSize, state)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
    }
}

@Composable
private fun EventLists(
    events: List<GameEventUI>,
    windowSize: WindowSizeClass,
) {
    if (events.isEmpty()) {
        EmptyContent(windowSize, modifier = Modifier.fillMaxSize())
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items = events) { item ->
            GameEvent(
                windowSize = windowSize,
                name = item.name,
                players = item.players,
                gameIconResId = item.gameIconResId ?: R.drawable.ic_question
            )
        }
    }
}

@Composable
private fun GameEvent(
    windowSize: WindowSizeClass,
    name: String,
    players: Int,
    gameIconResId: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp),
                painter = painterResource(gameIconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Column {
            Text(text = "Game event", style = adaptiveTitleByHeight(windowSize))
            Text(name, style = adaptiveBodyByHeight(windowSize))
        }
        Text("players: $players")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GreetingBar(
    windowSize: WindowSizeClass,
    state: UiState<MainScreenUI>,
) {
    if (state !is UiState.Normal) return

    if (windowSize.heightSizeClass != WindowHeightSizeClass.Compact) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, false),
                text = state.data.title,
                style = adaptiveHeadlineByHeight(windowSize),
                color = MaterialTheme.colorScheme.onSurface
            )
            UserAvatar()
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
            pollingDialogState = UiState.Empty,
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
            pollingDialogState = UiState.Empty,
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
            pollingDialogState = UiState.Empty,
            windowSize = WINDOWS_SIZE_COMPACT,
            onAction = {},
        )
    }
}
//endregion