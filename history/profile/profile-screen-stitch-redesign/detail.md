# Detail: Redesign ProfileScreen UI According to Stitch Mockup

* **Target Commit Title**: `[ndk-debug-symbol-level-dsl] feat(profile): redesign ProfileScreen UI according to Stitch mockup`

## 1. Intent & Architectural Trade-offs
Aligned the native Jetpack Compose `ProfileScreen` implementation with the Stitch GameSquad `ProfileScreen` design (`projects/17613910814499941770/screens/9a20d53fb46f40c0b03d1c810bb1cca7`) while strictly preserving existing unidirectional data flow, `ProfileScreenAction` contracts, and domain models:
1. **Card-Based Grouping**: Replaced flat list items and standalone section dividers with self-contained `GroupSectionCard` containers (`surfaceContainer` background, `1.dp` `outlineVariant` border, `16.dp` corner radius), improving visual hierarchy between distinct squads.
2. **Inline Group Header & Member Count Badge**: Combined the group title, member count pill badge (`section.members.size`), and ownership-gated action buttons onto a single horizontal header row inside each card, separated from member rows by an internal `HorizontalDivider`.
3. **Labeled Action Chips**: Replaced icon-only `Create new group` and `Settings` buttons with labeled `ProfileActionChipButton` components (`surfaceContainerHigh` background, `1.dp` `outlineVariant` border, `12.dp` corner radius) for clearer affordance.
4. **Domain Fidelity**: Omitted purely decorative mockup placeholders not backed by domain models (such as `PRO` badges or real-time game presence strings) to keep the UI strictly driven by `ProfileScreenUI`, `User`, `UserGroupSection`, and `Friend`.

## 2. Detailed Contract & Schema Specifications
* **Public APIs / Interfaces**:
  - `ProfileScreen`, `ProfileScreenUI`, and `ProfileScreenAction` signatures remain unchanged.
* **String Resources**:
  - `app/src/main/res/values/strings.xml`: `<string name="profile_screen_title">Profile</string>`
  - `app/src/main/res/values-ru/strings.xml`: `<string name="profile_screen_title">Профиль</string>`

## 3. Executed Plan
- [x] Step 1: Add `profile_screen_title` string resource to `app/src/main/res/values/strings.xml` and `app/src/main/res/values-ru/strings.xml`.
- [x] Step 2: Update `ProfileScreen` top bar in `ProfileScreen.kt` to use `CenterAlignedTopAppBar` with `R.string.profile_screen_title`, `surface` container color, and `primary`-tinted logout action icon.
- [x] Step 3: Redesign `ProfileScreenMediumLayout` in `ProfileScreen.kt` to wrap the user header in a `surfaceContainer` rounded card (`16.dp`) and replace the icon-only action row with labeled surface-container action buttons (`Create new group` and `Settings`) matching the Stitch mockup.
- [x] Step 4: Redesign `GroupsList`, `GroupHeader`, and `MemberRow` in `ProfileScreen.kt` so each `UserGroupSection` renders as a bordered `surfaceContainer` card with an inline header (title + member count pill badge + ownership-gated icon buttons), inner `HorizontalDivider`, and `MemberRow` items inside the card.
- [x] Step 5: Verify compilation, unit tests, and Compose previews.

## 4. Touched Files
* `app/src/main/res/values/strings.xml`
* `app/src/main/res/values-ru/strings.xml`
* `app/src/main/kotlin/com/eysamarin/squadplay/screens/profile/ProfileScreen.kt`

## 5. Architectural Divergences & Discoveries
* Used `TopAppBarDefaults.topAppBarColors` instead of the deprecated `TopAppBarDefaults.centerAlignedTopAppBarColors` in Material 3.

## 6. Resulting Commits
* `09a2968` - `[ndk-debug-symbol-level-dsl] feat(profile): redesign ProfileScreen UI according to Stitch mockup`
