# Brief: Guard unauthenticated deep link navigation and preserve pending invite

* **Commit Title**: `fix(navigation): guard deep link when unauthenticated and preserve pending invite`
* **Domain**: `navigation`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Deep link intent URIs are consumed synchronously at the `MainActivity` boundary (`intent.data = null`) to prevent Navigation Compose from automatically auto-routing to protected child destinations before authentication state is resolved.
* `DeepLinkManager` serves as the centralized singleton state holder for pending deep link parameters (`pendingInviteGroupId`) across both cold starts and warm starts (`onNewIntent`).
* Authenticated users opening invite links navigate directly to `Destination.HomeGraph` / `Destination.HomeScreen` without ever displaying the authentication screen.
* Unauthenticated users opening invite links are gated to `Destination.AuthGraph`. Pending invite IDs are preserved and automatically consumed upon login to display the squad invite confirmation dialog.
* Fallback navigation from home and profile screens uses `navigator.navigateToAuthGraph()` to safely pop `HomeGraph` rather than navigating to individual child screens of inactive graphs.

## Affected Capabilities & Side Effects
* **Behavior**: Deep links now properly respect authentication state without flashing or crashing on `HomeScreen` when logged out, and preserve squad invites across authentication flows.
* **Contract Adjustments**: Introduced `DeepLinkManager` interface and made `AuthRepository.getCurrentUserId(): String?` null-safe.
* **Hotspots**: Navigation state transitions between `AuthGraph` and `HomeGraph`, and `singleInstance` intent delivery in `MainActivity.onNewIntent`.
