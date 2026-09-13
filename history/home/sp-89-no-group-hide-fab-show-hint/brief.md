# Brief: Hide new event button and show squad hint when user has no groups

* **Commit Title**: `feat(home): hide new event button and show squad hint when user has no groups`
* **Domain**: `home`
* **Parent**: `home/show-all-groups-events-on-home-screen`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `HomeScreenUI`, `isCreateEventButtonVisible: Boolean` controls whether the primary floating action button to create new game events is displayed to the user.
* In `HomeScreenViewModel`, `isCreateEventButtonVisible` evaluates to `true` if and only if the user belongs to at least one group (`userInfo.groups.isNotEmpty()`), an enabled calendar date is selected, and its date is on or after today (`selectedLocalDate >= today`).
* In `HomeScreenViewModel.onAddGameEventTap()`, game event creation is guarded against dates before today, and must also guard against users having no groups (`userInfo.groups.isEmpty()`), returning early without triggering navigation.
* In `HomeScreen`, when `user.groups.isEmpty()`, an explanation banner tailored to a gamer audience is displayed directly below the calendar in both medium and expanded layouts, styled using `DesignSystemTheme.extendedColors.orange` and `DesignSystemTheme.typography.bodyMedium`.

## Affected Capabilities & Side Effects
* **Behavior**: Hides the new game event floating action button and prevents event creation when the user belongs to no squads. Displays a gamer-oriented explanation hint in accent orange under the calendar in both medium and expanded layouts.
* **Contract Adjustments**: Added string resource `no_squad_create_event_hint` to `values/strings.xml` and `values-ru/strings.xml`.
* **Hotspots**: None.
