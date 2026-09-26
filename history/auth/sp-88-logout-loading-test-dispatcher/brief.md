# Brief: Bind HomeScreenViewModel Test Dispatcher in LogoutLoadingTest

* **Commit Title**: `fix(auth): bind HomeScreenViewModel test dispatcher in LogoutLoadingTest`
* **Domain**: `auth`
* **Parent**: `auth/sp-88-reactive-auth-user-flow`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `HomeScreenViewModel` delegates asynchronous UI state flow collection to `ioDispatcher` (defaulting to `Dispatchers.IO`).
* Unit tests instantiating `HomeScreenViewModel` must bind `HomeScreenViewModel.defaultIoDispatcher` to the active `testDispatcher` in `@Before setUp()` and restore `Dispatchers.IO` in `@After tearDown()`.
* ViewModels instantiated in test factory methods (`createHomeScreenViewModel()`) must explicitly assign `vm.ioDispatcher = testDispatcher` to guarantee single-threaded, deterministic virtual time execution.
* Prevents asynchronous background thread continuations from completing after test scope teardown, resolving `IllegalStateException at ContinuationImpl.kt:130`.

## Affected Capabilities & Side Effects
* **Behavior**: `LogoutLoadingTest` runs deterministically without thread races or intermittent coroutine completion handler exceptions under heavy build concurrency.
* **Contract Adjustments**: None. Test-only configuration.
* **Hotspots**: Coroutine dispatching in `LogoutLoadingTest.kt` and `HomeScreenViewModel` UI data collection.
