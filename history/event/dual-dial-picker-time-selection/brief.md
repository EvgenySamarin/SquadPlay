# Brief: Dual dial picker for new event time selection

* **Commit Title**: `refactor(event): use dual dial pickers for new event time selection`
* **Domain**: `event`
* **Parent**: `event/rawg-game-parser`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Time selection for event start and end times directly exposes separate, preselected dial pickers ("From" and "To"), removing the previous tap-to-switch interaction.
* Redundant time validation strings and checks are eliminated since pickers default to valid current time selections.
* Reusable `TimePicker` widget encapsulates the card presentation, label, and `PickHourMinute` dial picker.

## Affected Capabilities & Side Effects
* **Behavior**: Event creators see two dedicated dial pickers side-by-side for start and end times with responsive layout and overnight scheduling hints.
* **Contract Adjustments**: Removed obsolete `SquadPlayTimePicker`, `DialPickerTarget` enum, and `TimePickerUI` model.
* **Hotspots**: None.
