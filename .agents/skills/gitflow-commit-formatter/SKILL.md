---
name: gitflow-commit-formatter
description: Analyzes staged or working tree diffs and outputs formatted conventional commit messages with branch prefixes and optional traceability footers.
---

# Gitflow Commit Formatter

## Objective
Generate atomic, conventional Git commit messages incorporating branch identifiers, standard types, and optional architectural history references.

## Input Contract
* **Mandatory**:
    * `branch_name`: Name of the active git branch (used to extract `[<cleaned-branch>]` prefix).
    * `diff_summary`: Unified diff (`git diff`) or line-level modification summary required to verify commit atomicity.
* **Optional**:
    * `target_commit_title`: Baseline title agreed upon during planning (`<type>(<scope>): <description>`). If provided and diff is atomic, reuse this subject directly with the branch prefix.
    * `history_ref_path`: Relative task history path to append as `Ref: <path>` footer (e.g., `history/<domain>/<slug>/`).
    * `is_breaking`: Boolean indicating breaking contract changes (triggers `!` type suffix and breaking footer).
    * `breaking_description`: Explanation of what broke and migration instructions.

## Formatting Rules
1. **Branch Tag**:
    - Strip prefixes such as `feature/`, `bugfix/`, `hotfix/`, or `release/`.
    - Wrap the cleaned branch or issue ID in brackets: `[<cleaned-branch>]`.
2. **Title Line**:
    - Format: `[<branch>] <type>(<scope>): <imperative summary>`
    - Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`.
    - Use lowercase imperative mood, no ending period.
    - If `is_breaking` is true, append `!` before the colon (e.g., `[SP-101] feat(api)!: drop legacy endpoint`).
3. **Traceability Footer**:
    - If `history_ref_path` is provided, append:
      ```text
      Ref: <history_ref_path>
      ```
4. **Breaking Change Footer**:
    - If `is_breaking` is true, separate with a blank line and start with `BREAKING CHANGE: <breaking_description>`.

## Diff Atomicity Check
1. Inspect the provided changes.
2. If the diff contains logically disconnected changes (e.g., a bugfix bundled with an unrelated refactor):
    - Propose splitting the changes into distinct atomic commits with concrete staging recommendations.

## Output Contract
Return one of the following structures:

### Single Atomic Commit Proposal
```text
[<branch>] <type>(<scope>): <description>

[optional BREAKING CHANGE explanation]

[optional Ref: history/<domain>/<slug>/]
```

### Multi-Commit Split Proposal (if changes are not atomic)
```text
Multiple distinct changes detected:
- Commit 1:
  - Staging: `<file or lines>`
  - Message: `[<branch>] <type>(<scope>): <description>`
- Commit 2:
  - Staging: `<file or lines>`
  - Message: `[<branch>] <type>(<scope>): <description>`
```