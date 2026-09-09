# Detail: Hide new game event button when past date is selected

* **Target Commit Title**: `feat(home): hide new game event button when past date is selected`

## 1. Intent & Architectural Trade-offs
To prevent users from inadvertently initiating or creating game events in the past, UI affordances must align with domain validation rules. Hiding the `ExtendedFloatingActionButton` on `HomeScreen` when a past calendar date is selected ensures clear feedback and eliminates impossible user actions. Evaluating this condition in `HomeScreenViewModel` keeps the screen composable declarative, while wrapping the button in `AnimatedVisibility` provides smooth transitions when switching between past and future dates without breaking the existing list scroll collapse behavior (`expanded = !isScrolling`). A defensive guard in `onAddGameEventTap()` ensures past date navigation cannot occur even under race conditions.

## 2. Detailed Contract & Schema Specifications
* **Models**:
  ```kotlin
  data class HomeScreenUI(
      val user: User,
      val calendarUI: CalendarUI,
      val gameEventsOnDate: List<EventUI> = emptyList(),
      val isCreateEventButtonVisible: Boolean = true,
  )
  ```
* **ViewModel State Computation**:
  ```kotlin
  val today = todayProvider()
  val dayOfMonth = selectedDate?.dayOfMonth
  val isCreateEventButtonVisible = if (selectedDate != null && dayOfMonth != null && selectedDate.enabled) {
      val selectedLocalDate = LocalDate(
          year = eventBasedCalendar.yearMonth.year,
          monthNumber = selectedDate.monthNumber ?: eventBasedCalendar.yearMonth.month.number,
          dayOfMonth = dayOfMonth,
      )
      selectedLocalDate >= today
  } else {
      false
  }
  ```
* **UI Integration**:
  ```kotlin
  floatingActionButton = {
      AnimatedVisibility(
          visible = (state as? UiState.Normal)?.data?.isCreateEventButtonVisible == true,
          enter = fadeIn() + scaleIn(),
          exit = fadeOut() + scaleOut(),
      ) {
          ExtendedFloatingActionButton(...)
      }
  }
  ```

## 3. Executed Plan
- [x] Step 1: Add `val isCreateEventButtonVisible: Boolean = true` property to `HomeScreenUI` in `MainScreenModels.kt`.
- [x] Step 2: In `HomeScreenViewModel.kt`, compute `isCreateEventButtonVisible` based on whether the selected calendar date resolves to a `LocalDate` greater than or equal to current date (today). Guard `onAddGameEventTap()` against dates before today.
- [x] Step 3: In `HomeScreen.kt`, wrap `ExtendedFloatingActionButton` in `AnimatedVisibility` conditioned on `(state as? UiState.Normal)?.data?.isCreateEventButtonVisible == true` so the button animates out/in when past vs current/future dates are selected.
- [x] Step 4: Add unit tests in `HomeScreenEventNavigationTest.kt` covering past date selection (hiding button, blocking navigation) and current/future date selection.
- [x] Step 5: Verify build and run all unit tests via `./gradlew testDebugUnitTest`.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
* In `HomeScreenViewModel`, smart casting `selectedDate.dayOfMonth` required extracting it to a local variable because `Date` is defined in an external module (`:models`).
* To keep Koin's `viewModelOf(::HomeScreenViewModel)` reflection resolution unambiguous while enabling deterministic unit tests without background thread race conditions, test dispatcher and date providers were configured via companion object defaults (`defaultIoDispatcher` and `defaultTodayProvider`).

## 6. Resulting Commits
* `26638b6` - `[settings-app-version] feat(home): hide new game event button when past date is selected`
