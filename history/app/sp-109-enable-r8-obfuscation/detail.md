# Detail: Enable R8 Minification and Obfuscation for Release Builds

* **Target Commit Title**: `[SP-109] feat(app): enable r8 minification and obfuscation for release builds`

## 1. Intent & Architectural Trade-offs
Google Play Console flagged SquadPlay with an obfuscation warning:
*"Percentages under 25% in any category of your app may impact your visibility and publishing capabilities on Google Play. Obfuscation (2%)."*

Investigation revealed that `buildTypes.release` in `app/build.gradle.kts` had `isMinifyEnabled = false`, completely disabling R8 code shrinking and obfuscation. As a result, 98% of the application bytecode remained unobfuscated, posing security risks and violating Google Play quality thresholds.

Key architectural decisions implemented:
1. **Enable R8 Minification and Resource Shrinking**: Enabled `isMinifyEnabled = true` and `isShrinkResources = true` in `app/build.gradle.kts` for the `release` build type to optimize bytecode, strip unused code, and obfuscate symbols.
2. **Crash Reporting De-obfuscation Preservation**: Added `-keepattributes SourceFile,LineNumberTable` and `-renamesourcefileattribute SourceFile` to `app/proguard-rules.pro` to retain accurate line numbers in mapping files uploaded to Firebase Crashlytics and Sentry.
3. **Serialization & Route Model Integrity**: Configured keep rules for `kotlinx.serialization` (`*Companion*`, `*$serializer`, and `@Serializable` members) as well as the `com.eysamarin.squadplay.models.**` package to ensure Jetpack Navigation 3 backstack arguments and RAWG API models serialize and deserialize without runtime reflection or NoSuchMethod errors.
4. **Firestore Entity Mapping Safety**: Maintained `-keep class com.eysamarin.squadplay.data.entity.** { *; }` to protect Firestore document mapping fields (e.g. `EventEntity`) from property renaming.
5. **Conditional Sentry Mapping Upload**: Configured the Sentry Gradle plugin's `autoUploadProguardMapping` to execute conditionally based on `SENTRY_AUTH_TOKEN` presence so that local and non-secret CI build steps do not fail with `sentry-cli` authentication errors.

## 2. Detailed Contract & Schema Specifications
* `app/build.gradle.kts` release build type configuration:
  ```kotlin
  release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
      ndk {
          debugSymbolLevel = SYMBOL_TABLE.toString()
      }
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
  }
  ```
* Sentry Gradle plugin mapping configuration:
  ```kotlin
  sentry {
      includeProguardMapping.set(true)
      autoUploadProguardMapping.set(
          System.getenv("SENTRY_AUTH_TOKEN") != null || properties.getProperty("SENTRY_AUTH_TOKEN") != null
      )
  }
  ```

## 3. Executed Plan
- [x] Step 1: Enable `isMinifyEnabled = true` and `isShrinkResources = true` under `buildTypes.release` in `app/build.gradle.kts`.
- [x] Step 2: Configure ProGuard / R8 keep rules in `app/proguard-rules.pro` for line number preservation, Kotlinx Serialization, Firestore entities, and third-party SDK compatibility.
- [x] Step 3: Run `./gradlew test` to ensure existing unit tests pass.
- [x] Step 4: Run `./gradlew app:assembleRelease` to verify that R8 shrinking, obfuscation, resource shrinking, and dex compilation complete without errors.

## 4. Touched Files
* `app/build.gradle.kts`
* `app/proguard-rules.pro`

## 5. Architectural Divergences & Discoveries
* **Sentry Auto-Upload Authentication**: Enabling R8 triggered Sentry Gradle plugin's default behavior of attempting to upload ProGuard mapping files via `uploadSentryProguardMappingsRelease`, failing builds when `SENTRY_AUTH_TOKEN` was absent. Fixed by configuring `autoUploadProguardMapping.set(...)` conditionally while keeping `includeProguardMapping.set(true)` enabled.
* **Top-Level Properties Scoping**: Moved `local.properties` loading to the top-level script scope in `app/build.gradle.kts` so both `android { ... }` and `sentry { ... }` blocks can safely access local environment properties without deprecation or scoping collisions.

## 6. Resulting Commits
* `e9dcd84` - `[SP-109] feat(app): enable r8 minification and obfuscation for release builds`
