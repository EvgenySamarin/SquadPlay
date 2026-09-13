# Brief: Implement EventItem composable component

* **Commit Title**: `feat(event): implement EventItem composable component`
* **Domain**: `event`
* **Parent**: `event/event-details-group-members`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `EventItem` renders an event item card with a rounded surface container (`SquircleShape`), subtle green border glow, game cover art, title, time schedule, attendance status badge, participant avatar list with overlaid response status badges, and a details action button.
* Reusable across event lists, including `HomeScreen`.
* Supports `EventUI` models and optional member lists (`List<EventMemberUI>`), with an overflow pill for participant lists exceeding `maxVisibleAvatars`.

## Affected Capabilities & Side Effects
* **Behavior**: Event items on `HomeScreen` and across event lists render with rich event metadata, status checkmarks, participant avatars, and an interactive "VIEW DETAILS" button.
* **Contract Adjustments**:
  * Created `EventItem` composable in `app/src/main/kotlin/com/eysamarin/squadplay/ui/EventItem.kt`.
  * Added `view_details` string resource to English and Russian localization.
* **Hotspots**: None.
