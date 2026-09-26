# Detail: App Check Attestation Failure Lockout Flow

* **Target Commit Title**: `feat(security): implement app check attestation failure lockout flow`

## 1. Intent & Architectural Trade-offs
Firebase App Check protects backend resources from abuse and unverified app binaries. In development, on emulators, or on compromised environments, App Check attestation calls can fail with HTTP 403 ("App attestation failed") or rate-limiting. Rather than crashing with unhandled Firestore or FirebaseAuth exceptions, the application requires an explicit security lockout flow that isolates the user from sensitive app features, intercepts back navigation, and provides clear remediation instructions (retry token exchange, navigate to the Play Store to install an authentic build, or terminate the process).

Rendering the lockout UI directly in `MainActivity` above `SquadPlayNavigation` ensures that back navigation within Navigation 3 cannot circumvent the lockout screen without modifying or resetting destination backstack history.

## 2. Detailed Contract & Schema Specifications
* **Models (`:models`)**:
  ```kotlin
  sealed interface AppError {
      data object SecurityAttestationFailed : AppError
  }

  class AppErrorException(
      val error: AppError,
      cause: Throwable? = null,
  ) : Exception(...)
  ```
* **Contract (`:contract`)**:
  ```kotlin
  interface SecurityLockoutManager {
      val isLockedOut: StateFlow<Boolean>
      fun triggerLockout()
      suspend fun retryAttestation(): Result<Unit>
      fun clearLockout()
  }
  ```
* **Security Extensions & Data (`:data`)**:
  ```kotlin
  fun Throwable.isAppCheckAttestationFailure(): Boolean
  fun Throwable.toAppError(): AppError?
  ```
* **Method Annotations**:
  `@Throws(AppErrorException::class)` added to `AuthRepository`, `AuthProvider`, `ProfileRepository`, and `EventRepository` methods.

## 3. Executed Plan
- [x] Step 1: Add `ic_gpp_bad_24.xml` vector drawable asset in `app/src/main/res/drawable/`.
- [x] Step 2: Define `AppError.SecurityAttestationFailed` and `AppErrorException` in `:models`.
- [x] Step 3: Define `SecurityLockoutManager` interface in `:contract`.
- [x] Step 4: Implement `Throwable.isAppCheckAttestationFailure()`, `Throwable.toAppError()`, and `SecurityLockoutManagerImpl` in `:data`.
- [x] Step 5: Wire App Check attestation failure detection into `:data` data source (`FirebaseFirestoreDataSourceImpl`, `FirebaseAuthManagerImpl`) and repository implementations (`AuthRepositoryImpl`, `EventRepositoryImpl`, `ProfileRepositoryImpl`), adding `@Throws` annotations.
- [x] Step 6: Create `SecurityVerificationErrorScreen` composable in `:app` (`com.eysamarin.squadplay.screens.security`) using `painterResource(R.drawable.ic_gpp_bad_24)`, `DSButton` actions, guidance card, and blocking `BackHandler`.
- [x] Step 7: Bind `SecurityLockoutManager` singleton in `SquadPlayApplication.kt` Koin module.
- [x] Step 8: Update `LaunchApplicationViewModel` to observe `SecurityLockoutManager.isLockedOut` and handle retry execution.
- [x] Step 9: Integrate `SecurityVerificationErrorScreen` into `MainActivity` root host to overlay when attestation fails, dispatching Play Store and Exit intents.
- [x] Step 10: Add unit tests for detection logic, `SecurityLockoutManagerImpl`, and `LaunchApplicationViewModel`.

## 4. Touched Files
* `app/build.gradle.kts`
* `app/src/main/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/MainActivity.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/security/SecurityVerificationErrorScreen.kt`
* `app/src/main/res/drawable/ic_gpp_bad_24.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModelSecurityTest.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AuthRepository.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/EventRepository.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/ProfileRepository.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/SecurityLockoutManager.kt`
* `data/build.gradle.kts`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/FirebaseAuthManager.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityExtensions.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/security/SecurityLockoutManagerImpl.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityTest.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/auth/AuthProvider.kt`
* `gradle/libs.versions.toml`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/AppError.kt`

## 5. Architectural Divergences & Discoveries
None. The implementation strictly satisfied all architectural invariants and passed validation.

## 6. Resulting Commits
* `8ffab03` - `[SP-88] feat(security): implement app check attestation failure lockout flow`
