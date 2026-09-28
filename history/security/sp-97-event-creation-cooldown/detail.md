# Detail: Event Creation Cooldown Rate Limiting

* **Target Commit Title**: `[SP-97] feat(security): enforce event creation cooldown rate limiting`

## 1. Intent & Architectural Trade-offs
To prevent rapid spam floods and accidental double-submissions from mobile clients, write frequency on event creation must be throttled per account. 

### Key Architectural Decisions:
1. **Per-User Timestamp in `/users/{userId}`**: Rather than managing complex rate-limiting counter subcollections or client-only flags, a single `lastEventCreatedAt: FieldValue.serverTimestamp()` field is persisted on the user's root profile document. This is written atomically in the event creation transaction using `SetOptions.merge()`, guaranteeing zero race conditions between concurrent requests.
2. **Backend & Frontend Defense-in-Depth**:
   * **Firestore Security Rules**: Rules enforce `request.time >= get(/databases/$(database)/documents/users/$(request.auth.uid)).data.lastEventCreatedAt + duration.value(30, 's')`, ensuring non-conforming or bypassed clients are strictly rejected with `PERMISSION_DENIED`.
   * **Client UI State & Ticker**: `NewEventScreenViewModel` detects recent event creations via `user.lastEventCreatedAt` and starts a 1-second interval ticker job. During cooldown or in-flight saves, the primary button is disabled and an informative countdown warning informs the user of the remaining wait time.
3. **App Check Isolation**: Rejections due to Firestore security rules or network interruptions are caught and handled as standard event save errors without tripping the sensitive `SecurityLockoutManager.triggerLockout()` attestation lockout flow.
4. **Clean Action UX**: The schedule button keeps its stable semantic action verb (`Schedule event`) rather than mutating into a timer label, while an explicit warning caption below provides live countdown feedback without redundant duplication.

## 2. Detailed Contract & Schema Specifications
* **Models (`:models`)**:
  ```kotlin
  data class User(
      val uid: String,
      val username: String,
      val email: String?,
      val photoUrl: String?,
      val groups: List<Group>,
      val nickname: String? = null,
      val lastEventCreatedAt: Long? = null,
  )

  data class NewEventScreenUI(
      val title: String,
      val selectedDate: Date,
      val yearMonth: LocalDate,
      val gameTitle: String = "",
      val eventIconUrl: String? = null,
      val userGroups: List<Group> = emptyList(),
      val isCooldownActive: Boolean = false,
      val cooldownRemainingSeconds: Long = 0L,
      val isSaving: Boolean = false,
  )
  ```
* **Data Source (`:data`)**:
  * In `FirebaseFirestoreDataSource.saveEvent()`:
    Reads: `groupsDocumentRef` and `userDocumentRef` (strict read-before-write ordering).
    Writes: `eventDocumentRef.set(...)`, `groupsDocumentRef.update(...)`, and `userDocumentRef.set(mapOf("lastEventCreatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())`.
  * In `FirebaseFirestoreDataSource.getUserInfoFlow()`:
    Extracts and maps `lastEventCreatedAt` (handling Firestore `Timestamp`, `Long`, and numeric representations) into epoch milliseconds.

## 3. Executed Plan
- [x] Step 1: Specify Firestore security rule rate limiting constraints for `/events`, `/users`, and `/groups` checking `users/{userId}.lastEventCreatedAt + 30s`.
- [x] Step 2: Update `User` model in `ProfileModels.kt` to include `lastEventCreatedAt: Long? = null`.
- [x] Step 3: Update `FirebaseFirestoreDataSource.kt` to extract `lastEventCreatedAt` from user snapshots in `getUserInfoFlow()`, and include `userDocumentRef` read and `lastEventCreatedAt` server timestamp write in `saveEvent()` transaction.
- [x] Step 4: Update `NewEventScreenModels.kt` to include `isCooldownActive`, `cooldownRemainingSeconds`, and `isSaving` in `NewEventScreenUI`.
- [x] Step 5: Add localized cooldown warning strings to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
- [x] Step 6: Enhance `NewEventScreenViewModel.kt` to observe `user.lastEventCreatedAt`, run 1-second countdown ticker job, throttle repeated button presses using `isSaving`, and show feedback if creation fails.
- [x] Step 7: Update `NewEventScreen.kt` layout to disable `DSButton` when `isSaving` or `isCooldownActive` is true, keeping standard label and rendering countdown warning notice below.
- [x] Step 8: Add unit tests in `NewEventScreenViewModelTest.kt` and `RepositoryPermissionDeniedTest.kt` covering cooldown state tracking, ticker countdown, double-click prevention, and non-lockout error handling.
- [x] Step 9: Verify build, lint, and unit tests via Gradle.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreenViewModel.kt`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/contract/RepositoryPermissionDeniedTest.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/NewEventScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/ProfileModels.kt`

## 5. Architectural Divergences & Discoveries
1. **Firestore Rules Repository Decoupling**: Security rules are maintained and deployed via the external Firebase configuration/console rather than directly inside this client repository codebase.
2. **Positional Resource Formatting**: Discovered non-positional formatting warnings on existing string resource `from_to_date` during resource packaging, which was adjusted to explicit positional markers (`%1$s - %2$s`).
3. **UX Streamlining**: Avoided redundant timer duplication across both button label and caption, opting for a disabled primary button with a dynamic countdown helper text underneath.

## 6. Resulting Commits
* `88765c2` - `[SP-97] feat(security): enforce event creation cooldown rate limiting`
