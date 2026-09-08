# Brief: Add RAWG_API_KEY secret to Android CI/CD build

* **Commit Title**: `ci(workflows): add RAWG_API_KEY secret to android cicd build`
* **Domain**: `ci`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* The RAWG API key is required at build time to populate `BuildConfig.RAWG_API_KEY` configured in `app/build.gradle.kts` via `System.getenv("RAWG_API_KEY") ?: properties.getProperty("RAWG_API_KEY")`.
* In CI/CD (GitHub Actions), Gradle runs release builds (`app:bundleRelease app:assembleRelease`) and needs `RAWG_API_KEY` defined in the step's environment variables via `${{ secrets.RAWG_API_KEY }}`.

## Affected Capabilities & Side Effects
* **Behavior**: Injected `RAWG_API_KEY` into the `Build with Gradle` step inside `.github/workflows/android_cicd.yml` using GitHub Secrets.
* **Contract Adjustments**: Workflow `.github/workflows/android_cicd.yml` now depends on GitHub repository secret `RAWG_API_KEY`.
* **Hotspots**: Repository secrets in GitHub must include `RAWG_API_KEY` with a valid RAWG API key for builds to access external game metadata.
