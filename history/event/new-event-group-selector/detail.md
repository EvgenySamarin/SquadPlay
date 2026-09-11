# Detail: Allow selecting group when creating new event

* **Target Commit Title**: `feat(event): allow selecting group when creating new event`

## 1. Intent & Architectural Trade-offs
Previously, `NewEventScreenViewModel.onEventSaveTap` hardcoded `groupId = currentUser.groups.first().uid`. For users belonging to multiple squads, this completely prevented creating events for any squad other than the first one.

To resolve this limitation:
1. `NewEventScreenModels.kt` was updated to include `userGroups: List<Group> = emptyList()` in `NewEventScreenUI` and `val groupId: String` in `NewEventScreenAction.OnEventSaveTap`.
2. `NewEventScreenViewModel` now re-emits UI state whenever `userInfoState` emits updated user info, propagating `user.groups` into the UI.
3. `onEventSaveTap` accepts `groupId: String`, falling back to `currentUser.groups.firstOrNull()?.uid` if blank, and safely notifies the user via snackbar (`stringProvider.youHaveNoSquad`) if the user has no squads.
4. In `NewEventScreen.kt`, an `ExposedDropdownMenuBox` is rendered above the "Game Title" text field. It manages local selection state initialized and synced to `state.data.userGroups.firstOrNull()`, allowing the user to select the desired group from a dropdown menu.
5. Full unit test coverage was added in `NewEventScreenViewModelTest.kt` verifying UI state population with groups, saving events with specific group IDs, empty squad handling, and navigation.

## 2. Detailed Contract & Schema Specifications
* **NewEventScreenAction.OnEventSaveTap (`NewEventScreenModels.kt`)**:
  ```kotlin
  data class OnEventSaveTap(
      val title: String,
      val timeFrom: LocalDateTime,
      val timeTo: LocalDateTime,
      val eventIconUrl: String?,
      val groupId: String,
  ) : NewEventScreenAction
  ```
* **NewEventScreenUI (`NewEventScreenModels.kt`)**:
  ```kotlin
  data class NewEventScreenUI(
      val title: String,
      val date: Date,
      val yearMonth: LocalDate,
      val gameTitle: String = "",
      val eventIconUrl: String? = null,
      val userGroups: List<Group> = emptyList(),
  )
  ```
* **String Resources (`strings.xml`)**:
  ```xml
  <string name="select_group">Select group</string>
  ```

## 3. Executed Plan
- [x] Step 1: In `NewEventScreenModels.kt`, add `groupId: String` to `NewEventScreenAction.OnEventSaveTap`, add `userGroups: List<Group> = emptyList()` to `NewEventScreenUI`, and update `PREVIEW_NEW_EVENT_SCREEN_UI`.
- [x] Step 2: In `strings.xml`, add string resource `select_group` for the group selector dropdown label.
- [x] Step 3: In `NewEventScreenViewModel.kt`, trigger `updateUiState` when `userInfoState` receives new data so `userGroups` is exposed in `uiState`, update `onEventSaveTap` to accept `groupId: String` parameter, use it when instantiating `Event`, and pass `action.groupId` from `NewEventScreenAction.OnEventSaveTap`.
- [x] Step 4: In `NewEventScreen.kt`, introduce an `ExposedDropdownMenuBox` dropdown above the "Game Title" text field displaying user group titles with single selection, maintaining selected group state (defaulting to the first group), and pass the selected `groupId` to `OnEventSaveTap`.
- [x] Step 5: Add unit tests in `NewEventScreenViewModelTest.kt` verifying UI state group population and event creation with selected `groupId`.
- [x] Step 6: Verify build and test suite execution via `./gradlew test` and `./gradlew compileDebugKotlin`.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/NewEventScreenModels.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `f2a9f65` - `[new-event-group-selector] feat(event): allow selecting group when creating new event`
