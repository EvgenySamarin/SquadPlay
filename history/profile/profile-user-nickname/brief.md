# Brief: Allow User to View and Update Nickname

* **Commit Title**: `feat(profile): allow user to view and update nickname`
* **Domain**: `profile`
* **Parent**: `profile/sp-89-leave-group-remove-event-responses`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* User document in Firestore `users/{userId}` stores `nickname: String`.
* Real-time reactive synchronization is maintained via snapshot listeners in `getUserInfoFlow`.
* Default fallback for absent or blank nickname is `"recrut"`, rendered as `@<nickname>` in gray (`onSurfaceVariant`) under the email.
* Tapping the user avatar triggers a Material 3 modal bottom sheet with a single-word validated text field and confirm button.
* Input validation ensures single-word nicknames and automatically strips any leading `@` symbols before persistence.
* Firestore profile saves use `SetOptions.merge()` to ensure partial profile updates (e.g., auth sign-in) do not overwrite existing nicknames.
* Failure during network updates is caught defensively and logged via `AppLogger`, returning `false` without crashing the application.

## Affected Capabilities & Side Effects
* **Behavior**: Users can view their nickname under their email on ProfileScreen and update it by tapping their avatar, opening a bottom sheet with a text field and confirmation button.
* **Contract Adjustments**:
  - Added `nickname: String? = null` to `User`.
  - Added `OnAvatarTap`, `OnDismissChangeNicknameBottomSheet`, and `OnConfirmChangeNickname` to `ProfileScreenAction`.
  - Added `isChangeNicknameBottomSheetVisible: Boolean = false` to `ProfileScreenUI`.
  - Added `suspend fun updateNickname(userId: String, nickname: String): Boolean` to `FirebaseFirestoreDataSource`, `ProfileRepository`, and `ProfileProvider`.
* **Hotspots**: Firestore `users/{userId}` document snapshot listeners and profile merge operations.
