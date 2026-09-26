# Detail: Bind HomeScreenViewModel Test Dispatcher in LogoutLoadingTest

* **Target Commit Title**: `fix(auth): bind HomeScreenViewModel test dispatcher in LogoutLoadingTest`

## 1. Intent & Architectural Trade-offs
In `HomeScreenViewModel`, the combined user and events flow uses `flowOn(ioDispatcher)`. When `LogoutLoadingTest` ran alongside other concurrent build tasks (such as `assembleDebug`), the real thread pool backing `Dispatchers.IO` resumed its flow continuation after `runTest` finished and cancelled its scope. This caused `ContinuationImpl` to encounter an invalid continuation state, throwing `IllegalStateException at ContinuationImpl.kt:130` wrapped in `CompletionHandlerException at JobSupport.kt:1628`.

Adhering to the project's testing conventions established in `HomeScreenEventNavigationTest` and `EventAttendanceTest`, `LogoutLoadingTest` was updated to bind `HomeScreenViewModel.defaultIoDispatcher` to `testDispatcher` in `setUp()` and restore it in `tearDown()`, as well as explicitly setting `vm.ioDispatcher = testDispatcher` in the test ViewModel factory.

## 2. Detailed Contract & Schema Specifications
* **Test Dispatcher Binding (`LogoutLoadingTest.kt`)**:
  ```kotlin
  @Before
  fun setUp() {
      Dispatchers.setMain(testDispatcher)
      HomeScreenViewModel.defaultIoDispatcher = testDispatcher
  }

  @After
  fun tearDown() {
      Dispatchers.resetMain()
      HomeScreenViewModel.defaultIoDispatcher = Dispatchers.IO
  }
  ```

## 3. Executed Plan
- [x] Step 1: Update `@Before setUp()` in `LogoutLoadingTest.kt` to bind `HomeScreenViewModel.defaultIoDispatcher = testDispatcher`.
- [x] Step 2: Update `@After tearDown()` in `LogoutLoadingTest.kt` to reset `HomeScreenViewModel.defaultIoDispatcher = Dispatchers.IO`.
- [x] Step 3: Update `createHomeScreenViewModel()` in `LogoutLoadingTest.kt` to explicitly assign `vm.ioDispatcher = testDispatcher`.
- [x] Step 4: Run `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest` to verify that `LogoutLoadingTest` and all project unit tests pass deterministically.

## 4. Touched Files
* `app/src/test/kotlin/com/eysamarin/squadplay/screens/LogoutLoadingTest.kt`

## 5. Architectural Divergences & Discoveries
None.

## 6. Resulting Commits
* `52dd09f` - `[SP-88] fix(auth): bind HomeScreenViewModel test dispatcher in LogoutLoadingTest`
