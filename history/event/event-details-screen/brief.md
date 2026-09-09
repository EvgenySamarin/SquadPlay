# Brief: Event Details Screen Overview

* **Commit Title**: `feat(event): introduce EventDetailsScreen for selected event overview`
* **Domain**: `event`
* **Parent**: `event/dual-dial-picker-time-selection`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `EventDetailsScreen` presents selected event metadata (title, date/time, and game thumbnail) accessed from `HomeScreen` via `HomeScreenAction.OnEventTap`.
* Top thumbnail is rendered behind a transparent toolbar with scrim gradient using the reusable `ImageTopBar` composable.
* Status bar light/dark appearance is managed dynamically with `WindowCompat.getInsetsController` and safely restored on disposal.
* Screen navigation uses type-safe Compose Navigation destination `Destination.EventDetailsScreen` with primitive parameters for instant rendering without extra network hops.

## Affected Capabilities & Side Effects
* **Behavior**: Users tapping on any event card in `HomeScreen` navigate to `EventDetailsScreen` to view the game thumbnail, event title, and scheduled date/time.
* **Contract Adjustments**: Added `Destination.EventDetailsScreen`, `HomeScreenAction.OnEventTap`, and extracted common `ImageTopBar` composable.
* **Hotspots**: None.
