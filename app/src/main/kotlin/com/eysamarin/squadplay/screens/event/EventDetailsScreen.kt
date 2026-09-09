package com.eysamarin.squadplay.screens.event

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.screens.main.ConfirmationDialog
import com.eysamarin.squadplay.ui.ImageTopBar
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.designSystem.compose.utils.TabletDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.TabletLightModePreview
import com.eysamarin.squadplay.models.EventDetailsScreenAction
import com.eysamarin.squadplay.models.EventDetailsScreenUI
import com.eysamarin.squadplay.models.PREVIEW_EVENT_DETAILS_SCREEN_UI
import com.eysamarin.squadplay.ui.theme.adaptiveBodyByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveHeadlineByHeight
import com.eysamarin.squadplay.ui.theme.adaptiveTitleByHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    state: EventDetailsScreenUI,
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (EventDetailsScreenAction) -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            val originalAppearance = insetsController.isAppearanceLightStatusBars

            insetsController.isAppearanceLightStatusBars = false

            onDispose { insetsController.isAppearanceLightStatusBars = originalAppearance }
        }
    }

    val cornerRadius = 24.dp
    val headerHeight = 220.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignSystemTheme.colorScheme.background)
    ) {
        ImageTopBar(
            imageUrl = state.imageUrl,
            headerHeight = headerHeight,
            onBackTap = { onAction(EventDetailsScreenAction.OnBackButtonTap) },
            actions = {
                if (state.isYourEvent) {
                    IconButton(
                        onClick = { onAction(EventDetailsScreenAction.OnDeleteTap) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_24),
                            contentDescription = stringResource(R.string.content_description_delete),
                            tint = Color.White,
                        )
                    }
                }
            }
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight - cornerRadius),
            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
            color = DesignSystemTheme.colorScheme.surface
        ) {
            val isExpanded = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentAlignment = if (isExpanded) Alignment.TopCenter else Alignment.TopStart,
            ) {
                Column(
                    modifier = if (isExpanded) Modifier.fillMaxWidth(0.6f) else Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.title,
                        style = adaptiveHeadlineByHeight(windowSize),
                        color = DesignSystemTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.event_details_date_label),
                        style = adaptiveTitleByHeight(windowSize),
                        color = DesignSystemTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.date,
                        style = adaptiveBodyByHeight(windowSize),
                        color = DesignSystemTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    if (state.showDeleteConfirmation) {
        ConfirmationDialog(
            windowSize = windowSize,
            title = stringResource(R.string.delete_event_dialog_title),
            text = stringResource(R.string.delete_event_dialog_message),
            onConfirmTap = { onAction(EventDetailsScreenAction.OnConfirmDeleteTap) },
            onDismiss = { onAction(EventDetailsScreenAction.OnDismissDeleteDialog) },
        )
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
private fun EventDetailsScreenPhonePreview() {
    DesignSystemTheme {
        EventDetailsScreen(
            state = PREVIEW_EVENT_DETAILS_SCREEN_UI,
            onAction = {}
        )
    }
}

@TabletDarkModePreview
@TabletLightModePreview
@Composable
private fun EventDetailsScreenTabletPreview() {
    DesignSystemTheme {
        EventDetailsScreen(
            state = PREVIEW_EVENT_DETAILS_SCREEN_UI,
            onAction = {}
        )
    }
}
//endregion
