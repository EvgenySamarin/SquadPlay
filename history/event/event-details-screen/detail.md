# Detail: Event Details Screen Overview

* **Target Commit Title**: `feat(event): introduce EventDetailsScreen for selected event overview`

## 1. Intent & Architectural Trade-offs
To provide users with a dedicated overview of a scheduled event, `EventDetailsScreen` was added to the `HomeGraph`. To maintain visual and architectural parity with `NewEventScreen`, the hero image header with transparent `TopAppBar` was factored into a reusable `ImageTopBar` component in `com.eysamarin.squadplay.ui`. Passing primitive fields (`title`, `date`, `imageUrl`) through `Destination.EventDetailsScreen` avoids redundant network queries or loading flickers upon navigation while keeping the destination contract decoupled from database entities.

## 2. Detailed Contract & Schema Specifications
* **Navigation Destination**:
  ```kotlin
  @Serializable
  data class EventDetailsScreen(
      val title: String,
      val date: String,
      val imageUrl: String? = null,
  ) : Destination
  ```
* **HomeScreen Action**:
  ```kotlin
  class OnEventTap(val event: EventUI) : HomeScreenAction
  ```
* **Reusable UI Component**:
  ```kotlin
  @Composable
  fun ImageTopBar(
      imageUrl: String?,
      onBackTap: () -> Unit,
      modifier: Modifier = Modifier,
      headerHeight: Dp = 220.dp,
      contentDescription: String? = "Game Thumbnail",
  )
  ```

## 3. Executed Plan
- [x] Step 1: Define UI models and actions in `EventDetailsScreenModels.kt` and `HomeScreenAction.OnEventTap` in `MainScreenModels.kt`.
- [x] Step 2: Add `Destination.EventDetailsScreen` to `NavigationModels.kt`.
- [x] Step 3: Add string resources in `strings.xml` and `values-ru/strings.xml` for event details title and labels.
- [x] Step 4: Implement `EventDetailsScreenViewModel` to handle screen lifecycle and back navigation.
- [x] Step 5: Extract common `ImageTopBar` composable into `app/src/main/kotlin/com/eysamarin/squadplay/ui/ImageTopBar.kt` and reuse in both `NewEventScreen` and `EventDetailsScreen`.
- [x] Step 6: Register `EventDetailsScreenViewModel` in `SquadPlayApplication.kt` and add route in `SquadPlayNavigation.kt`.
- [x] Step 7: Update `HomeScreen.kt` to bind `onClick` on event items to trigger `HomeScreenAction.OnEventTap`.
- [x] Step 8: Update `HomeScreenViewModel.kt` to handle `OnEventTap` and navigate to `Destination.EventDetailsScreen`.
- [x] Step 9: Add unit test in `HomeScreenEventNavigationTest.kt` verifying event tap triggers navigation to `EventDetailsScreen`.
- [x] Step 10: Verify unit tests and build via Gradle.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/EventDetailsScreenModels.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/NavigationModels.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/EventDetailsScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/ImageTopBar.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/navigation/SquadPlayNavigation.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`

## 5. Architectural Divergences & Discoveries
* Code duplication between `NewEventScreen` and `EventDetailsScreen` was identified during review, prompting extraction of `ImageTopBar` into `com.eysamarin.squadplay.ui` for shared use across event screens.
* Localized strings (`event_details_screen_title`, `event_details_date_label`) added to both default and Russian resource catalogs (`values-ru`).

## 6. Resulting Commits
* `7049320` - `[settings-app-version] feat(event): introduce EventDetailsScreen for selected event overview`
