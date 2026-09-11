# Brief: Run unit tests on PR and restrict artifact upload to tags

* **Commit Title**: `ci(workflows): run unit tests on pr and restrict artifact upload to tags`
* **Domain**: `ci`
* **Parent**: `ci/cicd-rawg-api-key`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Pull request triggers evaluate on both `trunk` and `feature/**` branches.
* Pull requests execute only unit tests (`./gradlew test`) via a dedicated `test` job, preserving fast feedback cycles without generating or uploading release artifacts.
* Keystore decoding and full release bundle/APK builds (`./gradlew app:bundleRelease app:assembleRelease`) execute strictly on `push` events.
* GitHub Actions artifacts (`app-bundle`, `app-apk`, `release-notes`) and production deployments (`deploy` job) execute exclusively for release tags matching `v*`.

## Affected Capabilities & Side Effects
* **Behavior**: Added a dedicated `test` job running `./gradlew test` for pull requests targeting `trunk` and `feature/**`. Restricted `build` job to push events, and restricted artifact uploads (`app-bundle`, `app-apk`, `release-notes`) and `deploy` to `v*` tags.
* **Contract Adjustments**: Workflow `.github/workflows/android_cicd.yml` contracts updated for `pull_request`, `test`, `build`, and `deploy` jobs.
* **Hotspots**: Tag releases matching `v*` must have all necessary secrets configured in GitHub Actions to build release artifacts and deploy to production channels.
