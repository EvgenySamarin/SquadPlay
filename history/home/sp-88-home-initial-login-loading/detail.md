# Detail: Resolve Loading Stall on Initial Login and Adopt withTimeout

* **Target Commit Title**: `fix(home): resolve loading stall on initial login and adopt withTimeout`

## 1. Intent & Architectural Trade-offs
1. **Initial Login Stall**: On first login or fresh installation, `authProvider.signInWithGoogle()` caches the user document locally, but `getUserGroupsFlow(userId)` queries `groups.whereArrayContains("members", userId)`. Because that query lacks local persistence cache, it waits for remote Firestore server communication. Because `combine(getUserInfoFlow, getUserGroupsFlow)` requires both flows to emit, initial emission stalled. Adding `.onStart { emit(emptyList()) }` unblocks downstream emission immediately, rendering the home screen in milliseconds while groups load asynchronously in the background.
2. **Persistent Logout Overlay**: `isLoggingOut.value` was set to `true` upon tapping logout, but was never reset to `false` when sign-out completed. Because Jetpack Navigation 3 retains Koin ViewModels scoped to activity lifecycle, re-logging in left the user facing an input-blocking dark logout overlay. Explicitly resetting `isLoggingOut.value = false` fixes this issue.
3. **Structured Concurrency**: Manual `Job` tracking was replaced with `withTimeout(loadingTimeoutMillis) { uiState.first { it is UiState.Normal } }`.

## 2. Detailed Contract & Schema Specifications
* **ViewModel (`:app`)**:
  ```kotlin
  fun initData() {
      collectUiStateData()
  }
  ```
* **Repository (`:data`)**:
  ```kotlin
  override fun getUserInfoFlow(userId: String): Flow<User?> =
      firestoreDataSource.getUserInfoFlow(userId)
          .combine(
              firestoreDataSource.getUserGroupsFlow(userId)
                  .onStart { emit(emptyList()) }
          ) { user, groups -> ... }
  ```

## 3. Executed Plan
- [x] Step 1: In `ProfileRepositoryImpl.kt`, chain `.onStart { emit(emptyList()) }` to `firestoreDataSource.getUserGroupsFlow(userId)` within `getUserInfoFlow(userId)`, ensuring immediate emission of cached user profile without stalling on remote query synchronization. In `.catch`, rethrow uncaught non-permission errors (`throw it`).
- [x] Step 2: In `HomeScreenViewModel.kt`, replace `timeoutJob` with `withTimeout` awaiting `uiState.first { it is UiState.Normal }`. Ensure `isLoggingOut.value = false` is restored on successful sign-out in `onLogOutTap()` and reset in `collectUiStateData()`. Expose `fun initData()` to restart UI state collection cleanly.
- [x] Step 3: In `ProfileScreenViewModel.kt`, ensure `isLoggingOut.value = false` is restored on successful sign-out in `onLogOutTap()`.
- [x] Step 4: In `SquadPlayNavigation.kt`, invoke `LaunchedEffect(Unit) { viewModel.initData() }` inside `entry<Destination.HomeScreen>`.
- [x] Step 5: Update unit test suites (`LogoutLoadingTest.kt`, `HomeScreenLoadingTimeoutTest.kt`, `RepositoryPermissionDeniedTest.kt`) to verify `isLoggingOut` reset, immediate emission on initial load, and `initData()` re-collection with `withTimeout`.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenLoadingTimeoutTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/RepositoryPermissionDeniedTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `48509d2` - `[SP-88] fix(home): resolve loading stall on initial login and adopt withTimeout`
