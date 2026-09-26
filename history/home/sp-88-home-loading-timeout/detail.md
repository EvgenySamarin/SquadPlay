# Detail: Home Screen Loading Timeout & Connection Error Dialog

* **Target Commit Title**: `feat(home): introduce loading timeout and connection error alert dialog`

## 1. Intent & Architectural Trade-offs
During initial sign-in or under delayed network/attestation conditions, Firestore snapshot listeners in `HomeScreenViewModel` could stall prior to initial emission. Because `uiState` starts in `UiState.Loading`, the user was presented with an indefinite full-screen spinner with no feedback or recovery mechanism.

Introducing a configurable timeout on `HomeScreenViewModel.collectUiStateData()` presents a `ConfirmationDialog` when initial loading exceeds 10 seconds. The user can retry loading (which resets the timeout and triggers a fresh collection) or dismiss the dialog.

## 2. Detailed Contract & Schema Specifications
* **Actions (`:models`)**:
  ```kotlin
  sealed interface HomeScreenAction {
      data object OnRetryLoadingTap : HomeScreenAction
      data object OnDismissTimeoutDialog : HomeScreenAction
  }
  ```
* **Confirmation Dialog (`:app`)**:
  ```kotlin
  @Composable
  fun ConfirmationDialog(
      windowSize: WindowSizeClass,
      title: String? = null,
      text: String? = null,
      confirmButtonText: String = stringResource(R.string.yes),
      dismissButtonText: String = stringResource(R.string.no),
      onConfirmTap: () -> Unit,
      onDismiss: () -> Unit,
  )
  ```
* **ViewModel State (`:app`)**:
  ```kotlin
  val isTimeoutDialogVisible: StateFlow<Boolean>
      field = MutableStateFlow(false)
  ```

## 3. Executed Plan
- [x] Step 1: Add string resources (`retry`, `cancel`, `loading_timeout_title`, `loading_timeout_message`) in `app/src/main/res/values/strings.xml` and `values-ru/strings.xml`.
- [x] Step 2: Add `OnRetryLoadingTap` and `OnDismissTimeoutDialog` to `HomeScreenAction` in `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`.
- [x] Step 3: Update `ConfirmationDialog.kt` to accept optional `confirmButtonText` and `dismissButtonText` with default values `stringResource(R.string.yes)` and `stringResource(R.string.no)`.
- [x] Step 4: In `HomeScreenViewModel.kt`, implement `isTimeoutDialogVisible: StateFlow<Boolean>`, timeout scheduler on `collectUiStateData()` with configurable `loadingTimeoutMillis = DEFAULT_LOADING_TIMEOUT_MS`, timeout cancellation on `UiState.Normal`, and retry/dismiss handlers.
- [x] Step 5: In `HomeScreen.kt`, accept `isTimeoutDialogVisible: Boolean` and display `ConfirmationDialog` with retry and cancel actions.
- [x] Step 6: In `SquadPlayNavigation.kt`, collect `isTimeoutDialogVisible` from `viewModel` and pass it to `HomeScreen`.
- [x] Step 7: Add comprehensive unit tests in `HomeScreenLoadingTimeoutTest.kt` verifying timeout triggering, cancellation on successful data load, retry restart, and dismissal.
- [x] Step 8: Clean up compiler and lint warnings across auth and data layers (`LaunchApplicationViewModel`, `FirebaseAuthManager`, `FirebaseFirestoreDataSource`).

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/ConfirmationDialog.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenLoadingTimeoutTest.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/FirebaseAuthManager.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `509a3d7` - `[SP-88] feat(home): introduce loading timeout and connection error alert dialog`
* `d1f4b89` - `[SP-88] chore(code-style): resolve compiler and lint warnings in auth and data layers`
