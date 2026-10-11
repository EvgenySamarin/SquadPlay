# Brief: Add SteamGridDB Attribution and Explanation Alert Dialog

* **Commit Title**: `[steamgriddb-game-thumbnails] feat(settings): add SteamGridDB attribution and explanation alert dialog`
* **Domain**: `settings`
* **Parent**: `settings/settings-app-version`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `SettingsScreen.kt` provides unified third-party licensing and attribution notices.
* Attribution dialogs are structured via a private `SettingsNoticeDialog` state enum and reusable `AttributionNoticeDialog` composable using Material3 `AlertDialog` and Design System `DSButton` for the dismiss action.
* Third-party service entries in `SettingsMediumLayout` use `ListItem` with `ic_license_24` leading icons and localized titles and subtitles.
* All user-facing strings are strictly localized in English (`values/strings.xml`) and Russian (`values-ru/strings.xml`).

## Affected Capabilities & Side Effects
* **Behavior**: Added a clickable SteamGridDB list item to the Settings screen that opens an explanation alert dialog with attribution information regarding SteamGridDB API usage, community contribution, and website link.
* **Contract Adjustments**: Added string resources `steamgriddb_attribution_title`, `steamgriddb_attribution_subtitle`, and `steamgriddb_attribution_notice` in English and Russian.
* **Hotspots**: None.
