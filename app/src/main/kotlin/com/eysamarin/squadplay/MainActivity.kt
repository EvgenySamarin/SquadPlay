package com.eysamarin.squadplay

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.eysamarin.squadplay.ui.PermissionDialog
import com.eysamarin.squadplay.ui.theme.SquadPlayTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val permissionsToRequest = arrayOf(
        getPostNotificationsPermissionName()
    )

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SquadPlayTheme {
                val windowSize = calculateWindowSizeClass(this)
                val viewModel: LaunchApplicationViewModel = koinViewModel()
                val permissionDialogQueue = viewModel.visiblePermissionDialogQueue

                val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->
                        perms.keys.forEach { permission ->
                            viewModel.onPermissionResult(
                                permission = permission,
                                isGranted = perms[permission] == true,
                            )
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                }

                permissionDialogQueue
                    .reversed()
                    .forEach { permission ->
                        PermissionDialog(
                            windowSize = windowSize,
                            descriptionText = if (!shouldShowRequestPermissionRationale(permission)) {
                                "It seems you permanently declined $permission permission. You can go to the app settings to grant it."
                            } else {
                                "Notification permission is required to see new event created by your friends"
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                            onDismiss = viewModel::dismissPermissionDialog,
                            onConfirmTap = {
                                viewModel.dismissPermissionDialog()
                                multiplePermissionResultLauncher.launch(input = permissionsToRequest)
                            },
                            onGoToAppSettingsTap = {
                                viewModel.dismissPermissionDialog()
                                openAppSettings()
                            }
                        )
                    }

                SquadPlayApp(windowSize)
            }
        }
    }

    private fun Activity.openAppSettings() {
        Intent(
            /* action = */ Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            /* uri = */ Uri.fromParts("package", packageName, null)
        ).also(::startActivity)
    }

    private fun getPostNotificationsPermissionName(): String  {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            POST_NOTIFICATIONS
        } else {
            "android.permission.POST_NOTIFICATIONS"
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun FinanceStocksAppPreview() {
    SquadPlayTheme {
        SquadPlayApp(WindowSizeClass.calculateFromSize(DpSize(400.dp, 900.dp)))
    }
}