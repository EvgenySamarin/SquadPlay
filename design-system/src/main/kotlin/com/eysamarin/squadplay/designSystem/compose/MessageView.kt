package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview

enum class CardMessageType() {
    ERROR,
    WARNING,
    SUCCESS
}


/**
 * Mode this composable to design system when, it will be ready for develop in design system.
 */
@Composable
fun MessageView(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    type: CardMessageType,
    primaryButtonTitle: String,
    onPrimaryButtonTap: () -> Unit = {},
    secondaryButtonTitle: String? = null,
    onSecondaryButtonTap: () -> Unit = {},
) {
    val ribbonWidthInDp = 4.dp
    val ribbonColor = when (type) {
        CardMessageType.ERROR -> DesignSystemTheme.extendedColors.red
        CardMessageType.WARNING -> DesignSystemTheme.extendedColors.orange
        CardMessageType.SUCCESS -> DesignSystemTheme.extendedColors.green
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .drawBehind {
                val ribbonWidthPx = ribbonWidthInDp.toPx()

                drawRect(
                    color = ribbonColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(ribbonWidthPx, size.height)
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = DesignSystemTheme.extendedTypography.bodyEmphasized,
                color = DesignSystemTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = subtitle,
                style = DesignSystemTheme.extendedTypography.subheadlineRegular,
                color = DesignSystemTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    title = primaryButtonTitle,
                    onTap = onPrimaryButtonTap,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
                secondaryButtonTitle?.let {
                    Button(
                        title = it,
                        onTap = onSecondaryButtonTap,
                        size = ButtonSize.Small,
                        style = ButtonStyle.Plain,
                    )
                }
            }
        }

    }
}

@DarkLightModePreview
@Composable
private fun CardMessagePreview() {
    DesignSystemTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.background(DesignSystemTheme.colorScheme.surface),
        ) {
            MessageView(
                modifier = Modifier.padding(16.dp),
                title = LoremIpsum(2).values.joinToString { it },
                subtitle = LoremIpsum(7).values.joinToString { it },
                type = CardMessageType.ERROR,
                primaryButtonTitle = "Primary"
            )
            MessageView(
                modifier = Modifier.padding(16.dp),
                title = LoremIpsum(2).values.joinToString { it },
                subtitle = LoremIpsum(7).values.joinToString { it },
                type = CardMessageType.WARNING,
                primaryButtonTitle = "Primary",
                secondaryButtonTitle = "Secondary"
            )
            MessageView(
                modifier = Modifier.padding(16.dp),
                title = LoremIpsum(2).values.joinToString { it },
                subtitle = LoremIpsum(7).values.joinToString { it },
                type = CardMessageType.SUCCESS,
                primaryButtonTitle = "Primary"
            )
        }
    }
}
