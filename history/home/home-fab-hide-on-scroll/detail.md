# Detail: Collapse floating action button on scroll

* **Target Commit Title**: `fix(home): collapse floating action button on scroll`

## 1. Intent & Architectural Trade-offs
* **Initial Intent**: The floating action button on HomeScreen overlapped the bottom-most list items in `LazyColumn`. Hiding the button completely was initially implemented, but collapsing the `ExtendedFloatingActionButton` to a regular small action button via its native `expanded = !isScrolling` parameter provides a superior user experience by keeping the primary action accessible while minimizing visual obstruction during scrolling.
* **Padding Strategy**: Bottom padding (`PaddingValues(bottom = 80.dp)`) was introduced directly into `LazyColumn` across medium and expanded layouts so the final item is never obscured when the FAB is expanded at rest.

## 2. Detailed Contract & Schema Specifications
* None. Internal UI state and layout adjustments within `HomeScreen.kt`.

## 3. Executed Plan
- [x] Step 1: Replace `AnimatedVisibility` around `ExtendedFloatingActionButton` with direct `ExtendedFloatingActionButton(text = { ... }, icon = { ... }, expanded = !isScrolling, ...)`.
- [x] Step 2: Remove unused animation imports (`AnimatedVisibility`, `fadeIn`, `fadeOut`, `slideInVertically`, `slideOutVertically`).
- [x] Step 3: Verify build with `./gradlew compileDebugKotlin` and tests with `./gradlew testDebugUnitTest`.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`

## 5. Architectural Divergences & Discoveries
* Initially implemented complete hiding of the FAB via `AnimatedVisibility`, then refined based on design feedback to use `ExtendedFloatingActionButton`'s built-in collapse animation (`expanded = !isScrolling`), maintaining access to the action while scrolling.

## 6. Resulting Commits
* `74d5918` - `[settings-app-version] fix(home): hide floating action button during scroll`
* `4ebdb9b` - `[settings-app-version] fix(home): collapse floating action button on scroll`
