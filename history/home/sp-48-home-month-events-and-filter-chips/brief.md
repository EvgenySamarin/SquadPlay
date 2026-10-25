# Brief: Support All-Month Events View and Filter Chips on HomeScreen

* **Commit Title**: `[NDK-Debug-Symbol-Level-DSL] feat(home): support all-month events view and filter chips on HomeScreen`
* **Domain**: `home`
* **Parent**: `home/empty-content-stitch-redesign`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `CalendarDataSource.getDates(yearMonth)` initializes all `Date` entries with `isSelected = false` by default when opening `HomeScreen` or navigating between months (`OnPrevMonthTap` / `OnNextMonthTap`).
* When no calendar day is selected (`selectedDate == null`), `HomeScreenViewModel` displays all events belonging to `calendar.yearMonth.year` and `calendar.yearMonth.month.number`, sorted chronologically by `fromDateTime`.
* `isCreateEventButtonVisible` in `HomeScreenUI` remains `true` if and only if `userInfo.groups.isNotEmpty()`, an enabled calendar date is selected (`selectedDate != null`), and `selectedLocalDate >= today`.
* `HomeScreen` renders a horizontally scrollable filter chips bar (`EventFilterChipsRow`) matching the Stitch `HomeScreen variant filter chips` design with 5 pills: `All Month` (clears selected day and resets category filter to `HomeEventFilter.ALL`), `Today • <Date>` (jumps to today's month and selects today's date), `Squad Events` (`!event.isYourEvent`), `My Matches` (`event.isYourEvent`), and `Confirmed` (`event.members.isNotEmpty() && event.members.all { it.status == EventResponseStatus.ACCEPTED }`).

## Affected Capabilities & Side Effects
* **Behavior**: Displays all events for the active calendar month chronologically when no day is selected and provides Stitch filter chips (`All Month`, `Today`, `Squad Events`, `My Matches`, `Confirmed`) to reset/select dates or filter events on `HomeScreen`.
* **Contract Adjustments**: Added `HomeEventFilter` enum, extended `HomeScreenUI` (`activeFilter`, `isTodaySelected`, `todayDate`), added `OnAllMonthTap`, `OnTodayTap`, and `OnFilterTap` to `HomeScreenAction`, and added `clearSelectedDate(target: CalendarUI): CalendarUI` to `CalendarUIProvider`.
* **Hotspots**: `HomeScreen.kt`, `HomeScreenViewModel.kt`, `CalendarUIProvider.kt`, `MainScreenModels.kt`.
