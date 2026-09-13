# Detail: Implement EventItem composable component

* **Target Commit Title**: `feat(event): implement EventItem composable component`

## 1. Intent & Architectural Trade-offs
To provide a clear, modern event list item matching the specified design layout, `EventItem` was built as a standalone Composable. It incorporates game artwork, title typography, scheduled time range, response status badge, and small participant avatars with overlaid attendance status icons.

## 2. Detailed Contract & Schema Specifications
* `EventItem`:
  ```kotlin
  @Composable
  fun EventItem(
      event: EventUI,
      modifier: Modifier = Modifier,
      members: List<EventMemberUI> = emptyList(),
      maxVisibleAvatars: Int = 4,
      onDetailsTap: () -> Unit = {},
  )
  ```

## 3. Executed Plan
- [x] Step 1: Design `EventItemCard` layout in `app/src/main/kotlin/com/eysamarin/squadplay/ui/EventItem.kt`.
- [x] Step 2: Implement member avatar row with small status badge overlays and `+N` overflow indicator.
- [x] Step 3: Add `@DarkLightModePreview` previews for `EventItem`.
- [x] Step 4: Integrate `EventItem` into `HomeScreen.kt`.
- [x] Step 5: Verify build and unit/UI tests.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/ui/EventItem.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`

## 5. Architectural Divergences & Discoveries
* Property access on `event.subtitle` requires local variable assignment to prevent Kotlin smart-cast restrictions when referencing multi-module data class properties.

## 6. Resulting Commits
* `8797fa2` - `[trunk] feat(event): implement EventItem composable component`
