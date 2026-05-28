---
title: Reconcile — Progressive Overload Spec vs PRD + Addendum
status: draft
created: 2026-05-22
owner: Gauravprakashshinde
inputs:
  - docs/superpowers/specs/2026-05-18-progressive-overload-design.md (authoritative)
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
scope: |
  Spec is authoritative on progression mechanics, Progression Index, e1RM, set-row schema, RIR,
  set-of-the-day/Up-next, exercise card states. Gaps here are HIGH severity — they affect
  FR-3, FR-4, FR-5, FR-10, FR-12, FR-13, FR-14, and the Glossary's Progression Index entry.
---

# 1. Brief

The progressive-overload spec (`2026-05-18-progressive-overload-design.md`) is the canonical design for how Vis computes the next prescribed load, persists per-set history, surfaces "what you did last time" in Active Session, and lets the trainer override. The PRD references this spec in its inputs frontmatter and again at §0, but the body of the PRD restates a *different* model — five Progression Rules including AI-suggested, a Progression Index that's a "numeric score" over last N sessions, ghost overlay across all rule types, last-write-wins co-edit. The spec's actual design (single AI auto-bump path with rep-first-weight-second bias, `exercise_target` row with `ai_locked` semantics, `exercise_session_log` set-row schema, `ai_target_suggestion` audit log, history bottom-sheet) is not surfaced in the PRD. The addendum is silent on progression entirely — its §1 covers the equipment-aware logger, not the brain.

Consequence: downstream architecture and UX phases that read only the PRD will produce something materially different from the spec. This document enumerates what the PRD honors, where it diverges or omits, and where it specs *more* than the source (anti-gaps that need user confirmation).

---

# 2. Honored

What the PRD captures correctly from the spec:

| Spec clause | PRD location | Notes |
|---|---|---|
| Prescribed vs Actual per-set fields | FR-3 (§4.1) | PRD adds `rir_target` + `rir_actual`, consistent with spec's `rpe` column intent. |
| Warm-up excluded from Progression Index | FR-2 (§4.1), Glossary "Progression Index" | Honored. Spec §4.1 implies same via `completed_at`-bounded history. |
| Trainer override pins target | FR-12, FR-13 (§4.3) | PRD says "Trainer picks the rule"; spec says trainer edit flips `ai_locked=true`. Compatible in spirit. |
| AI suggestion is Trainer-only v1 | FR-13, §7.3, SM-C1, SM-C5 | Aligned. Spec did not constrain to trainer-only but PRD's cost-containment rationale (Block C description) extends spec defensibly. |
| Ghost overlay for next-suggested-weight | FR-14 (§4.3) | Aligned with spec §4.5's "AI-set chip" / rationale-on-tap, though spec is on the *trainer plan builder* surface, PRD generalizes to the logger. |
| AI cost ceiling + cache hit rate | §7.3, SM-C1 | Honored. Spec §4.3 calls for prompt caching with system+schema cached; PRD targets ≥80% hit rate. |
| Append-only AI audit log | (implicit; not stated) | Spec §4.1 defines `ai_target_suggestion`. PRD only references brand-wide audit log (FR-26) for member/payment/role events — silent on AI audit. **Partial honor.** |
| Bodyweight handling | FR-8 (bodyweight logger) | Spec §7 enforces "weight_kg null stays null; AI bumps reps only". PRD logger has the silhouette but the progression rule for bodyweight is not stated. **Partial honor.** |
| First-time bootstrap | (implicit) | Spec §4.4 has a dedicated `AI_BOOTSTRAP` path with `body_weight_kg + training_age_months + goal` inputs. PRD does not surface bootstrap as a distinct concept; FR-13 only covers steady-state suggestion. **Gap (see §3 G-7).** |
| History endpoint shape | (not in PRD) | Spec §4.6 defines `GET /api/clients/me/exercises/{id}/history?limit=1` with full response shape including extrapolated-set rendering. PRD has no equivalent API surface description. **Gap (see §3 G-3).** |

---

# 3. Gaps — ranked by downstream impact

## G-1 (HIGH — blocks architecture). Progression Index is defined two incompatible ways.

- **Spec:** Progression Index is *not* a numeric score. The spec models a target as a row in `exercise_target` with `(weight_kg, reps_low, reps_high, sets, source, ai_locked, ai_rationale, ai_confidence)`, and AI returns a `decision` enum (`MAINTAIN | BUMP_REPS | BUMP_WEIGHT`). There is no "Progression Index" in the spec at all.
- **PRD Glossary:** "Progression Index — A per-(client × exercise) numeric score derived from `actual` set data over the last N sessions. Drives suggested loads."
- **Impact:** The PRD's Glossary entry is fabricated and will mislead the architect. Downstream stories like "compute Progression Index" will be unimplementable against the spec's design. FR-2, FR-5, FR-14, SM-C5 all reference Progression Index as if it were a scalar.
- **Fix:** Either (a) delete the "Progression Index" term and replace with "Target" (per spec) + "AI Recompute Decision" (`MAINTAIN | BUMP_REPS | BUMP_WEIGHT`), or (b) explicitly define Progression Index as the *composite* of (current target row + last AI decision + confidence) and keep the term as a UX-level concept.

