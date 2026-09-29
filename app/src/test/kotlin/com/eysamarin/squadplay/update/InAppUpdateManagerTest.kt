package com.eysamarin.squadplay.update

import android.app.Activity
import android.app.PendingIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import com.eysamarin.squadplay.contracts.AppLogger
import com.eysamarin.squadplay.contracts.StringRepository
import com.eysamarin.squadplay.messaging.SnackbarMessage
import com.eysamarin.squadplay.messaging.SnackbarProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InAppUpdateManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var testAppUpdateManager: TestAppUpdateManager
    private lateinit var fakeSnackbarProvider: FakeSnackbarProvider
    private lateinit var fakeStringRepository: FakeStringRepository
    private lateinit var fakeLogger: FakeAppLogger
    private lateinit var fakeLauncher: FakeActivityResultLauncher
    private lateinit var inAppUpdateManager: InAppUpdateManagerImpl

    private class FakeStringRepository : StringRepository {
        override val cannotSignText: String = ""
        override val alreadyInSquad: String = ""
        override fun squadNotFound(groupId: String): String = ""
        override fun wantToJoinSquad(groupTitle: String): String = ""
        override fun fromToDate(fromDate: String, toDate: String): String = ""
        override val youHaveNoSquad: String = ""
        override val eventSaved: String = ""
        override val eventSaveFailed: String = ""
        override val joinedSquad: String = ""
        override val joinSquadFailed: String = ""
        override val updateDownloadedMessage: String = "An update has been downloaded."
        override val updateRestartAction: String = "Restart"
    }

    private class FakeSnackbarProvider : SnackbarProvider {
        var lastMessage: SnackbarMessage? = null
        override val messagesChannel: Flow<SnackbarMessage> = flowOf()

        override suspend fun showMessage(message: SnackbarMessage) {
            lastMessage = message
        }
    }

    private class FakeAppLogger : AppLogger {
        override fun d(tag: String?, message: () -> String) {}
        override fun i(tag: String?, message: () -> String) {}
        override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
        override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    }

    private class FakeActivityResultLauncher : ActivityResultLauncher<IntentSenderRequest>() {
        var launchedRequest: IntentSenderRequest? = null

        override fun launch(input: IntentSenderRequest, options: ActivityOptionsCompat?) {
            launchedRequest = input
        }

        override fun unregister() {}

        override val contract: ActivityResultContract<IntentSenderRequest, *>
            get() = throw UnsupportedOperationException()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    private class TestAppUpdateManager : AppUpdateManager {
        var updateInfoToReturn: AppUpdateInfo? = null
        var shouldFailRequest = false
        var registeredListener: InstallStateUpdatedListener? = null
        var startFlowCallCount = 0
        var lastUpdateTypeStarted: Int? = null
        var completeUpdateCalled = false

        override fun getAppUpdateInfo(): Task<AppUpdateInfo> {
            return if (shouldFailRequest) {
                Tasks.forException(Exception("Update check failed"))
            } else {
                Tasks.forResult(updateInfoToReturn ?: createAppUpdateInfo())
            }
        }

        override fun startUpdateFlowForResult(
            appUpdateInfo: AppUpdateInfo,
            launcher: ActivityResultLauncher<IntentSenderRequest>,
            appUpdateOptions: AppUpdateOptions,
        ): Boolean {
            startFlowCallCount++
            lastUpdateTypeStarted = appUpdateOptions.appUpdateType()
            return true
        }

        override fun registerListener(listener: InstallStateUpdatedListener) {
            registeredListener = listener
        }

        override fun unregisterListener(listener: InstallStateUpdatedListener) {
            if (registeredListener == listener) {
                registeredListener = null
            }
        }

        override fun completeUpdate(): Task<Void> {
            completeUpdateCalled = true
            return Tasks.forResult(null)
        }

        override fun startUpdateFlow(info: AppUpdateInfo, activity: Activity, options: AppUpdateOptions): Task<Int> =
            Tasks.forResult(0)

        override fun startUpdateFlowForResult(info: AppUpdateInfo, type: Int, activity: Activity, code: Int): Boolean = true
        override fun startUpdateFlowForResult(info: AppUpdateInfo, type: Int, starter: IntentSenderForResultStarter, code: Int): Boolean = true
        override fun startUpdateFlowForResult(info: AppUpdateInfo, activity: Activity, options: AppUpdateOptions, code: Int): Boolean = true
        override fun startUpdateFlowForResult(info: AppUpdateInfo, starter: IntentSenderForResultStarter, options: AppUpdateOptions, code: Int): Boolean = true
    }

    companion object {
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        fun createAppUpdateInfo(
            updateAvailability: Int = UpdateAvailability.UPDATE_AVAILABLE,
            installStatus: Int = InstallStatus.UNKNOWN,
            updatePriority: Int = 0,
            isImmediateAllowed: Boolean = true,
            isFlexibleAllowed: Boolean = true,
        ): AppUpdateInfo {
            val pendingIntentConstructor = PendingIntent::class.java.getDeclaredConstructor()
            pendingIntentConstructor.isAccessible = true
            val pendingIntent = pendingIntentConstructor.newInstance()

            val zzbMethod = AppUpdateInfo::class.java.getDeclaredMethod(
                "zzb",
                String::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Integer::class.java,
                Int::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                PendingIntent::class.java,
                PendingIntent::class.java,
                PendingIntent::class.java,
                PendingIntent::class.java,
                Map::class.java,
            )
            zzbMethod.isAccessible = true
            return zzbMethod.invoke(
                null,
                "com.eysamarin.squadplay",
                2,
                updateAvailability,
                installStatus,
                0,
                updatePriority,
                0L,
                1000L,
                0L,
                0L,
                if (isImmediateAllowed) pendingIntent else null,
                if (isFlexibleAllowed) pendingIntent else null,
                null,
                null,
                emptyMap<Any, Any>(),
            ) as AppUpdateInfo
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testAppUpdateManager = TestAppUpdateManager()
        fakeSnackbarProvider = FakeSnackbarProvider()
        fakeStringRepository = FakeStringRepository()
        fakeLogger = FakeAppLogger()
        fakeLauncher = FakeActivityResultLauncher()

        inAppUpdateManager = InAppUpdateManagerImpl(
            appUpdateManager = testAppUpdateManager,
            snackbarProvider = fakeSnackbarProvider,
            stringRepository = fakeStringRepository,
            logger = fakeLogger,
            coroutineScope = testScope,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkForUpdate_whenUpdateAvailableAndHighPriority_startsImmediateUpdate() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
            updatePriority = 5,
            isImmediateAllowed = true,
            isFlexibleAllowed = true,
        )

        inAppUpdateManager.checkForUpdate(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, testAppUpdateManager.startFlowCallCount)
        assertEquals(AppUpdateType.IMMEDIATE, testAppUpdateManager.lastUpdateTypeStarted)
    }

    @Test
    fun checkForUpdate_whenUpdateAvailableAndNormalPriority_startsFlexibleUpdate() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
            updatePriority = 2,
            isImmediateAllowed = true,
            isFlexibleAllowed = true,
        )

        inAppUpdateManager.checkForUpdate(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, testAppUpdateManager.startFlowCallCount)
        assertEquals(AppUpdateType.FLEXIBLE, testAppUpdateManager.lastUpdateTypeStarted)
    }

    @Test
    fun checkForUpdate_whenUpdateAvailableAndOnlyImmediateAllowed_startsImmediateUpdateFallback() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
            updatePriority = 1,
            isImmediateAllowed = true,
            isFlexibleAllowed = false,
        )

        inAppUpdateManager.checkForUpdate(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, testAppUpdateManager.startFlowCallCount)
        assertEquals(AppUpdateType.IMMEDIATE, testAppUpdateManager.lastUpdateTypeStarted)
    }

    @Test
    fun checkForUpdate_whenDownloaded_triggersSnackbarWithRestart() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            installStatus = InstallStatus.DOWNLOADED,
        )

        inAppUpdateManager.checkForUpdate(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeSnackbarProvider.lastMessage)
        assertEquals("An update has been downloaded.", fakeSnackbarProvider.lastMessage?.message)
        assertEquals("Restart", fakeSnackbarProvider.lastMessage?.actionLabel)

        // Tapping action invokes completeUpdate
        fakeSnackbarProvider.lastMessage?.onAction?.invoke()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(testAppUpdateManager.completeUpdateCalled)
    }

    @Test
    fun handleInstallState_whenDownloaded_triggersSnackbarWithRestart() = runTest(testDispatcher) {
        val installState = object : InstallState() {
            override fun bytesDownloaded(): Long = 1000
            override fun totalBytesToDownload(): Long = 1000
            override fun installErrorCode(): Int = InstallErrorCode.NO_ERROR
            override fun installStatus(): Int = InstallStatus.DOWNLOADED
            override fun packageName(): String = "com.eysamarin.squadplay"
        }

        inAppUpdateManager.handleInstallState(installState)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeSnackbarProvider.lastMessage)
        assertEquals("An update has been downloaded.", fakeSnackbarProvider.lastMessage?.message)
        assertEquals("Restart", fakeSnackbarProvider.lastMessage?.actionLabel)

        fakeSnackbarProvider.lastMessage?.onAction?.invoke()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(testAppUpdateManager.completeUpdateCalled)
    }

    @Test
    fun resumeCheck_whenImmediateUpdateInProgress_resumesImmediateUpdate() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            updateAvailability = UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
            isImmediateAllowed = true,
        )

        inAppUpdateManager.resumeCheck(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, testAppUpdateManager.startFlowCallCount)
        assertEquals(AppUpdateType.IMMEDIATE, testAppUpdateManager.lastUpdateTypeStarted)
    }

    @Test
    fun resumeCheck_whenUpdateDownloaded_triggersSnackbarWithRestart() = runTest(testDispatcher) {
        testAppUpdateManager.updateInfoToReturn = createAppUpdateInfo(
            installStatus = InstallStatus.DOWNLOADED,
        )

        inAppUpdateManager.resumeCheck(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(fakeSnackbarProvider.lastMessage)
        assertEquals("An update has been downloaded.", fakeSnackbarProvider.lastMessage?.message)
        assertEquals("Restart", fakeSnackbarProvider.lastMessage?.actionLabel)
    }

    @Test
    fun checkForUpdate_whenFails_logsWarningAndDoesNotCrash() = runTest(testDispatcher) {
        testAppUpdateManager.shouldFailRequest = true

        inAppUpdateManager.checkForUpdate(fakeLauncher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, testAppUpdateManager.startFlowCallCount)
        assertNull(fakeSnackbarProvider.lastMessage)
    }

    @Test
    fun registerAndUnregisterListener_lifecycleIntegration() {
        inAppUpdateManager.registerListener()
        assertNotNull(testAppUpdateManager.registeredListener)

        inAppUpdateManager.unregisterListener()
        assertNull(testAppUpdateManager.registeredListener)
    }

    @Test
    fun onUpdateActivityResult_handlesResultWithoutException() {
        inAppUpdateManager.onUpdateActivityResult(Activity.RESULT_OK)
        inAppUpdateManager.onUpdateActivityResult(Activity.RESULT_CANCELED)
    }
}
