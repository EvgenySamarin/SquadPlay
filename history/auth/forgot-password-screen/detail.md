# Detail: ForgotPasswordScreen and Password Reset Email Flow

* **Target Commit Title**: `[forgot-password-screen] feat(auth): implement ForgotPasswordScreen and password reset email flow`

## 1. Intent & Architectural Trade-offs
1. **Dedicated Screen vs. Bottom Sheet**: A dedicated `ForgotPasswordScreen` destination was chosen over an inline bottom sheet to match the existing `RegistrationScreen` UX pattern (`TopAppBar` back button, centered `LazyColumn` form, `Patterns.EMAIL_ADDRESS` live validation, and `SnackbarHost` error feedback).
2. **Firebase Native Password Reset**: Delegating password reset to `firebaseAuth.sendPasswordResetEmail(email).await()` supports both standard Email/Password users and users who previously authenticated via Google Sign-In (linking the `password` provider to their existing `uid` upon completion of the reset link).
3. **App Check Security Enforcement**: Consistent with other `FirebaseAuthManagerImpl` methods, `resetPassword` inspects caught exceptions via `isAppCheckAttestationFailure()` and invokes `securityLockoutManager?.triggerLockout()` when App Check attestation fails.

## 2. Detailed Contract & Schema Specifications
* **Auth Contracts (`:data`, `:contract`, `:domain`)**:
  ```kotlin
  interface FirebaseAuthManager {
      suspend fun resetPassword(email: String): UiState<Boolean>
      ...
  }

  interface AuthRepository {
      suspend fun resetPassword(email: String): UiState<Boolean>
      ...
  }

  interface AuthProvider {
      suspend fun resetPassword(email: String): UiState<Boolean>
      ...
  }
  ```
* **Navigation & Screen Actions (`:models`, `:app`)**:
  ```kotlin
  sealed interface AuthScreenAction {
      ...
      object OnForgotPasswordTap : AuthScreenAction
  }

  sealed interface ForgotPasswordScreenAction {
      class OnSendResetEmailTap(val email: String) : ForgotPasswordScreenAction
      object OnBackButtonTap : ForgotPasswordScreenAction
  }

  @Serializable
  data object ForgotPasswordScreen : Destination {
      const val SCREEN_NAME = "ForgotPasswordScreen"
      override val screenName: String get() = SCREEN_NAME
  }
  ```
* **Analytics & Strings (`:contract`, `:domain`)**:
  - `AnalyticsEvent.PasswordResetRequested` (`eventName = "password_reset_requested"`)
  - `StringRepository.passwordResetEmailSent` / `StringProvider.passwordResetEmailSent`

## 3. Executed Plan
- [x] Step 1: Add `suspend fun resetPassword(email: String): UiState<Boolean>` to `FirebaseAuthManager` and implement it in `FirebaseAuthManagerImpl` using `firebaseAuth.sendPasswordResetEmail(email).await()`, handling `FirebaseAuthInvalidCredentialsException` and `isAppCheckAttestationFailure()` lockout.
- [x] Step 2: Expose `resetPassword(email: String): UiState<Boolean>` in `AuthRepository`, `AuthRepositoryImpl`, `AuthProvider`, and `AuthProviderImpl`.
- [x] Step 3: Add `PasswordResetRequested` to `AnalyticsEvent` and `passwordResetEmailSent` to `StringRepository`, `StringRepositoryImpl`, `StringProvider`, `StringProviderImpl`, and localized `strings.xml` files (`values` and `values-ru`).
- [x] Step 4: Add `AuthScreenAction.OnForgotPasswordTap` in `AuthScreenModels.kt`, create `ForgotPasswordScreenModels.kt` with `ForgotPasswordScreenAction`, and add `Destination.ForgotPasswordScreen` in `NavigationModels.kt`.
- [x] Step 5: Create `ForgotPasswordScreen.kt` and `ForgotPasswordScreenViewModel.kt` with email format validation (`Patterns.EMAIL_ADDRESS`), reset email submission, snackbar feedback, analytics tracking, and back navigation.
- [x] Step 6: Update `AuthScreen.kt` and `AuthScreenViewModel.kt` to display the `"Forgot password?"` button (`ButtonStyle.Text`) and navigate to `Destination.ForgotPasswordScreen`, and wire `ForgotPasswordScreen` + `ForgotPasswordScreenViewModel` in `SquadPlayNavigation.kt` and `SquadPlayApplication.kt`.
- [x] Step 7: Update test doubles across `:data`, `:domain`, and `:app`, create `ForgotPasswordScreenViewModelTest.kt`, and run unit tests and build verification.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/data/StringRepositoryImpl.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/auth/AuthScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/auth/AuthScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/forgotpassword/ForgotPasswordScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/forgotpassword/ForgotPasswordScreenViewModel.kt`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModelSecurityTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ForgotPasswordScreenViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenLoadingTimeoutTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AuthRepository.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/StringRepository.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/FirebaseAuthManager.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositoryImpl.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositorySignOutTest.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/auth/AuthProvider.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/resource/StringProvider.kt`
* `domain/src/test/java/com/eysamarin/squadplay/domain/profile/ProfileProviderTest.kt`
* `gradle/libs.versions.toml`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/AuthScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ForgotPasswordScreenModels.kt`

## 5. Architectural Divergences & Discoveries
* Updated `agp` (`9.4.1` -> `9.5.0`), `comGoogleAndroidGmsOssLicenses` (`17.5.2` -> `17.5.3`), `sentry` (`6.24.0` -> `6.25.0`), and `ioSentryAndroid` (`8.61.1` -> `8.62.0`) in `gradle/libs.versions.toml`.

## 6. Resulting Commits
* `565fe9e` - `[forgot-password-screen] chore(deps): bump agp to 9.5.0, sentry to 8.62.0/6.25.0, and oss-licenses to 17.5.3`
* `657604d` - `[forgot-password-screen] feat(auth): implement ForgotPasswordScreen and password reset email flow`
