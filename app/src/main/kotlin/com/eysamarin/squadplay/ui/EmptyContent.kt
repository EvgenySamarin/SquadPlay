package com.eysamarin.squadplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.ui.theme.adaptiveLabelByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight

@Composable
fun EmptyContent(
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.no_events_scheduled_title),
    subtitle: String = stringResource(R.string.no_events_scheduled_subtitle),
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DesignSystemTheme.colorScheme.surfaceContainerLow,
                    shape = DesignSystemTheme.shapes.medium,
                )
                .border(
                    width = 1.dp,
                    color = DesignSystemTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = DesignSystemTheme.shapes.medium,
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            renderEffect = BlurEffect(
                                radiusX = 12.dp.toPx(),
                                radiusY = 12.dp.toPx(),
                                edgeTreatment = TileMode.Decal,
                            )
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DesignSystemTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    DesignSystemTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                ),
                            ),
                            shape = DesignSystemTheme.shapes.medium,
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = DesignSystemTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
                            shape = DesignSystemTheme.shapes.medium,
                        )
                        .border(
                            width = 1.dp,
                            color = DesignSystemTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = DesignSystemTheme.shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.ic_sports_esports_24),
                        contentDescription = null,
                        tint = DesignSystemTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = adaptiveTitleByHeight(windowSize),
                fontWeight = FontWeight.Bold,
                color = DesignSystemTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.widthIn(max = 280.dp),
                text = subtitle,
                style = adaptiveLabelByHeight(windowSize),
                color = DesignSystemTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
