---
name: gitflow-commit-formatter
description: Formats git commit messages with branch-based prefixes, Conventional Commits types, and links commits to task history. Use this for all commits in the project.
---

# Gitflow Commit Formatter Skill

When generating a git commit message, you MUST follow this specific format to maintain consistency with Gitflow, issue tracking, and architectural history.

## Format
```text
[branch-name] <type>(<scope>): <description>

[optional body / BREAKING CHANGE]

[optional reference footer]
```

## Instructions
1. **Identify the Branch Name**: Get the name of the current git branch.
2. **Clean Branch Name**: Remove category prefixes like `feature/`, `bugfix/`, `hotfix/`, or `release/` from the branch name. Use only the descriptive part or issue ID (e.g., if branch is `feature/SP-123`, use `SP-123`).
3. **Prefix**: Wrap the cleaned branch name in square brackets at the very beginning of the commit title (e.g., `[SP-123]`).
4. **Alignment with Feature History Plan**:
   - If this commit is part of an active session from `history-tracker`, ensure the `<type>(<scope>): <description>` matches or accurately refines the Target Commit Title defined during the `.artifacts` planning stage.
5. **Type**: Use one of the standard Conventional Commits types:
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation
   - `style`: Formatting, whitespace, missing semicolons, etc.
   - `refactor`: Code change that neither fixes a bug nor adds a feature
   - `perf`: Performance improvements
   - `test`: Adding missing tests or correcting existing tests
   - `chore`: Changes to the build process or auxiliary tools
6. **Scope**: (Optional) Add a scope in parentheses if the change is specific to a module or component (e.g., `app`, `data`, `theme`, `auth`).
7. **Description**: Use the imperative mood, present tense (e.g., "add feature", "fix bug"). Do not capitalize the first letter and do not end with a period.
8. **Breaking Changes**: If the changes break backward compatibility, you MUST:
   - Add a `!` after the type/scope (e.g., `feat(api)!: remove deprecated endpoint`).
   - Add a footer starting with `BREAKING CHANGE: ` followed by a description of what was changed and how to migrate.
9. **History Reference Footer (Traceability)**:
   - If the commit fulfills a task tracked under `history/<domain>/<slug>/`, append a footer to the commit message body: `Ref: history/<domain>/<slug>/`
10. **Atomic Commits**: Every commit MUST be atomic, meaning it should contain only one logical change or fix.
11. **Analyze and Propose**:
    - Before generating the message, analyze all changed files and the specific modifications within them at the line level.
    - If changes (even within the same file) span multiple unrelated logical features or fixes, you MUST explicitly ask the user: *"I've detected multiple distinct changes. Do you want to include them all in one commit, or should I help you split them into separate atomic commits using Android Studio's line-level staging?"*
    - If the user prefers to split, propose a clear plan specifying which logical blocks or lines should be staged for each commit (e.g., "Commit 1 (Fix in repository): [branch] fix(data): ..., Commit 2 (New field in model): [branch] feat(models): ...").


## Examples
### Simple Atomic Commit

```text
[login-fix] fix(auth): resolve null pointer in social login
```

### Commit with History Reference
```text
[SP-456] feat(security): enforce biometric prompt on high-value transfers

Ref: history/security/biometric-transaction-guard/
```

### Breaking Change with History Reference
```text
[SP-789] feat(auth)!: migrate token refresh to encrypted nonce pipeline

BREAKING CHANGE: TokenRefreshRequest requires crypto-signed nonce argument.

Ref: history/auth/token-refresh-pipeline/
```