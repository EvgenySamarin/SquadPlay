# Detail: Migrate to Jetpack Navigation 3

* **Target Commit Title**: `feat(navigation): migrate from Navigation 2 to Jetpack Navigation 3`

## 1. Intent & Architectural Trade-offs
Navigation 2 (`navigation-compose`) is obsolete. Migrating to Navigation 3 (`androidx.navigation3`) provides type-safe routes via `NavKey`, simpler state management with `rememberNavBackStack`, and decoupling of UI rendering through `NavDisplay` and `entryProvider`.

## 2. Detailed Contract & Schema Specifications
* `Destination` sealed interface implements `androidx.navigation3.runtime.NavKey`.
* `DefaultNavigator` delegates `Destination` actions directly.
* Obsolete `NavTypes.kt` was removed because Navigation 3 handles serializable `NavKey` objects natively.

## 3. Executed Plan
- [x] Step 1: Add Navigation 3 dependencies in `gradle/libs.versions.toml` & `app/build.gradle.kts`.
- [x] Step 2: Make `Destination` implement `NavKey` and remove `NavTypes.kt`.
- [x] Step 3: Update `Navigator`, `DefaultNavigator`, and unit test `FakeNavigator`.
- [x] Step 4: Refactor `SquadPlayNavigation.kt` to `entryProvider` and `NavDisplay`.
- [x] Step 5: Verify build (`:app:assembleDebug`) and unit tests (`testDebugUnitTest`).

## 4. Touched Files
* `gradle/libs.versions.toml`
* `app/build.gradle.kts`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/Navigator.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavTypes.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `249cf52` - `[libs-update] feat(navigation): migrate from Navigation 2 to Jetpack Navigation 3`
