# Detail: Display Nickname Instead of Username on Event Details Screen

* **Target Commit Title**: `feat(event): display nickname instead of username on event details screen`

## 1. Intent & Architectural Trade-offs
With user nicknames introduced into Firestore `users` documents, `EventDetailsScreen` needed to display member nicknames when set, while preserving backward compatibility for users without a nickname.
Key architectural decisions:
1. **Plain Format Selection**: The nickname on the event member row is displayed as plain text (e.g. `ninja` rather than `@ninja`) to visually blend with the typography of the member name slot.
2. **Encapsulated Fallback (`displayName`)**: Added a computed property `val displayName: String get() = nickname?.takeIf { it.isNotBlank() } ?: username` to `EventMemberUI`. This encapsulates fallback logic cleanly in the model layer and keeps UI composables declarative.
3. **End-to-End Pipeline**: Plumbed the field from Firestore snapshot parsing (`getGroupsMembersInfoFlow`) -> `Friend` domain model -> `EventDetailsScreenViewModel` -> `EventMemberUI` -> `EventDetailsScreen`.

## 2. Detailed Contract & Schema Specifications
* **Public APIs / Schemas**:
  - `Friend`: Added `val nickname: String? = null` in `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`.
  - `EventMemberUI`: Added `val nickname: String? = null` and `val displayName: String get() = nickname?.takeIf { it.isNotBlank() } ?: username` in `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`.

## 3. Executed Plan
- [x] Step 1: Add `val nickname: String? = null` to `Friend` data class in `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`.
- [x] Step 2: Add `val nickname: String? = null` and `val displayName: String get() = nickname?.takeIf { it.isNotBlank() } ?: username` to `EventMemberUI` in `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`.
- [x] Step 3: In `FirebaseFirestoreDataSourceImpl.getGroupsMembersInfoFlow`, retrieve `nickname` from `USERS_COLLECTION` snapshot documents and pass it when constructing `Friend` objects.
- [x] Step 4: In `EventDetailsScreenViewModel.loadGroupMembers`, map `friend.nickname` into `EventMemberUI`.
- [x] Step 5: In `EventDetailsScreen.kt`, update `EventMemberRow` to render `member.displayName` instead of `member.username`.
- [x] Step 6: Add unit tests in `EventAttendanceTest.kt` verifying that when a member has a nickname, `EventMemberUI.displayName` resolves to the nickname, and when null/blank, falls back to `username`.
- [x] Step 7: Verify project compilation and run unit tests.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`

## 5. Architectural Divergences & Discoveries
None. Implementation strictly followed the approved plan.

## 6. Resulting Commits
* `a790429` - `[calendar-and-account-optimizations] feat(event): display nickname instead of username on event details screen`
