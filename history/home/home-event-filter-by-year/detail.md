# Detail: Filter events by year on selected date and optimize EventUI mapping

* **Target Commit Title**: `fix(home): filter events by year on selected date and optimize EventUI mapping`

## 1. Intent & Architectural Trade-offs
Previously, both `HomeScreenViewModel` and `CalendarUIProvider` evaluated events matching calendar dates by comparing only `dayOfMonth` and `monthNumber`, omitting the year check. When a user created an event in a given year and subsequently navigated to another year (past or future) on the calendar, any event matching that same day and month was erroneously displayed.

To address this bug while avoiding breaking changes across serialization and API contracts:
1. `Date` model was extended with an optional `year: Int? = null` property with a default value of `null`, maintaining full backwards compatibility with existing positional and named callers.
2. `CalendarDataSource.getDates()` populates `year = date.year` from each `LocalDate`.
3. `CalendarUIProviderImpl.mergedCalendarWithEvents()` was refactored to pre-index events by `Triple(year, month, day)` in a single pass ($O(M)$), reducing calendar date lookup complexity from $O(N \times M)$ with repeated list allocations to $O(N + M)$ with $O(1)$ lookups and year matching.
4. In `HomeScreenViewModel`, event-to-UI mapping was extracted into a dedicated `getEventsBySelectedDate()` helper with early-exit guard clauses. A single-pass `mapNotNull` transforms matching events into `EventUI` instances while matching `fromDate.year == selectedYear`. In addition, `formatTime()` was optimized to eliminate `String.format` locale overhead in favor of fast zero-padded string interpolation.

## 2. Detailed Contract & Schema Specifications
* **Date Model (`MainScreenModels.kt`)**:
  ```kotlin
  @Serializable
  data class Date(
      val dayOfMonth: Int? = null,
      val monthNumber: Int? = null,
      val countEvents: Int,
      val isSelected: Boolean,
      val enabled: Boolean,
      val hasUserEvents: Boolean = false,
      val year: Int? = null,
  )
  ```
* **Event Aggregation Pre-indexing (`CalendarUIProvider.kt`)**:
  ```kotlin
  data class DateEventSummary(val count: Int, val hasUserEvents: Boolean)
  val eventsByDate = mutableMapOf<Triple<Int, Int, Int>, DateEventSummary>()
  for (event in events) {
      val key = Triple(event.fromDateTime.year, event.fromDateTime.month.number, event.fromDateTime.day)
      val current = eventsByDate[key]
      val isUserEvent = event.creatorId == currentUserId
      eventsByDate[key] = if (current == null) {
          DateEventSummary(count = 1, hasUserEvents = isUserEvent)
      } else {
          DateEventSummary(
              count = current.count + 1,
              hasUserEvents = current.hasUserEvents || isUserEvent
          )
      }
  }
  ```
* **Selected Date Event Filtering (`HomeScreenViewModel.kt`)**:
  ```kotlin
  private fun getEventsBySelectedDate(
      events: List<Event>,
      selectedDate: Date?,
      calendarYear: Int,
      currentUserId: String,
  ): List<EventUI> {
      if (selectedDate == null || events.isEmpty()) return emptyList()
      val selectedDay = selectedDate.dayOfMonth ?: return emptyList()
      val selectedMonth = selectedDate.monthNumber ?: return emptyList()
      val selectedYear = selectedDate.year ?: calendarYear

      return events.mapNotNull { event ->
          val fromDate = event.fromDateTime
          if (fromDate.day == selectedDay &&
              fromDate.month.number == selectedMonth &&
              fromDate.year == selectedYear
          ) {
              EventUI(
                  eventId = event.uid,
                  title = event.title,
                  subtitle = stringProvider.fromToDate(
                      fromDate = formatTime(fromDate.hour, fromDate.minute),
                      toDate = formatTime(event.toDateTime.hour, event.toDateTime.minute),
                  ),
                  iconUrl = event.eventIconUrl,
                  isYourEvent = event.creatorId == currentUserId
              )
          } else {
              null
          }
      }
  }
  ```

## 3. Executed Plan
- [x] Step 1: Update `Date` model with optional `val year: Int? = null` property in `MainScreenModels.kt`.
- [x] Step 2: Update `CalendarDataSource.getDates` and `CalendarUIProvider.mergedCalendarWithEvents` in `CalendarUIProvider.kt` to include `year` and match events against date year, optimizing event aggregation via grouped pre-indexing.
- [x] Step 3: In `HomeScreenViewModel.kt`, fix event filtering inside the `combine` block to compare event year against the selected date / calendar year, early-exit if no date is selected, combine filter and map into a single pass (`mapNotNull`), and optimize `formatTime` string construction.
- [x] Step 4: Add comprehensive unit tests in `HomeScreenEventNavigationTest.kt` verifying year-based event filtering across month/year transitions and calendar merged events year matching.
- [x] Step 5: Run tests and verify build integrity via `./gradlew test`.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/calendar/CalendarUIProvider.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
* During implementation review, the event filtering block in `HomeScreenViewModel`'s `combine` collector was extracted into a dedicated private function `getEventsBySelectedDate()` with guard clauses and early returns to improve readability and testability.

## 6. Resulting Commits
* `ee11c9f` - `[home-event-filter-by-year] fix(home): filter events by year on selected date and optimize EventUI mapping`
* `c9d9c89` - `[home-event-filter-by-year] refactor(home): extract eventsBySelectedDate to helper with early returns`
