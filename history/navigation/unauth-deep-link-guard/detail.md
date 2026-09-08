# Detail: Guard unauthenticated deep link navigation and preserve pending invite

* **Target Commit Title**: `fix(navigation): guard deep link when unauthenticated and preserve pending invite`

## 1. Intent & Architectural Trade-offs
When an unauthenticated user opened an invite deep link (`https://evgenysamarin.github.io/invite/{inviteGroupID}`), Navigation Compose automatically inspected the activity intent and routed directly to `Destination.HomeScreen`, bypassing `startDestination = Destination.AuthGraph`. This caused premature rendering of `HomeScreen`, Firestore queries with null user IDs, and broken redirect attempts back to `Destination.AuthScreen`.

Instead of allowing Navigation Compose to handle the intent automatically or passing asynchronous callback lambdas to `LaunchApplicationViewModel`, `MainActivity` consumes incoming intent URIs synchronously (`intent.data = null`) during `onCreate` and `onNewIntent`. `DeepLinkManager` extracts and retains `pendingInviteGroupId`. `LaunchApplicationViewModel` determines `startDestination` based on `authProvider.isUserExists()`:
- Authenticated users go directly to `Destination.HomeGraph` without seeing the auth screen.
- Unauthenticated users remain on `Destination.AuthGraph`. Once they sign in or register, `HomeScreenViewModel` observes and consumes the pending invite group ID from `DeepLinkManager`, displaying the join dialog seamlessly.

## 2. Detailed Contract & Schema Specifications
* **`DeepLinkManager`**: Interface exposing `pendingInviteGroupId: StateFlow<String?>`, `setPendingInviteGroupId(groupId: String?)`, `consumePendingInviteGroupId(): String?`, and `extractInviteGroupId(uri: Uri?): String?`. Registered as singleton in Koin.
* **`AuthRepository.getCurrentUserId(): String?`**: Made nullable; catches `IllegalStateException` when user is not signed in and logs a warning instead of throwing.
* **`AuthProvider.signOut(): Boolean`**: Null-safe check before deleting profile and signing out.
* **`ProfileProvider.getUserInfoFlow(): Flow<User?>`**: Returns `flowOf(null)` if `currentUserId` is null or blank.

## 3. Executed Plan
- [x] Step 1: Create `DeepLinkManager` singleton to hold `pendingInviteGroupId` and register it in `SquadPlayApplication.kt`.
- [x] Step 2: In `LaunchApplicationViewModel`, inspect incoming deep link URI on startup. If user is authenticated (`authProvider.isUserExists() == true`), set `startDestination = Destination.HomeGraph` so the user goes straight to `HomeScreen` without seeing `AuthScreen`. If unauthenticated, save `inviteGroupID` to `DeepLinkManager`.
- [x] Step 3: In `MainActivity`, consume `intent.data = null` synchronously in `onCreate` and `onNewIntent` to handle both cold and warm starts cleanly.
- [x] Step 4: In `HomeScreenViewModel`, observe `DeepLinkManager.pendingInviteGroupId` to show the invite confirmation dialog for both direct deep links and restored pending invites after login. If user is null, safely use `navigator.navigateToAuthGraph()`.
- [x] Step 5: Finalize null-safety in `AuthRepositoryImpl`, `AuthProviderImpl`, and `ProfileProviderImpl` so `getCurrentUserId()` returning null never causes crashes.
- [x] Step 6: Build project, verify unit tests, and validate cold and warm start deep link behavior on the emulator.

## 4. Touched Files
* `app/src/main/AndroidManifest.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/LaunchApplicationViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/MainActivity.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/DeepLinkManager.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/DeepLinkManagerTest.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/AuthRepository.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/FirebaseAuthManager.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositoryImpl.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/auth/AuthProvider.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/profile/ProfileProvider.kt`

## 5. Architectural Divergences & Discoveries
* Initial implementation passed a callback lambda into `handleIncomingIntent` to clear intent data asynchronously. Refactored to synchronous consumption in `MainActivity` to eliminate race conditions, lifecycle leak risks, and maintain clean UDF in the ViewModel.
* When navigating back to authentication from `HomeScreenViewModel` and `ProfileScreenViewModel`, `navigator.navigateToAuthGraph()` must be used instead of `navigator.navigate(Destination.AuthScreen)` because sibling graph destinations require popping `HomeGraph` (`popUpTo(Destination.HomeGraph) { inclusive = true }`).

## 6. Resulting Commits
* `33a3ccc` - `[fork-timepick-library] fix(navigation): guard deep link when unauthenticated and preserve pending invite`
