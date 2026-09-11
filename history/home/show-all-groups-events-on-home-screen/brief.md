# Brief: Display events from all user groups with group overline

* **Commit Title**: `feat(home): display events from all user groups with group overline`
* **Domain**: `home`
* **Parent**: `home/home-event-filter-by-year`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `HomeScreenViewModel`, user squad identifiers are collected as an order-independent set (`Set<String>`) with `distinctUntilChanged()`, ensuring membership changes trigger re-subscription without redundant queries upon reordering.
* `EventProvider.getEventsFlow`, `EventRepository.getEventsFlow`, and `FirebaseFirestoreDataSource.getEventsFlow` operate on `groupIds: Set<String>`.
* If `groupIds` is empty, data sources must immediately return `flowOf(emptyList())` to avoid Firestore `whereIn` empty collection errors.
* For sets exceeding 30 groups, Firestore listeners must chunk queries into batches of at most 30 and merge results using `combine`.
* In `HomeScreenUI`, `EventUI.groupTitle` maps each event's `groupId` to its human-readable title from the user's groups, rendered via `DSListItem(overline = item.groupTitle)` in both medium and expanded layouts.

## Affected Capabilities & Side Effects
* **Behavior**: Users belonging to multiple squads now see events from all of their squads on the home screen calendar and events list, with each item's squad title shown as an overline.
* **Contract Adjustments**: Added `val groupTitle: String? = null` to `EventUI`. Updated `EventRepository`, `EventProvider`, and `FirebaseFirestoreDataSource` `getEventsFlow` signature to `Set<String>` and dropped legacy single `groupId` method.
* **Hotspots**: None.
