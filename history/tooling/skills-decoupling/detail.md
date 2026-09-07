# Detail: Decouple History Tracking

* **Target Commit Title**: `feat(tooling): decouple history tracking into planning and publishing phases`

## 1. Intent & Architectural Trade-offs
The previous `history-tracker` approach bundled both context resolution and history publishing, which violated the separation of concerns and allowed agents to skip the user-approval Hard Gate. Decoupling into `task-planner` and `history-publisher` ensures that planning happens explicitly in `.artifacts/` before execution, and history is published only after successful code commits.

## 2. Detailed Contract & Schema Specifications
* **task-planner**: Reads lineage context; outputs `.artifacts/<slug>/plan.md`.
* **history-publisher**: Reads final state and git commits; writes to `history/<domain>/<slug>/` and prepends to `history/INDEX.md`.

## 3. Executed Plan
- [x] Step 1: Remove `history-tracker` skill.
- [x] Step 2: Implement `task-planner` to isolate plan generation in `.artifacts/`.
- [x] Step 3: Implement `history-publisher` for writing final results to `history/`.
- [x] Step 4: Update `GEMINI.md` to orchestrate the 6 explicit lifecycle phases.
- [x] Step 5: Verify contracts match between skills.

## 4. Touched Files
* `.agents/skills/history-tracker/SKILL.md` (Delete)
* `.agents/skills/task-planner/SKILL.md` (Create)
* `.agents/skills/history-publisher/SKILL.md` (Create)
* `.agents/skills/gitflow-commit-formatter/SKILL.md` (Modify)
* `.agents/skills/history-context-resolver/SKILL.md` (Modify)
* `GEMINI.md` (Modify)

## 5. Architectural Divergences & Discoveries
None. The implementation cleanly followed the defined plan.

## 6. Resulting Commits
* `e2b9af8` - `[skills-decoupling] feat(tooling): decouple history tracking into planning and publishing phases`
