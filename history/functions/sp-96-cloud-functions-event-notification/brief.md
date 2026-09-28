# Brief: Enforce App Check and Notify Group Members via Topics on Event Creation

* **Commit Title**: `[SP-96] feat(functions): enforce app check and notify group members via topics on event creation`
* **Domain**: `functions`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* **App Check Enforcement**: 2nd Gen Firebase Functions enforce App Check globally (`enforceAppCheck: true` in `setGlobalOptions`) to reject untrusted or forged invocations.
* **Topic-Based Event Notification**: New events triggering `onDocumentCreated("events/{eventId}")` dispatch a single multicast FCM notification to `topic: groupId`, eliminating expensive `/users` token aggregation queries in Firestore.
* **Client-Side Notification Suppression**: Cloud Functions includes `creatorId` in the FCM `data` payload so `SquadPlayMessagingService` can silence notification display for the creator on device.
* **Creator Topic Registration**: Group creators are subscribed to the group topic in `ProfileRepositoryImpl.createNewUserGroup()` ensuring symmetry with `joinGroup()`.

## Affected Capabilities & Side Effects
* **Behavior**: When an event is created in Firestore, a Cloud Function sends a push notification to all group members subscribed to the group topic, while the event creator's device silences the notification.
* **Contract Adjustments**: None. Public data models remain untouched; Cloud Functions publishes payload with `eventId`, `groupId`, and `creatorId` in `data`.
* **Hotspots**: `SquadPlayMessagingService` notification suppression logic, group creation topic subscription flow in `ProfileRepositoryImpl`, and Cloud Functions Firestore triggers.
