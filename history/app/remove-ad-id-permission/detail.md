# Detail: Remove AD_ID Permission From Manifest

* **Target Commit Title**: `fix(app): remove AD_ID permission from manifest`

## 1. Intent & Architectural Trade-offs
The Google Play deployment step (`r0adkll/upload-google-play@v1`) failed with `Error: This release includes the com.google.android.gms.permission.AD_ID permission but your declaration on Play Console says your app doesn't use advertising ID.` SquadPlay does not use advertising identifiers or display ads. While Google Play Console allows declaring "Yes" for analytics, explicitly removing the permission via Android manifest merger rules (`tools:node="remove"`) adheres to privacy minimization, prevents unneeded permission aggregation, and keeps the Play Console declaration accurate without requiring external console updates.

## 2. Detailed Contract & Schema Specifications
* Added `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />` and `<uses-permission android:name="android.permission.ACCESS_ADSERVICES_AD_ID" tools:node="remove" />` to `app/src/main/AndroidManifest.xml`.

## 3. Executed Plan
- [x] Step 1: Add `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />` and `android.permission.ACCESS_ADSERVICES_AD_ID` removal to `app/src/main/AndroidManifest.xml`.
- [x] Step 2: Run manifest processing / Gradle build to verify `com.google.android.gms.permission.AD_ID` is removed from the merged manifest.
- [x] Step 3: Verify tests and linting via `./gradlew test`.

## 4. Touched Files
* `app/src/main/AndroidManifest.xml`

## 5. Architectural Divergences & Discoveries
* In addition to `com.google.android.gms.permission.AD_ID`, the Android Privacy Sandbox permission `android.permission.ACCESS_ADSERVICES_AD_ID` introduced by measurement dependencies was also stripped to prevent future Play Console policy validation conflicts.

## 6. Resulting Commits
* `b039987` - `[remove-ad-id-permission] fix(app): remove AD_ID permission from manifest`
