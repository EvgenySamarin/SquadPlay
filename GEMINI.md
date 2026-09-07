# SquadPlay - Agent Rules and Guidelines

## Development Lifecycle Policy

Before modifying, creating, or deleting source code:
1. **History Context Check**: Call `history-context-resolver` to evaluate potential domain conflicts from `history/INDEX.md`.
2. **Plan & Draft**: Call `history-tracker` to create the architectural plan inside `.artifacts/`.
3. **Hard Gate**: Wait for explicit user approval before writing any code.
4. **Commits**: Delegate to `gitflow-commit-formatter`.
5. **Finalize**: After completion confirmation, let `history-tracker` publish the records to `history/` and update `history/INDEX.md`.