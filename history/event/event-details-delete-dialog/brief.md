# Brief: Move event deletion to EventDetailsScreen with confirmation dialog

* **Commit Title**: `feat(event): move event deletion to EventDetailsScreen with confirmation dialog`
* **Domain**: `event`
* **Parent**: `event/event-details-screen`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Event deletion is consolidated inside `EventDetailsScreen` with an explicit `ConfirmationDialog` before triggering `EventProvider.deleteEvent`.
* `ImageTopBar` supports trailing action composables (`actions: @Composable RowScope.() -> Unit`) to host screen-level actions such as deletion under the transparent toolbar.
* `HomeScreen` list items no longer display direct deletion controls, simplifying the list layout.

## Affected Capabilities & Side Effects
* **Behavior**: Users who created an event see a delete trash icon in `EventDetailsScreen`. Tapping it prompts a confirmation dialog, and confirming deletes the event and navigates back to `HomeScreen`.
* **Contract Adjustments**: Updated `Destination.EventDetailsScreen` to carry `eventId` and `isYourEvent`. Added actions and state fields to `EventDetailsScreenModels`. Removed `OnDeleteEventTap` from `HomeScreenAction`.
* **Hotspots**: None.
