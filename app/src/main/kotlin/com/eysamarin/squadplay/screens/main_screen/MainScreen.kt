package com.eysamarin.squadplay.screens.main_screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.MainScreenAction
import com.eysamarin.squadplay.models.MainScreenUI
import com.eysamarin.squadplay.models.PREVIEW_MAIN_SCREEN_UI
import com.eysamarin.squadplay.models.PollingDialogUI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.ui.calendar.Calendar
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient1
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient2
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient3
import com.eysamarin.squadplay.ui.theme.PrimaryFont
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.getAdaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveHeadlineByHeight
import com.eysamarin.squadplay.utils.PhonePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_COMPACT
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_EXPANDED
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.utils.TabletPreview
import com.eysamarin.squadplay.utils.WearPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState<MainScreenUI>,
    pollingDialogState: UiState<PollingDialogUI>,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (MainScreenAction) -> Unit
) {
    Scaffold(
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
        }
    )


    if (pollingDialogState is UiState.Normal<PollingDialogUI>) {
        ModalBottomSheet(onDismissRequest = { onAction(MainScreenAction.OnDismissPolingDialog) }) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Polling dialog with date: ${pollingDialogState.data.selectedDate}",
                    style = getAdaptiveBodyByHeight(windowSize),
                    color = PrimaryFont,
                )
                Button(
                    onClick = { onAction(MainScreenAction.OnPollingStartTap) }
                ) {
                    Text(
                        text = "Start polling",
                        style = getAdaptiveBodyByHeight(windowSize),
                        color = PrimaryFont,
                    )
                }
            }

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
                style = getAdaptiveHeadlineByHeight(windowSize),
                color = PrimaryFont
            )
            UserAvatar()
        }
    }
}

@Composable
private fun UserAvatar(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(
                shape = SquircleShape(
                    percent = 100,
                    cornerSmoothing = CornerSmoothing.High
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AvatarBorderGradient1,
                        AvatarBorderGradient2,
                        AvatarBorderGradient3
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                ),
                shape = SquircleShape(
                    percent = 100,
                    cornerSmoothing = CornerSmoothing.High
                )
            )
            .padding(4.dp)
    ) {
        Icon(
            modifier = Modifier
                .clip(
                    shape = SquircleShape(
                        percent = 100,
                        cornerSmoothing = CornerSmoothing.High
                    )
                ),
            painter = painterResource(R.drawable.default_avatar),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}

//region screen preview
@TabletPreview
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

@PhonePreview
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

@WearPreview
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