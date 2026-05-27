---
name: gitflow-commit-formatter
description: Formats git commit messages with branch-based prefixes and Conventional Commits types. Use this for all commits in this project.
---

# Gitflow Commit Formatter Skill

When generating a git commit message, you MUST follow this specific format to maintain consistency with our Gitflow and issue-tracking practices.

## Format
`[branch-name] <type>(<scope>): <description>`

## Instructions
1. **Identify the Branch Name**: Get the name of the current git branch.
2. **Prefix**: Wrap the branch name in square brackets at the very beginning of the commit title (e.g., `[feature/SP-123]`). 
   - *Tip*: If the branch name is long, focus on the part containing the issue ID.
3. **Type**: Use one of the standard Conventional Commits types:
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation
   - `style`: Formatting, whitespace, missing semi-colons, etc.
   - `refactor`: Code change that neither fixes a bug nor adds a feature
   - `perf`: Performance improvements
   - `test`: Adding missing tests or correcting existing tests
   - `chore`: Changes to the build process or auxiliary tools
4. **Scope**: (Optional) Add a scope in parentheses if the change is specific to a module or component (e.g., `app`, `data`, `theme`).
5. **Description**: Use the imperative mood, present tense (e.g., "add feature", "fix bug"). Do not capitalize the first letter and do not end with a period.

## Example
`[feature/login-fix] fix(auth): resolve null pointer in social login`
`[SP-456] feat(ui): add warm color scheme to theme`
