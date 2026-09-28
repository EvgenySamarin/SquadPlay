# Brief: Event Creation Cooldown Rate Limiting

* **Commit Title**: `[SP-97] feat(security): enforce event creation cooldown rate limiting`
* **Domain**: `security`
* **Parent**: `security/sp-88-debug-app-check-provider`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* **App Check Isolation**: Standard Firebase exceptions and Firestore `PERMISSION_DENIED` errors (such as those thrown when rate limiting triggers) must not be treated as App Check attestation failures and must not trip `SecurityLockoutManager.triggerLockout()`.
* **Atomic Server Cooldown Tracking**: Event creation writes `lastEventCreatedAt: FieldValue.serverTimestamp()` to `/users/{userId}` using `SetOptions.merge()` within the same atomic transaction that sets `/events/{eventId}` and updates `/groups/{groupId}.events`.
* **Transactional Ordering**: In `FirebaseFirestoreDataSource.saveEvent()`, all document snapshot reads (`groups/{groupId}` and `users/{userId}`) strictly precede write operations.
* **Defensive UI Throttling**: The client UI disables repeated submissions (`isSaving`) during in-flight operations and disables the creation action while an active 30-second cooldown is running, displaying a live countdown explanation below the button.

## Affected Capabilities & Side Effects
* **Behavior**: Consecutive event creation by a single account within a 30-second cooldown window is blocked on the client and rejected by backend security rules. The client UI renders immediate countdown feedback and disables rapid double-clicking.
* **Contract Adjustments**:
  * Added `val lastEventCreatedAt: Long? = null` (epoch milliseconds) to `com.eysamarin.squadplay.models.User`.
  * Added `val isCooldownActive: Boolean = false`, `val cooldownRemainingSeconds: Long = 0L`, and `val isSaving: Boolean = false` to `com.eysamarin.squadplay.models.NewEventScreenUI`.
  * Added `event_creation_cooldown_warning` localized string resources.
* **Hotspots**: `FirebaseFirestoreDataSource.saveEvent()` transaction execution, user snapshot parsing in `getUserInfoFlow()`, and `NewEventScreenViewModel` countdown ticker lifecycle.
