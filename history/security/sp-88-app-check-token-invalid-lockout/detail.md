# Detail: Invalid App Check Token Security Lockout Detection

* **Target Commit Title**: `fix(security): detect invalid app check tokens to trigger security lockout`

## 1. Intent & Architectural Trade-offs
Firebase Auth throws raw `com.google.firebase.FirebaseException` with detail `[ Firebase App Check token is invalid. ]` when App Check tokens fail backend verification, rather than a dedicated exception class or HTTP 403 status code. The previous detection logic strictly required `"403"` along with `"App Check"`, causing Firebase Auth attestation rejections on unconfigured emulators to bypass the lockout mechanism and surface as generic sign-in failures.

Checking `this is FirebaseException` alone was rejected because `FirebaseException` is the root class for the entire Firebase SDK hierarchy (including `FirebaseAuthInvalidCredentialsException` and `FirebaseNetworkException`), which would cause normal user errors like wrong passwords or network disconnects to falsely lock users out.

The chosen solution combines type checks (`this is FirebaseException && isAppCheckMessage`) with message inspection to reliably detect invalid token rejections across Auth and Firestore while safeguarding standard error workflows.

## 2. Detailed Contract & Schema Specifications
* **Security Extensions (`:data`)**:
  ```kotlin
  fun Throwable.isAppCheckAttestationFailure(): Boolean {
      if (this is AppErrorException && error is AppError.SecurityAttestationFailed) return true

      val msg = message.orEmpty()
      val isAppCheckMessage = msg.contains("App attestation failed", ignoreCase = true) ||
              msg.contains("App Check", ignoreCase = true)

      return when {
          this is FirebaseException && isAppCheckMessage -> true
          msg.contains("403", ignoreCase = true) && isAppCheckMessage -> true
          msg.contains("App attestation failed", ignoreCase = true) -> true
          else -> cause?.isAppCheckAttestationFailure() == true
      }
  }
  ```

## 3. Executed Plan
- [x] Step 1: Update `Throwable.isAppCheckAttestationFailure()` in `data/src/main/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityExtensions.kt` to recognize Firebase Auth and Firestore token rejection messages containing `"Firebase App Check token is invalid"`, `"App Check token is invalid"`, or general App Check token/rejection patterns without strictly requiring `"403"`.
- [x] Step 2: Add unit tests in `data/src/test/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityTest.kt` specifically verifying detection for `"Firebase App Check token is invalid."` (as seen during emulator Google Sign-In failure).
- [x] Step 3: Run `:data:test` to verify all existing and new unit tests pass cleanly.

## 4. Touched Files
* `data/src/main/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityExtensions.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/security/AppCheckSecurityTest.kt`

## 5. Architectural Divergences & Discoveries
Firebase Auth throws the base `com.google.firebase.FirebaseException` without a specific error code for backend App Check rejections, whereas Cloud Firestore throws `FirebaseFirestoreException`. Combining `FirebaseException` type verification with message matching allows reliable detection across both services without false positives.

## 6. Resulting Commits
* `6cde7e9` - `[SP-88] fix(security): detect invalid app check tokens to trigger security lockout`
