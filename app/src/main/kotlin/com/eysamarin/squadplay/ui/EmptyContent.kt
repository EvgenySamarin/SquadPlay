package com.eysamarin.squadplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight

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
                tint = DesignSystemTheme.colorScheme.outline
            )
            Text(
                text = stringResource(R.string.no_data_yet),
                style = adaptiveBodyByHeight(windowSize),
                color = DesignSystemTheme.colorScheme.outline,
            )
        }
    }
}

@DarkLightModePreview
@Composable
fun EmptyContentPreview() {
    DesignSystemTheme {
        EmptyContent(windowSize = WINDOWS_SIZE_MEDIUM)
    }
}