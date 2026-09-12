# Brief: Cascade Delete Group Events on Group Deletion

* **Commit Title**: `feat(profile): delete all group events on group deletion`
* **Domain**: `profile`
* **Parent**: `profile/sp-89-group-management`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Deleting a group cascades to all events associated with that group in the Firestore `events` collection, ensuring no orphaned event documents remain.
* Associated event documents are identified both by querying `events` where `groupId == groupId` and by inspecting the `events` ID list in the group document.
* Batch write operations in Firestore must be chunked into sets of at most 500 operations to satisfy Firestore's single-batch write limits.
* Group deletion preserves the FCM topic unsubscription lifecycle (`unsubscribeFromGroupTopic(groupId)`).
* Deletions gracefully handle empty groups or non-existent documents without throwing unhandled exceptions.

## Affected Capabilities & Side Effects
* **Behavior**: When a user deletes a group, all events belonging to that group in Firestore are systematically and atomically deleted alongside the group document.
* **Contract Adjustments**: None.
* **Hotspots**: Firestore batch write volume when deleting groups with a large number of events; FCM topic unsubscription.
