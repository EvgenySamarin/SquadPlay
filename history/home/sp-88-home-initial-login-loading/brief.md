# Brief: Resolve Loading Stall on Initial Login and Adopt withTimeout

* **Commit Title**: `fix(home): resolve loading stall on initial login and adopt withTimeout`
* **Domain**: `home`
* **Parent**: `home/sp-88-home-loading-timeout`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `ProfileRepositoryImpl.getUserInfoFlow(userId)`, `getUserGroupsFlow(userId)` must emit an initial empty list via `.onStart { emit(emptyList()) }` to unblock `combine` on initial login when only user profile data is cached locally.
* Structured concurrency using `withTimeout` handles loading timeout observation cleanly without leaking orphaned `Job` instances.
* Sign-out completion must reset `isLoggingOut.value = false` across `HomeScreenViewModel` and `ProfileScreenViewModel` to prevent persistent logout overlay across authentication transitions within the same app session.
* Public `initData()` in `HomeScreenViewModel` enables explicit re-collection and transient flag resetting.

## Affected Capabilities & Side Effects
* **Behavior**: Fixes initial login stall where the home screen was stuck indefinitely or triggered timeout due to remote Firestore group queries waiting for server response. Fixes screen freeze on re-login caused by stale `isLoggingOut == true`.
* **Contract Adjustments**: Added `fun initData()` to `HomeScreenViewModel`.
* **Hotspots**: `ProfileRepositoryImpl.getUserInfoFlow()` combining user profile and groups.
