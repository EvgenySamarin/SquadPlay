# Detail: Redesign EmptyContent Widget According to Stitch NoContent Mockup

* **Target Commit Title**: `[ndk-debug-symbol-level-dsl] feat(home): redesign EmptyContent widget according to Stitch NoContent mockup`

## 1. Intent & Architectural Trade-offs
* **Stitch Design Alignment**: Redesigned `EmptyContent` to match the "No Event" card from the Stitch GameSquad `HomeScreen variant NoContent` screen (`projects/17613910814499941770/screens/899f1c327f4c4f9191b77b29af6120ba`).
* **Gaussian Transparent Blur Icon Container**: Applied a layered `64.dp` icon container using `graphicsLayer` with `BlurEffect(12.dp, 12.dp, TileMode.Decal)` and a radial gradient (`primary.copy(alpha = 0.25f)` to `surfaceContainerHigh.copy(alpha = 0.6f)`) behind the translucent `surfaceContainerHigh.copy(alpha = 0.65f)` box so the `ic_sports_esports_24` icon remains sharp while preserving a soft frosted glow.
* **Cross-Screen Reusability**: Because `EmptyContent` is shared between `HomeScreen` and `ProfileScreen`, `title` and `subtitle` parameters with defaults were added so `HomeScreen` displays the no-events copy while `ProfileScreen` displays the no-groups copy (`R.string.no_groups_found` / `R.string.no_groups_found_subtitle`).

## 2. Detailed Contract & Schema Specifications
* **Composable Signature**:
  - `fun EmptyContent(windowSize: WindowSizeClass, modifier: Modifier = Modifier, title: String = stringResource(R.string.no_events_scheduled_title), subtitle: String = stringResource(R.string.no_events_scheduled_subtitle))`
* **Resources Added**:
  - `app/src/main/res/drawable/ic_sports_esports_24.xml`
  - `no_events_scheduled_title`, `no_events_scheduled_subtitle`, and `no_groups_found_subtitle` in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.

## 3. Executed Plan
- [x] Step 1: Create vector drawable `ic_sports_esports_24.xml` in `app/src/main/res/drawable/`.
- [x] Step 2: Add `no_events_scheduled_title`, `no_events_scheduled_subtitle`, and `no_groups_found_subtitle` localized string resources to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
- [x] Step 3: Update `EmptyContent` in `app/src/main/kotlin/com/eysamarin/squadplay/ui/EmptyContent.kt` to accept customizable `title` and `subtitle` parameters and render the bordered `surfaceContainerLow` card, `64.dp` translucent `surfaceContainerHigh` gamepad icon box with Gaussian blur (`BlurEffect`), bold title, and muted subtitle; pass context-specific title/subtitle from `HomeScreen.kt` and `ProfileScreen.kt`.
- [x] Step 4: Verify compilation, unit tests, and Compose preview rendering (`EmptyContentPreview`).

## 4. Touched Files
* `app/src/main/res/drawable/ic_sports_esports_24.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/EmptyContent.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`

## 5. Architectural Divergences & Discoveries
* Per user refinement during review, the small top-right close badge from the initial Stitch HTML was removed in favor of a Gaussian transparent blur shader behind the translucent gamepad icon container, and `title`/`subtitle` parameters were exposed to differentiate `HomeScreen` and `ProfileScreen` empty states.

## 6. Resulting Commits
* `2963679` - `[ndk-debug-symbol-level-dsl] feat(home): redesign EmptyContent widget according to Stitch NoContent mockup`
