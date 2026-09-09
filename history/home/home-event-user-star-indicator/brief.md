# Brief: Display trailing star icon for user events on home screen

* **Commit Title**: `feat(home): display trailing star icon for user events on home screen`
* **Domain**: `home`
* **Parent**: `home/home-fab-hide-on-scroll`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* In `HomeScreen`, `DSListItem` displays a decorative trailing star icon (`R.drawable.ic_star_24`) when `item.isYourEvent` is `true`.
* Leaving `onTrailingIconClick = null` preserves row click propagation to open `EventDetailsScreen`.
* Accessibility is supported with localized `content_description_your_event`.

## Affected Capabilities & Side Effects
* **Behavior**: Users viewing the events list on the home screen can visually distinguish events they created from other squad members' events via a star indicator on the right side of the card.
* **Contract Adjustments**: None.
* **Hotspots**: None.
