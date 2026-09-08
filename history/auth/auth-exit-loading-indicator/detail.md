# Detail: Show LoadingIndicator on exit until navigation to AuthScreen

* **Target Commit Title**: `feat(auth): show loading indicator on exit until navigation to auth screen`

## 1. Intent & Architectural Trade-offs
When user taps the exit action button on `HomeScreen` or `ProfileScreen`, asynchronous sign-out operations (`authProvider.signOut()`) must execute before navigating back to the authentication screen (`AuthScreen`). Previously, no loading state was displayed and buttons remained clickable, opening possibilities for duplicate requests or unclear responsiveness.
We added `isLoggingOut: StateFlow<Boolean>` to `HomeScreenViewModel` and `ProfileScreenViewModel`. When active, an input-blocking semi-transparent overlay renders Material 3's expressive `LoadingIndicator` (under `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`). In addition, `HomeScreen` and `ProfileScreen` display `LoadingIndicator` when in `UiState.Loading`.

## 2. Detailed Contract & Schema Specifications
* `HomeScreenViewModel`: Added `val isLoggingOut: StateFlow<Boolean>` with backing field, set to `true` during `onLogOutTap()` and reverted to `false` if `signOut()` returns `false`.
* `ProfileScreenViewModel`: Added `val isLoggingOut: StateFlow<Boolean>` with backing field, set to `true` during `onLogOutTap()` and reverted to `false` if `signOut()` returns `false`.
* `HomeScreen`: Added `isLoggingOut: Boolean = false` parameter, disabled exit `IconButton` while `isLoggingOut` is true, rendered centered `LoadingIndicator` during `UiState.Loading` and full-screen blocking overlay when `isLoggingOut` is true.
* `ProfileScreen`: Added `isLoggingOut: Boolean = false` parameter, disabled exit `IconButton` while `isLoggingOut` is true, rendered centered `LoadingIndicator` during `UiState.Loading` and full-screen blocking overlay when `isLoggingOut` is true.
* `SquadPlayNavigation`: Collected `isLoggingOut` from view models and supplied to `HomeScreen` and `ProfileScreen`.

## 3. Executed Plan
- [x] Step 1: Update `HomeScreenViewModel` and `ProfileScreenViewModel` to maintain and expose `isLoggingOut: StateFlow<Boolean>`, setting it to `true` during `onLogOutTap()` before `authProvider.signOut()` and reverting to `false` if sign-out fails.
- [x] Step 2: Update `HomeScreen.kt` and `ProfileScreen.kt` with `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` to render `LoadingIndicator()` centered with an input-blocking scrim when `isLoggingOut` is `true`, and when initial screen `state is UiState.Loading`.
- [x] Step 3: Wire `isLoggingOut` state collection in `SquadPlayNavigation.kt` for `HomeScreen` and `ProfileScreen`.
- [x] Step 4: Add unit tests verifying `isLoggingOut` state transitions during exit/logout.
- [x] Step 5: Verify build, tests, and linting (`./gradlew testDebugUnitTest compileDebugKotlin assembleDebug`).

## 4. Touched Files
* `app/build.gradle.kts`
* `gradle/libs.versions.toml`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`

## 5. Architectural Divergences & Discoveries
* Added `kotlinx-coroutines-test` dependency and configured `testOptions.unitTests.isReturnDefaultValues = true` to allow JVM unit testing of ViewModel coroutine flows without Android Log runtime mock errors.

## 6. Resulting Commits
* `e19280c` - `[settings-app-version] feat(auth): show loading indicator on exit until navigation to auth screen`
