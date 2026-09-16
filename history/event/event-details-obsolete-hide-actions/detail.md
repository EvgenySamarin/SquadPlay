# Detail: Hide Accept and Reject Buttons for Obsolete Events on Event Details Screen

* **Target Commit Title**: `feat(event): hide accept and reject buttons for obsolete events on event details screen`

## 1. Intent & Architectural Trade-offs
To prevent inconsistent state updates and unnecessary UI affordances for past game sessions, obsolete events (where `toDateTime < now`) should not permit users to accept or reject participation. On `EventDetailsScreen`, the Accept/Reject button row is conditionally hidden when `isObsolete` is true or when the user is the creator. In addition, the ViewModel guards `updateEventResponse` actions at the controller boundary. Time comparisons rely on an injectable `nowProvider: () -> LocalDateTime` to ensure deterministic execution in unit tests while defaulting to system time in production.

## 2. Detailed Contract & Schema Specifications
* **Event (`models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`)**:
  ```kotlin
  fun isObsolete(now: LocalDateTime): Boolean = toDateTime < now
  ```
* **EventDetailsScreenUI (`models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`)**:
  ```kotlin
  data class EventDetailsScreenUI(
      ...
      val isObsolete: Boolean = false,
  )
  ```
* **Destination.EventDetailsScreen (`app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`)**:
  ```kotlin
  data class EventDetailsScreen(
      ...
      val isObsolete: Boolean = false,
  ) : Destination
  ```
* **EventDetailsScreen (`app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`)**:
  - Buttons row visibility conditioned on `if (!state.isYourEvent && !state.isObsolete)`.
* **EventDetailsScreenViewModel (`app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`)**:
  - In `initData`, initializes `isObsolete = args.isObsolete`.
  - In `loadGroupMembers`, evaluates `val isObsolete = matchingEvent?.isObsolete(nowProvider())`.
  - In `updateEventResponse(status)` and `updateEventResponse(eventId, userId, status)`, returns early if `isObsolete` is true.

## 3. Executed Plan
- [x] Step 1: In `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`, add `fun isObsolete(now: LocalDateTime): Boolean = toDateTime < now` to `Event`.
- [x] Step 2: In `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`, add `val isObsolete: Boolean = false` to `EventDetailsScreenUI`.
- [x] Step 3: In `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`, add `val isObsolete: Boolean = false` to `Destination.EventDetailsScreen`.
- [x] Step 4: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`, add `defaultNowProvider` / `nowProvider`, evaluate `isObsolete` on `onEventTap`, and pass it to `Destination.EventDetailsScreen`.
- [x] Step 5: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`, add `defaultNowProvider` / `nowProvider`, initialize `isObsolete` in `initData`, dynamically update `isObsolete` when `matchingEvent` is observed in `loadGroupMembers`, and guard `updateEventResponse` when `isObsolete == true`.
- [x] Step 6: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`, update the condition rendering the Accept/Reject buttons row to `if (!state.isYourEvent && !state.isObsolete)`.
- [x] Step 7: In `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`, add unit tests covering obsolete detection in `Event`, navigation from `HomeScreenViewModel`, dynamic state resolution in `EventDetailsScreenViewModel`, and guarding `updateEventResponse` for obsolete events.
- [x] Step 8: Run `./gradlew testDebugUnitTest` to verify all tests pass without regressions.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
None. Unit tests used `removeAt` instead of `removeLast` to preserve compatibility with Java 17 toolchain.

## 6. Resulting Commits
* `b77d24d` - `[calendar-and-account-optimizations] feat(event): hide accept and reject buttons for obsolete events on event details screen`
