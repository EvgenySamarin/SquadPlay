# Brief: App Check Attestation Failure Lockout Flow

* **Commit Title**: `feat(security): implement app check attestation failure lockout flow`
* **Domain**: `security`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Application security lockout screen must be rendered at the root host level (`MainActivity`) above `SquadPlayNavigation` to guarantee complete UI lockout without mutating navigation backstack state.
* `BackHandler(enabled = true)` inside `SecurityVerificationErrorScreen` intercepts and consumes all back gesture and hardware button events.
* `SecurityLockoutManager.isLockedOut` encapsulates internal mutability using Kotlin explicit backing fields (`val isLockedOut: StateFlow<Boolean> field = MutableStateFlow(false)`).
* Firestore snapshot listeners, auth operations, and suspend repository methods must not crash the application on Firebase App Check 403 attestation failures, but cleanly trigger the security lockout flow via `SecurityLockoutManager`.
* Public repository and domain provider methods that can encounter domain errors are explicitly annotated with `@Throws(AppErrorException::class)`.
* App Check failures, lockout state transitions, token retries, and errors are logged using `AppLogger` (`logger.w`, `logger.i`, `logger.e`).

## Affected Capabilities & Side Effects
* **Behavior**: When Firebase App Check attestation fails (HTTP 403 "App attestation failed"), the application intercepts the failure, activates lockout state, and displays a full-screen non-dismissible `SecurityVerificationErrorScreen` with options to retry attestation, launch the Google Play Store, or exit the application.
* **Contract Adjustments**:
  - Added `AppError` and `AppErrorException` in `:models`.
  - Added `SecurityLockoutManager` contract in `:contract` and implementation `SecurityLockoutManagerImpl` in `:data`.
  - Added `@Throws(AppErrorException::class)` to `AuthRepository`, `AuthProvider`, `ProfileRepository`, and `EventRepository` methods.
  - Added `Throwable.isAppCheckAttestationFailure()` and `Throwable.toAppError()` extensions in `:data`.
* **Hotspots**: Root activity composition overlay in `MainActivity`, Firebase App Check attestation token retrieval, and Firestore snapshot listeners.
