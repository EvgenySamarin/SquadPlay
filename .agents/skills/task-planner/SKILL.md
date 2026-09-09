---
name: task-planner
description: Prepares structured feature implementation plans in .artifacts/ incorporating domain invariants, lineage traces, and step-by-step tasks.
---

# Task Planner

## Objective
Generate an isolated, reviewable task implementation plan inside `.artifacts/` prior to any code modifications.

## Input Contract
* **Mandatory**:
    * `domain`: Technical domain identifier (e.g., `security`, `auth`, `data`).
    * `feature_slug`: Kebab-case task slug (e.g., `biometric-transaction-guard`).
    * `target_commit_title`: Planned conventional commit title (e.g., `feat(security): enforce biometric prompt`).
    * `task_description`: Functional requirements, user intent, or problem statement.
* **Optional**:
    * `parent_slug`: Preceding task slug in this domain (defaults to `none`).
    * `deprecates_slug`: Existing slug that this task explicitly renders obsolete (defaults to `none`).
    * `context_invariants`: Architectural constraints, interfaces, or hotspots gathered from history context resolution (defaults to `none`).

## Execution Algorithm
1. Be sure that current git branch is not `trunk`, `main`, `master` or `develop`, if so, create separate branch for this task.
2. Establish target path: `.artifacts/<feature-slug>/plan.md`.
3. Generate the planning file strictly adhering to the **Plan Schema** below.
4. Save the document to `.artifacts/<feature-slug>/plan.md`.

## Plan Schema (`.artifacts/<feature-slug>/plan.md`)
```markdown
# Plan: <feature_slug>

* **Domain**: `<domain>`
* **Target Commit Title**: `<target_commit_title>`
* **Parent**: `<parent_slug>`
* **Deprecates**: `<deprecates_slug>`

## 1. Context & Architectural Invariants
<Inherited and collision constraints contracts, existing from lineage, warnings>

## 2. Proposed Contract Adjustments
* **Public APIs / Schemas**: <Adjustments none or>
* **Events / Database**: <Adjustments none or>

## 3. Touched Files
* `path/to/file1` (Modify | Create | Delete)
* `path/to/file2` (Modify | Create | Delete)

## 4. Implementation Steps
- [ ] Step 1: <Concrete atomic step>
- [ ] Step 2: <Concrete atomic step>
- [ ] Step 3: Verify tests and linting
```

## Output Contract
Return a Markdown confirmation:
```markdown
Plan draft created successfully:
* **Path**: `.artifacts/<feature-slug>/plan.md`
* **Domain**: `<domain>`
* **Feature Slug**: `<feature-slug>`
* **Target Commit Title**: `<target_commit_title>`
* **Ready for Review**: Yes
```