# Brief: Display Nickname Instead of Username on Event Details Screen

* **Commit Title**: `feat(event): display nickname instead of username on event details screen`
* **Domain**: `event`
* **Parent**: `event/event-item-component`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Each member row on `EventDetailsScreen` renders `member.displayName`: if a member has a non-blank `nickname`, it displays the plain nickname without `@`; otherwise it falls back to `username`.
* The `Friend` data class in the `models` module encapsulates `nickname: String? = null` with backward-compatible defaults.
* In `FirebaseFirestoreDataSourceImpl.getGroupsMembersInfoFlow`, `nickname` is parsed from `USERS_COLLECTION` document snapshots and forwarded into `Friend`.
* In `EventDetailsScreenViewModel.loadGroupMembers`, `friend.nickname` is passed into `EventMemberUI`.
* Real-time reactive updates: when any attendee's nickname changes in Firestore, the reactive listener updates the attendee list on `EventDetailsScreen`.

## Affected Capabilities & Side Effects
* **Behavior**: Attendees on `EventDetailsScreen` show their nickname instead of username when set.
* **Contract Adjustments**:
  - `Friend`: Added `val nickname: String? = null`.
  - `EventMemberUI`: Added `val nickname: String? = null` and `val displayName: String get() = nickname?.takeIf { it.isNotBlank() } ?: username`.
* **Hotspots**: Reactive member query listener in `FirebaseFirestoreDataSourceImpl.getGroupsMembersInfoFlow`.
