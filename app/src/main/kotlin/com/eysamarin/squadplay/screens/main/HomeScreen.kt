package com.eysamarin.squadplay.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_COMPACT
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_EXPANDED
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.designSystem.compose.utils.TabletDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.TabletLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.WearDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.WearLightModePreview
import com.eysamarin.squadplay.models.HomeScreenAction
import com.eysamarin.squadplay.models.HomeScreenUI
import com.eysamarin.squadplay.models.PREVIEW_MAIN_SCREEN_UI
import com.eysamarin.squadplay.models.UiState
import com.eysamarin.squadplay.models.User
import com.eysamarin.squadplay.ui.EmptyContent
import com.eysamarin.squadplay.ui.EventItem
import com.eysamarin.squadplay.ui.UserAvatar
import com.eysamarin.squadplay.ui.calendar.Calendar
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight

@Composable
fun HomeScreen(
    state: UiState<HomeScreenUI>,
    isLoggingOut: Boolean = false,
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (HomeScreenAction) -> Unit,
) {
    val mediumListState = rememberLazyListState()
    val expandedCalendarListState = rememberLazyListState()
    val expandedEventsListState = rememberLazyListState()

    val isScrolling = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> expandedCalendarListState.isScrollInProgress || expandedEventsListState.isScrollInProgress
        else -> mediumListState.isScrollInProgress
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(
                        onClick = { if (!isLoggingOut) onAction(HomeScreenAction.OnLogOutTap) },
                        enabled = !isLoggingOut,
                    ) {
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
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    UiState.Loading -> {
                        LoadingIndicator()
                    }
                    is UiState.Normal -> {
                        when (windowSize.widthSizeClass) {
                            WindowWidthSizeClass.Compact,
                            WindowWidthSizeClass.Medium -> HomeScreenMediumLayout(
                                state = state,
                                windowSize = windowSize,
                                lazyListState = mediumListState,
                                onAction = onAction,
                            )

                            WindowWidthSizeClass.Expanded -> MainScreenExpandedLayout(
                                state = state,
                                windowSize = windowSize,
                                calendarListState = expandedCalendarListState,
                                eventsListState = expandedEventsListState,
                                onAction = onAction,
                            )
                        }
                    }
                    else -> Unit
                }
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            AnimatedVisibility(
                visible = (state as? UiState.Normal)?.data?.isCreateEventButtonVisible == true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(text = stringResource(R.string.new_game_event)) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_24),
                            contentDescription = stringResource(R.string.content_description_add_game),
                        )
                    },
                    onClick = {
                        onAction(HomeScreenAction.OnAddGameEventTap)
                    },
                    expanded = !isScrolling,
                    shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
                    containerColor = DesignSystemTheme.colorScheme.secondary,
                    contentColor = DesignSystemTheme.colorScheme.onSecondary,
                )
            }
        },
        containerColor = DesignSystemTheme.colorScheme.surface,
    )

    if (isLoggingOut) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignSystemTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center,
        ) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun HomeScreenMediumLayout(
    state: UiState<HomeScreenUI>,
    windowSize: WindowSizeClass,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (HomeScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        GreetingBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            windowSize = windowSize,
            user = state.data.user,
            onAvatarTap = {
                onAction(HomeScreenAction.OnAvatarTap)
            }
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Calendar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    ui = state.data.calendarUI,
                    windowSize = windowSize,
                    onPreviousMonthTap = { onAction(HomeScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(HomeScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(HomeScreenAction.OnDateTap(it)) }
                )
            }
            if (state.data.user.groups.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_squad_create_event_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = DesignSystemTheme.typography.bodyMedium,
                        color = DesignSystemTheme.extendedColors.orange,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item {
                HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            if (state.data.gameEventsOnDate.isEmpty()) {
                item { EmptyContent(windowSize, modifier = Modifier.fillMaxSize()) }
            } else {
                items(items = state.data.gameEventsOnDate) { item ->
                    EventItem(
                        event = item,
                        members = item.members,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onDetailsTap = { onAction(HomeScreenAction.OnEventTap(item)) }
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
    calendarListState: LazyListState = rememberLazyListState(),
    eventsListState: LazyListState = rememberLazyListState(),
    onAction: (HomeScreenAction) -> Unit = {},
) {
    if (state !is UiState.Normal) return

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            state = calendarListState,
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Calendar(
                    ui = state.data.calendarUI,
                    windowSize = windowSize,
                    onPreviousMonthTap = { onAction(HomeScreenAction.OnPrevMonthTap(it)) },
                    onNextMonthTap = { onAction(HomeScreenAction.OnNextMonthTap(it)) },
                    onDateTap = { onAction(HomeScreenAction.OnDateTap(it)) }
                )
            }
            if (state.data.user.groups.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_squad_create_event_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = DesignSystemTheme.typography.bodyMedium,
                        color = DesignSystemTheme.extendedColors.orange,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            GreetingBar(windowSize = windowSize, user = state.data.user, onAvatarTap = {
                onAction(HomeScreenAction.OnAvatarTap)
            })
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                state = eventsListState,
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = state.data.gameEventsOnDate) { item ->
                    EventItem(
                        event = item,
                        members = item.members,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onDetailsTap = { onAction(HomeScreenAction.OnEventTap(item)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingBar(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    user: User,
    onAvatarTap: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
            text = stringResource(R.string.greeting_text, user.username),
            style = adaptiveHeadlineByHeight(windowSize),
            color = DesignSystemTheme.colorScheme.onSurface
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
    DesignSystemTheme {
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
    DesignSystemTheme {
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
    DesignSystemTheme {
        HomeScreen(
            state = UiState.Normal(PREVIEW_MAIN_SCREEN_UI),
            windowSize = WINDOWS_SIZE_COMPACT,
            onAction = {},
        )
    }
}
//endregion