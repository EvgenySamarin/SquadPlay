package com.eysamarin.squadplay.screens.main_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.getAdaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.getAdaptiveLabelByHeight
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
        topBar = {
            if (windowSize.heightSizeClass != WindowHeightSizeClass.Compact) {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.main_screen_title)) },
                )
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (windowSize.widthSizeClass) {
                    WindowWidthSizeClass.Compact,
                    WindowWidthSizeClass.Medium -> Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxHeight()
                    ) {
                        Column {
                            Text(state.body, style = getAdaptiveBodyByHeight(windowSize))
                            Text(state.label, style = getAdaptiveLabelByHeight(windowSize))
                        }
                    }

                    WindowWidthSizeClass.Expanded -> Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(state.body, style = getAdaptiveBodyByHeight(windowSize))
                        Text(state.label, style = getAdaptiveLabelByHeight(windowSize))
                    }
                }
            }
        }
    )
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