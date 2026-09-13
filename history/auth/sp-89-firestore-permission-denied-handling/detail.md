# Detail: Handle Firestore PERMISSION_DENIED gracefully on logout and unauth

* **Target Commit Title**: `fix(auth): handle firestore permission denied gracefully on logout and unauth`

## 1. Intent & Architectural Trade-offs
When user logs out, `authProvider.signOut()` synchronously removes authentication credentials via `FirebaseAuth.signOut()`. Because screens within `Destination.HomeGraph` (`HomeScreen`, `ProfileScreen`) remain in the backstack until `navigator.navigateToAuthGraph()` is processed, their underlying snapshot listeners (`users`, `groups`, `events`) remained active. As a result, Firestore's watch stream rejected the queries with `PERMISSION_DENIED: Missing or insufficient permissions`, triggering `logger.e` error logs across both data sources and repositories, flooding Sentry and Crashlytics.

To solve this fundamentally, a two-pronged architectural solution was applied:
1. **Proactive unsubscription on logout**: `FirebaseFirestoreDataSourceImpl` maintains an internal synchronized registry of active `ListenerRegistration`s. When `AuthRepositoryImpl.signOut()` is executed, `firestoreDataSource.clearListeners()` unregisters all snapshot listeners prior to `firebaseAuthManager.signOut()`, ensuring no active queries remain on the stream when credentials are cleared.
2. **Defensive exception handling**: In the event of unexpected session invalidation or token expiration, snapshot listeners and repository flows (`ProfileRepositoryImpl`, `EventRepositoryImpl`) treat `FirebaseFirestoreException.Code.PERMISSION_DENIED` as an expected unauthenticated lifecycle state, logging as debug information (`logger.d`), safely emitting fallback values (`null` or `emptyList()`), and closing channels cleanly without throwing errors.

## 2. Detailed Contract & Schema Specifications
* `FirebaseFirestoreDataSource`: Added `fun clearListeners()`.
* `FirebaseFirestoreDataSourceImpl`:
  - Added thread-safe `activeListeners = Collections.synchronizedSet(mutableSetOf<ListenerRegistration>())`.
  - Implemented `clearListeners()` iterating through all registrations and calling `it.remove()`.
  - Updated `getUserInfoFlow`, `getUserGroupsFlow`, `getEventsFlowForGroupIdsChunk`, and `getGroupsMembersInfoFlow` to register/unregister listeners and catch `FirebaseFirestoreException.Code.PERMISSION_DENIED`, emitting safe fallback values and closing without errors.
* `AuthRepositoryImpl`: Injected `firestoreDataSource: FirebaseFirestoreDataSource` and invoked `firestoreDataSource.clearListeners()` in `signOut()` prior to `firebaseAuthManager.signOut()`.
* `SquadPlayApplication`: Updated Koin module binding for `AuthRepository` to provide `firestoreDataSource = get()`.
* `ProfileRepositoryImpl` and `EventRepositoryImpl`: Updated `.catch` blocks to detect `PERMISSION_DENIED`, log as debug info, and emit `null` or `emptyList()`.

## 3. Executed Plan
- [x] Step 1: Add unit test dependencies to `data/build.gradle.kts` (`junit`, `kotlinx-coroutines-test`).
- [x] Step 2: In `FirebaseFirestoreDataSource` and `FirebaseFirestoreDataSourceImpl`, implement listener registry tracking and `clearListeners()`, removing all active `ListenerRegistration` objects. Add defensive handling for `PERMISSION_DENIED` across all 4 snapshot listener flows (`getUserInfoFlow`, `getUserGroupsFlow`, `getEventsFlowForGroupIdsChunk`, `getGroupsMembersInfoFlow`), logging as debug and closing cleanly with fallback emissions.
- [x] Step 3: In `AuthRepositoryImpl`, inject `firestoreDataSource` and invoke `firestoreDataSource.clearListeners()` in `signOut()` prior to `firebaseAuthManager.signOut()`. Wire injection in `SquadPlayApplication.kt`.
- [x] Step 4: In `ProfileRepositoryImpl` and `EventRepositoryImpl`, update `.catch` blocks to treat `PERMISSION_DENIED` as a benign unauth event, logging at debug level and safely emitting fallbacks (`null` or `emptyList()`).
- [x] Step 5: Write unit tests verifying:
  - `AuthRepositoryImpl.signOut()` calls `clearListeners()` before `firebaseAuthManager.signOut()`.
  - `ProfileRepositoryImpl` and `EventRepositoryImpl` handle `PERMISSION_DENIED` gracefully and emit expected fallbacks.
- [x] Step 6: Verify build, compilation, and unit tests via `./gradlew compileDebugKotlin test`.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `data/build.gradle.kts`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/AuthRepositorySignOutTest.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/RepositoryPermissionDeniedTest.kt`

## 5. Architectural Divergences & Discoveries
* None. Proactive unsubscription cleanly detached all active snapshot listeners before `FirebaseAuth.signOut()` without affecting screen transition lifecycles or coroutine flows.

## 6. Resulting Commits
* `b05dcd1` - `[SP-89] fix(auth): handle firestore permission denied gracefully on logout and unauth`
