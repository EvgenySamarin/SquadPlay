package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText
import com.eysamarin.squadplay.designSystem.compose.utils.previewIconPainter

enum class DashboardButtonState {
    DEFAULT, DESTRUCTIVE, DISABLED
}

@Immutable
data class DashboardButtonColors(
    val defaultContainerColor: Color,
    val defaultContentColor: Color,
    val defaultLabelColor: Color,
    val destructiveContainerColor: Color,
    val destructiveContentColor: Color,
    val destructiveLabelColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledLabelColor: Color,
)

object DashboardButtonDefaults {

    /**
     * Creates a [DashboardButtonColors] that represents the default container and content colors used in a
     * [DashboardButton].
     *
     * @param defaultContainerColor the container color of this button for default state when enabled.
     * @param defaultContentColor the color of this button's icon for default state content when enabled.
     * @param destructiveContainerColor the container color of this button for destructive state when enabled
     * @param destructiveContentColor the color of this button's icon for destructive state content when enabled.
     */
    @Composable
    fun colors(
        defaultContainerColor: Color = DesignSystemTheme.colorScheme.primary,
        defaultContentColor: Color = DesignSystemTheme.colorScheme.onPrimary,
        defaultLabelColor: Color = DesignSystemTheme.colorScheme.primary,
        destructiveContainerColor: Color = DesignSystemTheme.colorScheme.error,
        destructiveContentColor: Color = DesignSystemTheme.colorScheme.onError,
        destructiveLabelColor: Color = DesignSystemTheme.colorScheme.error,
        disabledContainerColor: Color = DesignSystemTheme.colorScheme.secondary.copy(alpha = 0.16f),
        disabledContentColor: Color = DesignSystemTheme.colorScheme.secondary.copy(alpha = 0.6f),
        disabledLabelColor: Color = DesignSystemTheme.colorScheme.secondary.copy(alpha = 0.6f),
    ) = DashboardButtonColors(
        defaultContainerColor = defaultContainerColor,
        defaultContentColor = defaultContentColor,
        defaultLabelColor = defaultLabelColor,
        destructiveContainerColor = destructiveContainerColor,
        destructiveContentColor = destructiveContentColor,
        destructiveLabelColor = destructiveLabelColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledLabelColor = disabledLabelColor,
    )
}

/**
 * @see <a href="https://m3.material.io/components/icon-buttons/specs">
 *     Icon buttons material 3 specs
 *     </a>
 */
@Composable
fun DashboardButton(
    iconPainter: Painter,
    iconContentDescription: String? = null,
    title: String? = null,
    state: DashboardButtonState = DashboardButtonState.DEFAULT,
    colors: DashboardButtonColors = DashboardButtonDefaults.colors(),
    testTag: String? = null,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick, enabled = state != DashboardButtonState.DISABLED)
            .testTag(testTag.orEmpty()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(DesignSystemTheme.shapes.medium)
                .background(
                    when (state) {
                        DashboardButtonState.DEFAULT -> colors.defaultContainerColor
                        DashboardButtonState.DESTRUCTIVE -> colors.destructiveContainerColor
                        DashboardButtonState.DISABLED -> colors.disabledContainerColor
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = iconPainter,
                contentDescription = iconContentDescription,
                tint = when (state) {
                    DashboardButtonState.DEFAULT -> colors.defaultContentColor
                    DashboardButtonState.DESTRUCTIVE -> colors.destructiveContentColor
                    DashboardButtonState.DISABLED -> colors.disabledContentColor
                },
            )
        }
        title?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = title,
                textAlign = TextAlign.Center,
                style = DesignSystemTheme.typography.titleSmall,
                color = when (state) {
                    DashboardButtonState.DEFAULT -> colors.defaultLabelColor
                    DashboardButtonState.DESTRUCTIVE -> colors.destructiveLabelColor
                    DashboardButtonState.DISABLED -> colors.disabledLabelColor
                }
            )
        }
    }
}

@DarkLightModePreview
@Composable
private fun DashboardButtonPreview() {
    DesignSystemTheme {
        Column(modifier = Modifier.background(DesignSystemTheme.colorScheme.surface)) {
            VariantPreviewText("Variant 1")
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DEFAULT
                )
                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DESTRUCTIVE
                )
                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DISABLED
                )
            }
            VariantPreviewText("Variant 2- labeled")
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DEFAULT,
                    title = "Label",
                )

                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DESTRUCTIVE,
                    title = "Label",
                )
                DashboardButton(
                    iconPainter = previewIconPainter(),
                    state = DashboardButtonState.DISABLED,
                    title = "Label"
                )
            }
        }
    }
}