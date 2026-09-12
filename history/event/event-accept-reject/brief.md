# Brief: Support event accept and reject attendance status

* **Commit Title**: `feat(event): support event accept and reject attendance status`
* **Domain**: `event`
* **Parent**: `event/new-event-group-selector`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Event attendance response tracking uses Firestore map field `responses: Map<String, String>` on `events/{eventId}` documents.
* User response updates must be applied atomically via Firestore dot notation (`responses.$userId`) to prevent overwriting other attendees' responses.
* For creator-owned events (`event.creatorId == currentUserId` / `item.isYourEvent == true`):
  * Status is automatically `ACCEPTED` in `Event.getStatusForUser(userId)`.
  * `HomeScreen` displays the decorative trailing star icon (`R.drawable.ic_star_24`).
  * `EventDetailsScreen` hides Accept and Reject buttons and disables response mutation.
* For non-creator events (`item.isYourEvent == false`):
  * `HomeScreen` displays trailing status badges: `ACCEPTED` (check circle / green), `REJECTED` (cancel / red), `NOT_SET` (help / gray).
  * `EventDetailsScreen` displays Accept and Reject action buttons with toggle behavior (clicking the active response toggles back to `NOT_SET`).
* Material icon drawables are maintained locally as vector drawables (`ic_check_circle_24.xml`, `ic_cancel_24.xml`, `ic_help_24.xml`) without depending on `material-icons-extended`.

## Affected Capabilities & Side Effects
* **Behavior**: Users can accept or reject game events from Event Details, with status badges reflected on the Home screen cards. Event creators are auto-accepted on event creation and own events hide response action buttons.
* **Contract Adjustments**:
  * Added `EventResponseStatus` enum (`ACCEPTED`, `REJECTED`, `NOT_SET`).
  * Added `responses: Map<String, String>` to `Event` domain model and `EventEntity` DTO.
  * Added `userStatus: EventResponseStatus` to `EventUI`, `EventDetailsScreenUI`, and `Destination.EventDetailsScreen`.
  * Added `OnAcceptTap` and `OnRejectTap` to `EventDetailsScreenAction`.
  * Added `updateEventResponse(eventId, userId, status)` to `EventRepository` and `EventProvider`.
  * Added `trailingIconTint: Color? = null` to `DSListItem`.
* **Hotspots**: Firestore document schema evolution (`responses` field missing on legacy documents defaults safely to empty map).
