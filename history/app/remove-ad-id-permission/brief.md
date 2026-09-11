# Brief: Remove AD_ID Permission From Manifest

* **Commit Title**: `fix(app): remove AD_ID permission from manifest`
* **Domain**: `app`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* The app targets Android 13+ (compileSdk/targetSdk 37).
* Transitive dependency `play-services-measurement-impl` from Firebase Analytics injects `com.google.android.gms.permission.AD_ID` and `android.permission.ACCESS_ADSERVICES_AD_ID` during manifest merging.
* SquadPlay declares no advertising ID usage in Google Play Console. To maintain alignment and prevent release upload rejections on Google Play deployment tracks, advertising ID permissions must be explicitly removed using `tools:node="remove"`.
* Core Firebase capabilities (Analytics, Crashlytics, Messaging, Auth) remain functional without advertising ID collection.

## Affected Capabilities & Side Effects
* **Behavior**: Stripped `com.google.android.gms.permission.AD_ID` and `android.permission.ACCESS_ADSERVICES_AD_ID` from the merged Android manifest via `tools:node="remove"`.
* **Contract Adjustments**: None
* **Hotspots**: None
