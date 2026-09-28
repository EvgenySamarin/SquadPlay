# Detail: Enforce App Check and Notify Group Members via Topics on Event Creation

* **Target Commit Title**: `[SP-96] feat(functions): enforce app check and notify group members via topics on event creation`

## 1. Intent & Architectural Trade-offs
To alert group members when a new game event is created, a push notification mechanism was needed. Two options were evaluated:
- Direct FCM Device Tokens via Multicast (`sendEachForMulticast`): Required storing device FCM registration tokens in Firestore `/users/{userId}`, querying every member document in Firestore upon each event creation, and pruning stale tokens.
- FCM Topics (`topic: groupId`): Leverages existing client topic subscriptions (`subscribeToGroupTopic`), incurs zero `/users` reads in Firestore, handles multi-device users seamlessly, and offloads delivery scaling to FCM infrastructure.

FCM Topics was chosen for scalability, minimal backend read costs, and architectural simplicity. To avoid self-notifying the creator, `creatorId` is attached to the notification's `data` payload, and `SquadPlayMessagingService` silences display if `creatorId == currentUserId`. Additionally, App Check enforcement was configured globally in `functions/src/index.ts` via `enforceAppCheck: true` in `setGlobalOptions`.

## 2. Detailed Contract & Schema Specifications
### Cloud Functions Options (`functions/src/index.ts`)
```typescript
setGlobalOptions({
  maxInstances: 3,
  timeoutSeconds: 15,
  memory: "128MiB",
  enforceAppCheck: true,
});
```

### FCM Message Payload
```typescript
{
  topic: groupId,
  notification: {
    title: `New event in ${groupTitle}`,
    body: title || "A new event was created",
  },
  data: {
    eventId,
    groupId,
    creatorId,
  },
}
```

### Client Creator Mute Check (`SquadPlayMessagingService.kt`)
```kotlin
val creatorId = remoteMessage.data["creatorId"]
val currentUserId = authManager.getUserUid()
if (creatorId != null && creatorId == currentUserId) {
    logger.d(tag = "FCM") { "Suppressing event notification for creator: $creatorId" }
    return
}
```

## 3. Executed Plan
- [x] Step 1: Update `functions/src/index.ts` to include `enforceAppCheck: true` in `setGlobalOptions`.
- [x] Step 2: Implement `sendEventNotification` in `functions/src/index.ts`:
  - Read event snapshot data (`groupId`, `creatorId`, `title`) from `event.data`.
  - Fetch group title from `/groups/{groupId}` doc in Firestore (fallback to `"Squad"` if unavailable).
  - Construct FCM message targeting `topic: groupId` with notification and data payload (`eventId`, `groupId`, `creatorId`).
  - Send message via `admin.messaging().send(message)`.
  - Log successful dispatch and capture any errors.
- [x] Step 3: Run ESLint (`npm --prefix functions run lint`) and TypeScript build (`npm --prefix functions run build`) to ensure 100% compliance with style and typing rules.
- [x] Step 4: Update `ProfileRepositoryImpl.createNewUserGroup()` to call `firestoreDataSource.subscribeToGroupTopic(newGroupId)` upon successful group creation so creators receive subsequent notifications in groups they created.
- [x] Step 5: Update `SquadPlayMessagingService.kt` to inject `FirebaseAuthManager`, inspect `remoteMessage.data["creatorId"]`, and suppress notification display if the creator matches the current user.
- [x] Step 6: Run `./gradlew testDebugUnitTest` and `npm --prefix functions run build` to verify that Android and TypeScript builds and tests pass cleanly.

## 4. Touched Files
* `app/src/main/kotlin/com/eysamarin/squadplay/SquadPlayMessagingService.kt`
* `data/src/main/kotlin/com/eysamarin/squadplay/data/contract/ProfileRepositoryImpl.kt`
* `functions/src/index.ts`

## 5. Architectural Divergences & Discoveries
Initially considered storing device FCM tokens in `/users/{userId}` and using multicast delivery, but pivoted to FCM Topics after analyzing cost, scalability, and built-in multi-device handling. Client-side suppression via `creatorId` data payload reliably prevents creator self-notifications without incurring Firestore read costs.

## 6. Resulting Commits
* `634edc0` - `[SP-96] feat(functions): enforce app check and notify group members via topics on event creation`
