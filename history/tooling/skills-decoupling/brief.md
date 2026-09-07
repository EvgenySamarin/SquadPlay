# Brief: Decouple History Tracking

* **Commit Title**: `feat(tooling): decouple history tracking into planning and publishing phases`
* **Domain**: `tooling`
* **Parent**: `security/history-tracking-initial-setup`
* **Deprecates**: `none`

## Architectural Invariants & Constraints
* All future feature plans must query `history/INDEX.md` and use `.artifacts/` for task isolation.
* Task lifecycle must follow the explicit 6-phase sequence defined in `GEMINI.md`.

## Affected Capabilities & Side Effects
* **Behavior**: Separated the monolithic history-tracker into two specialized skills (`task-planner` and `history-publisher`) to properly isolate the planning and completion phases.
* **Contract Adjustments**: Updated `GEMINI.md` to strictly enforce the phase progression. Replaced `.agents/skills/history-tracker` with `.agents/skills/task-planner` and `.agents/skills/history-publisher`.
* **Hotspots**: The `history/INDEX.md` structure remains critical; any change must be reflected in `history-publisher` and `history-context-resolver`.
