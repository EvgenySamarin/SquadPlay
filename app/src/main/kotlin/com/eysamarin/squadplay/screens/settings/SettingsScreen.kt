package com.eysamarin.squadplay.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.models.SettingsScreenAction
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import com.eysamarin.squadplay.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.utils.PhoneLightModePreview
import com.eysamarin.squadplay.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (SettingsScreenAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(SettingsScreenAction.OnBackButtonTap) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.content_description_back),
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.settings_screen_title))
                }
            )
        },
        content = { innerPadding ->
            val isExpanded = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = if (isExpanded) Alignment.TopCenter else Alignment.TopStart,
            ) {
                SettingsMediumLayout(
                    modifier = if (isExpanded) Modifier.fillMaxWidth(0.6f) else Modifier.fillMaxSize(),
                    onAction = onAction
                )
            }
        },
        snackbarHost = snackbarHost,
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun SettingsMediumLayout(
    modifier: Modifier = Modifier,
    onAction: (SettingsScreenAction) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsScreenAction.OnLicensesTap) },
                headlineContent = { Text(text = stringResource(R.string.settings_screen_licenses_title)) },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_license_24),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun SettingsScreenPhonePreview() {
    SquadPlayTheme {
        SettingsScreen(onAction = {})
    }
}
//endregion