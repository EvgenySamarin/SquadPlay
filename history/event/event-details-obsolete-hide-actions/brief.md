# Brief: Hide Accept and Reject Buttons for Obsolete Events on Event Details Screen

* **Commit Title**: `feat(event): hide accept and reject buttons for obsolete events on event details screen`
* **Domain**: `event`
* **Parent**: `event/event-details-member-nickname`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* On `EventDetailsScreen`, attendees can respond to scheduled events via Accept and Reject buttons unless viewing their own created event (`isYourEvent == true`) or if the event is obsolete (`isObsolete == true`).
* An event is considered obsolete when its scheduled end time `toDateTime` is in the past relative to current time (`toDateTime < now`).
* In `EventDetailsScreenViewModel`, all pathways for attendance updates (`updateEventResponse`) are strictly guarded when `isObsolete == true`.
* `EventDetailsScreenUI` and `Destination.EventDetailsScreen` maintain backward compatibility with default `isObsolete = false`.
* Deterministic time evaluation is preserved across test suites via injectable `nowProvider: () -> LocalDateTime` in `HomeScreenViewModel` and `EventDetailsScreenViewModel`.

## Affected Capabilities & Side Effects
* **Behavior**: When an event's scheduled time has passed, the Accept and Reject buttons on `EventDetailsScreen` are hidden, and attendance response actions are disabled.
* **Contract Adjustments**:
  - `Event`: Added `fun isObsolete(now: LocalDateTime): Boolean = toDateTime < now`.
  - `EventDetailsScreenUI`: Added `val isObsolete: Boolean = false`.
  - `Destination.EventDetailsScreen`: Added `val isObsolete: Boolean = false`.
* **Hotspots**: None.
