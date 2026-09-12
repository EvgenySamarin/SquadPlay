# Detail: Scope Deep Link Handling to HomeGraph and Clear State on Completion

* **Target Commit Title**: `fix(navigation): scope deep link handling to HomeGraph and clear state on completion`

## 1. Intent & Architectural Trade-offs
Previously, deep link invite handling was tied to `composable<Destination.HomeScreen>` and `HomeScreenViewModel`. This resulted in two defects:
1. When opening an invite link while on `ProfileScreen` (or other non-home screens), the confirmation dialog was rendered inside `HomeScreen` in the background backstack, hiding the prompt from the user until they navigated back.
2. Because `inviteGroupID` was remembered in `HomeScreen`'s navigation entry and stored permanently in `HomeScreenViewModel.inviteGroupIdState`, whenever a user left the group, the reactive user stream emission triggered `HomeScreenViewModel`'s combine flow to re-prompt the user to join the group they had just voluntarily left.

To solve this:
- Introduced `HomeGraphViewModel` scoped to the `Destination.HomeGraph` backstack entry.
- `HomeGraphDeepLinkHandler` renders `ConfirmationDialog` at the `HomeGraph` root level, making it appear over any active child screen.
- The pending invite group ID is immediately consumed from `DeepLinkManager`.
- Active invite state in `HomeGraphViewModel` is completely cleared on confirm or dismiss, preventing any re-triggering upon subsequent user model emissions.
- `HomeScreenViewModel` is fully decoupled from deep links, simplifying its responsibilities.

## 2. Detailed Contract & Schema Specifications
* **Navigation Destinations (`NavigationModels.kt`)**:
  - `data object HomeScreen : Destination`
* **HomeGraph ViewModel (`HomeGraphViewModel.kt`)**:
  - `val confirmInviteDialogState: StateFlow<UiState<String>>`
  - `fun onJoinGroupDialogConfirm()`
  - `fun onJoinGroupDialogDismiss()`
* **Models (`MainScreenModels.kt`)**:
  - `HomeScreenAction`: removed `OnJoinGroupDialogConfirm` and `OnJoinGroupDialogDismiss`.

## 3. Executed Plan
- [x] Step 1: Create `HomeGraphViewModel` in `com.eysamarin.squadplay.navigation`:
  - Observe `deepLinkManager.pendingInviteGroupId`, consume immediately upon retrieval.
  - Verify user is not already in squad and squad exists.
  - Expose `confirmInviteDialogState: StateFlow<UiState<String>>`.
  - Clear state completely on confirm or dismiss, joining squad via `profileProvider.joinGroup(...)` on confirm.
- [x] Step 2: Register `HomeGraphViewModel` in `SquadPlayApplication.kt` via `viewModelOf(::HomeGraphViewModel)`.
- [x] Step 3: Update `Destination.HomeScreen` in `NavigationModels.kt` to `data object HomeScreen : Destination`.
- [x] Step 4: In `SquadPlayNavigation.kt`:
  - Add `HomeGraphDeepLinkHandler` composable that retrieves `HomeGraphViewModel` from the `HomeGraph` back stack entry and renders `ConfirmationDialog` when `confirmInviteDialogState is UiState.Normal`.
  - Remove deep link specification and `remember { entry.arguments... }` from `composable<Destination.HomeScreen>`.
- [x] Step 5: Clean up `HomeScreen.kt`, `HomeScreenViewModel.kt`, and `MainScreenModels.kt` by removing the decoupled invite dialog code.
- [x] Step 6: Add comprehensive unit tests in `HomeGraphViewModelTest.kt` and update test instantiations in `HomeScreenEventNavigationTest.kt`, `LogoutLoadingTest.kt`, and `EventAttendanceTest.kt`.
- [x] Step 7: Verify Kotlin compilation and run tests via `./gradlew compileDebugKotlin` and `./gradlew test`.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/HomeGraphViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/navigation/HomeGraphViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`

## 5. Architectural Divergences & Discoveries
* Using `remember(currentBackStackEntry) { runCatching { navController.getBackStackEntry<Destination.HomeGraph>() }.getOrNull() }` provides a safe, null-checked way to retrieve the `HomeGraph` entry only when `HomeGraph` is actually active in the backstack, naturally isolating `AuthGraph` from unauthenticated dialog prompts.
* Decoupling `DeepLinkManager` from `HomeScreenViewModel` eliminated fragile dependencies in home screen test suites.

## 6. Resulting Commits
* `b8a93dd` - `[SP-89] fix(navigation): scope deep link handling to HomeGraph and clear state on completion`
