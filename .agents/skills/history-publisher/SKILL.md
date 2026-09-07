---
name: history-publisher
description: Publishes immutable architectural records (brief.md, detail.md) to history/ and registers them in history/INDEX.md using canonical formatting.
---

# History Publisher

## Objective
Persist the completed task record into permanent project history as read-only architectural documentation.

## Input Contract
* **Mandatory**:
    * `domain`: Technical domain identifier.
    * `feature_slug`: Unique kebab-case task slug.
    * `target_commit_title`: Final applied conventional commit title.
    * `behavior_summary`: 1–2 sentences explaining functional changes.
    * `executed_steps`: Final list of completed steps with checked status (`- [x] ...`).
    * `touched_files`: List of modified or created repository files.
    * `commit_records`: List of short SHAs and commit titles (e.g., `["a1b2c3d - [SP-123] feat: ..."]`).
* **Optional**:
    * `parent_slug`: Ancestor task slug (defaults to `none`).
    * `deprecates_slug`: Replaced task slug (defaults to `none`).
    * `contract_adjustments`: Public interfaces, schemas, or events modified (defaults to `none`).
    * `hotspots`: Fragile downstream components or regression areas (defaults to `none`).
    * `intent_notes`: Core reasoning, design decisions, and trade-offs.
    * `divergence_notes`: Edge cases encountered or deviations from the initial plan.

## Execution Algorithm
1. **Canonical Style Reference**:
    - Read `history/security/history-tracking-initial-setup/brief.md` and `detail.md` as reference templates for formatting and tone.
2. **Directory Creation**:
    - Create directory `history/<domain>/<feature-slug>/`.
3. **Write Brief Record**:
    - Write `history/<domain>/<feature-slug>/brief.md` adhering to the Brief Schema.
4. **Write Detail Record**:
    - Write `history/<domain>/<feature-slug>/detail.md` adhering to the Detail Schema.
5. **Update Index**:
    - Prepend the new entry row to the table in `history/INDEX.md`.

## Schemas

### Brief Schema (`brief.md`)
```markdown
# Brief: <Feature Title>

* **Commit Title**: `<target_commit_title>`
* **Domain**: `<domain>`
* **Parent**: `<domain>/<parent_slug>` (or `none`)
* **Deprecates**: `<domain>/<deprecates_slug>` (or `none`)

## Architectural Invariants & Constraints
* <Core non-negotiable architectural rules established by this change>

## Affected Capabilities & Side Effects
* **Behavior**: <1-2 sentences on functional changes>
* **Contract Adjustments**: <Public DB, events, APIs, or schemas modified>
* **Hotspots**: <Downstream fragile components to monitor for regression>
```

### Detail Schema (`detail.md`)
```markdown
# Detail: <Feature Title>

* **Target Commit Title**: `<target_commit_title>`

## 1. Intent & Architectural Trade-offs
<Why this design was chosen, alternative options rejected, and patterns applied>

## 2. Detailed Contract & Schema Specifications
<Detailed public interface signatures, payload schemas, or DB migration details>

## 3. Executed Plan
- [x] Step 1
- [x] Step 2

## 4. Touched Files
* `path/to/FileA`
* `path/to/FileB`

## 5. Architectural Divergences & Discoveries
<Edge cases or deviations encountered during implementation>

## 6. Resulting Commits
* `<short-sha>` - `<commit-title>`
```

### Index Schema (`history/INDEX.md`)
```markdown
| `<domain>` | `<feature-slug>` | `<target_commit_title>` | `<parent_slug>` | `<deprecates_slug>` | `<domain>/<feature-slug>/` |
```

## Output Contract
Return a plain execution confirmation:
```yaml
status: SUCCESS | ERROR
published_path: "history/<domain>/<feature-slug>/"
index_updated: true
```