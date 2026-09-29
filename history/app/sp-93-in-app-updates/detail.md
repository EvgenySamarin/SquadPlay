# Detail: Integrate Google Play In-App Updates API

* **Target Commit Title**: `[SP-93] feat(app): integrate google play in-app updates api`

## 1. Intent & Architectural Trade-offs
Previously, users were required to manually visit the Google Play Store to update SquadPlay, resulting in fragmented client versions in production and delay in propagating critical bug fixes or security policies. Integrating Google Play In-App Updates (`com.google.android.play:app-update` and `com.google.android.play:app-update-ktx`) enables in-app prompts and automatic installation flows.

Architectural choices made:
1. **Delegate Separation (`InAppUpdateManager`)**: Rather than bloating `MainActivity`, update availability checks, priority thresholds (`updatePriority >= 4`), install state listeners, and restart completions are encapsulated in `InAppUpdateManagerImpl`.
2. **Modern `ActivityResultLauncher` Integration**: Follows Android modern architecture guidelines by registering `ActivityResultContracts.StartIntentSenderForResult()` in `MainActivity` and passing the launcher to `startUpdateFlowForResult()`.
3. **Reactive Compose UI Integration**: To notify the user when a flexible update finishes downloading, `SnackbarProvider` was enhanced to support `SnackbarMessage` with action labels and execution callbacks (`onAction`), seamlessly integrating with Compose's `SnackbarHostState` across navigation destinations.
4. **Resilience & Testing**: Decoupled string loading via `StringRepository` and implemented unit test coverage (`InAppUpdateManagerTest`) verifying priority evaluation, flexible/immediate transitions, resume states, and listener lifecycle without relying on real Google Play services.

## 2. Detailed Contract & Schema Specifications
* Added `InAppUpdateManager` interface:
  ```kotlin
  interface InAppUpdateManager {
      fun registerListener()
      fun unregisterListener()
      fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>)
      fun resumeCheck(launcher: ActivityResultLauncher<IntentSenderRequest>)
      fun completeUpdate()
      fun onUpdateActivityResult(resultCode: Int)
  }
  ```
* Enhanced `SnackbarProvider` interface:
  ```kotlin
  data class SnackbarMessage(
      val message: String,
      val actionLabel: String? = null,
      val duration: SnackbarDuration = SnackbarDuration.Short,
      val onAction: (() -> Unit)? = null,
  )

  interface SnackbarProvider {
      val messagesChannel: Flow<SnackbarMessage>
      suspend fun showMessage(message: String)
      suspend fun showMessage(message: String, actionLabel: String?, duration: SnackbarDuration = SnackbarDuration.Short, onAction: (() -> Unit)? = null)
      suspend fun showMessage(message: SnackbarMessage)
  }
  ```
* Extended `StringRepository` contract:
  ```kotlin
  val updateDownloadedMessage: String
  val updateRestartAction: String
  ```

## 3. Executed Plan
- [x] Step 1: Add `com.google.android.play:app-update` and `com.google.android.play:app-update-ktx` to `gradle/libs.versions.toml` and configure dependencies in `app/build.gradle.kts`.
- [x] Step 2: Add string resources for update downloaded notification message and restart action label in `strings.xml`.
- [x] Step 3: Enhance `SnackbarProvider` to support action-capable snackbar messages (`SnackbarMessage`) and update `SquadPlayNavigation` to handle snackbar action callbacks via `SnackbarHostState`.
- [x] Step 4: Implement `InAppUpdateManager` delegate encapsulating `AppUpdateManager`, `InstallStateUpdatedListener` lifecycle, flexible/immediate decision logic, launcher invocation, and resume checks.
- [x] Step 5: Register `AppUpdateManager` and `InAppUpdateManager` in Koin DI in `SquadPlayApplication.kt`.
- [x] Step 6: Integrate `InAppUpdateManager` into `MainActivity` with `ActivityResultContracts.StartIntentSenderForResult()`, hooking into `onCreate()`, `onResume()`, and `onDestroy()`.
- [x] Step 7: Add unit tests in `InAppUpdateManagerTest` verifying flexible updates, immediate updates, downloaded notifications, and resume behavior; update existing `FakeSnackbarProvider` in view model tests.
- [x] Step 8: Verify build, run unit tests via `./gradlew testDebugUnitTest`, and verify linting.

## 4. Touched Files
* `gradle/libs.versions.toml`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/StringRepository.kt`
* `app/build.gradle.kts`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/data/StringRepositoryImpl.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/messaging/SnackbarProvider.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/update/InAppUpdateManager.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/MainActivity.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/update/InAppUpdateManagerTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/navigation/HomeGraphViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`

## 5. Architectural Divergences & Discoveries
* **Extension Import Package**: Discovered that `requestAppUpdateInfo` in `com.google.android.play:app-update-ktx:2.1.0` resides in `com.google.android.play.core.ktx`, not `com.google.android.play.core.appupdate.ktx`.
* **JVM Unit Test Compatibility**: Standard JVM testing does not have access to live `PendingIntent` generation; built `TestAppUpdateManager` adhering directly to `AppUpdateManager` interface and reflections on `AppUpdateInfo.zzb` to thoroughly test priority handling and install listeners in fast unit tests.
* **Russian Localization**: Added `values-ru/strings.xml` resources (`update_downloaded_message` and `update_restart_action`) ensuring parity with the application's supported locales.

## 6. Resulting Commits
* `fdd8806` - `[SP-93] feat(app): integrate google play in-app updates api`
