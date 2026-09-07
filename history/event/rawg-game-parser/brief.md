# Brief: RAWG game parser on new event screen

* **Commit Title**: `feat(event): implement RAWG game parser on new event screen`
* **Domain**: `event`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* The `RAWG` API key is fetched via `BuildConfig.RAWG_API_KEY` (must be defined in environment or `local.properties`).
* Raw URL fetching uses `HttpURLConnection` in `RawgDataSource` to avoid introducing a massive HTTP client library (Retrofit/Ktor) for a single network request.
* `kotlinx.serialization` is used in the `data` module to decode API payloads.

## Affected Capabilities & Side Effects
* **Behavior**: Event creators can type a game title to instantly search RAWG API and load its thumbnail via Coil.
* **Contract Adjustments**: Added `eventIconUrl` to `Event` entity in Firestore data serialization.
* **Hotspots**: The RAWG search is debounced using coroutines (500ms delay) in `NewEventScreenViewModel`.
