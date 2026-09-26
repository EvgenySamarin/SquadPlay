# Brief: Invalid App Check Token Security Lockout Detection

* **Commit Title**: `fix(security): detect invalid app check tokens to trigger security lockout`
* **Domain**: `security`
* **Parent**: `security/sp-88-app-check-security-lockout`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `Throwable.isAppCheckAttestationFailure()` must detect both HTTP 403 attestation failure messages and Firebase SDK token invalidation/rejection errors (`Firebase App Check token is invalid`).
* Standard Firebase error types (such as `FirebaseAuthInvalidCredentialsException`, `FirebaseNetworkException`, and normal `FirebaseException` instances without App Check indicators) must not be falsely identified as attestation failures or trigger security lockout.
* Security lockout continues to be governed by `SecurityLockoutManager.isLockedOut` and rendered at the root activity composition level in `MainActivity`.

## Affected Capabilities & Side Effects
* **Behavior**: When Firebase Auth or Firestore operations fail due to invalid, expired, or rejected App Check tokens (including on unconfigured emulators), the app recognizes the exception as an attestation failure and triggers the non-dismissible `SecurityVerificationErrorScreen` lockout flow instead of failing silently or showing generic auth error messages.
* **Contract Adjustments**: None. Public signature of `Throwable.isAppCheckAttestationFailure(): Boolean` is preserved.
* **Hotspots**: Error mapping extension in `:data` (`AppCheckSecurityExtensions.kt`), Firebase Auth sign-in/up flows (`FirebaseAuthManagerImpl`), and Firestore snapshot listeners.
