package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText
import com.eysamarin.squadplay.designSystem.compose.utils.previewIconPainter

enum class ButtonStyle {
    Filled,
    Tinted,
    Plain,
}

enum class ButtonSize {
    Default,
    Small,
}

enum class ButtonState {
    Default,
    Disabled,
    Destructive,
}

@Composable
fun Button(
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    tint: Color = DesignSystemTheme.colorScheme.primary,
    size: ButtonSize = ButtonSize.Default,
    state: ButtonState = ButtonState.Default,
    iconPainter: Painter? = null,
    title: AnnotatedString,
    onTap: () -> Unit = {},
) {
    BaseButton(
        modifier = modifier,
        style = style,
        tint = tint,
        size = size,
        state = state,
        iconPainter = iconPainter,
        onTap = onTap,
        title = title
    )
}

@Composable
fun Button(
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    tint: Color = DesignSystemTheme.colorScheme.primary,
    size: ButtonSize = ButtonSize.Default,
    state: ButtonState = ButtonState.Default,
    iconPainter: Painter? = null,
    title: String? = null,
    onTap: () -> Unit = {},
) {
    BaseButton(
        modifier = modifier,
        style = style,
        tint = tint,
        size = size,
        state = state,
        iconPainter = iconPainter,
        title = title?.let { AnnotatedString(it) },
        onTap = onTap,
    )
}

@Composable
private fun BaseButton(
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    tint: Color = DesignSystemTheme.colorScheme.primary,
    size: ButtonSize = ButtonSize.Default,
    state: ButtonState = ButtonState.Default,
    iconPainter: Painter? = null,
    title: AnnotatedString? = null,
    onTap: () -> Unit = {},
) {
    val containerColor = when (state) {
        ButtonState.Default -> tint
        ButtonState.Destructive -> DesignSystemTheme.colorScheme.errorContainer
        ButtonState.Disabled -> DesignSystemTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    val contentColor = when (style) {
        ButtonStyle.Filled -> when (state) {
            ButtonState.Disabled -> DesignSystemTheme.colorScheme.outline.copy(alpha = 0.3f)
            ButtonState.Default,
            ButtonState.Destructive -> Color.White
        }

        ButtonStyle.Tinted,
        ButtonStyle.Plain -> when (state) {
            ButtonState.Disabled -> DesignSystemTheme.colorScheme.outline.copy(alpha = 0.3f)
            ButtonState.Default -> tint
            ButtonState.Destructive -> DesignSystemTheme.colorScheme.onErrorContainer
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
                enabled = state != ButtonState.Disabled,
                onClick = onTap
            )
            .background(
                when (style) {
                    ButtonStyle.Filled -> containerColor
                    ButtonStyle.Tinted -> containerColor.copy(alpha = 0.3f)
                    ButtonStyle.Plain -> Color.Transparent
                }
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
            if (!title.isNullOrEmpty()) {
                Spacer(Modifier.size(horizontalSpaceBy))
            }
        }
        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
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
@Composable
private fun DashboardButtonPreview() {
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
                title = "Full line Button with long text label",
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    modifier = Modifier.weight(1f),
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Plain,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Plain,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
                    size = ButtonSize.Default,
                    style = ButtonStyle.Plain,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Filled,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Tinted,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
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
                    title = "Label",
                    state = ButtonState.Default,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Plain,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Disabled,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Plain,
                )
                Button(
                    iconPainter = previewIconPainter(),
                    title = "Label",
                    state = ButtonState.Destructive,
                    size = ButtonSize.Small,
                    style = ButtonStyle.Plain,
                )
            }
        }
    }
}