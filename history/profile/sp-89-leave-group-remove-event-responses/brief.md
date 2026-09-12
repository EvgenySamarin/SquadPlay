# Brief: Remove User Responses from Group Events on Leave Group

* **Commit Title**: `feat(profile): remove user responses from group events on leave group`
* **Domain**: `profile`
* **Parent**: `profile/sp-89-cascade-delete-group-events`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* When a user leaves a group, their UID is removed from the `members` list in `groups/{groupId}` and also purged from the `responses` map across all events belonging to that group in Firestore.
* Targeted map key deletion uses Firestore's `FieldPath.of("responses", userId)` with `FieldValue.delete()`.
* Batch write operations in Firestore are chunked into sets of up to 500 operations to respect Firestore's batch limits.
* Leaving a group preserves FCM topic unsubscription lifecycle (`unsubscribeFromGroupTopic(groupId)`).
* Failure during execution is caught, logged via `AppLogger`, and safely returns `false` without application crashes.

## Affected Capabilities & Side Effects
* **Behavior**: Leaving a group cleans up all user attendance responses across that group's events, ensuring no stale attendance indicators or member references remain.
* **Contract Adjustments**: None.
* **Hotspots**: Multi-document Firestore batch writes across event documents during leave group operations.
