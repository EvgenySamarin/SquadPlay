# Detail: Add RAWG_API_KEY secret to Android CI/CD build

* **Target Commit Title**: `ci(workflows): add RAWG_API_KEY secret to android cicd build`

## 1. Intent & Architectural Trade-offs
Added `RAWG_API_KEY` to the environment block of the `Build with Gradle` step in `.github/workflows/android_cicd.yml`. In local development, the key is resolved from `local.properties` or local environment variables. In CI environments, Gradle relies on the environment variable `RAWG_API_KEY` supplied by GitHub Actions secrets to generate `BuildConfig.RAWG_API_KEY`.

## 2. Detailed Contract & Schema Specifications
* Updated `.github/workflows/android_cicd.yml` to supply `RAWG_API_KEY: ${{ secrets.RAWG_API_KEY }}` under `env:` for the `Build with Gradle` step.

## 3. Executed Plan
- [x] Step 1: Add `RAWG_API_KEY: ${{ secrets.RAWG_API_KEY }}` under `env:` in the `Build with Gradle` step inside `.github/workflows/android_cicd.yml`.
- [x] Step 2: Validate workflow syntax and review git diff.
- [x] Step 3: Provide detailed user instructions on configuring the secret in GitHub repository settings.

## 4. Touched Files
* `.github/workflows/android_cicd.yml`

## 5. Architectural Divergences & Discoveries
* None.

## 6. Resulting Commits
* `8947aad` - `[cicd-rawg-api-key] ci(workflows): add RAWG_API_KEY secret to android cicd build`
