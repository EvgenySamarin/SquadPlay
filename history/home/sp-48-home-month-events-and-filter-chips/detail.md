# Detail: Support All-Month Events View and Filter Chips on HomeScreen

* **Target Commit Title**: `[NDK-Debug-Symbol-Level-DSL] feat(home): support all-month events view and filter chips on HomeScreen`

## 1. Intent & Architectural Trade-offs
* **All-Month Default View (`SP-48`)**: Previously, `CalendarDataSource.getDates` automatically selected today's date on the current month and returned an empty event list whenever `selectedDate == null` (such as when navigating to another month). Initializing dates with `isSelected = false` and returning all events matching `calendar.yearMonth.year` and `calendar.yearMonth.month.number` (sorted by `fromDateTime`) when `selectedDate == null` gives users an immediate overview of the entire month's schedule while preserving year-level isolation (`home-event-filter-by-year`) and FAB visibility rules (`home-fab-hide-on-past-date`, `sp-89-no-group-hide-fab-show-hint`).
* **Stitch Filter Chips Bar (`projects/17613910814499941770/screens/22994ce4c27a41dd8aa6b4ca1aae0aa8`)**: Implemented the Stitch `HomeScreen variant filter chips` pill bar between the calendar and the events list in both `HomeScreenMediumLayout` and `MainScreenExpandedLayout`:
  - `All Month` acts as the reset button to clear any selected day and reset the category filter to `HomeEventFilter.ALL`.
  - `Today • <Date>` provides one-tap navigation back to today's month and date.
  - `Squad Events`, `My Matches`, and `Confirmed` toggle category filtering orthogonal to whether a single date or the entire month is viewed.

## 2. Detailed Contract & Schema Specifications
* **`MainScreenModels.kt`**:
  - `enum class HomeEventFilter { ALL, SQUAD_EVENTS, MY_MATCHES, CONFIRMED }`
  - `HomeScreenUI`: added `val activeFilter: HomeEventFilter = HomeEventFilter.ALL`, `val isTodaySelected: Boolean = false`, `val todayDate: LocalDate = LocalDate(2026, 1, 1)`
  - `HomeScreenAction`: added `OnAllMonthTap`, `OnTodayTap`, `OnFilterTap(val filter: HomeEventFilter)`
* **`CalendarUIProvider.kt`**:
  - `fun clearSelectedDate(target: CalendarUI): CalendarUI = target.copy(dates = target.dates.map { it.copy(isSelected = false) })`
* **Resources Added**:
  - `app/src/main/res/drawable/ic_calendar_today_24.xml`
  - `filter_all_month`, `filter_today_format`, `filter_squad_events`, `filter_my_matches`, `filter_confirmed` in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.

## 3. Executed Plan
- [x] Step 1: Update `MainScreenModels.kt` to add `HomeEventFilter`, extend `HomeScreenUI` (`activeFilter`, `isTodaySelected`, `todayDate`), and add `OnAllMonthTap`, `OnTodayTap`, and `OnFilterTap` to `HomeScreenAction`.
- [x] Step 2: Update `CalendarDataSource.getDates` in `CalendarUIProvider.kt` so dates default to `isSelected = false`, and add `clearSelectedDate(target: CalendarUI)` to `CalendarUIProvider`.
- [x] Step 3: Add `ic_calendar_today_24.xml` drawable and localized filter chip strings in `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
- [x] Step 4: Update `HomeScreenViewModel.kt` to return all month events (sorted chronologically) when no date is selected, apply `HomeEventFilter`, and handle `OnAllMonthTap`, `OnTodayTap`, and `OnFilterTap`.
- [x] Step 5: Update `HomeScreen.kt` to render the Stitch horizontally scrollable filter chips bar (`All Month`, `Today • <Date>`, `Squad Events`, `My Matches`, `Confirmed`) in both medium and expanded layouts.
- [x] Step 6: Update and expand unit tests in `HomeScreenEventNavigationTest.kt` and verify all tests pass.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/calendar/CalendarUIProvider.kt`
* `app/src/main/res/drawable/ic_calendar_today_24.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
* Also added `EmptyContent` rendering inside `MainScreenExpandedLayout` when `gameEventsOnDate.isEmpty()` so tablet/expanded layouts behave consistently with phone/medium layouts when a filter or date selection yields no events.

## 6. Resulting Commits
* `1b5de3c` - `[NDK-Debug-Symbol-Level-DSL] feat(home): support all-month events view and filter chips on HomeScreen`
