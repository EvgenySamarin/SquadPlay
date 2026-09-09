# Detail: Move event deletion to EventDetailsScreen with confirmation dialog

* **Target Commit Title**: `feat(event): move event deletion to EventDetailsScreen with confirmation dialog`

## 1. Intent & Architectural Trade-offs
Previously, events could be deleted directly from the `HomeScreen` list via a trailing trash icon without any confirmation gate. This introduced the risk of accidental deletions. Consolidating deletion into `EventDetailsScreen` with a `ConfirmationDialog` ensures safe management while simplifying the `HomeScreen` event card layout.

## 2. Detailed Contract & Schema Specifications
* **Destination Update**:
  ```kotlin
  @Serializable
  data class EventDetailsScreen(
      val eventId: String,
      val title: String,
      val date: String,
      val imageUrl: String? = null,
      val isYourEvent: Boolean = false,
  ) : Destination
  ```
* **EventDetailsScreenUI Update**:
  ```kotlin
  data class EventDetailsScreenUI(
      val eventId: String,
      val title: String,
      val date: String,
      val imageUrl: String? = null,
      val isYourEvent: Boolean = false,
      val showDeleteConfirmation: Boolean = false,
  )
  ```
* **TopBar Extension**:
  Added `actions: @Composable RowScope.() -> Unit = {}` to `ImageTopBar` to render contextual actions in the transparent `TopAppBar`.

## 3. Executed Plan
- [x] Step 1: Update `EventDetailsScreenModels.kt` with `eventId`, `isYourEvent`, `showDeleteConfirmation`, and delete actions.
- [x] Step 2: Update `Destination.EventDetailsScreen` in `NavigationModels.kt` with `eventId` and `isYourEvent`.
- [x] Step 3: Add `actions: @Composable RowScope.() -> Unit = {}` to `ImageTopBar.kt`.
- [x] Step 4: Add string resources for delete confirmation dialog in `strings.xml` and `values-ru/strings.xml`.
- [x] Step 5: Update `EventDetailsScreenViewModel` to handle delete confirmation dialog and Firestore deletion.
- [x] Step 6: Update `EventDetailsScreen.kt` to show trash icon in `ImageTopBar` and display `ConfirmationDialog`.
- [x] Step 7: Update `SquadPlayNavigation.kt` to forward `eventId` and `isYourEvent`.
- [x] Step 8: Remove delete icon and handler from `HomeScreen.kt` and `HomeScreenViewModel.kt`.
- [x] Step 9: Update unit tests in `HomeScreenEventNavigationTest.kt`.
- [x] Step 10: Run Gradle unit tests to verify all tests pass.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/ImageTopBar.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `348062c` - `[settings-app-version] feat(event): move event deletion to EventDetailsScreen with confirmation dialog`
