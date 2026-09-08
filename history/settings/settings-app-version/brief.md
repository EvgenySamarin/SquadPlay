# Brief: Display app version in Settings bottom bar

* **Commit Title**: `feat(settings): display app version in bottom bar`
* **Domain**: `settings`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* The Settings screen (`SettingsScreen.kt`) displays application metadata and licensing information.
* Version information is obtained at runtime via `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE`, driven by `app/build.gradle.kts`.
* Bottom bar elements in `Scaffold` must apply `Modifier.navigationBarsPadding()` to maintain proper spacing across edge-to-edge system navigation styles.
* Text label adheres to design system typography (`DesignSystemTheme.typography.bodyMedium`) and muted color styling (`DesignSystemTheme.colorScheme.onSurfaceVariant`).

## Affected Capabilities & Side Effects
* **Behavior**: Added a centered bottom bar in `SettingsScreen` showing `Version <version name> (<build number>)`.
* **Contract Adjustments**: Added `settings_screen_version` string resource in English and Russian.
* **Hotspots**: None.
