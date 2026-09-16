# Detail: Allow User to View and Update Nickname

* **Target Commit Title**: `feat(profile): allow user to view and update nickname`

## 1. Intent & Architectural Trade-offs
Users needed the ability to display and update a custom nickname in their profile document in Firestore.
Key architectural decisions:
1. **Model Extension with Default Parameter**: Added `nickname: String? = null` to `User` to avoid breaking existing constructors, serializers, or test fixtures.
2. **Reactive Flow Binding**: Updated `getUserInfoFlow` to parse `"nickname"` from the Firestore document snapshot. Because `userInfoFlow` feeds into `uiState`, any update dynamically refreshes the UI without needing manual re-fetches.
3. **Safe Merge Strategy**: Updated `saveUserProfile` in `FirebaseFirestoreDataSourceImpl` to use `SetOptions.merge()`, guaranteeing that auth sign-in synchronization (which only knows auth provider claims) does not overwrite custom Firestore fields like `nickname`.
4. **Validation & Formatting**: Implemented single-word validation in `ChangeNicknameBottomSheet` matching existing app patterns, trimmed leading `@` characters upon confirmation, and formatted the display as `@<nickname>` (defaulting to `@recrut` in `onSurfaceVariant` gray color).

## 2. Detailed Contract & Schema Specifications
* **Public APIs / Interfaces**:
  - `User`: Added `val nickname: String? = null`.
  - `ProfileScreenAction`: Added:
    - `data object OnAvatarTap : ProfileScreenAction`
    - `data object OnDismissChangeNicknameBottomSheet : ProfileScreenAction`
    - `data class OnConfirmChangeNickname(val newNickname: String) : ProfileScreenAction`
  - `ProfileScreenUI`: Added `val isChangeNicknameBottomSheetVisible: Boolean = false`.
  - `FirebaseFirestoreDataSource`, `ProfileRepository`, `ProfileProvider`: Added `suspend fun updateNickname(userId: String, nickname: String): Boolean`.
* **Database Schema (`users/{userId}`)**:
  - Field: `nickname: String` (optional).

## 3. Executed Plan
- [x] Step 1: Update `User` model in `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt` to include `nickname: String? = null`.
- [x] Step 2: Add actions (`OnAvatarTap`, `OnDismissChangeNicknameBottomSheet`, `OnConfirmChangeNickname`) and state property `isChangeNicknameBottomSheetVisible: Boolean = false` in `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`.
- [x] Step 3: Add `updateNickname(userId: String, nickname: String): Boolean` to `FirebaseFirestoreDataSource`, `ProfileRepository`, and `ProfileProvider`, and update `getUserInfoFlow` and `saveUserProfile` in `FirebaseFirestoreDataSourceImpl` to support `nickname` with `SetOptions.merge()`.
- [x] Step 4: Add string resources in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml` for bottom sheet title, nickname textfield label, and error helper text.
- [x] Step 5: Implement `ChangeNicknameBottomSheet` and integrate nickname text display (`@<nickname>`, defaulting to `@recrut`, in `DesignSystemTheme.colorScheme.onSurfaceVariant`) under email on `ProfileScreen`, making the avatar clickable to trigger `OnAvatarTap`.
- [x] Step 6: Handle `OnAvatarTap`, `OnDismissChangeNicknameBottomSheet`, and `OnConfirmChangeNickname` in `ProfileScreenViewModel`.
- [x] Step 7: Update test doubles and add unit tests for nickname change flow in `ProfileScreenViewModelTest` and repository test suites.
- [x] Step 8: Verify build and run unit tests.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileScreenModels.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/ProfileRepository.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/ProfileScreenViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/navigation/HomeGraphViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositorySignOutTest.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/RepositoryPermissionDeniedTest.kt`

## 5. Architectural Divergences & Discoveries
* Used `SetOptions.merge()` on user profile writes in `FirebaseFirestoreDataSourceImpl` so that authentication sign-in operations do not wipe out existing Firestore nicknames when mapping from Google or Email auth profiles.
* Added support for stripping leading `@` prefixes to make user input in the text field forgiving when typing nicknames.

## 6. Resulting Commits
* `0ede789` - `[calendar-and-account-optimizations] feat(profile): allow user to view and update nickname`
