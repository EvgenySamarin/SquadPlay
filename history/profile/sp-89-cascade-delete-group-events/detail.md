# Detail: Cascade Delete Group Events on Group Deletion

* **Target Commit Title**: `feat(profile): delete all group events on group deletion`

## 1. Intent & Architectural Trade-offs
Previously in `sp-89-group-management`, deleting a group only deleted the document in the `groups` collection. However, events created under that group remained in the `events` collection.
To eliminate orphaned documents and avoid loading defunct events, `FirebaseFirestoreDataSource.deleteGroup` was updated to perform a cascade delete:
- Queries the `events` collection for documents where `groupId == groupId`.
- Fetches the group document to collect any event IDs stored in its `"events"` array field.
- Collects unique `DocumentReference` instances for all matched events and the group document.
- Deletes all collected references using batched writes chunked by 500 documents (`allRefsToDelete.chunked(500)`).
- Preserves FCM push notification topic unsubscription and error logging.

## 2. Detailed Contract & Schema Specifications
* Public interfaces `ProfileProvider.deleteGroup`, `ProfileRepository.deleteGroup`, and `FirebaseFirestoreDataSource.deleteGroup` retain the signature `suspend fun deleteGroup(groupId: String): Boolean`.
* Firestore Database:
  - `groups/{groupId}`: Document deleted via batch.
  - `events/{eventId}`: All documents matching `groupId == groupId` or listed in `groups/{groupId}.events` deleted via batch write.

## 3. Executed Plan
- [x] Step 1: In `FirebaseFirestoreDataSource.deleteGroup(groupId)`:
  - Query all documents in `events` collection where `groupId == groupId`.
  - Fetch `groups/{groupId}` document to collect any event IDs listed in its `"events"` array.
  - Form a set of all unique document references to delete (event documents + group document).
  - Perform batched deletions in chunks of up to 500 documents using `firebaseFirestore.batch()`.
  - Maintain existing FCM topic unsubscription `unsubscribeFromGroupTopic(groupId)` and exception handling/logging.
- [x] Step 2: Verify project compilation and run unit tests via `./gradlew compileDebugKotlin` and `./gradlew test`.

## 4. Touched Files
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`

## 5. Architectural Divergences & Discoveries
* Both `whereEqualTo("groupId", groupId)` query and the group's `"events"` array field are inspected and merged into a `Set<DocumentReference>` to guarantee complete cleanup even if legacy records have indexing discrepancies.
* Chunked batching (`chunked(500)`) guarantees compliance with Firestore batch limits regardless of the number of events in the group.

## 6. Resulting Commits
* `4bb9d6b` - `[SP-89] feat(profile): delete all group events on group deletion`
