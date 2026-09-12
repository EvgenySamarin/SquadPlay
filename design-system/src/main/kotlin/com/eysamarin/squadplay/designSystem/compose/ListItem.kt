package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText
import com.eysamarin.squadplay.designSystem.compose.utils.previewIconPainter
import com.eysamarin.squadplay.designSystem.compose.utils.previewImagePainter

enum class DSListItemLeadingType { Monogram, Image }

@Immutable
data class DSListItemColors(
    val contentColor: Color,
    val overlineColor: Color,
    val supportingTextColor: Color,
    val containerColor: Color,
    val selectedContainerColor: Color,
    val monogramBackgroundColor: Color,
    val disabledAlpha: Float = 0.38f,
)

@Immutable
data class DSListItemSizes(
    val minHeight: Dp,
    val maxHeight: Dp,
    val iconSize: Dp,
    val imageWidth: Dp,
    val horizontalSpacing: Dp,
    val verticalPadding: Dp,
)

object DSListItemDefaults {

    @Composable
    fun colors(
        contentColor: Color = DesignSystemTheme.colorScheme.onSurface,
        overlineColor: Color = DesignSystemTheme.colorScheme.onSurfaceVariant,
        supportingTextColor: Color = DesignSystemTheme.colorScheme.onSurfaceVariant,
        containerColor: Color = Color.Transparent,
        selectedContainerColor: Color = DesignSystemTheme.colorScheme.surfaceVariant,
        monogramBackgroundColor: Color = DesignSystemTheme.colorScheme.primaryContainer,
    ) = DSListItemColors(
        contentColor = contentColor,
        overlineColor = overlineColor,
        supportingTextColor = supportingTextColor,
        containerColor = containerColor,
        selectedContainerColor = selectedContainerColor,
        monogramBackgroundColor = monogramBackgroundColor,
    )

    @Composable
    fun sizes(
        minHeight: Dp = 56.dp,
        maxHeight: Dp = 56.dp,
        iconSize: Dp = 40.dp,
        imageWidth: Dp = 80.dp,
        horizontalSpacing: Dp = 16.dp,
        verticalPadding: Dp = 8.dp,
    ) = DSListItemSizes(
        minHeight = minHeight,
        maxHeight = maxHeight,
        iconSize = iconSize,
        imageWidth = imageWidth,
        horizontalSpacing = horizontalSpacing,
        verticalPadding = verticalPadding,
    )
}

