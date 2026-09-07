---
name: history-context-resolver
description: Analyzes project history before feature planning to extract prior decisions, trace domain lineage, and detect collision risks with minimal token consumption.
---

# History Context Resolver

## Objective
Retrieve relevant architectural context and identify potential contract collisions from `history/`
before drafting an implementation plan. Follow domain lineage chains while avoiding loading historical
details unless strictly necessary.

## Input Contract & Validation

* **Mandatory**:
    * `target_domain`: Specific technical area (e.g., `security`, `auth`, `payment`).
    * `target_intent`: 1–2 sentences explaining the planned functionality or keywords.
* **Optional**:
    * `explicit_deep_dive_slug`: An exact task slug or path explicitly flagged by the user for verification (e.g., `security/biometric-transaction-guard`). If provided, Step 3 bypasses heuristic triggers and reads `detail.md` for this task directly.

### Input Guard
Before starting Step 1, verify that both `target_domain` and `target_intent` are non-empty:
* If either parameter is missing or ambiguous, immediately halt and return:
  `ERROR: MISSING_REQUIRED_INPUTS. Please provide both target_domain and target_intent.`
* Once validated, proceed to Step 1 (Scan Index Table).

## Triage & Discovery Funnel

* **Step 1: Scan `history/INDEX.md`**
   * *No matches found* ➔ **Finish**: Return `NO_HISTORICAL_CONFLICT_DETECTED`.
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
   - The entry's **Domain** matches the input `domain`.
   - Or **Target Commit Title** contains keywords matching the input `target_intent`.
3. If no matching or related rows exist:
   - Return immediately: `NO_HISTORICAL_CONFLICT_DETECTED`.

### Step 2: Lineage Traversal via Brief Records (`brief.md`)
1. Identify the matching task from `history/INDEX.md`.
2. Open its `history/<domain>/<slug>/brief.md`.
3. **Trace the Parent Chain**:
    - Inspect the `Parent:` field.
    - If `Parent` is not `none`, open `history/<parent_path>/brief.md` directly (where `<parent_path>` is `<domain>/<parent_slug>`).
    - Traverse upstream up to a **maximum depth of 3 levels**.
4. **Prune Deprecated Constraints**:
    - If any ancestor specifies a value under `Deprecates:`, discard constraints and hotspots belonging to that deprecated slug.
5. **Aggregate Context**:
    - Collect `Architectural Invariants & Constraints`, `Contract Adjustments`, and `Hotspots` across all active ancestors in the chain.

### Step 3: Deep Dive Escalation (`detail.md`)
Read `history/<domain>/<slug>/detail.md` **only** if one of the following triggers fires:
- **Contract Collision Trigger**: The upcoming task modifies an interface or schema explicitly established by an active ancestor in the chain.
- **Side Effect Trigger**: The upcoming task touches a component designated as a hotspot in an ancestor's `brief.md`.
- **Explicit Override Trigger**: `explicit_deep_dive_slug` is provided in the input, or the user explicitly requested verification of that specific prior decision.

## Output Contract
Return the findings structured exactly as:

### Architectural History Context
* **Immediate Parent Slug**: `<slug>` (or `none`)
* **Active Lineage Chain**: `<current-parent>` -> `<grandparent>` (Depth: N)
* **Inherited Deprecations**: `<slug-1>, <slug-2>` (or `none`)
* **Established Constraints & Invariants**:
    * Key contracts from inherited lineage or patterns
* **Identified Hotspots & Collision Risks**:
    * Specific fragile areas or contracts requiring preservation
* **Deep Dive Performed**: None | Read `<path/to/detail.md>` (Reason: Contract Collision | Hotspot | Explicit Request)