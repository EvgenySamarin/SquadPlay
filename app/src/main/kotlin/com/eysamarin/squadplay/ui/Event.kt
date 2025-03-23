package com.eysamarin.squadplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.EventUI
import com.eysamarin.squadplay.models.PREVIEW_EVENTS
import com.eysamarin.squadplay.ui.squircle.CornerSmoothing
import com.eysamarin.squadplay.ui.squircle.SquircleShape
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight
import com.eysamarin.squadplay.utils.DarkLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@Composable
fun Event(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    ui: EventUI,
    onDeleteEventTap: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            if (ui.iconUrl != null) {
                AsyncImage(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                    model = ui.iconUrl,
                    contentDescription = null,
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                    painter = painterResource(R.drawable.ic_question),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Column {
            Text(
                text = ui.title,
                style = adaptiveTitleByHeight(windowSize),
                color = MaterialTheme.colorScheme.onSurface,
            )
            ui.subtitle?.let {
                Text(
                    text = it,
                    style = adaptiveBodyByHeight(windowSize),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (ui.isYourEvent) {
            IconButton(onClick = onDeleteEventTap) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .clip(shape = SquircleShape(cornerSmoothing = CornerSmoothing.High)),
                    painter = painterResource(R.drawable.ic_delete_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@DarkLightModePreview
@Composable
private fun EventPreview() {
    SquadPlayTheme {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PREVIEW_EVENTS.forEach {
                Event(windowSize = WINDOWS_SIZE_MEDIUM, ui = it)
            }
        }
    }
}