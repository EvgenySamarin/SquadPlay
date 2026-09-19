# Brief: Pin Compose Foundation to 1.8.2

* **Commit Title**: `fix(design-system): pin compose foundation to 1.8.2 to resolve CustomStyle AbstractMethodError`
* **Domain**: `design-system`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Compose Foundation version must remain binary compatible with Material 3 library version to avoid `AbstractMethodError` in experimental Style APIs.

## Affected Capabilities & Side Effects
* **Behavior**: Pin `androidx.compose.foundation:foundation` to version `1.8.2` in `libs.versions.toml` to match `material3:1.4.0` binary interface expectations for `CustomStyle.applyStyle`.
* **Contract Adjustments**: None
* **Hotspots**: None
