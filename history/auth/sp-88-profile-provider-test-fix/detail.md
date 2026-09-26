# Detail: ProfileProviderTest Flow Replay and Scheduler Fix

* **Target Commit Title**: `fix(auth): enable replay and advance scheduler in ProfileProviderTest`

## 1. Intent & Architectural Trade-offs
In `ProfileProviderTest`, `FakeAuthRepository` initialized `userIdFlow = MutableSharedFlow()`, which defaults to `replay = 0`. When the test launched `provider.getUserInfoFlow().collect { ... }` in a coroutine and immediately emitted `"uid1"` without first running the scheduler, the collector had not yet attached. Consequently, the initial emission was lost, resulting in an empty emissions list and an `AssertionError: expected:<[User(uid=uid1, ...)]> but was:<[]>`.

In real Firebase implementations (`FirebaseAuthManagerImpl`), adding an `AuthStateListener` immediately delivers the current authenticated user on registration. Setting `MutableSharedFlow(replay = 1)` in `FakeAuthRepository` models this initial-value contract, and invoking `testScheduler.runCurrent()` immediately after `launch` guarantees the collector is active before subsequent emissions occur.

## 2. Detailed Contract & Schema Specifications
* **Test Flow Replay and Scheduler Advancement (`ProfileProviderTest.kt`)**:
  ```kotlin
  private class FakeAuthRepository(
      val userIdFlow: MutableSharedFlow<String?> = MutableSharedFlow(replay = 1)
  ) : AuthRepository { ... }

  val job = launch {
      provider.getUserInfoFlow().collect { emissions.add(it) }
  }
  testScheduler.runCurrent()
  ```

## 3. Executed Plan
- [x] Step 1: Update `FakeAuthRepository` in `ProfileProviderTest.kt` to use `MutableSharedFlow<String?>(replay = 1)`.
- [x] Step 2: Call `testScheduler.runCurrent()` immediately after `launch` in `ProfileProviderTest.kt` to ensure collector coroutine is subscribed before emissions.
- [x] Step 3: Run `./gradlew :domain:test --tests "com.eysamarin.squadplay.domain.profile.ProfileProviderTest"` to verify the test passes.
- [x] Step 4: Run `./gradlew testDebugUnitTest` across the entire project to ensure all tests pass cleanly.

## 4. Touched Files
* `domain/src/test/java/com/eysamarin/squadplay/domain/profile/ProfileProviderTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `649d95b` - `[SP-88] fix(auth): enable replay and advance scheduler in ProfileProviderTest`
