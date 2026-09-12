# Detail: Display group members and attendance responses on event details screen

* **Target Commit Title**: `feat(event): display group members and attendance responses on event details screen`

## 1. Intent & Architectural Trade-offs
Following the addition of event attendance status tracking (`event-accept-reject`), users needed visibility into who else in their group is attending an event. Previously, `EventDetailsScreen` only displayed the event banner, title, date, and self-attendance action buttons, without listing other participants.

To resolve this while preserving existing design conventions:
1. **Reuse Existing Profile Visual Style**:
   - Instead of inventing a new layout, members are rendered using the established `MemberRow` pattern from `ProfileScreen`: 48dp squircle avatars (`SquircleShape` with `CornerSmoothing.High`), default avatar fallbacks (`ic_default_avatar`), and standard typography.
   - Each item includes the attendance status icon (24dp) positioned on the start (left) side, with matching color coding (`ACCEPTED` green, `REJECTED` red, `NOT_SET` gray).
2. **Reactive Flow Composition**:
   - `EventDetailsScreenViewModel` resolves the event's `Group` via `profileProvider.getGroupInfo(groupId)`.
   - It combines `profileProvider.getGroupsMembersInfoFlow(listOf(group))` with `eventProvider.getEventsFlow(setOf(groupId))` to dynamically correlate each member's `uid` with the latest event responses map.
   - When the user toggles their attendance, an optimistic update is applied immediately to both `userStatus` and their row in `members` before backend completion.
3. **Scroll Container Refactoring**:
   - `EventDetailsScreen` replaced its outer non-scrollable column with a `LazyColumn`, integrating the event header, date, action buttons, members section title, and member rows into a unified, performant scrollable container.
4. **Navigation Contract Evolution**:
   - Added `groupId` to `EventUI`, `Destination.EventDetailsScreen`, and `EventDetailsScreenUI`, passed from `HomeScreenViewModel.onEventTap` to allow direct group resolution on details navigation.

## 2. Detailed Contract & Schema Specifications
* **EventMemberUI (`EventDetailsScreenModels.kt`)**:
  ```kotlin
  data class EventMemberUI(
      val uid: String,
      val username: String,
      val photoUrl: String? = null,
      val status: EventResponseStatus = EventResponseStatus.NOT_SET,
  )
  ```
* **EventDetailsScreenUI (`EventDetailsScreenModels.kt`)**:
  ```kotlin
  data class EventDetailsScreenUI(
      val eventId: String,
      val groupId: String = "",
      val title: String,
      val date: String,
      val imageUrl: String? = null,
      val isYourEvent: Boolean = false,
      val showDeleteConfirmation: Boolean = false,
      val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
      val members: List<EventMemberUI> = emptyList(),
  )
  ```
* **EventUI (`MainScreenModels.kt`)**:
  ```kotlin
  data class EventUI(
      val uid: String,
      val groupId: String? = null,
      ...
  )
  ```
* **Destination.EventDetailsScreen (`NavigationModels.kt`)**:
  ```kotlin
  data class EventDetailsScreen(
      val eventId: String,
      val groupId: String = "",
      val userStatus: EventResponseStatus = EventResponseStatus.NOT_SET,
  ) : Destination()
  ```

## 3. Executed Plan
- [x] Step 1: In `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`, add `val groupId: String? = null` to `EventUI`.
- [x] Step 2: In `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`, define `EventMemberUI(uid, username, photoUrl, status)` and add `val groupId: String = ""` and `val members: List<EventMemberUI> = emptyList()` to `EventDetailsScreenUI`. Update previews (`PREVIEW_EVENT_DETAILS_SCREEN_UI`, `PREVIEW_CREATOR_EVENT_DETAILS_SCREEN_UI`) with sample members.
- [x] Step 3: In `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`, add `val groupId: String = ""` to `Destination.EventDetailsScreen`.
- [x] Step 4: In `app/src/main/res/values/strings.xml` and `values-ru/strings.xml`, add `event_members_label` and Russian translations for status content descriptions.
- [x] Step 5: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`, map `groupId = event.groupId` in `getEventsBySelectedDate` and pass `groupId = event.groupId ?: matchingEvent?.groupId.orEmpty()` when navigating in `onEventTap`.
- [x] Step 6: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`:
  - Update `initData(args: Destination.EventDetailsScreen)` to store `groupId` and trigger reactive loading of group members and event responses.
  - Fetch group via `profileProvider.getGroupInfo(groupId)`, observe `profileProvider.getGroupsMembersInfoFlow(listOf(group))` and `eventProvider.getEventsFlow(setOf(groupId))`, and combine them into `List<EventMemberUI>` with each member's `status` evaluated using `matchingEvent.getStatusForUser(member.uid)`.
  - When user updates attendance via `updateEventResponse`, update optimistic status on corresponding member in `uiState.members`.
- [x] Step 7: In `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`:
  - Convert content inside `Surface` to `LazyColumn`.
  - Render event header item (title, date, Accept/Reject buttons if not creator, members section header).
  - Render member items matching `ProfileScreen`'s `MemberRow`: on the left side show attendance status icon (`ic_check_circle_24`, `ic_cancel_24`, `ic_help_24`) with corresponding tint, followed by squircle avatar and username.
  - If `members` is empty, show empty state message `no_group_members`.
  - Update phone and tablet Compose previews.
- [x] Step 8: In `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`:
  - Add unit tests verifying `EventDetailsScreenViewModel` loads and combines group members with their attendance statuses.
  - Verify optimistic updates to member attendance status upon accept/reject actions.
  - Verify `HomeScreenViewModel` propagates `groupId` on event navigation.
- [x] Step 9: Verify compilation and tests via `./gradlew test` and `./gradlew compileDebugKotlin`.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/EventAttendanceTest.kt`

## 5. Architectural Divergences & Discoveries
* Added localized Russian accessibility content descriptions for attendance status icons in `values-ru/strings.xml`.
* Leveraged existing `SquircleShape` from `com.eysamarin.squadplay.ui.squircle.*` with high corner smoothing to ensure visual parity with profile avatars.

## 6. Resulting Commits
* `da7d12d` - `[event-details-group-members] feat(event): display group members and attendance responses on event details screen`
