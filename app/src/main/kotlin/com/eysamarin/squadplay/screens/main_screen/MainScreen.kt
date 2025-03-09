package com.eysamarin.squadplay.screens.main_screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.PrimaryFont
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.getAdaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient1
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient2
import com.eysamarin.squadplay.ui.theme.AvatarBorderGradient3
import com.eysamarin.squadplay.utils.PhonePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_COMPACT
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_EXPANDED
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.utils.TabletPreview
import com.eysamarin.squadplay.utils.WearPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUIState,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
) {
    Scaffold(
        topBar = { SquadPlayTopBar(windowSize, state) },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> MainScreenMediumLayout(state, windowSize)

                    WindowWidthSizeClass.Expanded -> MainScreenExpandedLayout(state, windowSize)
                }
            }
        }
    )
}

@Composable
private fun MainScreenMediumLayout(
    state: MainUIState,
    windowSize: WindowSizeClass
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight()
    ) {
        Column {
            Text(
                text = state.body,
                style = getAdaptiveBodyByHeight(windowSize),
                color = PrimaryFont
            )
            Text(
                text = state.label,
                style = getAdaptiveLabelByHeight(windowSize),
                color = PrimaryFont
            )
        }
    }
}

@Composable
private fun MainScreenExpandedLayout(
    state: MainUIState,
    windowSize: WindowSizeClass
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(state.body, style = getAdaptiveBodyByHeight(windowSize))
        Text(state.label, style = getAdaptiveLabelByHeight(windowSize))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SquadPlayTopBar(
    windowSize: WindowSizeClass,
    state: MainUIState
) {
    if (windowSize.heightSizeClass != WindowHeightSizeClass.Compact) {
        TopAppBar(
            title = {
                Text(
                    text = state.title,
                    style = getAdaptiveHeadlineByHeight(windowSize),
                    color = PrimaryFont
                )
            },
            actions = {
                UserAvatar(Modifier.padding(horizontal = 16.dp))
            }
        )
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
            state = MainUIState(),
            windowSize = WINDOWS_SIZE_EXPANDED,
        )
    }
}

@PhonePreview
@Composable
fun MainScreenPhonePreview() {
    SquadPlayTheme {
        MainScreen(
            state = MainUIState(),
            windowSize = WINDOWS_SIZE_MEDIUM,
        )
    }
}

@WearPreview
@Composable
fun MainScreenWearPreview() {
    SquadPlayTheme {
        MainScreen(
            state = MainUIState(),
            windowSize = WINDOWS_SIZE_COMPACT,
        )
    }
}
//endregion