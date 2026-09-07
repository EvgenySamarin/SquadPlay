---
name: history-tracker
description: Orchestrates the pre-implementation planning lifecycle in .artifacts/ and finalizes immutable records to history/ upon user completion sign-off.
---

# Feature History Tracker

## Objective
Orchestrate safe feature delivery. Ensures pre-implementation plans account for historical context, isolates drafting in `.artifacts/`, and publishes immutable post-implementation artifacts to `history/`.

---

## Workflow Phases

### Phase 1: Pre-Implementation Planning
1. **Context Resolution**:
    - Invoke `history-context-resolver` with the target domain and feature intent.
    - Inherit constraints, interface invariants, and collision warnings.
2. **Drafting in `.artifacts/`**:
    - Determine `<domain>`, `<feature-slug>`, and **Target Commit Title** (e.g., `feat(auth): add biometric guard`).
    - Create task plan artifact incorporating findings from the history resolver.
    - Detail: Intent, Constraints, Touched Files, and Plan.
3. **Hard Gate**:
    - **Do not modify workspace files** until explicit user approval inside the artifact review.

### Phase 2: Implementation & Commit Handoff
1. Implement changes in source files after approval.
2. Delegate to `gitflow-commit-formatter` to structure atomic commits using the agreed **Target Commit Title**.
3. Ensure git working tree is clean.

### Phase 3: Final Verification & Publishing

1. Ask the user:
   > *"Working tree is clean. Is the work on task '<task-name>' officially completed?"*
2. Upon user confirmation:
    - Inspect git commit log for applied SHAs: `git log -n <count> --pretty=format:"%h - %s"`.
    - Create `history/<domain>/<feature-slug>/`.
    - Publish `brief.md` (domain tags, behavioral diff, contract adjustments, hotspots).
    - Publish `detail.md` (plan, touched files, divergence notes, commit SHAs).
    - Prepend new row into `history/INDEX.md`.
3. Freeze the created history directory.

---

## Canonical Style Reference
When generating new history entries or updating `history/INDEX.md`, always inspect:
* `history/security/history-tracking-initial-setup/brief.md`
* `history/security/history-tracking-initial-setup/detail.md`

Use them as the canonical style, scope, and structure reference. Do not deviate from the tone and section layout established in these files.