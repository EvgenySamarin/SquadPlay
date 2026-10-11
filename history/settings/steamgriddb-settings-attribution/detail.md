# Detail: Add SteamGridDB Attribution and Explanation Alert Dialog

* **Target Commit Title**: `feat(settings): add SteamGridDB attribution and explanation alert dialog`

## 1. Intent & Architectural Trade-offs
SquadPlay integrates SteamGridDB to fetch high-quality vertical game cover thumbnails when scheduling game events. To honor SteamGridDB attribution guidelines and acknowledge community contributors, an attribution entry was added to `SettingsScreen`.
Rather than duplicating alert dialog implementations for PickTime and SteamGridDB, we refactored the dialog presentation into a `SettingsNoticeDialog` enum state paired with a reusable `AttributionNoticeDialog` composable. This keeps the composable declarative, eliminates boilerplate, and ensures uniform dismiss handling via `DSButton`.

## 2. Detailed Contract & Schema Specifications
* Added string resources `steamgriddb_attribution_title`, `steamgriddb_attribution_subtitle`, and `steamgriddb_attribution_notice` in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
* Refactored `SettingsScreen` dialog handling:
  - Introduced `SettingsNoticeDialog { PickTime, SteamGridDb }`.
  - Introduced private `AttributionNoticeDialog(title: String, notice: String, onDismiss: () -> Unit)`.
  - Added SteamGridDB `ListItem` to `SettingsMediumLayout` with `onSteamGridDbTap` callback.

## 3. Executed Plan
- [x] Step 1: Add SteamGridDB string resources (`steamgriddb_attribution_title`, `steamgriddb_attribution_subtitle`, `steamgriddb_attribution_notice`) to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
- [x] Step 2: Update `SettingsScreen.kt` to:
      1. Define dialog state representation (`SettingsNoticeDialog`) supporting both PickTime and SteamGridDB attribution dialogs.
      2. Add the SteamGridDB `ListItem` to `SettingsMediumLayout` with `ic_license_24` leading icon and title/subtitle.
      3. Render the SteamGridDB explanation alert dialog with `DSButton` close button when clicked.
- [x] Step 3: Verify the screen preview and project compilation via `./gradlew compileDebugKotlin` and `./gradlew testDebugUnitTest`.

## 4. Touched Files
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/settings/SettingsScreen.kt`

## 5. Architectural Divergences & Discoveries
* Unified the existing PickTime dialog and the new SteamGridDB dialog into a single `AttributionNoticeDialog` composable to prevent duplicate alert dialog logic.

## 6. Resulting Commits
* `fbc9142` - `[steamgriddb-game-thumbnails] feat(settings): add SteamGridDB attribution and explanation alert dialog`
