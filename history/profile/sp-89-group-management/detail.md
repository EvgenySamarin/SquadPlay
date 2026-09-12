# Detail: Manage User Groups on Profile Screen

* **Target Commit Title**: `feat(profile): allow managing user groups with edit, delete, and leave actions`

## 1. Intent & Architectural Trade-offs
To enable users to manage their squads directly from the Profile screen, group documents in Firestore now persist an `ownerId` matching the creator's UID. Permission handling cleanly partitions capabilities based on group ownership:
- Group owners (and any member of legacy groups without `ownerId`) have administrative control to rename and delete groups.
- Regular members can leave groups by removing their UID from the group's `members` array.
- Bottom sheet presentation states are managed locally in Compose rather than inflating `ProfileScreenUI` with transient dialog pointers, maintaining clean unidirectional data flow while routing action confirmations and analytics through `ProfileScreenViewModel`.
- Accidental taps are guarded with confirmation modal bottom sheets for delete and leave operations, and title editing enforces the project-wide single-word title constraint.

## 2. Detailed Contract & Schema Specifications
* **Models (`models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`)**:
  - `data class Group(val uid: String, val title: String, val members: List<String>, val ownerId: String? = null)`
  - `data class UserGroupSection(val groupId: String, val title: String, val members: List<Friend>, val ownerId: String? = null)`
* **Profile Screen Models (`models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`)**:
  - `ProfileScreenAction.OnConfirmEditGroup(val groupId: String, val newTitle: String)`
  - `ProfileScreenAction.OnConfirmDeleteGroup(val groupId: String)`
  - `ProfileScreenAction.OnConfirmLeaveGroup(val groupId: String)`
* **Repository, Data Source & Provider Interfaces**:
  - `ProfileRepository`, `FirebaseFirestoreDataSource`, `ProfileProvider`:
    - `suspend fun renameGroup(groupId: String, newTitle: String): Boolean`
    - `suspend fun deleteGroup(groupId: String): Boolean`
    - `suspend fun leaveGroup(userId: String, groupId: String): Boolean`
* **Analytics Contract (`contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`)**:
  - `EditGroupClicked(val groupId: String)` -> `"edit_group_clicked"`
  - `GroupRenamed(val groupId: String)` -> `"group_renamed"`
  - `DeleteGroupClicked(val groupId: String)` -> `"delete_group_clicked"`
  - `GroupDeleted(val groupId: String)` -> `"group_deleted"`
  - `LeaveGroupClicked(val groupId: String)` -> `"leave_group_clicked"`
  - `GroupLeft(val groupId: String)` -> `"group_left"`

## 3. Executed Plan
- [x] Step 1: In `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`, add `val ownerId: String? = null` to `Group` and `UserGroupSection`.
- [x] Step 2: In `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`: add actions for confirming edit, delete, and leave group operations; update previews with sample group owner IDs.
- [x] Step 3: In `contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`, add `EditGroupClicked`, `GroupRenamed`, `DeleteGroupClicked`, `GroupDeleted`, `LeaveGroupClicked`, and `GroupLeft`.
- [x] Step 4: In `contract/src/main/java/com/eysamarin/squadplay/contracts/ProfileRepository.kt` and `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`, declare and implement `renameGroup`, `deleteGroup`, and `leaveGroup`.
- [x] Step 5: In `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`: write `ownerId` on group creation, read `ownerId` when querying groups in `getUserGroupsFlow` and `getGroupInfo`, propagate `ownerId` in `getGroupsMembersInfoFlow` into `UserGroupSection`, implement `renameGroup`, `deleteGroup`, and `leaveGroup`.
- [x] Step 6: In `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`, implement repository methods delegating to datasource and handling group topic unsubscriptions.
- [x] Step 7: In `app/src/main/res/values/strings.xml` and `values-ru/strings.xml`, add string resources for edit group bottom sheet, delete confirmation, leave confirmation, and content descriptions.
- [x] Step 8: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`: implement action handling for `OnEditGroupTap`, `OnDeleteGroupTap`, and `OnLeaveGroupTap`; implement action handling for confirming edit, delete, and leave operations with logging and analytics tracking.
- [x] Step 9: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`: enforce `GroupHeader` button visibility rules (owner vs non-owner vs legacy), implement `EditGroupBottomSheet` and `ConfirmationBottomSheet`.
- [x] Step 10: In `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`: add comprehensive unit tests for edit, delete, and leave flows; update test fakes across the test suite.
- [x] Step 11: Build and test verification using `./gradlew test` and `./gradlew compileDebugKotlin`.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/ProfileRepository.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`

## 5. Architectural Divergences & Discoveries
* Managed confirmation bottom sheets (`EditGroupBottomSheet` and `ConfirmationBottomSheet`) within Compose local state in `ProfileScreen.kt` rather than hoisting pointers into `ProfileScreenUI`. This prevents state object bloat while keeping action callbacks, analytics tracking, and backend execution strictly within `ProfileScreenViewModel`.
* In `ProfileRepositoryImpl`, both `deleteGroup` and `leaveGroup` safely ensure that the client is unsubscribed from the FCM topic for that group.

## 6. Resulting Commits
* `6de17df` - `[SP-89] feat(profile): allow managing user groups with edit, delete, and leave actions`
