# Brief: Display group members and attendance responses on event details screen

* **Commit Title**: `feat(event): display group members and attendance responses on event details screen`
* **Domain**: `event`
* **Parent**: `event/event-accept-reject`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `EventDetailsScreen` displays all group members for the event's associated group (`groupId`) in a scrollable `LazyColumn`.
* Member rows adhere to the design system visual style established on `ProfileScreen`: 48dp squircle avatar (`SquircleShape` with `CornerSmoothing.High`), fallback default avatar when `photoUrl` is null/empty, and user title typography.
* Each member row displays an attendance status icon on the left (start) side using local vector drawables:
  - `ACCEPTED`: `ic_check_circle_24` (Green `Color(0xFF4CAF50)`)
  - `REJECTED`: `ic_cancel_24` (Red `Color(0xFFF44336)`)
  - `NOT_SET`: `ic_help_24` (Gray `Color.Gray`)
* The event creator always evaluates to `ACCEPTED` in accordance with `Event.getStatusForUser(creatorId)`.
* Attendance responses in `EventDetailsScreenViewModel` are reactively observed via `eventProvider.getEventsFlow(setOf(groupId))` combined with `profileProvider.getGroupsMembersInfoFlow(listOf(group))`, with optimistic local updates on user accept/reject actions.
* If a group has no other members or `groupId` cannot be resolved, an empty state message (`no_group_members`) is rendered safely without crash.

## Affected Capabilities & Side Effects
* **Behavior**: Users viewing an event on `EventDetailsScreen` see the full roster of group members and their attendance statuses, updated in real time as responses change.
* **Contract Adjustments**:
  * Added `groupId: String? = null` to `EventUI`.
  * Added `EventMemberUI(uid, username, photoUrl, status)` to `models`.
  * Added `groupId: String = ""` and `members: List<EventMemberUI> = emptyList()` to `EventDetailsScreenUI`.
  * Added `groupId: String = ""` to `Destination.EventDetailsScreen`.
  * Added `event_members_label` string and localized Russian translations for attendance status accessibility content descriptions.
* **Hotspots**: Reactive flow combination in `EventDetailsScreenViewModel` (ensuring stale coroutine jobs are cancelled on re-initialization).
