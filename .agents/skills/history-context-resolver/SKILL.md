---
name: history-context-resolver
description: Analyzes project history before feature planning to extract prior decisions, trace domain lineage, and detect collision risks with minimal token consumption.
---

# History Context Resolver

## Objective
Retrieve relevant architectural context and identify potential contract collisions from `history/` 
before drafting an implementation plan. Follow domain lineage chains while avoiding loading historical 
details unless strictly necessary.

## Triage & Discovery Funnel

* **Input**: Target Intent + Target Domain
   * **Step 1: Scan `history/INDEX.md`**
      * *No matches found* ➔ **Finish**: Return "Clean Context".
      * *Domain or keyword match found* ➔ **Proceed to Step 2**.
   * **Step 2: Trace Parent Lineage via `brief.md`**
      * Follow `Parent` links up to 3 levels; discard anything marked under `Deprecates`.
      * *No contract collision & no hotspots affected* ➔ **Finish**: Return Lineage Insights.
      * *Contract collision, hotspot affected, or explicit user ask* ➔ **Proceed to Step 3**.
   * **Step 3: Deep Dive Escalation**
      * **Finish**: Read targeted `detail.md`.

## Execution Algorithm

### Step 1: Scan Index Table
1. Open and inspect `history/INDEX.md`.
2. Filter entries where:
   - The entry's **Domain** matches the current task domain.
   - Or **Target Commit Title** contains keywords matching the planned capability.
3. If no matching or related rows exist:
   - Return immediately: `NO_HISTORICAL_CONFLICT_DETECTED`.

### Step 2: Lineage Traversal via Brief Records (`brief.md`)
1. Identify the most recent matching task in the domain from `history/INDEX.md`.
2. Open its `history/<domain>/<slug>/brief.md`.
3. **Trace the Parent Chain**:
   - Inspect the `Parent:` field.
   - If `Parent` is not `none`, read that parent's `brief.md`.
   - Continue traversing upstream up to a **maximum depth of 3 levels**.
4. **Prune Deprecated Constraints**:
   - If any task in the active chain specifies a slug under `Deprecates:`, completely ignore and discard all constraints from the deprecated slug.
5. Aggregate all active constraints, `API / Contract Adjustments`, and `Hotspots` across the non-deprecated chain.

### Step 3: Deep Dive Escalation (`detail.md`)
Read `history/<domain>/<slug>/detail.md` **only** if one of the following triggers fires:
- **Contract Collision Trigger**: The upcoming task modifies an interface or schema explicitly established by an active ancestor in the chain.
- **Side Effect Trigger**: The upcoming task touches a component designated as a hotspot in an ancestor's `brief.md`.
- **Explicit Override Trigger**: The user explicitly requested verification of prior decisions (e.g., *"check how we implemented X"*).

## Output Contract
Return a concise summary structured as:

```markdown
### Architectural History Context
* **Active Lineage Chain**: `<current-parent>` -> `<grandparent>` (Depth: N)
* **Pruned (Deprecated) Tasks**: `<slug>` (or `none`)
* **Established Constraints & Invariants**:
  * <Key contracts from inherited lineage or patterns>
* **Identified Hotspots & Collision Risks**:
  * <Specific areas or contracts that are fragile or need preservation>
* **Deep Dive Required**: Yes (`<path/to/detail.md>`) | No
```