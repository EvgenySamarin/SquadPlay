# Brief: Scope Deep Link Handling to HomeGraph and Clear State on Completion

* **Commit Title**: `fix(navigation): scope deep link handling to HomeGraph and clear state on completion`
* **Domain**: `navigation`
* **Parent**: `navigation/unauth-deep-link-guard`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Deep link invite prompts are strictly scoped to `Destination.HomeGraph`, guaranteeing that unauthenticated users on `Destination.AuthGraph` are never prompted before login.
* The squad invite confirmation dialog is managed globally within `Destination.HomeGraph` by `HomeGraphViewModel` and rendered via `HomeGraphDeepLinkHandler`, appearing consistently across any active child screen (`HomeScreen`, `ProfileScreen`, `EventDetailsScreen`, `NewEventScreen`, `SettingsScreen`).
* Deep link invite parameters and active dialog state are consumed immediately upon retrieval and cleared upon confirm or dismiss. Subsequent user profile or group membership changes (e.g. leaving a group) cannot re-trigger the prompt.
* `Destination.HomeScreen` is decoupled from deep link routing arguments and declared as `data object HomeScreen : Destination`.

## Affected Capabilities & Side Effects
* **Behavior**: Deep links clicked when the app is in the background now immediately display the squad invite confirmation dialog on whichever authenticated screen is active (e.g., Profile screen). Leaving a squad no longer causes an erroneous re-prompt to join that squad upon returning to the Home screen.
* **Contract Adjustments**:
  - `Destination.HomeScreen` changed from `data class HomeScreen(val inviteGroupID: String? = null)` to `data object HomeScreen : Destination`.
  - `HomeScreenAction`: removed `OnJoinGroupDialogConfirm` and `OnJoinGroupDialogDismiss`.
  - `HomeScreen`: removed `confirmInviteDialogState`.
  - `HomeScreenViewModel`: decoupled from `DeepLinkManager` and join invite dialog handling.
* **Hotspots**: Navigation Compose graph backstack transitions between `AuthGraph` and `HomeGraph`; `koinViewModel(viewModelStoreOwner = homeGraphEntry)` lifecycle binding.
