# Detail: Run unit tests on PR and restrict artifact upload to tags

* **Target Commit Title**: `ci(workflows): run unit tests on pr and restrict artifact upload to tags`

## 1. Intent & Architectural Trade-offs
Previously, `.github/workflows/android_cicd.yml` executed the heavyweight release build (`app:bundleRelease app:assembleRelease`) on pull requests targeting `trunk` without running unit tests, and had incomplete branch coverage for PRs targeting feature branches. Furthermore, artifact upload steps were running on branch pushes.

To streamline PR validation and enforce strict release artifact isolation:
1. A dedicated `test` job was introduced that triggers on `pull_request` targeting `trunk` and `feature/**`, running `./gradlew test`.
2. The `build` job was gated with `if: github.event_name == 'push'` to avoid unnecessary release builds on PRs.
3. Artifact upload steps (`Upload App Bundle Artifact`, `Upload APK Artifact`, `Upload Release Notes Artifact`) and the `deploy` job were restricted exclusively to `v*` tags (`startsWith(github.ref, 'refs/tags/v')`).

## 2. Detailed Contract & Schema Specifications
* `on.pull_request.branches`: Expanded from `['trunk']` to `['trunk', 'feature/**']`.
* Added `jobs.test`:
  - Condition: `if: github.event_name == 'pull_request'`
  - Runs JDK 17 setup, generates `app/google-services.json` from `GOOGLE_SERVICES_JSON` secret, and executes `./gradlew test`.
* Updated `jobs.build`:
  - Condition: `if: github.event_name == 'push'`
* Updated artifact upload step conditions in `jobs.build`:
  - `Upload App Bundle Artifact`: `if: startsWith(github.ref, 'refs/tags/v')`
  - `Upload APK Artifact`: `if: startsWith(github.ref, 'refs/tags/v')`
  - `Upload Release Notes Artifact`: `if: startsWith(github.ref, 'refs/tags/v')`
* Updated `jobs.deploy`:
  - Condition: `if: startsWith(github.ref, 'refs/tags/v')`

## 3. Executed Plan
- [x] Step 1: Update `on.pull_request` in `.github/workflows/android_cicd.yml` to target both `trunk` and `feature/**`.
- [x] Step 2: Add a new `test` job configured with JDK 17, `google-services.json` generation, and `./gradlew test` execution when `github.event_name == 'pull_request'`.
- [x] Step 3: Gate `build` job with `if: github.event_name == 'push'` so it does not run on PRs.
- [x] Step 4: Update artifact upload steps (`app-bundle`, `app-apk`, `release-notes`) to run only on `v*` tags (`startsWith(github.ref, 'refs/tags/v')`).
- [x] Step 5: Update `deploy` job condition to run only on `v*` tags (`startsWith(github.ref, 'refs/tags/v')`).
- [x] Step 6: Validate workflow syntax and test tasks.

## 4. Touched Files
* `.github/workflows/android_cicd.yml`

## 5. Architectural Divergences & Discoveries
* None.

## 6. Resulting Commits
* `15976df` - `[group-improvements] ci(workflows): run unit tests on pr and restrict artifact upload to tags`