## G-2 (HIGH — blocks architecture + UX). Five Progression Rules is the PRD's invention; spec ships ONE rule.

- **Spec:** Single AI rule with hardcoded heuristics inside the prompt — rep-first-weight-second bias, +2.5 kg for compounds / +1.0 kg for isolation, "never bump more than one variable per call", "prefer maintain if history inconsistent". No double-progression, linear, RPE-based, or DUP modes.
- **PRD FR-12:** "Trainer picks one of: double-progression, linear, RPE-based, DUP, AI-suggested. Persisted per (trainer × client × exercise) tuple."
- **Impact:** Architecture would scope four extra rule engines that don't exist in the spec. UX would design a rule-picker that the spec never contemplated. Massive scope inflation.
- **Fix:** Confirm with user whether the four extra rules are intentional scope expansion beyond the spec (in which case they need their own design pass) or a Discovery-era over-spec that should be cut to "AI-suggested only v1, with rule-picker deferred to v2".

## G-3 (HIGH — blocks UX). "Up next" subtitle + history bottom sheet are not in the PRD.

- **Spec §5.1, §5.2:** Named UI patterns:
  - **Up next card** subtitle line with four explicit states: has-history-same-set-count, has-history-more-sets, has-history-fewer-sets, first-time-with-peach-chip. Exact typography (11 px mono for numbers, 9 px peach chip with `var(--vis-amber-soft)` bg).
  - **History bottom sheet** at 70% screen height with four sections (header, today's target card with rationale, last-session set list, first-time full-card AI message). Dotted-border extrapolated-set rows with `1px dashed var(--gc-accent-ring)`.
- **PRD:** Zero mention of "Up next" card, history bottom sheet, extrapolated-set rendering, AI rationale display, or `firstTime` UX state.
- **Impact:** UX spec author will design a different Active Session surface from scratch.
- **Fix:** Add a UJ note or FR (e.g., FR-3b "Last-Session History Surface in Active Session") referencing spec §5 verbatim, or carry these named patterns into the addendum.

## G-4 (HIGH — formula choice). RIR vs RPE — PRD uses RIR; spec uses RPE.

- **Spec set-row schema:** `rpe numeric(3,1) nullable (RPE 1.0–10.0)`. Prompt includes `"rpe": 7` per set. Heuristic "avg RPE ≤ 8" triggers weight bump.
- **PRD FR-3, FR-4:** Stores `rir_target`, `rir_actual`. FR-4 says "RIR target locks to 0; actual RIR derives from `actual_reps - prescribed_reps`" — which is a *rep-delta*, not RIR in any standard sense.
- **Impact:** Two different mental models. RIR (Reps in Reserve) and RPE (Rate of Perceived Exertion) are convertible but not identical; the PRD's "derived RIR = actual − prescribed reps" formula is non-standard and will not feed the spec's prompt. The spec's heuristic ("avg RPE ≤ 8") cannot be evaluated from PRD-shaped data.
- **Fix:** Pick one. Recommend RPE (per spec) since the AI prompt is already designed around it, and add a UI affordance for the client to set RPE post-set. If RIR is preferred for UX, document the RPE↔RIR conversion (RIR = 10 − RPE) and update the spec.

## G-5 (HIGH — formula choice). e1RM formula is named in PRD but not in spec.

- **PRD FR-10:** "e1RM computed from `actual_load × (1 + actual_reps/30)` exceeds prior max" with `[ASSUMPTION: Epley formula; could substitute Brzycki]`.
- **Spec:** Silent on e1RM. The spec's bump decision is rule-driven on rep ranges + RPE, not on e1RM thresholds.
- **Impact:** PR detection (FR-10) is a PRD-scope feature; spec doesn't cover it. This is not a *contradiction* but a gap — PR detection needs its own formula spec. Epley is fine but the choice rationale (why not Brzycki, which is more accurate at low reps) is absent.
- **Fix:** Confirm Epley with rationale note (simpler, more permissive at high reps, gym-standard). Or pin a per-rep-range piecewise: Brzycki for reps ≤ 5, Epley for reps > 5. Add to Open Questions either way.

## G-6 (MEDIUM — data model). The `exercise_target.ai_locked` and `source` enum semantics are not in PRD.

- **Spec:** `source ∈ {TRAINER, AI, AI_BOOTSTRAP}`; `ai_locked` flips true on trainer edit; "Let AI manage this" toggle flips back to false; on untoggle, source flips to AI on next session-end recompute.
- **PRD FR-12, FR-13:** Trainer picks a rule, but there is no `ai_locked` toggle, no `source` enum on the target, no "Let AI manage this" UX, no behavior for what happens to source on untoggle.
- **Impact:** Architect won't know to add `ai_locked` or `source` columns. The trainer-override → unlock → re-engage workflow won't exist.
- **Fix:** Add a Trainer App FR (extension to FR-12) capturing the lock/unlock semantics, or reference spec §4.5 from FR-12.

## G-7 (MEDIUM — onboarding). AI bootstrap path is missing from PRD.

- **Spec §4.4:** When (a) trainer first assigns an exercise OR (b) client opens an exercise with neither target nor history, call `OverloadService.bootstrap(client, exercise)` with `body_weight_kg + training_age_months + goal`. Persisted with `source=AI_BOOTSTRAP`.
- **PRD:** Onboarding Questionnaire (FR-27) captures the inputs but doesn't connect them to bootstrap. FR-13 only handles steady-state.
- **Impact:** First session for any new (client × exercise) has no target. UX will see an empty prescribed field.
- **Fix:** Add FR (e.g., "FR-13b AI Bootstrap on first assignment") that references spec §4.4.

## G-8 (MEDIUM — error handling). Spec's edge-case table is not in PRD.

- **Spec §7:** Seven specific edge cases with behaviors: AI call fails 3× → leave existing target + audit; invalid JSON → reject; weight delta > 10 kg → reject as implausible; late-arriving sets do not re-trigger AI; trainer mid-session edit not pushed live; bodyweight schema-enforced null.
- **PRD:** §7.2 covers WebSocket disconnect and offline queue, but the AI-call-failure / implausible-delta / late-arriving-sets paths are absent.
- **Impact:** Architect has no acceptance criteria for the AI failure modes. The "weight delta > 10 kg implausibility filter" is a critical safety rail.
- **Fix:** Add an NFR or extend §7.2 with a table summarizing spec §7.

## G-9 (LOW — terminology). "set-of-the-day" pattern.

- The user's instruction references a "set-of-the-day" UI pattern. Neither the spec nor the PRD uses this exact term. Closest match in the spec is the **Up-next card** + **History bottom sheet** combo (§5.1, §5.2). The PRD has no analog.
- **Fix:** Confirm with user whether "set-of-the-day" is the user's nickname for the Up-next subtitle, or a distinct pattern that needs spec-ing.

## G-10 (LOW — observability). AI audit table is not surfaced.

- **Spec §4.1:** `ai_target_suggestion` table records `prompt_hash`, `prompt_body`, `output_json`, `applied`, `reject_reason`. Append-only. Critical for debugging.
- **PRD FR-26:** Audit log covers member/payment/role/plan-template events. AI is not in the list.
- **Impact:** Architect won't know AI prompts/responses must be logged for replay/debug.
- **Fix:** Extend FR-26 scope or add a separate "AI Operations Log" FR.

## G-11 (LOW — dedupe). Burst-call dedupe via prompt-hash + 1-hour window.

- **Spec §4.2:** `if aiSuggestionRepo.existsRecent(client, exercise, hash, withinHours=1): return`.
- **PRD:** Silent.
- **Impact:** Architect would re-discover this independently or skip it.
- **Fix:** Note as NFR under §7.3 AI section.

---

# 4. Anti-gaps (PRD specs more than the spec; needs user confirmation)

These are areas where the PRD extends or contradicts the spec without the spec authorizing it. Flag for user.

| # | PRD claim | Spec position | Action |
|---|---|---|---|
| A-1 | **Four other Progression Rules** (double-progression, linear, RPE-based, DUP) in FR-12. | Spec ships one AI rule. | Confirm scope. If real, these need their own spec. |
| A-2 | **PR detection across four dimensions** (1RM load, e1RM, total-volume, rep-count-at-given-load) in FR-10. | Spec is silent on PR detection entirely. | PR detection is a PRD-scope feature, not in spec. Note as out-of-spec. |
| A-3 | **PR Card composer** + share-sheet in FR-11. | Spec is silent. | Same as A-2. PRD-scope. |
| A-4 | **Per-rep assistance tagging** (unassisted / spotter / forced) in FR-5, with half-credit weighting on Progression Index. | Spec's `exercise_session_log` schema has no `assistance` column; spec is silent on assistance entirely. | New scope. Add column to spec's set-row schema. Spec says "0 if attempted-but-failed" — closest existing concept. |
| A-5 | **Co-edit / WebSocket** for real-time trainer+client editing (FR-1). | Spec assumes single-actor logging — set rows persist on session complete, AI runs async. No co-edit consideration. | Co-edit is orthogonal; spec doesn't conflict but doesn't address. Architecture must reconcile WebSocket-flushed writes with the spec's session-complete trigger. |
| A-6 | **Voice fallback** ("≥1s speech → weight + reps") in FR-8. | Spec is silent. | PRD-scope assumption. Already flagged in PRD §10 Open Questions. |
| A-7 | **Cardio logging** (FR-32) with HR + perceived effort. | Spec is strength-training-only (`exercise.type ∈ COMPOUND|ISOLATION|MACHINE|BODYWEIGHT|CORE`). | Cardio is out of spec; needs its own progression model (or none). |
| A-8 | **`actual_load` field naming.** | Spec uses `weight_kg`. | Minor naming drift; pick one (canonical kg implied by §7.1) and reconcile. |
| A-9 | **Solo Client + AI = blocked** (per Block C description). | Spec doesn't block solo clients from AI. | PRD adds a cost-containment policy; reasonable but documents the *why*. |
| A-10 | **Equipment-Aware Logger as primary input surface** with six visualizers (FR-8). | Spec's history sheet shows numeric values only (`90 kg × 8 · RPE 7`). | No contradiction; logger feeds into the same `exercise_session_log` row. But UX spec should reconcile: where does the logger appear in the spec's "Up-next" + bottom-sheet flow? |

---

# 5. Recommended Finalize edits to PRD

In priority order:

1. **Rewrite Glossary entry "Progression Index"** to match spec's target-row + decision-enum model. (G-1)
2. **Cut or scope-flag FR-12's four non-AI rules** as v2 unless user confirms intent. (G-2)
3. **Add an FR for the "Last-Session History Surface"** referencing spec §5.1 + §5.2 named patterns (Up-next card, History bottom sheet, extrapolated-set rendering, first-time peach chip). (G-3)
4. **Reconcile RIR vs RPE** — pick one, document the conversion, fix FR-4. (G-4)
5. **Add an FR for AI Bootstrap** on first-assignment / first-encounter. (G-7)
6. **Extend FR-26 or add FR for AI Operations Log** so `ai_target_suggestion` is captured. (G-10)
7. **Add an NFR under §7.3** for AI failure modes from spec §7 (3× retry, implausible-delta filter, late-set non-trigger, mid-session-edit snapshot). (G-8)
8. **Extend FR-12 with `ai_locked` + `source` enum + "Let AI manage this" toggle semantics.** (G-6)
9. **Confirm e1RM formula choice** (Epley vs Brzycki vs piecewise) and lock in FR-10. (G-5)
10. **Confirm what "set-of-the-day" means** to the user and align terminology. (G-9)

---

# 6. Architecture phase — data-model fields the spec requires

For the architect, this is the exact list of fields/tables to carry forward (from spec §4.1) that the PRD does not name:

- Table `exercise_session_log`: `id, branch_id, client_id, exercise_id, session_id, set_idx, weight_kg, reps, rpe, completed, completed_at`. Indexes `(client_id, exercise_id, completed_at DESC)`, `(session_id)`.
- Table `exercise_target`: `id, branch_id, client_id, exercise_id, weight_kg, reps_low, reps_high, sets, source ENUM(TRAINER|AI|AI_BOOTSTRAP), ai_locked, ai_rationale, ai_confidence, updated_at`. Unique `(client_id, exercise_id)`.
- Table `ai_target_suggestion`: `id, branch_id, client_id, exercise_id, session_id_trigger, model, prompt_hash, prompt_body JSONB, output_json JSONB, applied, reject_reason, created_at`. Index `(client_id, exercise_id, created_at DESC)`.

Endpoint: `GET /api/clients/me/exercises/{exerciseId}/history?limit=1` → returns `{exercise, target, lastSession, plannedSetsToday, firstTime}`.

Async service: `OverloadService.recomputeAsync(clientId, exerciseId, sessionId)` triggered from `POST /api/sessions/{id}/complete`, Spring `@Async`, 3× exponential backoff retry. Prompt cache target ≥80% hit.

Model: `claude-haiku-4-5` primary, `claude-sonnet-4-6` fallback. Prompt schema locked per spec §4.3.

PRD-equivalent fields to add (for completeness — beyond spec):
- `exercise_session_log.assistance ENUM(unassisted|spotter_assisted|forced)` per rep (PRD FR-5).
- `exercise_session_log.is_warmup BOOL` (PRD FR-2; spec excludes warm-up implicitly via Progression Index but doesn't model the column).
- `set.prescribed_load, prescribed_reps` separate from `actual_*` (PRD FR-3; spec only models actual).
- `set.failure_intent BOOL` (PRD FR-4).

---

*End of reconciliation. Output of this pass should feed Finalize edits to `prd.md` (Glossary, FR-2/3/4/5/10/12/13/14/26, §7.3) and a new artifact for the UX phase covering the spec §5 named patterns.*
