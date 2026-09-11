# Brief: Filter events by year on selected date and optimize EventUI mapping

* **Commit Title**: `fix(home): filter events by year on selected date and optimize EventUI mapping`
* **Domain**: `home`
* **Parent**: `home/home-fab-hide-on-past-date`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Calendar date representations (`Date`) support explicit year tracking via optional `val year: Int? = null`.
* Event filtering for the selected calendar date in `HomeScreenViewModel` must match `year` in addition to `dayOfMonth` and `monthNumber`, preventing events from bleeding across year navigation.
* Event merging in `CalendarUIProvider` matches events against the calendar date year, using grouped pre-indexing for $O(N + M)$ performance.
* In `HomeScreenViewModel`, event-to-UI transformation logic is encapsulated in `getEventsBySelectedDate()` with guard clauses returning `emptyList()` early when preconditions are unmet.

## Affected Capabilities & Side Effects
* **Behavior**: Navigating to previous or next years on the calendar no longer displays events created in other years on the same day and month.
* **Contract Adjustments**: Added optional `year: Int? = null` property to `@Serializable data class Date`.
* **Hotspots**: None.
