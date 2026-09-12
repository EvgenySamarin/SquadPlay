# Detail: Remove User Responses from Group Events on Leave Group

* **Target Commit Title**: `feat(profile): remove user responses from group events on leave group`

## 1. Intent & Architectural Trade-offs
When a member left a group previously, their UID was removed only from `groups/{groupId}.members`. Consequently, any event attendance responses previously submitted by that member remained in the event documents' `responses` map in Firestore.
To ensure clean state and avoid stale attendance data:
- `FirebaseFirestoreDataSource.leaveGroup` was updated to query all events in the `events` collection where `groupId == groupId`.
- Event documents where `responses` contains `userId` are identified.
- Updates are batched: the user UID is removed from `groups/{groupId}.members`, and `FieldPath.of("responses", userId)` is deleted via `FieldValue.delete()` on each affected event document.
- Operations are chunked by 500 (`batchOperations.chunked(500)`) to maintain safety against Firestore batch limits.

## 2. Detailed Contract & Schema Specifications
* Public interfaces `ProfileProvider.leaveGroup`, `ProfileRepository.leaveGroup`, and `FirebaseFirestoreDataSource.leaveGroup` retain the signature `suspend fun leaveGroup(userId: String, groupId: String): Boolean`.
* Firestore Database:
  - `groups/{groupId}`: `members` updated to remove `userId`.
  - `events/{eventId}`: `responses[userId]` deleted using `FieldValue.delete()` for all events belonging to `groupId`.

## 3. Executed Plan
- [x] Step 1: In `FirebaseFirestoreDataSource.leaveGroup(userId, groupId)`:
  - Query all documents in `events` collection where `groupId == groupId`.
  - Identify event documents where `responses` contains `userId`.
  - Fetch `groups/{groupId}` document and remove `userId` from `members`.
  - Execute batched updates (chunked by 500) applying `FieldValue.delete()` to `FieldPath.of("responses", userId)` on affected event documents and updating the group `members` list.
  - Maintain `unsubscribeFromGroupTopic(groupId)` and exception handling/logging.
- [x] Step 2: Verify project compilation and run unit tests via `./gradlew compileDebugKotlin` and `./gradlew test`.

## 4. Touched Files
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`

## 5. Architectural Divergences & Discoveries
* Using `FieldPath.of("responses", userId)` prevents any issues if user IDs contain special characters and specifically targets only the nested map entry without rewriting the entire map.
* Batch operations are encapsulated into executable closures chunked by 500 to cleanly execute both the group document update and arbitrary numbers of event document updates within Firestore constraints.

## 6. Resulting Commits
* `1da49ae` - `[SP-89] feat(profile): remove user responses from group events on leave group`
