# Detail: RAWG game parser on new event screen

* **Target Commit Title**: `feat(event): implement RAWG game parser on new event screen`

## 1. Intent & Architectural Trade-offs
Implemented a game search text field when scheduling a new event. Instead of introducing a heavy HTTP client library like Retrofit or Ktor (since it is only a single endpoint), `HttpURLConnection` was utilized in `RawgDataSource`. Data serialization relies on `kotlinx.serialization`, which required the plugin application in the `data` module. A debounce mechanism was implemented in the `NewEventScreenViewModel` to prevent spamming the RAWG API while typing.

## 2. Detailed Contract & Schema Specifications
* Added `GameRepository` interface and `GameProvider`.
* Updated `FirebaseFirestoreDataSource` to persist and retrieve the `eventIconUrl` field within the `Event` schema.

## 3. Executed Plan
- [x] Step 1: Create a service (`RawgDataSource`) in the data module to query the RAWG API `https://api.rawg.io/api/games?search={title}&key={key}`.
- [x] Step 2: Inject `RawgDataSource`, `GameRepository`, and `GameProvider` into Koin (`SquadPlayApplication.kt`).
- [x] Step 3: Add `OnGameTitleChanged(title: String)` to `NewEventScreenAction` and handle debounce searching in `NewEventScreenViewModel`.
- [x] Step 4: Add `gameTitle: String` and `eventIconUrl: String?` to `NewEventScreenUI`.
- [x] Step 5: Update `NewEventScreen.kt` to include an OutlinedTextField for the game title and an AsyncImage (using Coil) for the thumbnail if the URL is not null.
- [x] Step 6: Update `Event` saving/retrieving logic to include `eventIconUrl` in Firestore data source.
- [x] Step 7: Update `local.properties` handling of `RAWG_API_KEY` in `app/build.gradle.kts`.

## 4. Touched Files
* `app/build.gradle.kts`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreen.kt`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/event/NewEventScreenViewModel.kt`
* `contract/src/main/java/com/eysamarin/squadplay/contracts/GameRepository.kt`
* `data/build.gradle.kts`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/GameRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/FirebaseFirestoreDataSource.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/RawgDataSource.kt`
* `domain/src/main/java/com/eysamarin/squadplay/domain/game/GameProvider.kt`
* `models/src/main/kotlin/com/eysamarin/squadplay/models/NewEventScreenModels.kt`

## 5. Architectural Divergences & Discoveries
* `kotlinx.serialization` plugin and runtime had to be specifically applied in the `data` module because it wasn't transitively accessible for `Json` parsing.
* A bug occurred when using Kotlin string interpolation `$` syntax inside `replace_file_content` leading to an escaped variable reference which had to be amended.

## 6. Resulting Commits
* `a4b42e1` - `[game-thumbnails-parsing] feat(event): implement RAWG game parser on new event screen`
