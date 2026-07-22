package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText
import com.eysamarin.squadplay.designSystem.compose.utils.previewIconPainter

enum class ButtonStyle {
    Filled,
    Tinted,
    Outline,
    Text,
}

enum class ButtonSize {
    Default,
    Small,
}

enum class ButtonState {
    Default,
    Error,
}

@Composable
fun Button(
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    size: ButtonSize = ButtonSize.Default,
    state: ButtonState = ButtonState.Default,
    iconPainter: Painter? = null,
    text: String? = null,
    enabled: Boolean = true,
    onTap: () -> Unit = {},
) {
    val containerColor = if (!enabled) {
        DesignSystemTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    } else {
        when (style) {
            ButtonStyle.Filled -> when (state) {
                ButtonState.Default -> DesignSystemTheme.colorScheme.primary
                ButtonState.Error -> DesignSystemTheme.colorScheme.error
            }

            ButtonStyle.Tinted -> when (state) {
                ButtonState.Default -> DesignSystemTheme.colorScheme.secondary
                ButtonState.Error -> DesignSystemTheme.colorScheme.error
            }

            ButtonStyle.Text,
            ButtonStyle.Outline -> Color.Transparent
        }

    }

    val contentColor = if (!enabled) {
        DesignSystemTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        when (style) {
            ButtonStyle.Filled -> when (state) {
                ButtonState.Default -> DesignSystemTheme.colorScheme.onPrimary
                ButtonState.Error -> DesignSystemTheme.colorScheme.onError
            }

            ButtonStyle.Tinted -> when (state) {
                ButtonState.Default -> DesignSystemTheme.colorScheme.onSecondary
                ButtonState.Error -> DesignSystemTheme.colorScheme.onError
            }

            ButtonStyle.Text,
            ButtonStyle.Outline -> when (state) {
                ButtonState.Default -> DesignSystemTheme.colorScheme.primary
                ButtonState.Error -> DesignSystemTheme.colorScheme.error
            }
        }
    }

    val paddingsVertical = when (size) {
        ButtonSize.Default -> 12.dp
        ButtonSize.Small -> 8.dp
    }
    val paddingsHorizontal = when (size) {
        ButtonSize.Default -> 20.dp
        ButtonSize.Small -> 12.dp
    }
    val horizontalSpaceBy = when (size) {
        ButtonSize.Default -> 8.dp
        ButtonSize.Small -> 4.dp
    }
    val cornerRadius = when (size) {
        ButtonSize.Default -> 10.dp
        ButtonSize.Small -> 20.dp
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(size = cornerRadius))
            .clickable(
                enabled = enabled,
                onClick = onTap
            )
            .background(containerColor)
            .then(
                if (style == ButtonStyle.Outline && enabled) {
                    Modifier.border(
                        width = 1.dp,
                        color = contentColor,
                        shape = RoundedCornerShape(size = cornerRadius)
                    )
                } else Modifier
            )
            .padding(horizontal = paddingsHorizontal, vertical = paddingsVertical),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconPainter != null) {
            val iconSize = when (size) {
                ButtonSize.Default -> 24.dp
                ButtonSize.Small -> 18.dp
            }

            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize),
                tint = contentColor,
            )
            if (!text.isNullOrEmpty()) {
                Spacer(Modifier.size(horizontalSpaceBy))
            }
        }
        if (!text.isNullOrEmpty()) {
            Text(
                text = text,
                style = when (size) {
                    ButtonSize.Default -> DesignSystemTheme.extendedTypography.bodyEmphasized
                    ButtonSize.Small -> DesignSystemTheme.extendedTypography.footnoteEmphasized
                },
                overflow = Ellipsis,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = contentColor,
            )
        }
    }
}

@PhoneLightModePreview
@PhoneDarkModePreview
@Composable
private fun ButtonPreview() {
    DesignSystemTheme {
        Column(
            modifier = Modifier
                .background(DesignSystemTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            VariantPreviewText("Variant 0: external modifiers")
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                iconPainter = previewIconPainter(),
                text = "Full line Button with long text label",
                state = ButtonState.Default,
                size = ButtonSize.Default,
                style = ButtonStyle.Filled,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    modifier = Modifier.weight(1f),
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
            }
            VariantPreviewText("Variant 1: size=Default")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Outline,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Outline,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Outline,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Text,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Text,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Text,
                )
            }
            VariantPreviewText("Variant 2: size=Small")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Tinted,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Outline,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Outline,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Outline,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Text,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    enabled = false,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Text,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    text = "Label",
                    state = ButtonState.Error,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Text,
                )
            }
        }
    }
}