# Brief: Migrate to Jetpack Navigation 3

* **Commit Title**: `feat(navigation): migrate from Navigation 2 to Jetpack Navigation 3`
* **Domain**: `navigation`
* **Parent**: `navigation/sp-89-homegraph-deep-link-scope`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Navigation destinations implement `NavKey` (`androidx.navigation3.runtime.NavKey`).
* Application navigation uses `NavDisplay`, `entryProvider`, and `rememberNavBackStack` from Jetpack Navigation 3.

## Affected Capabilities & Side Effects
* **Behavior**: Replaced Navigation 2 (`androidx.navigation:navigation-compose`) with Jetpack Navigation 3 (`androidx.navigation3`), updating `Destination` to implement `NavKey` and using `NavDisplay` and `entryProvider` in `SquadPlayNavigation`.
* **Contract Adjustments**: `Destination` interface implements `NavKey`. Removed legacy `NavTypes.kt`.
* **Hotspots**: Downstream navigation flow in `SquadPlayNavigation` and ViewModels.
