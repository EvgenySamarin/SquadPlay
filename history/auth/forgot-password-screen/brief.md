# Brief: ForgotPasswordScreen and Password Reset Email Flow

* **Commit Title**: `[forgot-password-screen] feat(auth): implement ForgotPasswordScreen and password reset email flow`
* **Domain**: `auth`
* **Parent**: `auth/sp-88-profile-provider-test-fix`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `FirebaseAuthManagerImpl.resetPassword(email)` wraps `firebaseAuth.sendPasswordResetEmail(email).await()`, catches `FirebaseAuthInvalidCredentialsException` for invalid email formats, and inspects generic exceptions via `exception.isAppCheckAttestationFailure()` to trigger `securityLockoutManager?.triggerLockout()` on attestation failure.
* `Destination.ForgotPasswordScreen` implements `androidx.navigation3.runtime.NavKey`, declares `SCREEN_NAME = "ForgotPasswordScreen"` for screen analytics tracking, and is registered in `entryProvider` inside `SquadPlayNavigation.kt`.
* Password reset strings are localized across both English (`values/strings.xml`) and Russian (`values-ru/strings.xml`), and exposed via `StringRepository` and `StringProvider` with default interface values for test compatibility.

## Affected Capabilities & Side Effects
* **Behavior**: Adds a "Forgot password?" text button on `AuthScreen` that navigates to `ForgotPasswordScreen`, allowing users to enter their email address, request a Firebase password reset link, receive snackbar confirmation, and return to `AuthScreen`.
* **Contract Adjustments**:
  - Added `suspend fun resetPassword(email: String): UiState<Boolean>` to `FirebaseAuthManager`, `AuthRepository`, and `AuthProvider`.
  - Added `val passwordResetEmailSent: String` to `StringRepository` and `StringProvider`.
  - Added `AnalyticsEvent.PasswordResetRequested` (`password_reset_requested`).
  - Added `AuthScreenAction.OnForgotPasswordTap`, `ForgotPasswordScreenAction`, and `Destination.ForgotPasswordScreen`.
* **Hotspots**: `FirebaseAuthManagerImpl` error handling, `AuthProvider` test doubles across `:app`, `:domain`, and `:data` test suites, and `SquadPlayNavigation` entry wiring.
