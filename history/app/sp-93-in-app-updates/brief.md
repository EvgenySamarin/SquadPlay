# Brief: Integrate Google Play In-App Updates API

* **Commit Title**: `[SP-93] feat(app): integrate google play in-app updates api`
* **Domain**: `app`
* **Parent**: `app/remove-ad-id-permission`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* **In-App Update Strategy**: Support dual update flows: Flexible updates (default for low/medium priority, non-blocking background download with restart prompt upon completion) and Immediate updates (triggered when `updatePriority >= 4` or mandatory, presenting fullscreen blocking interaction).
* **Modern Activity Result API**: Delegate update intent launching exclusively via `ActivityResultContracts.StartIntentSenderForResult()`, strictly avoiding deprecated `startActivityForResult(..., REQUEST_CODE)`.
* **Activity Lifecycle Discipline**: `InstallStateUpdatedListener` must be registered in `MainActivity.onCreate()` and unregistered in `onDestroy()`. Both interrupted immediate update flows (`DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS`) and downloaded flexible updates (`DOWNLOADED`) must be checked and resumed in `onResume()`.
* **Decoupled Notification Architecture**: Download completion notifies user via `SnackbarProvider` dispatching `SnackbarMessage` with a "Restart" action, surfaced through Jetpack Compose `SnackbarHostState` across active app destinations without tight coupling to specific view trees.
* **Localization & Merged Manifest Compliance**: All update messages support English (`values/`) and Russian (`values-ru/`) string resources while preserving merged manifest hygiene.

## Affected Capabilities & Side Effects
* **Behavior**: Checks for available Google Play app updates on launch and resume. Starts flexible background download or immediate fullscreen flow based on priority, prompting the user with an interactive restart snackbar once download completes.
* **Contract Adjustments**: Extended `StringRepository` with `updateDownloadedMessage` and `updateRestartAction`; enhanced `SnackbarProvider` with `SnackbarMessage(message, actionLabel, duration, onAction)` to handle action callbacks; introduced `InAppUpdateManager` delegate contract.
* **Hotspots**: `MainActivity` lifecycle methods (`onCreate`, `onResume`, `onDestroy`) and `SquadPlayNavigation` snackbar event processing.
