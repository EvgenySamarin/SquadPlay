package com.eysamarin.squadplay.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.material3.SnackbarDuration
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.StringRepository
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface InAppUpdateManager {
    fun registerListener()
    fun unregisterListener()
    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>)
    fun resumeCheck(launcher: ActivityResultLauncher<IntentSenderRequest>)
    fun completeUpdate()
    fun onUpdateActivityResult(resultCode: Int)
}

class InAppUpdateManagerImpl(
    private val appUpdateManager: AppUpdateManager,
    private val snackbarProvider: SnackbarProvider,
    private val stringRepository: StringRepository,
    private val logger: AppLogger,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : InAppUpdateManager {

    companion object {
        private const val TAG = "InAppUpdateManager"
        const val HIGH_PRIORITY_UPDATE_PRIORITY = 4
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        handleInstallState(state)
    }

    override fun registerListener() {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    override fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        coroutineScope.launch {
            try {
                val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
                handleAppUpdateInfo(appUpdateInfo, launcher)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Failed to check for app update" }
            }
        }
    }

    override fun resumeCheck(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        coroutineScope.launch {
            try {
                val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    notifyUpdateDownloaded()
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startImmediateUpdate(appUpdateInfo, launcher)
                }
            } catch (e: Exception) {
                logger.w(TAG, e) { "Failed to check app update on resume" }
            }
        }
    }

    internal fun handleInstallState(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                logger.i(TAG) { "In-app update download completed" }
                notifyUpdateDownloaded()
            }
            InstallStatus.FAILED -> {
                logger.e(TAG) { "In-app update download failed with errorCode: ${state.installErrorCode()}" }
            }
            InstallStatus.CANCELED -> {
                logger.w(TAG) { "In-app update download was canceled" }
            }
            else -> {
                logger.d(TAG) { "In-app update install status: ${state.installStatus()}" }
            }
        }
    }

    internal fun handleAppUpdateInfo(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        when {
            appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                notifyUpdateDownloaded()
            }
            appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                startImmediateUpdate(appUpdateInfo, launcher)
            }
            appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                if (appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE_PRIORITY &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    logger.i(TAG) { "Triggering immediate update (priority: ${appUpdateInfo.updatePriority()})" }
                    startImmediateUpdate(appUpdateInfo, launcher)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    logger.i(TAG) { "Triggering flexible update (priority: ${appUpdateInfo.updatePriority()})" }
                    startFlexibleUpdate(appUpdateInfo, launcher)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    logger.i(TAG) { "Triggering immediate update as fallback (priority: ${appUpdateInfo.updatePriority()})" }
                    startImmediateUpdate(appUpdateInfo, launcher)
                } else {
                    logger.w(TAG) { "Update available but neither IMMEDIATE nor FLEXIBLE update type is allowed" }
                }
            }
            else -> {
                logger.d(TAG) { "Update not available or in unrecognized availability state: ${appUpdateInfo.updateAvailability()}" }
            }
        }
    }

    private fun startImmediateUpdate(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
            )
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to start immediate update flow" }
        }
    }

    private fun startFlexibleUpdate(
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
            )
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to start flexible update flow" }
        }
    }

    internal fun notifyUpdateDownloaded() {
        coroutineScope.launch {
            snackbarProvider.showMessage(
                message = stringRepository.updateDownloadedMessage,
                actionLabel = stringRepository.updateRestartAction,
                duration = SnackbarDuration.Indefinite,
                onAction = {
                    completeUpdate()
                },
            )
        }
    }

    override fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            logger.e(TAG, e) { "Failed to complete in-app update" }
        }
    }

    override fun onUpdateActivityResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            logger.w(TAG) { "In-app update flow failed or canceled with resultCode: $resultCode" }
        } else {
            logger.i(TAG) { "In-app update flow result accepted with RESULT_OK" }
        }
    }
}
