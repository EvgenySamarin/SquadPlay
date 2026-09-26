# Brief: ProfileProviderTest Flow Replay and Scheduler Fix

* **Commit Title**: `fix(auth): enable replay and advance scheduler in ProfileProviderTest`
* **Domain**: `auth`
* **Parent**: `auth/sp-88-logout-loading-test-dispatcher`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `ProfileProvider.getUserInfoFlow()` dynamically observes authentication transitions via `authRepository.getCurrentUserIdFlow().flatMapLatest { ... }`.
* In production, `FirebaseAuthManagerImpl.getCurrentUserIdFlow()` uses an `AuthStateListener` that immediately provides the current authenticated user on registration.
* In unit testing, `FakeAuthRepository` must configure `MutableSharedFlow(replay = 1)` to model this behavior and prevent initial emission drops when collectors subscribe asynchronously.
* Unit test execution must advance the test scheduler (`testScheduler.runCurrent()`) after launching flow collection before subsequent emissions are triggered.

## Affected Capabilities & Side Effects
* **Behavior**: Fixes `AssertionError` in `ProfileProviderTest` where initial user emission was dropped due to missing replay and premature emission before collector subscription.
* **Contract Adjustments**: None. Test-only fix.
* **Hotspots**: Flow collection and coroutine synchronization in `ProfileProviderTest.kt`.
