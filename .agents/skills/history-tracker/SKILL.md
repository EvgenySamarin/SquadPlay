---
name: history-tracker
description: Orchestrates the pre-implementation planning lifecycle in .artifacts/ and finalizes immutable records to history/ upon user completion sign-off.
---

# Feature History Tracker

## Objective
Orchestrate safe feature delivery by isolating iterative plans in `.artifacts/`, tracking architectural 
lineage (`Parent` / `Deprecates`), and publishing immutable post-implementation records to `history/` 
and `history/INDEX.md`.

## Workflow Phases

### Phase 1: Pre-Implementation Planning
1. **Context Resolution**:
   - Invoke `history-context-resolver` with target domain and feature intent.
   - Inherit constraints, interface invariants, and collision warnings from the domain lineage.
2. **Drafting in `.artifacts/`**:
   - Determine `<domain>`, `<feature-slug>`, and standardized **Target Commit Title** (e.g., `feat(auth): add biometric guard`).
   - Identify lineage links:
     - `Parent`: The immediate preceding task slug in this domain or `none`.
     - `Deprecates`: Any prior task slug or pattern this change renders obsolete, or `none`.
   - Create task plan artifact incorporating findings from the history resolver.
   - Detail: Intent, Constraints, Touched Files, and Step-by-Step Plan.
3. **Hard Gate**:
   - **Do not modify or delete workspace files** until explicit user approval inside the artifact review.

### Phase 2: Implementation & Commit Handoff
1. Implement changes in source files according to the approved plan.
2. Allow user review and adjustments.
3. Delegate to `gitflow-commit-formatter` to construct atomic commits using the agreed **Target Commit Title** and linking footer (`Ref: history/<domain>/<feature-slug>/`).
4. Ensure the working tree is clean (`git status --porcelain` is empty).

### Phase 3: Final Verification & Publishing
1. Ask the user directly:
   > *"Working tree is clean. Is the work on task '<task-name>' officially completed?"*
2. Upon user confirmation:
   - **Canonical Style Inspection**: Read `history/security/history-tracking-initial-setup/brief.md` and `detail.md` as the gold standard for formatting and tone.
   - Inspect git commit log for applied SHAs:
     ```bash
     git log -n <count> --pretty=format:"%h - %s"
     ```
   - Create directory: `history/<domain>/<feature-slug>/`.
   - Publish `brief.md` (metadata, parent, deprecates, contracts, and hotspots).
   - Publish `detail.md` (intent, plan, divergence notes, touched files, and commit SHAs).
   - Prepend the new summary entry to `history/INDEX.md`.
3. Freeze the created history directory (mark as immutable).

## Artifact Specifications

### 1. File: `history/<domain>/<feature-slug>/brief.md`
```markdown
# Brief: <Feature Title>

* **Commit Title**: `<type>(<scope>): <subject>`
* **Domains**: `<domain-1>`, `<domain-2>`
* **Parent**: `<domain>/<slug>` (or `none`)
* **Deprecates**: `<domain>/<slug>` (or `none`)

## Affected Capabilities & Side Effects
* **Behavior**: <1-2 sentences on what changed functionally>
* **Contract Adjustments**: <Public DB events interfaces, or schemas, touched>
* **Hotspots**: <Downstream areas for regression to watch>
```

### 2. File: `history/<domain>/<feature-slug>/detail.md`
```markdown
# Detail: <Feature Title>

* **Target Commit Title**: `<type>(<scope>): <subject>`

## 1. Intent & Architectural Constraints
<Why this design was chosen, the trade-offs made, and patterns applied>

## 2. Executed Plan
- [x] Step 1
- [x] Step 2

## 3. Touched Files
* `path/to/FileA`
* `path/to/FileB`

## 4. Architectural Divergences & Discoveries
<Edge cases uncovered during development or deviations from the initial plan>

## 5. Resulting Commits
* `<short-sha>` - `<commit-title>`
```

### 3. Entry Row: `history/INDEX.md`
```markdown
| `<domain>` | `<feature-slug>` | `<target-commit-title>` | `<parent>` | `<deprecates>` | `<domain>/<feature-slug>/` |
```

## Canonical Style Reference
When generating new history entries or updating `history/INDEX.md`, inspect:
* `history/security/history-tracking-initial-setup/brief.md`
* `history/security/history-tracking-initial-setup/detail.md`

Use them as the canonical style, scope, and structure reference. Do not deviate from the tone and section layout established in these files.