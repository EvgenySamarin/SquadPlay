# Detail: Pin Compose Foundation to 1.8.2

* **Target Commit Title**: `fix(design-system): pin compose foundation to 1.8.2 to resolve CustomStyle AbstractMethodError`

## 1. Intent & Architectural Trade-offs
Compose BOM `2026.09.00` resolved `foundation` to `1.12.1` and `material3` to `1.4.0`. In `foundation:1.12.1`, the experimental `androidx.compose.foundation.style.CustomStyle` interface signature was updated. Because `material3:1.4.0` was pre-compiled against `foundation:1.8.x`, `TextFieldDefaults` synthetic lambdas in `material3` threw `AbstractMethodError` at runtime when `applyStyle` was called during composition.
Pinning `foundation = "1.8.2"` aligns Foundation with `material3:1.4.0` without breaking any Material 3 APIs.

## 2. Detailed Contract & Schema Specifications
None.

## 3. Executed Plan
- [x] Step 1: Add `foundation = "1.8.2"` to `[versions]` and update `androidx-compose-foundation` library entry in `gradle/libs.versions.toml`.
- [x] Step 2: Verify build and unit tests pass cleanly.

## 4. Touched Files
* `gradle/libs.versions.toml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/auth/AuthScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `design-system/src/main/kotlin/com/eysamarin/squadplay/designSystem/compose/TextField.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `c902f25` - `[libs-update] fix(design-system): pin compose foundation to 1.8.2 to resolve CustomStyle AbstractMethodError`
