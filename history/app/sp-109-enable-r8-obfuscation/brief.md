# Brief: Enable R8 Minification and Obfuscation for Release Builds

* **Commit Title**: `[SP-109] feat(app): enable r8 minification and obfuscation for release builds`
* **Domain**: `app`
* **Parent**: `app/sp-93-in-app-updates`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* **Google Play Policy Compliance**: Release builds must enable R8 code shrinking and name obfuscation (`isMinifyEnabled = true`, `isShrinkResources = true`) to satisfy Google Play Console's minimum 25% obfuscation percentage threshold.
* **Symbolication & Crash Reporting Integrity**: Production stack traces must preserve source file names and line numbers (`-keepattributes SourceFile,LineNumberTable` and `-renamesourcefileattribute SourceFile`) so Firebase Crashlytics and Sentry mapping files can de-obfuscate runtime errors.
* **Serialization & DI Model Protection**: Keep rules must protect `kotlinx.serialization` generated serializers, companion objects, data models in `com.eysamarin.squadplay.models.**`, and Firestore document entities in `com.eysamarin.squadplay.data.entity.**` against R8 stripping and mangling.
* **CI/CD Build Resilience**: Sentry ProGuard mapping uploads are gated conditionally on `SENTRY_AUTH_TOKEN` presence to prevent local and CI build failures when credentials are not configured.

## Affected Capabilities & Side Effects
* **Behavior**: Enables R8 minification, code obfuscation, and resource shrinking for release APK and AAB targets while preserving runtime serialization, Firestore entity parsing, and Crashlytics/Sentry crash reporting.
* **Contract Adjustments**: None.
* **Hotspots**: R8 bytecode stripping of reflection-based libraries, Kotlinx Serialization model deserialization in Jetpack Navigation 3, and Firestore entity field mappings.
