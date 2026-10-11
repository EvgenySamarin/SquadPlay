# Brief: Integrate SteamGridDB for Game Cover Thumbnails

* **Commit Title**: `[steamgriddb-game-thumbnails] feat(event): integrate SteamGridDB for game cover thumbnails`
* **Domain**: `event`
* **Parent**: `event/event-details-obsolete-hide-actions`
* **Deprecates**: `event/rawg-game-parser`

## Architectural Invariants & Constraints
* Game cover art fetching queries SteamGridDB API (`https://www.steamgriddb.com/api/v2`) using `BuildConfig.STEAMGRIDDB_API_KEY`.
* Network requests use standard `HttpURLConnection` with custom `User-Agent: SquadPlay/1.0` and `Authorization: Bearer <apiKey>` headers to satisfy edge protection.
* JSON parsing uses `kotlinx.serialization` with `Json { ignoreUnknownKeys = true }`.
* Grid requests prioritize vertical aspect ratios (`dimensions=600x900,342x482,660x930&nsfw=false`) to fit `EventItem`'s 3:4 card proportions and prevent displaying random gameplay screenshots.
* Graceful fallback: when SteamGridDB yields no matching covers, `GameRepositoryImpl` falls back to `RawgDataSource` (if configured) or returns null.
* Public contracts (`GameRepository.getGameThumbnailUrl`, `GameProvider`) remain intact without UI regressions.

## Affected Capabilities & Side Effects
* **Behavior**: When users enter a game title while scheduling a new event, the application retrieves vertical box art and branded covers from SteamGridDB instead of arbitrary gameplay screenshots.
* **Contract Adjustments**:
  - Added `SteamGridDbDataSource` in the `data` module.
  - Updated `GameRepositoryImpl` constructor to accept `SteamGridDbDataSource` and optional `RawgDataSource`.
* **Hotspots**: None.
