# Brief: Reactive Auth User Flow for Profile and Home Data Collection

* **Commit Title**: `feat(auth): expose reactive user id flow to drive profile and home screen data collection`
* **Domain**: `auth`
* **Parent**: `auth/sp-89-firestore-permission-denied-handling`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Authentication state observation is reactive from Firebase Auth through to UI collectors: `FirebaseAuthManager.getCurrentUserIdFlow()` uses `callbackFlow` and `AuthStateListener`, propagated through `AuthRepository` and `AuthProvider`.
* `ProfileProvider.getUserInfoFlow()` dynamically drives data collection via `authRepository.getCurrentUserIdFlow().flatMapLatest { ... }`, emitting cached user data when authenticated or `null` when logged out.
* Imperative data reloading via `LaunchedEffect(Unit) { viewModel.initData() }` in Navigation 3 destinations is removed in favor of reactive lifecycle observation and `rememberViewModelStoreNavEntryDecorator<NavKey>()`.
* `HomeScreenViewModel` encapsulates state reset (`resetStates()`) at the start of collection and handles errors with cancellation protection and user-facing timeout prompts without crashing.
* Public repository and domain layers propagate fatal/unhandled errors (`AppErrorException`) while swallowing transient cancellation cleanly.

## Affected Capabilities & Side Effects
* **Behavior**: Login, logout, and account-switching automatically refresh home and profile screen state reactively without requiring manual re-initialization calls or suffering from stale ViewModel state across destination transitions. Error handling and coroutine cancellation are improved across event and home flows.
* **Contract Adjustments**:
  - Added `fun getCurrentUserIdFlow(): Flow<String?>` to `FirebaseAuthManager`, `AuthRepository`, and `AuthProvider`.
  - Removed `fun initData()` from `HomeScreenViewModel`.
* **Hotspots**: Auth state listener lifecycle, `ProfileProvider.getUserInfoFlow()`, and `HomeScreenViewModel` data collection job.
