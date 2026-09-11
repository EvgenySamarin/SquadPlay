# Detail: Display User Groups and Members in Separate Sections

* **Target Commit Title**: `feat(profile): display user groups and members in separate sections`

## 1. Intent & Architectural Trade-offs
Previously, tapping the invite link on `ProfileScreen` automatically generated a new user group on every tap instead of using existing groups, and displayed friends in a single flat list. This change restructures the profile screen to stream all groups the user belongs to and display each group with its members in its own section. A dedicated "Share link" button is provided per group header, tracking `AnalyticsEvent.ShareInviteClicked`. Group creation is restored as a deliberate user action via a top bar action that opens a modal bottom sheet with single-word title validation, invoking `profileProvider.createNewUserGroup(userId, title)` and tracking `AnalyticsEvent.GroupCreated`.

## 2. Detailed Contract & Schema Specifications
* **Models (`models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`)**:
  * Added `data class UserGroupSection(val groupId: String, val title: String, val members: List<Friend>)`.
* **Profile Screen Models (`models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`)**:
  * Added `UserGroupSection` list and `isCreateGroupBottomSheetVisible: Boolean = false` to `ProfileScreenUI`.
  * Updated `ProfileScreenAction.OnCreateInviteLinkTap(val groupId: String)`.
  * Added `ProfileScreenAction.OnCreateNewGroupTap`, `ProfileScreenAction.OnDismissCreateGroupBottomSheet`, and `ProfileScreenAction.OnConfirmCreateGroup(val title: String)`.
* **Repository & Data Source Interfaces**:
  * `ProfileRepository` & `FirebaseFirestoreDataSource`: `getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>`.
  * `ProfileProvider`: `createNewUserGroup(userId: String, title: String = "Friends"): String`.
* **Analytics Contract (`contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`)**:
  * Added `data class ShareInviteClicked(val groupId: String) : AnalyticsEvent`.

## 3. Executed Plan
- [x] Step 1: Update models (`ProfileModels.kt` and `ProfileScreenModels.kt`) to add `UserGroupSection`, update `ProfileScreenUI` with `groupSections`, update `ProfileScreenAction.OnCreateInviteLinkTap(val groupId: String)`, and adjust preview data.
- [x] Step 2: Update `ProfileRepository`, `FirebaseFirestoreDataSource`, `ProfileRepositoryImpl`, and `ProfileProvider` signatures and implementations for `getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>`, correctly mapping member documents to their respective group sections.
- [x] Step 3: Update `ProfileScreenViewModel` to collect `groupSections` from `ProfileProvider.getGroupsMembersInfoFlow` and update `onCreateInviteGroupLinkTap(groupId: String)` to generate invite links for the requested group without creating new groups.
- [x] Step 4: Update `strings.xml` and `values-ru/strings.xml` with strings for group headers, empty state placeholders, and group creation bottom sheet.
- [x] Step 5: Update `ProfileScreen.kt` layout to display groups in separate sections separated by group title header, each featuring a share invite link action and list of group members, with `EmptyContent` for empty groups state.
- [x] Step 6: Update test doubles in `LogoutLoadingTest.kt` and `HomeScreenEventNavigationTest.kt` to match updated signatures.
- [x] Step 7: Add unit tests in `ProfileScreenViewModelTest.kt` verifying group sections, invite links, and analytics tracking.
- [x] Step 8: Add group creation ModalBottomSheet with single-word title validation and `ProfileProvider.createNewUserGroup` integration.
- [x] Step 9: Verify build and run `./gradlew testDebugUnitTest` to ensure all tests pass.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/main/res/drawable/ic_group_add_24.xml`
* `app/src/main/res/drawable/ic_share_24.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AnalyticsEvent.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/ProfileRepository.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`

## 5. Architectural Divergences & Discoveries
* Preserved `title: String = "Friends"` default parameter in `ProfileProvider` interface to maintain compatibility across call sites while enabling custom group titles for the new modal bottom sheet creation flow.
* Explicitly separated the invite icon (`ic_share_24.xml`) from the create group action icon (`ic_group_add_24.xml`) for UI clarity.

## 6. Resulting Commits
* `6a79000` - `[profile-groups-sections] feat(profile): add bottom sheet for creating single-word user groups`
* `413c5f9` - `[profile-groups-sections] feat(profile): track ShareInviteClicked analytics event on invite button tap`
* `e2826a7` - `[profile-groups-sections] test(profile): add compose preview for empty groups state`
* `2fdc117` - `[profile-groups-sections] style(profile): polish group section styling and add invite icon`
* `abb8840` - `[profile-groups-sections] test(profile): update compose preview with multi-group sample data`
* `038033c` - `[profile-groups-sections] feat(profile): display user groups and members in separate sections`
