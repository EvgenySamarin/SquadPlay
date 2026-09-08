# Detail: Display app version in Settings bottom bar

* **Target Commit Title**: `feat(settings): display app version in bottom bar`

## 1. Intent & Architectural Trade-offs
Added build version and version code information to the bottom of the Settings screen (`SettingsScreen`) via `Scaffold`'s `bottomBar` slot. Factored out a modular `SettingsBottomBar` composable with default arguments (`versionName: String = BuildConfig.VERSION_NAME`, `versionCode: Int = BuildConfig.VERSION_CODE`) for testability and preview support. Applied `navigationBarsPadding()` to guarantee edge-to-edge navigation inset clearance, and centered the text using `textAlign = TextAlign.Center`.

## 2. Detailed Contract & Schema Specifications
* Added string resource `settings_screen_version` (`Version %1$s (%2$s)`) to `app/src/main/res/values/strings.xml`.
* Added string resource `settings_screen_version` (`Версия %1$s (%2$s)`) to `app/src/main/res/values-ru/strings.xml`.
* Updated `SettingsScreen` to populate the `bottomBar` parameter of its `Scaffold` with `SettingsBottomBar`.

## 3. Executed Plan
- [x] Step 1: Add `settings_screen_version` string resource in `values/strings.xml` and `values-ru/strings.xml`.
- [x] Step 2: Implement `SettingsBottomBar` composable in `SettingsScreen.kt` displaying `Version <version name> (<build number>)` with `textAlign = TextAlign.Center`, `navigationBarsPadding()`, and design system typography/color.
- [x] Step 3: Wire `SettingsBottomBar` into `Scaffold(bottomBar = { SettingsBottomBar() })` in `SettingsScreen`.
- [x] Step 4: Run `./gradlew compileDebugKotlin` and verify project builds successfully with the updated preview and composable.

## 4. Touched Files
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/settings/SettingsScreen.kt`

## 5. Architectural Divergences & Discoveries
* Formatted the text label strictly as `Version <version name> (<build number>)` centered via `TextAlign.Center` per user clarification.

## 6. Resulting Commits
* `cc1b2d7` - `[settings-app-version] feat(settings): display app version in bottom bar`