@Composable
fun DSListItem(
    modifier: Modifier = Modifier,
    headline: String,
    overline: String? = null,
    supportingText: String? = null,
    trailingSupportingText: String? = null,
    leadingType: DSListItemLeadingType? = null,
    leadingPainter: Painter? = null,
    leadingText: String? = null,
    leadingContentDescription: String? = null,
    trailingIconPainter: Painter? = null,
    trailingIconContentDescription: String? = null,
    trailingIconTint: Color? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    colors: DSListItemColors = DSListItemDefaults.colors(),
    sizes: DSListItemSizes = DSListItemDefaults.sizes(),
    onClick: (() -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
) {
    val currentContainerColor = if (selected) colors.selectedContainerColor else colors.containerColor
    val contentAlpha = if (enabled) 1f else colors.disabledAlpha

    Row(
        modifier = modifier
            .heightIn(min = sizes.minHeight)
            .background(currentContainerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else Modifier,
            )
            .padding(end = 16.dp)
            .then(
                if (leadingType != DSListItemLeadingType.Image) {
                    Modifier.padding(start = 16.dp)
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(sizes.horizontalSpacing)
    ) {
        // Leading content
        if (leadingType != null) {
            Box(contentAlignment = Alignment.Center) {
                when (leadingType) {
                    DSListItemLeadingType.Monogram -> DSMonogram(
                        text = leadingText ?: "",
                        backgroundColor = colors.monogramBackgroundColor,
                        alpha = contentAlpha,
                        sizes = sizes,
                    )

                    DSListItemLeadingType.Image -> {
                        val painter = leadingPainter ?: painterResource(com.eysamarin.squadplay.designSystem.R.drawable.img_stub)
                        DSListItemImage(
                            modifier
                                .heightIn(min = sizes.minHeight, max = sizes.maxHeight)
                                .width(sizes.imageWidth),
                            painter = painter,
                            contentDescription = leadingContentDescription,
                            alpha = contentAlpha,
                        )
                    }
                }
            }
        }

        // Content Area (Text + Trailing)
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = sizes.verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sizes.horizontalSpacing)
        ) {
            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (overline != null) {
                    Text(
                        text = overline,
                        style = DesignSystemTheme.typography.labelMedium,
                        color = colors.overlineColor.copy(alpha = contentAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = headline,
                    style = DesignSystemTheme.typography.bodyLarge,
                    color = colors.contentColor.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = DesignSystemTheme.typography.bodyMedium,
                        color = colors.supportingTextColor.copy(alpha = contentAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing content
            if ((trailingSupportingText != null) || (trailingIconPainter != null)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trailingSupportingText != null) {
                        Text(
                            text = trailingSupportingText,
                            style = DesignSystemTheme.typography.labelSmall,
                            color = colors.supportingTextColor.copy(alpha = contentAlpha),
                            maxLines = 1
                        )
                    }
                    if (trailingIconPainter != null) {
                        if (onTrailingIconClick != null) {
                            IconButton(
                                onClick = onTrailingIconClick,
                                modifier = Modifier.size(48.dp) // Standard IconButton size
                            ) {
                                Icon(
                                    painter = trailingIconPainter,
                                    contentDescription = trailingIconContentDescription,
                                    modifier = Modifier.size(24.dp),
                                    tint = trailingIconTint ?: colors.contentColor.copy(alpha = contentAlpha)
                                )
                            }
                        } else {
                            Icon(
                                painter = trailingIconPainter,
                                contentDescription = trailingIconContentDescription,
                                modifier = Modifier.size(24.dp),
                                tint = trailingIconTint ?: colors.contentColor.copy(alpha = contentAlpha)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DSMonogram(
    text: String,
    backgroundColor: Color,
    alpha: Float,
    sizes: DSListItemSizes,
) {
    Box(
        modifier = Modifier
            .size(sizes.iconSize)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DesignSystemTheme.typography.titleMedium,
            color = DesignSystemTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
        )
    }
}

@Composable
private fun DSListItemImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String?,
    alpha: Float,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alpha = alpha
    )
}

@PhoneLightModePreview
@PhoneDarkModePreview
@Composable
private fun DSListItemPreview() {
    DesignSystemTheme {
        Column(
            modifier = Modifier
                .background(DesignSystemTheme.colorScheme.surface)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            VariantPreviewText("Basic List Item")
            DSListItem(headline = "List item")

            VariantPreviewText("With Overline and Supporting")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem ipsum dolor sit amet..."
            )

            VariantPreviewText("Selected state")
            DSListItem(
                headline = "List item",
                supportingText = "Supporting line text lorem...",
                selected = true,
                trailingIconPainter = previewIconPainter()
            )

            VariantPreviewText("Monogram Leading")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem...",
                leadingType = DSListItemLeadingType.Monogram,
                leadingText = "A",
                trailingIconPainter = previewIconPainter()
            )

            VariantPreviewText("Image Leading + Trailing Support")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem...",
                leadingType = DSListItemLeadingType.Image,
                leadingPainter = previewImagePainter(),
                trailingSupportingText = "100+",
                trailingIconPainter = previewIconPainter()
            )

            VariantPreviewText("Monogram Leading + Selected")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem...",
                leadingType = DSListItemLeadingType.Monogram,
                leadingText = "A",
                selected = true,
                trailingIconPainter = previewIconPainter()
            )

            VariantPreviewText("Image Leading + Selected")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem...",
                leadingType = DSListItemLeadingType.Image,
                leadingPainter = previewImagePainter(),
                selected = true,
                trailingSupportingText = "100+",
                trailingIconPainter = previewIconPainter()
            )

            VariantPreviewText("Trailing Supporting Text only")
            DSListItem(
                headline = "List item",
                trailingSupportingText = "100+"
            )

            VariantPreviewText("Disabled state")
            DSListItem(
                headline = "List item",
                overline = "Overline",
                supportingText = "Supporting line text lorem...",
                leadingType = DSListItemLeadingType.Monogram,
                leadingText = "A",
                enabled = false,
                trailingIconPainter = previewIconPainter()
            )
        }
    }
}
