# Brief: Home Screen Loading Timeout & Connection Error Dialog

* **Commit Title**: `feat(home): introduce loading timeout and connection error alert dialog`
* **Domain**: `home`
* **Parent**: `home/sp-89-no-group-hide-fab-show-hint`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `HomeScreenViewModel.isTimeoutDialogVisible` state is encapsulated using Kotlin explicit backing fields (`val isTimeoutDialogVisible: StateFlow<Boolean> field = MutableStateFlow(false)`).
* UI interactions are dispatched through `HomeScreenAction` sealed interface (`OnRetryLoadingTap`, `OnDismissTimeoutDialog`).
* Alert dialog utilizes design system styling and tokens through `ConfirmationDialog` with backward-compatible custom button text parameters (`confirmButtonText`, `dismissButtonText`).
* Loading timeout threshold is configurable (`loadingTimeoutMillis = DEFAULT_LOADING_TIMEOUT_MS`) to enable deterministic coroutine unit testing.
* Logging strictly uses `AppLogger`.

## Affected Capabilities & Side Effects
* **Behavior**: If home screen data collection exceeds the 10-second timeout threshold (e.g. slow network or pending attestation), a confirmation dialog appears informing the user and offering options to retry loading or dismiss the prompt.
* **Contract Adjustments**:
  - Added `OnRetryLoadingTap` and `OnDismissTimeoutDialog` to `HomeScreenAction`.
  - Added `confirmButtonText` and `dismissButtonText` parameters to `ConfirmationDialog`.
* **Hotspots**: Home screen data collection pipeline in `HomeScreenViewModel`.
