package com.eysamarin.squadplay.designSystem.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.theme.Typography
import com.eysamarin.squadplay.designSystem.compose.utils.DarkLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.VariantPreviewText
import com.eysamarin.squadplay.designSystem.compose.utils.previewIconPainter

@Immutable
data class NavigationBarDefaultTestTags(
    val leadingIconTestTag: String,
    val trailingIcon1TestTag: String,
    val trailingIcon2TestTag: String,
    val trailingIcon3TestTag: String,
)

object NavigationBarDefaults {

    @Composable
    fun testTags(
        leadingIconTestTag: String = "",
        trailingIcon1TestTag: String = "",
        trailingIcon2TestTag: String = "",
        trailingIcon3TestTag: String = "",
    ) = NavigationBarDefaultTestTags(
        leadingIconTestTag = leadingIconTestTag,
        trailingIcon1TestTag = trailingIcon1TestTag,
        trailingIcon2TestTag = trailingIcon2TestTag,
        trailingIcon3TestTag = trailingIcon3TestTag,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBarDefault(
    title: String,
    modifier: Modifier = Modifier,
    leadingIconPainter: Painter? = null,
    onLeadingIconTap: () -> Unit = {},
    trailingIcon1Painter: Painter? = null,
    onTrailingIcon1Tap: () -> Unit = {},
    trailingIcon2Painter: Painter? = null,
    onTrailingIcon2Tap: () -> Unit = {},
    trailingIcon3Painter: Painter? = null,
    onTrailingIcon3Tap: () -> Unit = {},
    testTags: NavigationBarDefaultTestTags = NavigationBarDefaults.testTags(),
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                color = DesignSystemTheme.colorScheme.onSurface,
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.titleLarge,
            )
        },
        modifier = modifier,
        navigationIcon = {
            leadingIconPainter?.let { painter ->
                IconButton(
                    onClick = onLeadingIconTap,
                    modifier = Modifier.testTag(testTags.leadingIconTestTag),
                ) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = DesignSystemTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            Row(horizontalArrangement = Arrangement.End) {
                trailingIcon1Painter?.let { painter ->
                    IconButton(
                        onClick = onTrailingIcon1Tap,
                        modifier = Modifier.testTag(testTags.trailingIcon1TestTag),
                    ) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = DesignSystemTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                trailingIcon2Painter?.let { painter ->
                    IconButton(
                        onClick = onTrailingIcon2Tap,
                        modifier = Modifier.testTag(testTags.trailingIcon2TestTag),
                    ) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = DesignSystemTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                trailingIcon3Painter?.let { painter ->
                    IconButton(
                        onClick = onTrailingIcon3Tap,
                        modifier = Modifier.testTag(testTags.trailingIcon3TestTag),
                    ) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = DesignSystemTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

        }
    )
}

@DarkLightModePreview
@Composable
private fun NavigationBarDefaultPreview() {
    DesignSystemTheme {
        Column {
            VariantPreviewText("Variant 1")
            NavigationBarDefault(
                title = "Title",
                leadingIconPainter = previewIconPainter(),
                trailingIcon1Painter = previewIconPainter(),
                trailingIcon2Painter = previewIconPainter(),
                trailingIcon3Painter = previewIconPainter()
            )
            VariantPreviewText("Variant 2")
            NavigationBarDefault(
                title = "Title with long long long text",
                leadingIconPainter = previewIconPainter(),
                trailingIcon1Painter = previewIconPainter(),
                trailingIcon2Painter = previewIconPainter(),
            )
            VariantPreviewText("Variant 3")
            NavigationBarDefault(
                title = "Title",
                trailingIcon1Painter = previewIconPainter(),
            )
            VariantPreviewText("Variant 4")
            NavigationBarDefault(
                title = "Title",
            )
        }
    }
}