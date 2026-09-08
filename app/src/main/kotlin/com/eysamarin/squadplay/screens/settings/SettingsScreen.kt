package com.eysamarin.squadplay.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.BuildConfig
import com.eysamarin.squadplay.R
import com.eysamarin.squadplay.designSystem.compose.DSButton
import com.eysamarin.squadplay.designSystem.compose.theme.DesignSystemTheme
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneDarkModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PhoneLightModePreview
import com.eysamarin.squadplay.designSystem.compose.utils.PreviewUtils.WINDOWS_SIZE_MEDIUM
import com.eysamarin.squadplay.models.SettingsScreenAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    snackbarHost: @Composable () -> Unit = {},
    windowSize: WindowSizeClass = WINDOWS_SIZE_MEDIUM,
    onAction: (SettingsScreenAction) -> Unit,
) {
    var showPickTimeDialog by remember { mutableStateOf(false) }

    if (showPickTimeDialog) {
        AlertDialog(
            onDismissRequest = { showPickTimeDialog = false },
            title = {
                Text(text = stringResource(R.string.picktime_license_title))
            },
            text = {
                Text(text = stringResource(R.string.picktime_license_notice))
            },
            confirmButton = {
                DSButton(
                    text = stringResource(R.string.close),
                    onTap = { showPickTimeDialog = false }
                )
            }
        )
    }

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
                    onAction = onAction,
                    onPickTimeTap = { showPickTimeDialog = true }
                )
            }
        },
        bottomBar = {
            SettingsBottomBar()
        },
        snackbarHost = snackbarHost,
        containerColor = DesignSystemTheme.colorScheme.surface,
    )
}

@Composable
private fun SettingsMediumLayout(
    modifier: Modifier = Modifier,
    onAction: (SettingsScreenAction) -> Unit,
    onPickTimeTap: () -> Unit,
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
        item {
            ListItem(
                modifier = Modifier.clickable { onPickTimeTap() },
                headlineContent = { Text(text = stringResource(R.string.picktime_license_title)) },
                supportingContent = { Text(text = stringResource(R.string.picktime_license_subtitle)) },
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

@Composable
private fun SettingsBottomBar(
    modifier: Modifier = Modifier,
    versionName: String = BuildConfig.VERSION_NAME,
    versionCode: Int = BuildConfig.VERSION_CODE,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.settings_screen_version, versionName, versionCode),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = DesignSystemTheme.typography.bodyMedium,
            color = DesignSystemTheme.colorScheme.onSurfaceVariant,
        )
    }
}

//region screen preview
@PhoneDarkModePreview
@PhoneLightModePreview
@Composable
fun SettingsScreenPhonePreview() {
    DesignSystemTheme {
        SettingsScreen(onAction = {})
    }
}
//endregion