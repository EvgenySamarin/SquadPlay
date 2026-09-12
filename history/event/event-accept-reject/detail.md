# Detail: Support event accept and reject attendance status

* **Target Commit Title**: `feat(event): support event accept and reject attendance status`

## 1. Intent & Architectural Trade-offs
Previously, SquadPlay lacked event attendance response tracking. Participants could not accept or reject event invites, and the home screen offered no visibility into whether a user had agreed to join an upcoming match.

To deliver this capability while maintaining a clean user experience:
1. **Firestore Schema & Persistence**:
   - Extended `events/{eventId}` with `responses: Map<String, String>` where keys are user IDs and values are status strings (`"ACCEPTED"` or `"REJECTED"`).
   - In `FirebaseFirestoreDataSourceImpl`, `updateEventResponse` applies atomic dot-notation field updates (`mapOf("responses.$userId" to statusString)`), avoiding read-modify-write race conditions where concurrent updates from multiple attendees could overwrite each other.
   - Introduced `EventEntity` DTO in `:data` layer to isolate Firestore deserialization from the `:models` domain representation.
2. **Creator Auto-Accept & Action Button Suppression**:
   - When a user creates a new event via `NewEventScreenViewModel`, `responses` is initialized with `mapOf(currentUser.uid to EventResponseStatus.ACCEPTED.name)`.
   - `Event.getStatusForUser(userId)` explicitly returns `EventResponseStatus.ACCEPTED` if `creatorId == userId`, ensuring retro-compatibility for past events.
   - On `EventDetailsScreen`, Accept and Reject action buttons are suppressed if `state.isYourEvent` is `true`, avoiding redundant interactions for the organizer.
3. **Home Screen Status Badging**:
   - For attendee events (`!item.isYourEvent`), `DSListItem` renders colored status icons: green Check Circle for `ACCEPTED`, red Cancel for `REJECTED`, and gray Help Outline for `NOT_SET`.
   - For creator events (`item.isYourEvent`), the star icon is preserved.
4. **Local Drawable Assets**:
   - Rather than bundling the deprecated/heavy `material-icons-extended` dependency, vector drawables (`ic_check_circle_24.xml`, `ic_cancel_24.xml`, `ic_help_24.xml`) were imported directly into `app/src/main/res/drawable/`.

## 2. Detailed Contract & Schema Specifications
* **EventResponseStatus (`MainScreenModels.kt`)**:
  ```kotlin
  @Serializable
  enum class EventResponseStatus {
      ACCEPTED,
      REJECTED,
      NOT_SET,
  }
  ```
* **Event (`MainScreenModels.kt`)**:
  ```kotlin
  data class Event(
      val uid: String,
      val creatorId: String,
      val groupId: String,
      val title: String,
      val eventIconUrl: String? = null,
      val fromDateTime: LocalDateTime,
      val toDateTime: LocalDateTime,
      val responses: Map<String, String> = emptyMap(),
  ) {
      fun getStatusForUser(userId: String): EventResponseStatus {
          if (creatorId == userId) return EventResponseStatus.ACCEPTED
          return when (responses[userId]) {
              EventResponseStatus.ACCEPTED.name -> EventResponseStatus.ACCEPTED
              EventResponseStatus.REJECTED.name -> EventResponseStatus.REJECTED
              else -> EventResponseStatus.NOT_SET
          }
      }
  }
  ```
* **EventDetailsScreenUI & Action (`EventDetailsScreenModels.kt`)**:
  ```kotlin
  sealed interface EventDetailsScreenAction {
      data object OnBackButtonTap : EventDetailsScreenAction
      data object OnDeleteTap : EventDetailsScreenAction
      data object OnConfirmDeleteTap : EventDetailsScreenAction
      data object OnDismissDeleteDialog : EventDetailsScreenAction
      data object OnAcceptTap : EventDetailsScreenAction
      data object OnRejectTap : EventDetailsScreenAction
  }

  data class EventDetailsScreenUI(
      val eventId: String,
      val title: String,
      val date: String,
      val imageUrl: String? = null,
      val isYourEvent: Boolean = false,
      val showDeleteConfirmation: Boolean = false,
      val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
  )
  ```
* **EventRepository & EventProvider**:
  ```kotlin
  suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)
  ```
* **Firestore Schema**:
  - Path: `events/{eventId}`
  - Field: `responses: Map<String, String>`
  - Dot-notation update: `update("responses.$userId", status.name)`

