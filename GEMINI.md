# SquadPlay - Agent Rules and Guidelines

## Development Lifecycle Policy

Before modifying, creating, or deleting source code for any feature, bug fix, or refactoring:
1. **History Context Check**: Call `history-context-resolver` to evaluate potential domain conflicts, parent lineages, and hotspots from `history/INDEX.md`.
2. **Plan & Draft**: Call `history-tracker` to create the architectural plan inside `.artifacts/` (defining domain, feature slug, lineage, and target commit title).
3. **Hard Gate**: Wait for explicit user sign-off within the artifact review interface before modifying or creating any workspace files.
4. **Commits**: Delegate to `gitflow-commit-formatter` to structure atomic commits aligned with the agreed target commit title and history link footer.
5. **Clean Tree Verification**: Ensure `git status --porcelain` is completely clean and explicitly ask the user for task completion confirmation.
6. **Finalize & Publish**: Once confirmed, let `history-tracker` publish the immutable records (`brief.md`, `detail.md`) to `history/<domain>/<slug>/` and prepend the entry row to `history/INDEX.md`.
    - **Canonical Reference**: Always inspect `history/security/history-tracking-initial-setup/` as the style, structure, and granularity benchmark.

## Operational Constraints
- **Language**: All generated history files, architectural reasoning in `.artifacts/`, and git commit messages MUST be written in technical English.
- **Immutability**: Never edit, mutate, or overwrite completed directories under `history/`.