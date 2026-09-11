# Brief: Display User Groups and Members in Separate Sections

* **Commit Title**: `feat(profile): display user groups and members in separate sections`
* **Domain**: `profile`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `FirebaseFirestoreDataSource`, queries against the `users` collection must handle groups without other members or empty member lists without throwing exceptions or prematurely aborting reactive flow emissions.
* Per-group invite link generation uses `profileProvider.createNewInviteLink(groupId)` which formats deep links as `https://evgenysamarin.github.io/invite/<groupId>`, retaining group association.
* Group creation requires a single-word group title; multiple words or empty inputs are blocked via client-side UI validation before sending requests to `ProfileProvider`.
* Invite button tap tracks `AnalyticsEvent.ShareInviteClicked(groupId)` and group creation tracks `AnalyticsEvent.GroupCreated(groupId)`.

## Affected Capabilities & Side Effects
* **Behavior**: Replaced automatic group generation on invite button tap with listing all user groups and members in separate sections with distinct title headers, share invite actions per group, and a modal bottom sheet for creating new single-word user groups.
* **Contract Adjustments**:
  * Introduced `UserGroupSection(groupId, title, members)` in `models`.
  * Updated `ProfileScreenUI` in `models`: replaced flat `friends: List<Friend>` with `groupSections: List<UserGroupSection>` and added `isCreateGroupBottomSheetVisible: Boolean`.
  * Updated `ProfileScreenAction`: changed `OnCreateInviteLinkTap` to accept `groupId: String`, added `OnCreateNewGroupTap`, `OnDismissCreateGroupBottomSheet`, and `OnConfirmCreateGroup(title: String)`.
  * Updated `ProfileRepository`, `ProfileRepositoryImpl`, `FirebaseFirestoreDataSource`, and `ProfileProvider` to expose `getGroupsMembersInfoFlow(groups: List<Group>): Flow<List<UserGroupSection>>`.
  * Updated `ProfileProvider.createNewUserGroup(userId: String, title: String = "Friends"): String`.
  * Added `AnalyticsEvent.ShareInviteClicked(val groupId: String)` in `contracts`.
* **Hotspots**: Firestore `users` document query when rendering large groups or multiple groups simultaneously; `ProfileScreen` multi-section rendering with `LazyColumn`.
