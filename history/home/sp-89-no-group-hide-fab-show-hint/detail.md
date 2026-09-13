# Detail: Hide new event button and show squad hint when user has no groups

* **Target Commit Title**: `feat(home): hide new event button and show squad hint when user has no groups`

## 1. Intent & Architectural Trade-offs
Previously, the `HomeScreen` floating action button ("New Game Event") was displayed whenever a future date was selected (`selectedLocalDate >= today`), regardless of whether the user belonged to any squads. If a user left all groups or had not joined any squads, attempting to create an event was confusing because event scheduling requires a target squad.

To resolve this:
1. `HomeScreenViewModel` now considers `userInfo.groups.isNotEmpty()` in addition to calendar date enablement and future date constraints when setting `isCreateEventButtonVisible`.
2. `HomeScreenViewModel.onAddGameEventTap()` now guards against users with empty groups, logging a warning and returning early to prevent invalid navigation.
3. In `HomeScreen`, when `state.data.user.groups.isEmpty()`, a gamer-oriented explanation banner is displayed directly beneath the `Calendar` component in both medium and expanded layouts:
   - "Ready to squad up? Create or join a gaming squad to start scheduling game sessions!"
   - Styled with `DesignSystemTheme.extendedColors.orange` and `DesignSystemTheme.typography.bodyMedium`, centered horizontally.
4. Unit tests were expanded in `HomeScreenEventNavigationTest` to verify that `isCreateEventButtonVisible` remains false when `groups` is empty even on future dates, and that `onAddGameEventTap` prevents navigation.

## 2. Detailed Contract & Schema Specifications
* `app/src/main/res/values/strings.xml`:
  - Added `<string name="no_squad_create_event_hint">Ready to squad up? Create or join a gaming squad to start scheduling game sessions!</string>`
* `app/src/main/res/values-ru/strings.xml`:
  - Added `<string name="no_squad_create_event_hint">Готовы к игре? Создайте или вступите в отряд, чтобы планировать игровые сессии!</string>`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`:
  - `isCreateEventButtonVisible = if (userInfo.groups.isNotEmpty() && selectedDate != null && dayOfMonth != null && selectedDate.enabled)`
  - Guard in `onAddGameEventTap()`:
    ```kotlin
    val currentUser = userInfoState.value
    if (currentUser == null || currentUser.groups.isEmpty()) {
        logger.w { "User has no groups, cannot add game event" }
        return@launch
    }
    ```

## 3. Executed Plan
- [x] Step 1: Add `no_squad_create_event_hint` string resource to `app/src/main/res/values/strings.xml` and Russian localization to `app/src/main/res/values-ru/strings.xml`.
- [x] Step 2: Update `HomeScreenViewModel` to evaluate `isCreateEventButtonVisible` to `false` when `userInfo.groups.isEmpty()`, and guard `onAddGameEventTap()` against empty groups.
- [x] Step 3: In `HomeScreen.kt`, render the gamer-oriented explanation text directly under the `Calendar` component in both `HomeScreenMediumLayout` and `MainScreenExpandedLayout` when `state.data.user.groups.isEmpty()`, styled with `DesignSystemTheme.extendedColors.orange` and centered text alignment.
- [x] Step 4: Add unit tests in `HomeScreenEventNavigationTest.kt` verifying that `isCreateEventButtonVisible` is false when a user has no groups, and that tapping add event when having no groups does not navigate.
- [x] Step 5: Verify build, compilation, and unit tests via `./gradlew compileDebugKotlin test`.

## 4. Touched Files
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
* `FakeProfileProvider` default user in `HomeScreenEventNavigationTest` previously had `groups = emptyList()`. Because older tests verified that the FAB was visible on future dates, the fake default was updated to contain a default squad (`group-1`), while tests specific to empty groups explicitly override with `groups = emptyList()`.

## 6. Resulting Commits
* `642a8f0` - `[SP-89] feat(home): hide new event button and show squad hint when user has no groups`
