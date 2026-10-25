# Brief: Redesign ProfileScreen UI According to Stitch Mockup

* **Commit Title**: `[ndk-debug-symbol-level-dsl] feat(profile): redesign ProfileScreen UI according to Stitch mockup`
* **Domain**: `profile`
* **Parent**: `profile/profile-user-nickname`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* `ProfileScreen` uses `CenterAlignedTopAppBar` with a localized `"Profile"` title (`R.string.profile_screen_title`), `onSurface` back icon, and `primary`-tinted logout icon.
* User profile header is rendered inside a rounded `16.dp` (`DesignSystemTheme.shapes.medium`) `surfaceContainer` card displaying username, email (`onSurfaceVariant`), `@<nickname>` (`onSurfaceVariant`, defaulting to `R.string.default_nickname`), and a clickable squircle `UserAvatar` triggering `ProfileScreenAction.OnAvatarTap`.
* Group and settings quick actions are rendered as right-aligned labeled chip buttons (`ProfileActionChipButton`) with `surfaceContainerHigh` background, `1.dp` `outlineVariant` border, and `12.dp` corner radius.
* Each `UserGroupSection` in `GroupsList` is encapsulated in a bordered `surfaceContainer` card (`16.dp` radius, `1.dp` `outlineVariant` border) containing an inline header (title, circular `surfaceContainerHighest` member count badge, and ownership-gated Edit/Share/Delete/Leave `IconButton`s), an inner `HorizontalDivider`, and `MemberRow` items with `48.dp` bordered squircle avatars.
* Group ownership visibility rules (`isOwner` vs. `canLeave`), modal bottom sheets (`CreateGroupBottomSheet`, `ChangeNicknameBottomSheet`, `EditGroupBottomSheet`, `ConfirmationBottomSheet`), and the `isLoggingOut` overlay remain unchanged.

## Affected Capabilities & Side Effects
* **Behavior**: Redesigned `ProfileScreen` layout and visual styling to match the Stitch GameSquad `ProfileScreen` mockup, introducing card containers for user info and group sections, a centered top app bar title, labeled action chip buttons, and member count badges per group.
* **Contract Adjustments**: Added `profile_screen_title` string resource in `values/strings.xml` and `values-ru/strings.xml`. Public Kotlin APIs and state/action contracts remain unchanged.
* **Hotspots**: `ProfileScreen` Compose layout hierarchy and multi-group card rendering inside `LazyColumn`.
