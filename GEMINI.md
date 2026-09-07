# SquadPlay - Agent Rules and Guidelines

## Development Lifecycle Orchestration

The agent orchestrates all feature delivery, bug fixes, and architectural refactoring through 
explicit lifecycle phases.

+---------------------------+
| history-context-resolver  | (Step 1: Domain & Lineage Context)
+-------------+-------------+
| Context Invariants
v
+---------------------------+
|       task-planner        | (Step 2: Generate plan in .artifacts/)
+-------------+-------------+
| Draft Artifact
v
+---------------------------+
|   Hard Gate (User Review) | (Step 3: Explicit user sign-off)
+-------------+-------------+
| Approved
v
+---------------------------+
|   Code Implementation     | (Step 4: Modify workspace files)
+-------------+-------------+
| Working Tree Diffs
v
+---------------------------+
| gitflow-commit-formatter  | (Step 5: Format & Apply Atomic Commits)
+-------------+-------------+
| Clean Tree + User Sign-off
v
+---------------------------+
|     history-publisher     | (Step 6: Publish immutable history)
+---------------------------+

### Phase 1: Historical Context Resolution
1. **Parameter Extraction & Validation**:
   - Extract required parameters:
      - `target_domain`: Target technical domain.
      - `target_intent`: Core intent or functional summary.
   - Detect optional parameters:
      - `explicit_deep_dive_slug`: If the user explicitly asks to verify or base work on an existing task (e.g., *"check how we did auth in biometric-guard"* or references a specific `slug`), set this field. Otherwise, omit it.
   - **Validation Gate**: If `target_domain` or `target_intent` is missing/ambiguous, prompt the user for clarification before executing the skill.
2. **Execution**:
   - Invoke `history-context-resolver` with:
     ```yaml
     target_domain: <domain>
     target_intent: <intent>
     explicit_deep_dive_slug: <slug or omit>
     ```
3. **Parse Results**:
   - Map output fields directly to task planning parameters:
      - `parent_slug`: Extract value from **Immediate Parent Slug**.
      - `inherited_deprecations`: Extract from **Inherited Deprecations**.
      - `context_invariants`: Combine entries from **Established Constraints & Invariants** and **Identified Hotspots & Collision Risks**.
   - If the resolver returned `NO_HISTORICAL_CONFLICT_DETECTED`:
      - Set `parent_slug: none`.
      - Set `context_invariants: none`.

### Phase 2: Plan Generation
1. **Parameter Preparation**:
   - `domain`: High-level technical area (`security`, `auth`, `data`, etc.).
   - `feature_slug`: Derivation rule:
      - Must be lowercase `kebab-case` alphanumeric (e.g., `biometric-transaction-guard`).
      - Derive from the active issue/branch name (e.g., branch `feature/SP-123` -> `sp-123-<short-action>`) or from the subject of the planned commit title.
      - Must be unique within `history/<domain>/`.
   - Construct `target_commit_title` according to Conventional Commits.
   - Set `parent_slug` from Phase 1 (`immediate_parent_slug`).
   - Check if current task deprecates an older decision/task (set `deprecates_slug`, otherwise `none`).
   - Set `context_invariants` from Phase 1 (`context_invariants`).
2. **Execute Planner**:
   - Invoke `task-planner` with:
     ```yaml
     domain: <domain>
     feature_slug: <feature-slug>
     target_commit_title: <target_commit_title>
     task_description: <original intent or prompt>
     parent_slug: <parent_slug>
     deprecates_slug: <deprecates_slug>
     context_invariants: <context_invariants>
     ```
3. **Verify Plan**:
   - Ensure `.artifacts/<feature-slug>/plan.md` exists and contains defined steps, touched files, and contracts before presenting it to the user.

### Phase 3: Hard Gate Approval
1. **DO NOT** create, modify, or delete repository files until the user reviews and explicitly signs off on `.artifacts/<feature-slug>/plan.md`.

### Phase 4: Implementation
1. Execute the code changes strictly within the approved plan boundaries.
2. Run tests and builds to verify changes.

### Phase 5: Atomic Commits
1. **Context & Diff Collection**:
   - Get current branch name via `git branch --show-current`.
   - Inspect changes via `git status --short` and `git diff` (or `git diff --cached`).
   - Check approved plan in `.artifacts/<feature-slug>/plan.md` for breaking contract changes.
2. **Execute Formatter**:
   - Invoke `gitflow-commit-formatter` with:
     ```yaml
     branch_name: <branch_name>
     diff_summary: <output of git diff or changed files with hunk summaries>
     target_commit_title: <target_commit_title from Phase 2>
     history_ref_path: "history/<domain>/<feature-slug>/"
     is_breaking: <true | false>
     breaking_description: <migration note if breaking, else omit>
     ```
3. **Apply Commits**:
   - If the formatter returns a single proposal, stage and commit the changes.
   - If the formatter returns a multi-commit split proposal, stage files/hunks sequentially and apply each commit individually.
   - Verify working tree is clean: `git status --porcelain` must return empty.

### Phase 6: Final Sign-Off & Publishing
1. **Completion Confirmation**:
   - Verify `git status --porcelain` is completely empty.
   - Prompt user:
     > *"Working tree is clean. Is the work on task '<feature-slug>' officially completed and ready to be recorded in history?"*
   - Stop and wait for explicit confirmation.
2. **Data Aggregation**:
   - Read `.artifacts/<feature-slug>/plan.md` to extract:
      - `domain`, `feature_slug`, `target_commit_title`
      - `parent_slug`, `deprecates_slug`
      - Planned steps, contract adjustments, and touched files.
   - Retrieve all commits created during Phase 5:
     ```bash
     git log -n <applied_commits_count> --pretty=format:"%h - %s"
     ```
   - Formulate `behavior_summary` and note any deviations in `divergence_notes`.
3. **Execute Publisher**:
   - Invoke `history-publisher` with:
     ```yaml
     domain: <domain>
     feature_slug: <feature-slug>
     target_commit_title: <target_commit_title>
     behavior_summary: <1-2 sentences on functional changes>
     executed_steps: <completed steps list>
     touched_files: <list of files modified/added/deleted>
     commit_records: <list of Short SHAs and subjects>
     parent_slug: <parent_slug>
     deprecates_slug: <deprecates_slug>
     contract_adjustments: <contract adjustments or none>
     hotspots: <identified hotspots or none>
     intent_notes: <intent and architectural constraints>
     divergence_notes: <divergences or none>
     ```
4. **Verification**:
   - Check publisher output: ensure `status: SUCCESS` and `index_updated: true`.
   - Report final completion to the user.

## Operational Constraints
- **Language**: All generated history records, planning files in `.artifacts/`, and commit messages MUST be written in technical English.
- **Immutability**: Files in `history/` are strictly immutable. Never modify or remove past entries.