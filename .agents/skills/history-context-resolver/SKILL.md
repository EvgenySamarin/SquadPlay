---
name: history-context-resolver
description: Analyzes project history before feature planning to extract prior decisions, architectural constraints, and detect collision risks with minimal token consumption.
---

# History Context Resolver

## Objective
Retrieve relevant architectural context and identify potential contract collisions from `history/` before drafting an implementation plan. Avoid loading historical details unless strictly necessary.

---

## Triage & Discovery Funnel

[Target Intent + Target Domain]
│
▼
Step 1: Read history/INDEX.md
│
┌─────────┴────────────────────────────────────────┐
▼ (No Domain/Tag matches)                          ▼ (Matches found)
Return "Clean Context"                       Step 2: Read only brief.md
│
┌───────────────────────────┴────────────────────────┐
▼ (No contract/behavior conflict)                   ▼ (Breaking / Direct conflict OR Explicit user ask)
Return Brief Insights                                Step 3: Read specific detail.md

---

## Execution Algorithm

### Step 1: Scan Index Table
1. Open and inspect `history/INDEX.md`.
2. Filter entries where:
    - The entry's **Domain** matches the current task domain.
    - Or **Target Commit Title** contains keywords matching the planned capability.
3. If no matching or related rows exist:
    - Return immediately: `NO_HISTORICAL_CONFLICT_DETECTED`.

### Step 2: Read Brief Records (`brief.md`)
1. For each matching entry, read strictly `history/<domain>/<slug>/brief.md`.
2. Inspect:
    - `API / Contract Adjustments`
    - `Potential Fragility / Hotspots`
3. If the planned task does not touch or depend on these exact interfaces:
    - Synthesize brief context into the response and terminate retrieval.

### Step 3: Deep Dive Escalation (`detail.md`)
Read `history/<domain>/<slug>/detail.md` **only** if one of the following triggers fires:
- **Contract Collision Trigger**: The upcoming task modifies an interface/schema explicitly flagged as a contract change in `brief.md`.
- **Side Effect Trigger**: The upcoming task interacts with a component designated as a hotspot in `brief.md`.
- **Explicit Override Trigger**: The user explicitly requested verification of prior decisions (e.g., *"check how we implemented X"*).

---

## Output Contract
Return a concise summary structured as:

```markdown
### Architectural History Context
* **Related Sessions Checked**: `<domain>/<slug>`
* **Established Constraints & Invariants**:
  * <Key contract established in or past pattern, rule, work>
* **Identified Hotspots & Collision Risks**:
  * <Specific area avoid breaking fragile or respect to>
* **Deep Dive Required**: Yes (`<path/to/detail.md>`) | No