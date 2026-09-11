# Brief: Allow selecting group when creating new event

* **Commit Title**: `feat(event): allow selecting group when creating new event`
* **Domain**: `event`
* **Parent**: `event/event-details-delete-dialog`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Event creation and scheduling flow requires `creatorId`, `groupId`, `title`, `fromDateTime`, and `toDateTime` in `Event` model.
* `NewEventScreenViewModel` collects `User` from `ProfileProvider.getUserInfoFlow()` and exposes user groups (`List<Group>`) to `NewEventScreenUI`.
* When saving an event, `groupId` is supplied explicitly from the selected group in the UI or falls back to the user's first squad if blank, with guard handling showing a snackbar if the user has no squads.
* RAWG game search remains debounced using coroutines (500ms delay).
* Event time selection continues to support multi-day rolls when "To" time precedes "From" time.

## Affected Capabilities & Side Effects
* **Behavior**: Users with multiple squads can choose which group an event is scheduled for using an `ExposedDropdownMenuBox` selector before saving.
* **Contract Adjustments**: Added `groupId: String` to `NewEventScreenAction.OnEventSaveTap` and `userGroups: List<Group> = emptyList()` to `NewEventScreenUI`.
* **Hotspots**: None.
