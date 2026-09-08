# Detail: Dual dial picker for new event time selection

* **Target Commit Title**: `refactor(event): use dual dial pickers for new event time selection`

## 1. Intent & Architectural Trade-offs
Previously, the New Event screen relied on `SquadPlayTimePicker` which required creators to tap between "From" and "To" to adjust times. To streamline the creation flow, the screen was refactored to show two distinct time pickers side-by-side. The intermediate `DialPicker` and `SquadPlayTimePicker` were consolidated into a single reusable `TimePicker` composable widget. Redundant `DialPickerTarget` enum and `TimePickerUI` models were removed, and validation errors for unset times were eliminated since dial pickers pre-populate with the current time.

## 2. Detailed Contract & Schema Specifications
* Removed `SquadPlayTimePicker` and deleted `DialPicker.kt`, combining dial picking logic directly into `TimePicker.kt`.
* Removed `DialPickerTarget` enum from `MainScreenModels.kt`.
* Removed `TimePickerUI` and `PREVIEW_TIME_PICKER_UI` from models.
* Removed unused string resources `time_from_not_set` and `time_to_not_set`.
* Added `modifier` parameter support to `PickHourMinute` in the `picktime` module.

## 3. Executed Plan
- [x] Step 1: Refactor `NewEventScreenMediumLayout` in `NewEventScreen.kt`:
  - Add the "Select time" header (`R.string.select_time`).
  - Move the overnight scheduling warning (`R.string.from_to_time_warning`) under the "Select time" header.
  - Clean up unused state/references (`timePickerUI`, `dialPickerTarget`, `errorText`).
- [x] Step 2: Replace `SquadPlayTimePicker` in `TimePicker.kt` with a single reusable `TimePicker` composable widget encapsulating the Card, title label, and dial time picking logic.
- [x] Step 3: Remove `DialPicker.kt`, combining its logic directly into `TimePicker.kt`.
- [x] Step 4: Remove obsolete `DialPickerTarget` enum and unused `TimePickerUI` from models, simplifying the `TimePicker` signature.
- [x] Step 5: Remove redundant `time_from_not_set` and `time_to_not_set` error strings from resource files and simplify submit button handler.
- [x] Step 6: Reuse `TimePicker` in `NewEventScreenMediumLayout` for both "From" and "To" time selections.
- [x] Step 7: Run `./gradlew compileDebugKotlin` and `./gradlew testDebugUnitTest` to verify that the project builds cleanly and all tests pass.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/TimePicker.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/DialPicker.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/NewEventScreenModels.kt`
* `picktime/src/main/java/com/anhaki/picktime/PickHourMinute.kt`
* `picktime/src/main/java/com/anhaki/picktime/components/GenericPickTime.kt`

## 5. Architectural Divergences & Discoveries
* During development, the separate `DialPicker` abstraction was identified as redundant and combined directly into `TimePicker`, reducing unnecessary indirection.
* The layout was refined using `LazyColumn` for improved scrolling and vertical rhythm.

## 6. Resulting Commits
* `d4a417c` - `[trunk] refactor(event): use dual dial pickers for new event time selection`
