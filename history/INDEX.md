# Architectural Change Index

| Domain       | Feature / Task           | Target Commit Title                                                                 | Parent                                    | Deprecates | Path                                 |
|:-------------|:-------------------------|:------------------------------------------------------------------------------------|:------------------------------------------|:-----------|:-------------------------------------|
| `navigation` | `unauth-deep-link-guard` | `fix(navigation): guard deep link when unauthenticated and preserve pending invite` | `none`                                    | `none`     | `navigation/unauth-deep-link-guard/` |
| `event`      | `rawg-game-parser`       | `feat(event): implement RAWG game parser on new event screen`                       | `none`                                    | `none`     | `event/rawg-game-parser/`            |
| `tooling`    | `skills-decoupling`      | `feat(tooling): decouple history tracking into planning and publishing phases`      | `security/history-tracking-initial-setup` | `none`     | `tooling/skills-decoupling/`         |