## 3. Executed Plan
- [x] Step 1: In `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`, add `EventResponseStatus` enum (`ACCEPTED`, `REJECTED`, `NOT_SET`), add `responses: Map<String, String> = emptyMap()` and `fun getStatusForUser(userId: String): EventResponseStatus` helper to `Event` (auto-returning `ACCEPTED` if `creatorId == userId`), and add `userStatus: EventResponseStatus = EventResponseStatus.NOT_SET` to `EventUI`.
- [x] Step 2: In `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`, add `userStatus: EventResponseStatus = EventResponseStatus.NOT_SET` to `EventDetailsScreenUI`, and add `OnAcceptTap` and `OnRejectTap` actions to `EventDetailsScreenAction`.
- [x] Step 3: Create `data/src/main/kotlin/com/eysamarin/squadplay/data/entity/EventEntity.kt` Firestore DTO with `responses: Map<String, String> = emptyMap()` and bidirectional mapping to/from `Event`.
- [x] Step 4: In `contract/src/main/java/com/eysamarin/squadplay/contracts/EventRepository.kt` and `domain/src/main/java/com/eysamarin/squadplay/domain/event/EventProvider.kt`, add `suspend fun updateEventResponse(eventId: String, userId: String, status: EventResponseStatus)`.
- [x] Step 5: In `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`, update `saveEvent` and `getEventsFlow` to read/write `responses` map using `EventEntity`, and implement `updateEventResponse` using Firestore dot-notation `mapOf("responses.$userId" to statusString)` with `.await()`. In `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`, implement `updateEventResponse` by delegating to `FirebaseFirestoreDataSource`.
- [x] Step 6: In `design-system/src/main/kotlin/com/eysamarin/squadplay/designSystem/compose/ListItem.kt`, add optional `trailingIconTint: Color? = null` to `DSListItem`. Import vector drawables `ic_check_circle_24.xml`, `ic_cancel_24.xml`, and `ic_help_24.xml` directly into `app/src/main/res/drawable/`.
- [x] Step 7: In `app/src/main/res/values/strings.xml`, add string resources for Accept and Reject buttons and status content descriptions.
- [x] Step 8: In `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`, add `val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET` to `Destination.EventDetailsScreen`.
- [x] Step 9: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`, map `userStatus = event.getStatusForUser(currentUserId)` in `getEventsBySelectedDate` and pass `userStatus` when navigating in `onEventTap`.
- [x] Step 10: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`, update both `HomeScreenMediumLayout` and `MainScreenExpandedLayout` so that for events created by other users (`!item.isYourEvent`), `DSListItem` displays the status badge:
  - `ACCEPTED`: `painterResource(R.drawable.ic_check_circle_24)` (Green `Color(0xFF4CAF50)`)
  - `REJECTED`: `painterResource(R.drawable.ic_cancel_24)` (Red `Color(0xFFF44336)`)
  - `NOT_SET`: `painterResource(R.drawable.ic_help_24)` (Gray `Color.Gray`)
  while user-owned events (`item.isYourEvent`) retain the star icon (`R.drawable.ic_star_24`).
- [x] Step 11: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`, inject `profileProvider: ProfileProvider`, collect current user, handle `initData` with `userStatus`, implement `updateEventResponse(...)` with toggle logic (clicking active status resets to `NOT_SET`, otherwise sets new status), and handle `OnAcceptTap` / `OnRejectTap`.
- [x] Step 12: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`, render Accept and Reject buttons only if `!state.isYourEvent`.
- [x] Step 13: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreenViewModel.kt`, auto-accept creator on new event creation by seeding `responses = mapOf(currentUser.uid to EventResponseStatus.ACCEPTED.name)`.
- [x] Step 14: In `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt` and `NewEventScreenViewModelTest.kt`, add unit test coverage for `Event.getStatusForUser` auto-accepting creator, `NewEventScreenViewModel` auto-accepting creator on save, and `EventDetailsScreen` behavior.
- [x] Step 15: Run test and build verification (`./gradlew test`, `./gradlew compileDebugKotlin`) to guarantee zero regressions.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/EventRepository.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/event/EventProvider.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/entity/EventEntity.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`
* `design-system/src/main/kotlin/com/eysamarin/squadplay/designSystem/compose/ListItem.kt`
* `app/src/main/res/drawable/ic_check_circle_24.xml`
* `app/src/main/res/drawable/ic_cancel_24.xml`
* `app/src/main/res/drawable/ic_help_24.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`

## 5. Architectural Divergences & Discoveries
* Removed dependency on `androidx.compose.material:material-icons-extended` in favor of standalone vector XML drawables in `app/src/main/res/drawable/`.
* Added creator auto-accept logic both at dynamic query evaluation (`getStatusForUser`) and persistence creation (`NewEventScreenViewModel`), ensuring creators never need to respond to their own events.
* Hiding Accept/Reject buttons on `EventDetailsScreen` when viewing own event (`isYourEvent == true`), leaving only the delete event action available to the creator.

## 6. Resulting Commits
* `d338d1d` - `[event-accept-reject] feat(event): support event accept and reject attendance status`
