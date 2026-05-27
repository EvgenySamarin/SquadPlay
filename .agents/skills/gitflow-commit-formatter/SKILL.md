---
name: gitflow-commit-formatter
description: Formats git commit messages with branch-based prefixes and Conventional Commits types. Use this for all commits in the project.
---

# Gitflow Commit Formatter Skill

When generating a git commit message, you MUST follow this specific format to maintain consistency with Gitflow and issue-tracking practices.

## Format
`[branch-name] <type>(<scope>): <description>`

## Instructions
1. **Identify the Branch Name**: Get the name of the current git branch.
2. **Clean Branch Name**: Remove category prefixes like `feature/`, `bugfix/`, `hotfix/`, or `release/` from the branch name. Use only the descriptive part or issue ID (e.g., if branch is `feature/SP-123`, use `SP-123`).
3. **Prefix**: Wrap the cleaned branch name in square brackets at the very beginning of the commit title (e.g., `[SP-123]`).
4. **Type**: Use one of the standard Conventional Commits types:
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation
   - `style`: Formatting, whitespace, missing semicolons, etc.
   - `refactor`: Code change that neither fixes a bug nor adds a feature
   - `perf`: Performance improvements
   - `test`: Adding missing tests or correcting existing tests
   - `chore`: Changes to the build process or auxiliary tools
5. **Scope**: (Optional) Add a scope in parentheses if the change is specific to a module or component (e.g., `app`, `data`, `theme`).
6. **Description**: Use the imperative mood, present tense (e.g., "add feature", "fix bug"). Do not capitalize the first letter and do not end with a period.
7. **Breaking Changes**: If the changes break backward compatibility, you MUST:
   - Add a `!` after the type/scope (e.g., `feat(api)!: remove deprecated endpoint`).
   - Add a footer starting with `BREAKING CHANGE: ` followed by a description of what was changed and how to migrate.
8. **Atomic Commits**: Every commit MUST be atomic, meaning it should contain only one logical change or fix.
9. **Analyze and Propose**:
   - Before generating the message, analyze all changed files and the specific modifications within them at the line level.
   - If changes (even within the same file) span multiple unrelated logical features or fixes, you MUST explicitly ask the user: *"I've detected multiple distinct changes. Do you want to include them all in one commit, or should I help you split them into separate atomic commits using Android Studio's line-level staging?"*
   - If the user prefers to split, propose a clear plan specifying which logical blocks or lines should be staged for each commit (e.g., "Commit 1 (Fix in repository): [branch] fix(data): ..., Commit 2 (New field in model): [branch] feat(models): ...").

## Example
`[login-fix] fix(auth): resolve null pointer in social login`
`[SP-456] feat(ui): add warm color scheme to theme`
