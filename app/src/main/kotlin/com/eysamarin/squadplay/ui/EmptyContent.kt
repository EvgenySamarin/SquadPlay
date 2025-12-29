package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.utils.DarkLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@Composable
fun EmptyContent(
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier,
                painter = painterResource(R.drawable.ic_not_found),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = stringResource(R.string.no_data_yet),
                style = adaptiveBodyByHeight(windowSize),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@DarkLightModePreview
@Composable
fun EmptyContentPreview() {
    SquadPlayTheme {
        EmptyContent(windowSize = WINDOWS_SIZE_MEDIUM)
    }
}