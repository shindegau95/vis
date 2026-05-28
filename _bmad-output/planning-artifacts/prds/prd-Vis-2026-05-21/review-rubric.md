# PRD Quality Review — Vis v1

## Overall verdict

The PRD has a real thesis, an earned differentiator (Equipment-Aware Loggers + Co-Edit), and a counter-metric system that genuinely counterbalances — this is not box-ticked work. What's at risk is downstream usability and done-ness: several SM-C cross-references in §7 NFRs point at the wrong counter, FR-16 cross-refs a non-existent gate FR, "Adherence" drives a North Star–adjacent metric (SM-7) but is undefined in the Glossary, and FR-10's e1RM consequence still carries an orphaned `[ASSUMPTION: Epley]` that the reconciliation explicitly dropped. Net: the substance is there; the mechanical layer needs a 30-minute sweep before downstream extraction.

## 1. Decision-readiness — adequate

The PRD names decisions as decisions, not as "considerations." Reconciliation outcomes (Progression Index formula, RPE primary, AI-only v1, Nutrition restored) are stated in clean prose, not buried. The three `[NOTE FOR PM]` callouts in §6.2 (marketplace defer, brand-shared library defer, flat-plan default) sit at real tensions surfaced by the decision-log's "scope-risk flag" — not safe checkpoints. Open Questions §9 are genuinely open (PT inactive threshold, Index weight defaults per goal, marketplace moderation SLA).

Where it weakens: the trade-off implicit in "AI rule only v1" — that trainers who categorically reject AI suggestions have *no* prescribed-progression engine at all in v1 — is not surfaced honestly. FR-12 quietly assumes every trainer will tolerate AI; the cost of being wrong (SM-C5 trainer-override > 70%) is named, but the trade-off given up by killing the four rule-based options is not. A `[NOTE FOR PM]` belongs at FR-12 acknowledging this.

### Findings
- **medium** AI-only progression trade-off not named (§4.3 FR-12, lines 282-289) — Reconciliation cut linear/double-prog/RPE/DUP to v1.5 but PRD does not flag what's given up for AI-skeptical trainers. *Fix:* add `[NOTE FOR PM]` at FR-12 stating "v1 has no rule-based fallback; SM-C5 > 70% override is the kill-trigger."

## 2. Substance over theater — strong

