# Brief: Handle Firestore PERMISSION_DENIED gracefully on logout and unauth

* **Commit Title**: `fix(auth): handle firestore permission denied gracefully on logout and unauth`
* **Domain**: `auth`
* **Parent**: `auth/auth-exit-loading-indicator`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Active Firestore snapshot listeners must be proactively detached (`clearListeners()`) before credentials are invalidated in `firebaseAuth.signOut()`, preventing unauthenticated listen requests on the watch stream.
* Continuous snapshot listeners attempting to read Firestore documents after auth token invalidation or session expiration must defensively handle `PERMISSION_DENIED` as an expected unauthenticated lifecycle state, closing cleanly and emitting fallback values (`null` or `emptyList()`) rather than logging errors to Sentry / Crashlytics.
* Fallback and logout navigation from `HomeScreen` and `ProfileScreen` must use `navigator.navigateToAuthGraph()` to safely pop `HomeGraph` and transition back to `AuthGraph`.

## Affected Capabilities & Side Effects
* **Behavior**: Unregisters all active Firestore snapshot listeners during logout prior to `firebaseAuth.signOut()`, and catches `PERMISSION_DENIED` across all data flows and repository layers to prevent false-alarm error logs.
* **Contract Adjustments**: Added `clearListeners()` to `FirebaseFirestoreDataSource` interface; updated `AuthRepositoryImpl` to inject `FirebaseFirestoreDataSource` and invoke `clearListeners()` on sign out.
* **Hotspots**: State transitions during logout and token invalidation across `HomeGraph` and `AuthGraph`.
