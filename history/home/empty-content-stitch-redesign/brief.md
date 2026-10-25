# Brief: Redesign EmptyContent Widget According to Stitch NoContent Mockup

* **Commit Title**: `[ndk-debug-symbol-level-dsl] feat(home): redesign EmptyContent widget according to Stitch NoContent mockup`
* **Domain**: `home`
* **Parent**: `home/sp-88-home-initial-login-loading`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `EmptyContent` renders a bordered `surfaceContainerLow` card (`DesignSystemTheme.shapes.medium`, `24.dp` inner padding) containing a `64.dp` translucent `surfaceContainerHigh` gamepad icon box backed by a Gaussian transparent blur effect (`BlurEffect` + `Brush.radialGradient`), a bold centered title (`adaptiveTitleByHeight`), and a secondary muted description (`adaptiveLabelByHeight`, `maxWidth = 280.dp`).
* `EmptyContent` accepts customizable `title: String` and `subtitle: String` parameters (defaulting to `R.string.no_events_scheduled_title` and `R.string.no_events_scheduled_subtitle`) so both `HomeScreen` (empty events on selected date) and `ProfileScreen` (empty user groups list) display context-appropriate copy.
* Event creation on `HomeScreen` remains controlled by the `ExtendedFloatingActionButton` (`isCreateEventButtonVisible`, hidden when the user has no groups per `sp-89-no-group-hide-fab-show-hint`).

## Affected Capabilities & Side Effects
* **Behavior**: Replaces the plain icon-and-text empty placeholder in `EmptyContent` with the Stitch `HomeScreen variant NoContent` card widget featuring a translucent Gaussian-blurred gamepad icon box and localized title/subtitle strings for both `HomeScreen` and `ProfileScreen`.
* **Contract Adjustments**: Extended `EmptyContent` with `title: String` and `subtitle: String` parameters; added `ic_sports_esports_24.xml` drawable and `no_events_scheduled_title`, `no_events_scheduled_subtitle`, and `no_groups_found_subtitle` string resources in `values/strings.xml` and `values-ru/strings.xml`.
* **Hotspots**: `EmptyContent.kt`, `HomeScreen.kt`, `ProfileScreen.kt`.
