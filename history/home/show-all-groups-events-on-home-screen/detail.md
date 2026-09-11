# Detail: Display events from all user groups with group overline

* **Target Commit Title**: `feat(home): display events from all user groups with group overline`

## 1. Intent & Architectural Trade-offs
Previously, `HomeScreenViewModel` selected only the user's first squad (`user.groups.firstOrNull()`), preventing users belonging to multiple squads from viewing all their scheduled events on the home calendar and daily event list.

To resolve this limitation:
1. `HomeScreenViewModel` now extracts all squad IDs from the user profile as a set (`Set<String>`) and monitors them with `distinctUntilChanged()`. Using a set guarantees order-independent equality, avoiding superfluous Firestore snapshot listener rebuilds when squads arrive in arbitrary order.
2. `EventProvider`, `EventRepository`, and `FirebaseFirestoreDataSource` interfaces were streamlined to accept `groupIds: Set<String>`. Unused single-group overloads were pruned completely.
3. `FirebaseFirestoreDataSourceImpl` guards against empty inputs by returning `flowOf(emptyList())` early (preventing Firestore `IllegalArgumentException` on empty `whereIn` queries) and automatically chunks sets exceeding 30 groups into parallel listeners merged with `combine`.
4. `EventUI` was enriched with `groupTitle: String?`, mapped via `user.groups.associateBy { it.uid }` in `HomeScreenViewModel.getEventsBySelectedDate`, and rendered into `DSListItem(overline = item.groupTitle)` in `HomeScreen`.

## 2. Detailed Contract & Schema Specifications
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`:
  - Added `val groupTitle: String? = null` to `data class EventUI`.
* `contract/src/main/java/com/eysamarin/squadplay/contracts/EventRepository.kt`:
  - `fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>`
* `domain/src/main/java/com/eysamarin/squadplay/domain/event/EventProvider.kt`:
  - `fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`:
  - `fun getEventsFlow(groupIds: Set<String>): Flow<List<Event>>`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`:
  - Implements `getEventsFlow(groupIds: Set<String>)` delegating to datasource.

## 3. Executed Plan
- [x] Step 1: Add groupTitle property to EventUI in MainScreenModels.kt.
- [x] Step 2: Update EventRepository, EventProvider, and FirebaseFirestoreDataSource to accept groupIds: Set<String> and remove unused single groupId method.
- [x] Step 3: Implement getEventsFlow(groupIds: Set<String>) in FirebaseFirestoreDataSourceImpl with empty check and 30-item chunking; update EventRepositoryImpl and EventProviderImpl.
- [x] Step 4: Map user groups to Set<String> in HomeScreenViewModel, collect all groups' events, and populate EventUI.groupTitle from matching Group.title.
- [x] Step 5: Render overline = item.groupTitle in DSListItem on HomeScreen (both medium and expanded layouts).
- [x] Step 6: Update test fakes in HomeScreenEventNavigationTest, LogoutLoadingTest, and NewEventScreenViewModelTest.
- [x] Step 7: Add unit tests verifying multi-group fetching, group title overline, empty squad handling, and group reordering idempotence.
- [x] Step 8: Verify build and test suite execution via ./gradlew test.

## 4. Touched Files
* `models/src/main/kotlin/com/eysamarin/squadplay/models/MainScreenModels.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/EventRepository.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/event/EventProvider.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/EventRepositoryImpl.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreenViewModel.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/main/HomeScreen.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/HomeScreenEventNavigationTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/NewEventScreenViewModelTest.kt`

## 5. Architectural Divergences & Discoveries
* Switched `EventUI` overline text property name to `groupTitle` to separate semantic domain modeling from UI component parameter naming.
* Adopted `Set<String>` instead of `List<String>` across all query interfaces so that `distinctUntilChanged()` evaluates order-agnostic equality, preventing redundant query re-executions when Firestore group lists change order.
* Dropped legacy single `groupId` query function entirely after codebase analysis confirmed no other callers existed.

## 6. Resulting Commits
* `2560ece` - `[show-all-groups-events-on-home-screen] feat(home): display events from all user groups with group overline`
