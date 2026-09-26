# Brief: Debug App Check Provider Factory Setup

* **Commit Title**: `feat(security): configure DebugAppCheckProviderFactory for debug builds`
* **Domain**: `security`
* **Parent**: `security/sp-88-app-check-token-invalid-lockout`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Firebase App Check provider factory installation is configured centrally in `SquadPlayApplication.kt`.
* Release builds strictly retain `PlayIntegrityAppCheckProviderFactory.getInstance()` for device integrity attestation with Google Play.
* Debug builds (`BuildConfig.DEBUG`) install `DebugAppCheckProviderFactory.getInstance()`, allowing emulators and local development devices to generate and utilize allowlisted debug secrets.
* Security lockout handling in `:data` and `MainActivity` remains untouched and continues to guard operations if App Check attestation is invalid or rejected.

## Affected Capabilities & Side Effects
* **Behavior**: Developers and test environments running debug builds can authenticate and interact with Firebase services (Firestore, Auth) without triggering security attestation lockout, provided their generated debug token is allowlisted in the Firebase Console.
* **Contract Adjustments**: None.
* **Hotspots**: Application initialization in `SquadPlayApplication.kt` and Firebase App Check provider registration.
