# Detail: Debug App Check Provider Factory Setup

* **Target Commit Title**: `feat(security): configure DebugAppCheckProviderFactory for debug builds`

## 1. Intent & Architectural Trade-offs
With Firebase App Check enforced on backend services, local debug builds and emulators fail Google Play Integrity attestation by default, resulting in HTTP 403 or invalid token exceptions that engage the application's security lockout screen.

To enable smooth local testing and development workflows without compromising production security posture, `DebugAppCheckProviderFactory` is introduced for debug builds. On first run, `DebugAppCheckProviderFactory` generates a unique UUID debug secret and logs it to Logcat under the `DebugAppCheckProvider` tag. Once registered in the Firebase Console under **App Check > Manage debug tokens**, Firebase issues valid App Check tokens to the debug build.

In production / release builds, `BuildConfig.DEBUG` evaluates to `false`, ensuring that `PlayIntegrityAppCheckProviderFactory.getInstance()` is unconditionally installed.

## 2. Detailed Contract & Schema Specifications
* **Provider Installation (`SquadPlayApplication.kt`)**:
  ```kotlin
  val appCheckProviderFactory = if (BuildConfig.DEBUG) {
      DebugAppCheckProviderFactory.getInstance()
  } else {
      PlayIntegrityAppCheckProviderFactory.getInstance()
  }
  Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory)
  ```
* **Dependency Definition (`gradle/libs.versions.toml` & `app/build.gradle.kts`)**:
  - `libs.versions.toml`: `com-google-firebase-appcheck-debug = { group = "com.google.firebase", name = "firebase-appcheck-debug" }`
  - `app/build.gradle.kts`: `implementation(libs.com.google.firebase.appcheck.debug)`

## 3. Executed Plan
- [x] Step 1: Add `com-google-firebase-appcheck-debug = { group = "com.google.firebase", name = "firebase-appcheck-debug" }` to `gradle/libs.versions.toml` under `[libraries]`.
- [x] Step 2: Add `implementation(libs.com.google.firebase.appcheck.debug)` to `app/build.gradle.kts` dependencies.
- [x] Step 3: Update `SquadPlayApplication.kt` to install `DebugAppCheckProviderFactory.getInstance()` when `BuildConfig.DEBUG` is true, and fallback to `PlayIntegrityAppCheckProviderFactory.getInstance()` otherwise.
- [x] Step 4: Run `./gradlew testDebugUnitTest` and compile check `./gradlew assembleDebug` to verify compilation and test integrity.
- [x] Step 5: Document step-by-step instructions for retrieving the debug token from Logcat and allowlisting it in the Firebase Console under App Check.

## 4. Touched Files
* `gradle/libs.versions.toml`
* `app/build.gradle.kts`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`

## 5. Architectural Divergences & Discoveries
None. The debug provider library seamlessly integrates with the existing Firebase BOM version (`34.19.0`).

## 6. Resulting Commits
* `78a6329` - `[SP-88] feat(security): configure DebugAppCheckProviderFactory for debug builds`
