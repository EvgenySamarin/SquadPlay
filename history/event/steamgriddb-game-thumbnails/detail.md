# Detail: Integrate SteamGridDB for Game Cover Thumbnails

* **Target Commit Title**: `feat(event): integrate SteamGridDB for game cover thumbnails`

## 1. Intent & Architectural Trade-offs
Previously, game thumbnails were resolved exclusively via RAWG API's `background_image` field. Because RAWG does not maintain official box art or cover logos, `background_image` frequently resolved to user-uploaded gameplay screenshots, resulting in cropped in-game visuals on event cards.
SteamGridDB provides curated, high-quality game cover art and vertical grids. We introduced `SteamGridDbDataSource` to search games via autocomplete and fetch non-NSFW vertical covers (600x900 / 342x482 / 660x930) as thumbnails, while retaining `RawgDataSource` as a secondary fallback. The implementation retains `HttpURLConnection` to avoid heavy network libraries and adheres to dependency injection via Koin.

## 2. Detailed Contract & Schema Specifications
* Added `SteamGridDbDataSource` with method `suspend fun getGameCoverThumbnail(gameTitle: String): String?`.
* Updated `GameRepositoryImpl(steamGridDbDataSource: SteamGridDbDataSource, rawgDataSource: RawgDataSource? = null)`.
* Registered `SteamGridDbDataSource` and updated `GameRepository` bindings in `SquadPlayApplication.kt`.
* Maintained `BuildConfig.STEAMGRIDDB_API_KEY` in `app/build.gradle.kts` and GitHub Actions CI workflow.

## 3. Executed Plan
- [x] Step 1: Confirm `STEAMGRIDDB_API_KEY` in `app/build.gradle.kts` and `.github/workflows/android_cicd.yml`.
- [x] Step 2: Implement `SteamGridDbDataSource` with autocomplete search and vertical grid retrieval.
- [x] Step 3: Update `GameRepositoryImpl` to query `SteamGridDbDataSource` with `RawgDataSource` fallback.
- [x] Step 4: Register `SteamGridDbDataSource` in Koin inside `SquadPlayApplication.kt`.
- [x] Step 5: Add unit tests in `SteamGridDbDataSourceTest.kt` verifying payload parsing, dimensions filtering, and repository fallback.
- [x] Step 6: Verify Gradle compilation and test suite (`./gradlew testDebugUnitTest`).

## 4. Touched Files
* `.github/workflows/android_cicd.yml`
* `app/build.gradle.kts`
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayApplication.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/GameRepositoryImpl.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/datasource/SteamGridDbDataSource.kt`
* `data/src/test/kotlin/com/eysamarin/squadplay/data/datasource/SteamGridDbDataSourceTest.kt`

## 5. Architectural Divergences & Discoveries
* SteamGridDB Cloudflare edge security requires an explicit `User-Agent` header (e.g. `SquadPlay/1.0`) along with `Authorization: Bearer <apiKey>` to avoid HTTP 403 Forbidden.
* SteamGridDB's `thumb` URL provides a lightweight 300x450 JPG (~60KB) optimized for mobile list rendering compared to full ~800KB PNG grids.

## 6. Resulting Commits
* `20541b8` - `[steamgriddb-game-thumbnails] feat(event): integrate SteamGridDB for game cover thumbnails`
