# Brief: Show LoadingIndicator on exit until navigation to AuthScreen

* **Commit Title**: `feat(auth): show loading indicator on exit until navigation to auth screen`
* **Domain**: `auth`
* **Parent**: `none`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* Fallback and logout navigation from `HomeScreen` and `ProfileScreen` must use `navigator.navigateToAuthGraph()` to safely pop `HomeGraph` and transition back to `AuthGraph`.
* When exit/logout is triggered, an input-blocking overlay displays Material 3 Expressive `LoadingIndicator` to prevent duplicate user interactions while `authProvider.signOut()` is executing.
* Screen content displays `LoadingIndicator` during `UiState.Loading` to ensure visual consistency across loading states.

## Affected Capabilities & Side Effects
* **Behavior**: Added Material 3 Expressive `LoadingIndicator` during logout on both `HomeScreen` and `ProfileScreen`, preventing interaction until sign-out finishes and navigation to `AuthScreen` is performed.
* **Contract Adjustments**: Exposed `isLoggingOut: StateFlow<Boolean>` in `HomeScreenViewModel` and `ProfileScreenViewModel`.
* **Hotspots**: State transitions during logout between `HomeGraph` and `AuthGraph`.