Personas earn their place. Six role-contexts each drive distinct FR blocks (Anita → Block D, Vikram → Block E, Priya/Aarav → Block A's Co-Edit, Rohit → Block B). The persona table (§2.1) tags branch scope and path — load-bearing for RBAC, not flavor text.

The Vision (§1) does not read as swap-in boilerplate. Three load-bearing assertions are concrete and falsifiable. The "Free at launch. Global from day one. India-anchored, cinematic in feel, evidence-based in method." cadence is brand voice, not Vision-theater.

NFRs (§7) carry product-specific thresholds (Set save ≤ 250 ms p95, cache hit ≥ 80%, ACWR > 1.5 amber / > 2.0 red) rather than "scalable / secure / reliable" boilerplate. Counter-metric design is the strongest single section — SM-C1 through SM-C10 each counterbalance a specific primary metric with a pivot trigger.

No findings.

## 3. Strategic coherence — strong

The PRD has a thesis, stated clearly in §1's three load-bearing assertions: (1) input friction is the primary pain, (2) Trainer↔Client real-time + multi-branch are the structural moats, (3) free v1, global day 1. Feature prioritization follows: Block A (Co-Edit + Equipment-Aware Loggers) is FR-1 through FR-8; Block C (AI Progression) is the brain; Blocks D/E (gym ops) materialize the moat. Solo path gets Block B as the standalone surface. This is not a feature backlog with section headings.

SM-3 (Co-Edit usage ≥ 25%) directly validates assertion 2; SM-2 (D7 retention) tests assertion 1; SM-4 (Solo D30) carries a pivot trigger if assertion 3's solo bet doesn't hold. Counter-metrics align: SM-C1 keeps cost honest, SM-C9 (batch logging > 24h > 30%) catches the case where Vis becomes a logging app instead of a Co-Edit app.

Scope kind ("MVP scope kind — problem-solving, experience, platform, or revenue") is implicitly *experience* — and the in-scope list matches: every Equipment-Aware Logger, Light+Dark parity as v1 deliverable. This is right.

No findings.

## 4. Done-ness clarity — adequate

FRs with explicit "Consequences (testable)" blocks (FR-1, FR-2, FR-3, FR-7, FR-8, FR-10, FR-12, FR-13, FR-14, FR-36, FR-37, FR-40) carry verifiable conditions an engineer can write a test against. FR-1's "Trainer edits Set 1 reps from 10 → 12; within ≤ 500 ms Client's UI reflects 12" is genuinely testable.

FRs *without* consequence blocks: FR-4, FR-5, FR-6, FR-9, FR-11 (has partial), FR-15 through FR-26, FR-27 through FR-35 (mostly bare), FR-38, FR-39, FR-41, FR-42, FR-43. This is over half the FRs. Many are reasonable as-is (FR-22 "Per-branch KPIs" lists fields), but several need bounds before story creation:

- **FR-15 Member CSV Import** — no spec on column-mapping UI, error-row behavior, partial-success semantics, row-count ceiling.
- **FR-18 Reminder Cadence** — "WhatsApp / FCM reminder templates" but no list of templates, no trigger conditions, no acknowledgment behavior.
- **FR-21 Trainer Leaderboard** — "three independent dimensions: Experience, Client Feedback, Client Progress" but each dimension is undefined. What's the formula for "Client Progress"? Without it the leaderboard is theater.
- **FR-25 Client-Chosen Trainer Assignment** — "first-come-first-serve accept" with no timeout. UJ-3 edge case says 24h, but FR-25 doesn't carry it.
- **FR-31 Plan Templates** — three sources stated but no RBAC matrix (who publishes brand-shared? trainer requests? owner approves?).
- **FR-43 Access Gate** — three states named but no "pending activation" UX bounds (does the user see anything? can they re-trigger Staff?).

§7.5 Performance carries bounds. §7.7 Accessibility uses "VoiceOver / TalkBack labels on every interactive element" — testable but unenforceable without per-FR a11y consequences.

### Findings
- **high** FR-21 leaderboard formula undefined (§4.5 FR-21, line 344-346) — "Client Progress" dimension drives owner ranking but has no formula. *Fix:* define as "median Progression Index Δ across the trainer's active clients, last 30 days" or similar; otherwise leaderboard cannot ship.
- **medium** FR-15 CSV import lacks error-row spec (§4.4 FR-15, lines 314-316) — partial-success vs all-or-nothing not stated; duplicate-by-phone behavior names detection but not resolution. *Fix:* add Consequences block with row-error UX + duplicate-resolution rule.
- **medium** FR-18 reminder cadence has no template list or trigger map (§4.4 FR-18, lines 326-328) — Anita's surface is named in §2.2 JTBD but the FR is vague. *Fix:* list the four reminder types with trigger condition and acknowledge behavior.
- **medium** FR-25 trainer-accept timeout missing (§4.6 FR-25, lines 362-364) — UJ-3 edge case says 24h, FR doesn't. *Fix:* add "accept window ≤ 24h; lapses route to next-priority trainer" to FR-25.
- **low** FR-31 RBAC matrix implicit (§4.7 FR-31, lines 398-400) — "RBAC determines edit / publish rights" without naming who. *Fix:* add table or one-line spec per source.

## 5. Scope honesty — strong

§5 Non-Goals is substantive and lists 10 concrete exclusions, not just "no payments." §6.2 stratifies v1.5 / v2 / never with named items in each tier — the v1.5 tier in particular reflects honest scope pressure (marketplace, brand-shared library, flat-plan toggle, additional Progression Rules).

`[ASSUMPTION]` tags appear at real inferences (FR-5 spotter-credit, FR-8 voice fallback, FR-13 starting-load heuristic, FR-19 cert verifier division, §7.5 device target, §7.9 RPO/RTO) and all surface in the §10 Assumptions Index — except one orphan (see Mechanical notes).

Open-items density is calibrated: 10 Open Questions + 7 Assumptions + 3 `[NOTE FOR PM]` callouts against 43 FRs is appropriate for a higher-stakes launch product, not over- or under-decorated.

### Findings
- **low** FR-43 PT-lapse retention not explicit (§4.11 FR-43, lines 475-477) — "PT lapse → 'PT membership expired' screen (data retained)" — for how long? Indefinitely? Tied to gym membership state? *Fix:* add `[ASSUMPTION: PT-lapsed user retains read-only access to history indefinitely]` or move to Open Questions.

## 6. Downstream usability — thin

This is the dimension that needs the most repair before UX/architecture/story extraction.

**Glossary coverage:** "Adherence" is referenced in §2.2 JTBD, UJ-3 climax, FR-22 (branch KPI), and SM-7 (target ≥ 50% of Gym Trainers checking adherence 3×/week) — but never defined. Is it Sessions-completed / Sessions-prescribed in the plan window? Sets-completed / Sets-prescribed? On what time horizon? SM-7's "checking adherence" verb implies the metric exists, but a downstream Story for FR-22 cannot compute it. **Critical Glossary gap.**

**WAU vs WALS:** "WAU" is used in counter-metrics (SM-C1, SM-C4) and NFRs (§7.3 cost / WAU, §7.4 DPDPA tickets / WAU) but never defined. Is WAU = unique users with ≥ 1 logged Set in 7 days = WALS? Or distinct active users (could include browse-only)? Without disambiguation, SM-C1's "$0.40 / WAU / month" cannot be measured consistently.

**Cross-reference resolution:** FR-16 PT Activation says "State drives Client App access gate (see FR-29)." FR-29 is "Plan Builder — Flat or Phased." The access gate is FR-43. Broken cross-ref.

**SM-C cross-refs in NFRs are wrong in two places:**
- §7.3 line 567: "Cost ceiling: $0.40 / WAU / month (SM-C3)" — SM-C3 is Crash-free rate. The cost ceiling is SM-C1.
- §7.5 line 578: "Set save → server commit p95 ≤ 250 ms (SM-C8)" — SM-C8 is New-signup-no-log. The set-save latency counter is SM-C6.

**UJ persona linkage:** UJ-1/2/3 each correctly name personas from §2.1 by exact label (Priya, Aarav, Rohit) — clean.

**FR ID continuity:** FR-1 through FR-43 contiguous, no gaps after reconciliation. Section ordering matches.

### Findings
- **critical** "Adherence" undefined in Glossary (§3 + §4.5 FR-22 + §8 SM-7) — Drives a primary SM-7 target and an owner KPI. Without definition, neither can ship measurably. *Fix:* add Glossary entry: "Adherence — % of prescribed Sets in a Plan window the Client logged within session-window tolerance. Computed per-Client; aggregated to Trainer and Branch averages."
- **high** "WAU" undefined; ambiguous vs WALS (§3 Glossary + §7.3 + §7.4 + §8) — Counter-metric thresholds become unmeasurable. *Fix:* add Glossary entry distinguishing WAU (users with ≥ 1 in-app event) from WALS (Sessions with ≥ 1 Set logged).
- **high** FR-16 cross-refs FR-29 for access gate; actual gate is FR-43 (§4.4 FR-16, line 320) — Broken pointer. *Fix:* change "(see FR-29)" to "(see FR-43)".
- **high** §7.3 cites SM-C3 for AI cost; should be SM-C1 (§7.3, line 567) — Off-by-two label drift. *Fix:* change "(SM-C3)" to "(SM-C1)".
- **high** §7.5 cites SM-C8 for set-save latency; should be SM-C6 (§7.5, line 578) — Off-by-two label drift. *Fix:* change "(SM-C8)" to "(SM-C6)".
- **medium** FR-10 e1RM consequence uses Epley only, contradicts Glossary which locks Brzycki for reps 6–10 (§4.2 FR-10, line 266) — Inconsistency between FR and Glossary. *Fix:* rewrite the testable consequence as "e1RM computed per Glossary formula (Epley reps ≤ 5; Brzycki 6–10) exceeds prior max → e1RM PR fires" and remove the dangling `[ASSUMPTION: Epley formula]` tag.

## 7. Shape fit — strong

Multi-stakeholder B2B + consumer hybrid. UJs and personas are load-bearing (correctly). The PRD is over-formalized in some operational FRs (Gym Staff block) and under-formalized in others (FR-15/18/21 lack consequence blocks despite being downstream-critical) but the overall shape — six personas, three UJs, 43 FRs, full counter-metric system — is right for a launch product with a real-time co-edit moat and multi-tenant scope.

Brownfield references resolve: original spec, progression spec, prototype, brand-ref images all cited. New UJs are clearly new; no old-vs-new ambiguity.

Chain-top: downstream UX → architecture → stories will be source-extracting from this PRD. Dimension 6 (downstream usability) findings above are exactly the friction surface that will hurt extraction.

No findings.

---

## Mechanical notes

**Glossary drift:**
- "Adherence" — used 4x; not in Glossary (called out as critical in §6).
- "WAU" — used 6x; not defined (called out as high in §6).
- "Tenant" defined as gym brand OR independent trainer; subsequent §6.1 / §7 don't use the word — definition is correct but isolated. Minor.
- "Brand" vs "tenant" used consistently after definition.
- "Co-Edit" defined (§3) and used in SM-3, §7.2, FR-1 prose — consistent capitalization.

**ID continuity:**
- FR-1 through FR-43 contiguous after reconciliation renumber.
- UJ-1/2/3 contiguous.
- SM-1 through SM-8, SM-C1 through SM-C10 contiguous.
- Cross-refs: FR-16 → FR-29 BROKEN (should be FR-43); §7.3 → SM-C3 BROKEN (should be SM-C1); §7.5 → SM-C8 BROKEN (should be SM-C6). FR-17 → FR-26 ✓; FR-23 → FR-26 ✓; FR-29 → FR-12 (ACWR deload) ✓; FR-38 → FR-36/37 ✓.

**Assumptions Index roundtrip:**
- §10 lists 7 assumptions. Inline ASSUMPTION tags: FR-5 (line 210) ✓ indexed; FR-8 voice (line 240) ✓ indexed; FR-10 Epley (line 266) **ORPHANED — not in §10**. Decision-log step 2 says "dropped Epley assumption (now spec-defined)" but the inline tag was never removed from FR-10's prose. FR-28 (line 386) ✓ indexed.
- §10 entries: FR-5 ✓, FR-8 ✓, FR-28 ✓, FR-19 ✓ (inferred — no inline tag at FR-19 line 330-332; index entry exists without inline anchor — reverse orphan), FR-13 ✓ (inline at FR-13 line 293? checking — yes prose says "≈ 50% bodyweight scaled by experience"; no `[ASSUMPTION]` tag inline, only in §10. Reverse orphan). §7.5 ✓ matches inline at line 580. §7.9 ✓ matches inline at line 604.

Two reverse orphans (index entries with no inline anchor: FR-19, FR-13) and one forward orphan (FR-10 Epley inline tag missing from index). All low-severity but pollute the audit trail.

**UJ persona linkage:**
- UJ-1: Priya ✓, Aarav ✓ (both in §2.1).
- UJ-2: Rohit ✓.
- UJ-3: Aarav ✓, Anita ✓, Priya ✓.
All clean.

**Required sections for stakes:**
- Vision ✓, Personas ✓, JTBD ✓, UJs ✓, Glossary ✓, Features+FRs ✓, Non-Goals ✓, MVP Scope ✓, NFRs ✓, Success Metrics + Counter-metrics + Pivot Triggers ✓, Open Questions ✓, Assumptions Index ✓, Aesthetic pointer ✓.
- Missing for a launch-stakes product with real-time + multi-tenant: an explicit **RBAC matrix** section. RBAC is mentioned in §2.1 footnote and scattered FRs; no consolidated table. Recommend addition before architecture phase; not a v1 PRD blocker.
