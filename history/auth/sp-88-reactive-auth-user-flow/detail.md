# Detail: Reactive Auth User Flow for Profile and Home Data Collection

* **Target Commit Title**: `feat(auth): expose reactive user id flow to drive profile and home screen data collection`

## 1. Intent & Architectural Trade-offs
1. **Reactive Auth Lineage**: Previously, `ProfileProviderImpl.getUserInfoFlow()` retrieved user ID synchronously once via `authRepository.getCurrentUserId()`. In Jetpack Navigation 3, backstack transitions retain ViewModel instances unless explicitly scoped. When navigating between Auth and Home graphs, `HomeScreenViewModel` retained stale state unless forced via `LaunchedEffect(Unit) { viewModel.initData() }`. This caused race conditions and violated reactive principles.
2. **Solution**: Transforming `authRepository.getCurrentUserIdFlow()` via `flatMapLatest` reactively connects auth state changes to data collection. When auth transitions from `null -> userId`, data loads immediately; when logging out, it switches cleanly to `flowOf(null)`.
3. **Navigation Lifecycle**: Added `rememberViewModelStoreNavEntryDecorator<NavKey>()` to `SquadPlayNavigation` decorators to ensure proper entry-level lifecycle scoping in Navigation 3.
4. **Resilience & Robustness**: Follow-up refinements resolved code warnings across auth/data layers, improved coroutine cancellation handling in `EventDetailsScreenViewModel`, introduced explicit state resets in `HomeScreenViewModel.resetStates()`, and ensured critical exceptions like `AppErrorException` are rethrown in `EventRepositoryImpl`.

## 2. Detailed Contract & Schema Specifications
* **Auth Contracts (`:contract`, `:domain`, `:data`)**:
  ```kotlin
  interface AuthRepository {
      fun getCurrentUserIdFlow(): Flow<String?>
      ...
  }

  interface AuthProvider {
      fun getCurrentUserIdFlow(): Flow<String?>
      ...
  }

  interface FirebaseAuthManager {
      fun getCurrentUserIdFlow(): Flow<String?>
      ...
  }
  ```
* **Provider Flow Transformation (`:domain`)**:
  ```kotlin
  override fun getUserInfoFlow(): Flow<User?> =
      authRepository.getCurrentUserIdFlow().flatMapLatest { userId ->
          if (userId != null) {
              profileRepository.getUserInfoFlow(userId)
          } else {
              flowOf(null)
          }
      }
  ```

## 3. Executed Plan
- [x] Step 1: Add `getCurrentUserIdFlow(): Flow<String?>` to `FirebaseAuthManager` interface and implement with `callbackFlow` and `AuthStateListener` in `FirebaseAuthManagerImpl`.
- [x] Step 2: Add `getCurrentUserIdFlow(): Flow<String?>` to `AuthRepository` interface and delegate in `AuthRepositoryImpl`.
- [x] Step 3: Add `getCurrentUserIdFlow(): Flow<String?>` to `AuthProvider` interface and delegate in `AuthProviderImpl`.
- [x] Step 4: Update `ProfileProviderImpl.getUserInfoFlow()` to use `authRepository.getCurrentUserIdFlow().flatMapLatest { ... }`.
- [x] Step 5: Clean up `HomeScreenViewModel`: remove public `initData()`, ensure `onRetryLoadingTap()` handles retry, reset state on start of collection, and rely on `init` + reactive flow.
- [x] Step 6: In `SquadPlayNavigation.kt`, remove `LaunchedEffect(Unit) { viewModel.initData() }` and add `rememberViewModelStoreNavEntryDecorator` to `decorators`.
- [x] Step 7: Update unit tests and test doubles, add `ProfileProviderTest.kt`.
- [x] Step 8: Improve error handling and cancellation handling in `EventDetailsScreenViewModel`, `HomeScreenViewModel`, and `EventRepositoryImpl`.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/MainActivity.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModelSecurityTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenLoadingTimeoutTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AuthRepository.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/FirebaseAuthManager.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositorySignOutTest.kt`
* `domain/build.gradle.kts`
* `domain/src/main/java/com/eysamarin/squadplay/domain/auth/AuthProvider.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`
* `domain/src/test/java/com/eysamarin/squadplay/domain/profile/ProfileProviderTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `4be7b00` - `Improve error handling`
* `3fe929d` - `fix code warnings and code style`
* `f93da37` - `[SP-88] feat(auth): expose reactive user id flow to drive profile and home screen data collection`
