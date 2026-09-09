# Brief: Collapse floating action button on scroll

* **Commit Title**: `fix(home): collapse floating action button on scroll`
* **Domain**: `home`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `HomeScreen`, `ExtendedFloatingActionButton` tracks list scroll state via `LazyListState.isScrollInProgress` and smoothly collapses into a small action button showing only the icon when scrolling (`expanded = !isScrolling`).
* Lists across all window size classes (`HomeScreenMediumLayout` and `MainScreenExpandedLayout`) apply `contentPadding = PaddingValues(bottom = 80.dp)` to prevent the floating action button from overlapping the last item when stopped at the bottom.

## Affected Capabilities & Side Effects
* **Behavior**: The FAB on the home screen automatically collapses to a compact icon button while the user is scrolling and re-expands when stationary. Bottom content padding allows the last list item to be fully visible.
* **Contract Adjustments**: None.
* **Hotspots**: None.
