# Detail: Display trailing star icon for user events on home screen

* **Target Commit Title**: `feat(home): display trailing star icon for user events on home screen`

## 1. Intent & Architectural Trade-offs
After moving event deletion controls into `EventDetailsScreen`, events in the home screen list had no visual differentiator indicating ownership. Adding a decorative star icon on `DSListItem` when `item.isYourEvent == true` restores clear visual feedback without introducing separate action handlers or intercepting navigation taps.

## 2. Detailed Contract & Schema Specifications
* **Vector Asset**: Added `ic_star_24.xml` (24dp Material Star vector drawable).
* **UI Integration**:
  ```kotlin
  trailingIconPainter = if (item.isYourEvent) {
      painterResource(R.drawable.ic_star_24)
  } else null,
  trailingIconContentDescription = if (item.isYourEvent) {
      stringResource(R.string.content_description_your_event)
  } else null,
  ```

## 3. Executed Plan
- [x] Step 1: Add vector asset `ic_star_24.xml` in `app/src/main/res/drawable/`.
- [x] Step 2: Add content description string `content_description_your_event` to `strings.xml` and `values-ru/strings.xml`.
- [x] Step 3: Update `HomeScreen.kt` in `HomeScreenMediumLayout` and `MainScreenExpandedLayout` to set `trailingIconPainter = if (item.isYourEvent) painterResource(R.drawable.ic_star_24) else null`.
- [x] Step 4: Verify build and existing unit tests via `./gradlew testDebugUnitTest`.

## 4. Touched Files
* `app/src/main/res/drawable/ic_star_24.xml`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `acfa80b` - `[settings-app-version] feat(home): display trailing star icon for user events on home screen`
