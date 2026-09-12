# Brief: Manage User Groups on Profile Screen

* **Commit Title**: `feat(profile): allow managing user groups with edit, delete, and leave actions`
* **Domain**: `profile`
* **Parent**: `profile/profile-groups-sections`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Firestore documents in the `groups` collection store `ownerId: String` populated with the creating user's UID.
* Invariant permission rules:
  - If `ownerId` is null or blank (legacy groups), any group member can see and trigger **Edit** (rename) and **Delete** actions; **Leave** is hidden.
  - If `ownerId` matches the current user's UID, the owner sees **Edit** and **Delete** actions; **Leave** is hidden.
  - If `ownerId` does not match the current user's UID, the member sees only **Leave**; **Edit** and **Delete** are hidden.
  - **Share** invite link is always visible across all groups.
* Renaming a group requires a non-blank, single-word group title matching the group creation invariant.
* Destructive/state-changing actions (Delete, Leave) require explicit confirmation via Material 3 `ModalBottomSheet` before execution.
* Group deletion and group leaving trigger FCM topic unsubscription (`unsubscribeFromGroupTopic`) to prevent orphaned notifications.
* All group management user interactions are logged via `AppLogger` and emit dedicated `AnalyticsEvent`s (`EditGroupClicked`, `GroupRenamed`, `DeleteGroupClicked`, `GroupDeleted`, `LeaveGroupClicked`, `GroupLeft`).

## Affected Capabilities & Side Effects
* **Behavior**: Added group management controls in each group section on the Profile screen: group owners can rename or delete groups, non-owners can leave groups, protected by confirmation modal bottom sheets, with real-time reactive updates via Firestore snapshot listeners.
* **Contract Adjustments**:
  - `Group` and `UserGroupSection`: added `val ownerId: String? = null`.
  - `ProfileScreenAction`: added `OnConfirmEditGroup`, `OnConfirmDeleteGroup`, and `OnConfirmLeaveGroup`.
  - `ProfileRepository`, `ProfileRepositoryImpl`, `FirebaseFirestoreDataSource`, and `ProfileProvider`: added `renameGroup`, `deleteGroup`, and `leaveGroup`.
  - `AnalyticsEvent`: added `EditGroupClicked`, `GroupRenamed`, `DeleteGroupClicked`, `GroupDeleted`, `LeaveGroupClicked`, `GroupLeft`.
* **Hotspots**: FCM topic subscription/unsubscription lifecycle during group deletion and leaving; transactional member removal in Firestore `groups` document.
