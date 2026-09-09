# Brief: Hide new game event button when past date is selected

* **Commit Title**: `feat(home): hide new game event button when past date is selected`
* **Domain**: `home`
* **Parent**: `home/home-event-user-star-indicator`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `HomeScreenUI`, `isCreateEventButtonVisible: Boolean` controls whether the primary action button to create new game events is presented to the user.
* In `HomeScreenViewModel`, `isCreateEventButtonVisible` evaluates to `true` if and only if an enabled calendar date is selected and its calendar date is on or after the current local date (`selectedLocalDate >= today`).
* In `HomeScreenViewModel.onAddGameEventTap()`, game event creation is guarded against dates before today, returning early if a past date is selected.
* In `HomeScreen`, `ExtendedFloatingActionButton` is wrapped in `AnimatedVisibility` conditioned on `isCreateEventButtonVisible`, gracefully animating appearance and disappearance while preserving scroll collapse behavior (`expanded = !isScrolling`).

## Affected Capabilities & Side Effects
* **Behavior**: When selecting a date prior to today on the calendar, the "New Game Event" button on the home screen smoothly disappears, preventing users from creating events in the past. Selecting today or a future date makes the button visible.
* **Contract Adjustments**: Added `isCreateEventButtonVisible: Boolean = true` to `HomeScreenUI`.
* **Hotspots**: None.
