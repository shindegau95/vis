---
stepsCompleted: ["step-01-document-discovery", "step-02-prd-analysis", "step-03-epic-coverage-validation", "step-04-ux-alignment", "step-05-epic-quality-review"]
documentsSelected:
  prd: "_bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md"
  architecture: "_bmad-output/planning-artifacts/architecture.md"
  ux: "_bmad-output/planning-artifacts/ux-design-specification.md"
  epics: "docs/superpowers/plans/ (phase plan files)"
---

# Implementation Readiness Assessment Report

**Date:** 2026-05-24
**Project:** Vis

---

## PRD Analysis

### Functional Requirements

FR-1: Co-edited Session — Trainer + Client edit same Session on two devices via WebSocket. Last-write-wins per Set row (≤500ms sync, 5s reconnect drain).
FR-2: Warm-up vs Working Set — Either party flags Set as warm-up. Excluded from Progression Index, volume PR, weekly volume metrics.
FR-3: Prescribed vs Actual fields per Set — Every Set stores prescribed_load, prescribed_reps, actual_load, actual_reps, rpe_target, rpe_actual (1.0–10.0, 0.5 steps), source, ai_locked. UI shows divergence cue.
FR-4: Failure-intent flag + derived RPE — Trainer flags Set "to failure"; rpe_target locks 10.0 (RIR=0).
FR-5: Per-rep Assistance tagging — Label each rep: unassisted | spotter_assisted | forced. Affects PR detection + Progression Index credit.
FR-6: Superset + Drop-Set composition by gesture — Tap-to-pair = Superset; tap-to-drop = drop set. No typing.
FR-7: Rest Timer — Auto-starts on Set save. Compound 2-3 min default, isolation 60-90s. Trainer override per (client × exercise). Haptic at 10s.
FR-8: Equipment-Aware Logger (six types) — Cable pin-stack, barbell plates, dumbbell slider, machine pin-stack, bodyweight, kettlebell, bands. Numeric keypad fallback always available.
FR-9: Template Marketplace for Solo Clients — ~15 curated Plan Templates. User picks → edits → assigns. Blank-slate builder v2.
FR-10: PR Detection (four dimensions) — 1RM load, e1RM, total-volume per Session, rep-count-at-given-load. Fires after each Set save.
FR-11: PR Moment Screen + Card Composer — Cinematic moment screen on PR. 1 Vis template, 3 aspect ratios. Native share-sheet. Watermark default ON, user-togglable OFF, persists.
FR-12: AI Progression Rule per (trainer × client × exercise) — Claude proxy with prompt caching. Inputs: last 12 Sets, client goal, exercise class, days since last session. Output: prescribed_load, prescribed_reps, rpe_target, decision. Cache target ≥80%.
FR-13: AI Bootstrap (cold start) — First exercise assignment with no prior history: bootstrap from questionnaire (experience, goal, sex, age, exercise class). source = AI_BOOTSTRAP.
FR-14: Next-Suggested ghost overlay + ai_locked toggle — Ghost overlay on Equipment-Aware Logger. One-tap accept or trainer override. ai_locked removes override surface.
FR-15: Member CSV / Excel Import — Staff uploads CSV mapping name, phone, email, membership dates, PT dates, trainer_assigned. Duplicate detection by phone.
FR-16: PT Activation — Staff activates, suspends, expires member PT. Drives Client App access gate (FR-43).
FR-17: In-person Payment Logging — Staff records cash/UPI/card amount, date, member, PT block extension. Generates audit log entry.
FR-18: Reminder Cadence — WhatsApp / FCM templates: PT expiry, missed sessions, payment overdue. v1 = generate-and-copy (P0); direct send v2.
FR-19: Trainer Cert Verification — Trainer uploads cert doc; Staff or Owner reviews + approves → verified badge on public profile card. P0.
FR-20: Cross-Branch Revenue Dashboard — Owner sees per-branch + brand-aggregate revenue, PT activation count, churn, trainer utilisation. Configurable date range.
FR-21: Trainer Leaderboard — Per-branch + brand-wide on three dimensions: Experience (years), Client Feedback (5-star avg, n≥3), Client Progress (median Progression Index Δ over rolling 90 days).
FR-22: Branch Health Snapshot — Per-branch KPIs: active members, active PT, WALS, adherence avg, trainer-cert-pending count.
FR-23: Brand-Wide Audit Log Read — Owner reads brand-wide audit log (FR-26). Cannot edit history.
FR-24: Trainer Public Profile Card — Name, photo, certifications + verified badge, years experience, specialty tags, client rating, response-time stat, languages.
FR-25: Client-Chosen Trainer Assignment (gym path) — Client picks top 1-3 trainers priority-ordered. First-accept wins. Trainer can decline + transfer with client consent, carrying questionnaire + plan.
FR-26: Audit Log (P0) — Append-only: member edits, payment edits, role changes, plan-template publishes. Branch-scoped (Staff); brand-wide (Owner). No DELETE grant for any app role.
FR-27: Onboarding Questionnaire (~10 fields) — Age, sex, height, weight, experience, gap, body-type, primary goal, injuries, days/week, equipment, wearable opt-in. Optional advanced: sleep avg, calorie estimate.
FR-28: PT Relationship State Machine — NoPt | PtActive | PtEnded. Mid-session PT-end freezes writes, flushes WebSocket queue, marks Session terminated_during=true.
FR-29: Plan Builder (Flat or Phased) — Flat OR phased (named blocks with start/end dates). Phase transitions client-visible. Expiry reminders via FCM.
FR-30: Plan-Block Expiry Reminders — FCM push at T-2 days and T-0 until acknowledged or new Plan/Phase assigned.
FR-31: Plan Templates (three sources) — Vis starter (~15), per-trainer private, brand-shared. RBAC on edit/publish.
FR-32: Cardio Logging — Treadmill, elliptical, bike, rowing, free-form. Duration + avg HR + perceived effort. Minimal numeric + dropdown UI.
FR-33: Solo-Trainer Invite Link — Personal share link → routes new client to trainer's request inbox. Default at launch.
FR-34: Trainer Marketplace (opt-in) — Vis-wide opt-in directory. Filters: specialty, cert, language, rating, response time. Deferred to v1.5.
FR-35: Muscle Recovery Status — Color-coded chips per muscle group (12 groups). Green=recovered; red=hours remaining. Computed from last Session per group.
FR-36: Per-Muscle Weekly Set Count — Working Sets per muscle group per week (rolling 7-day). Schoenfeld ceiling ~20 sets/muscle/week. Amber 16-20; red >20.
FR-37: ACWR Trainer Alert — Acute:Chronic Workload Ratio per client/week. ACWR >1.5 = amber alert on trainer app. ACWR >2.0 = red + auto-deload via ghost overlay. Cold-start suppression first 14 days.
FR-38: Progress Tab — Body measurements over time, e1RM per exercise, attendance calendar heatmap, per-muscle set-count strip (FR-36), ACWR sparkline (FR-37).
FR-39: Profile Tab — Name, photo, branch (if gym), membership status + PT expiry, daily visit time + overrides + rest days, fitness goals, body stats, assigned trainer card.
FR-40: Trainer Macro Plan Authoring — Per-client daily macros: kcal, protein (g), carbs (g), fat (g), water (L). Optional weekly variants. 200-char free-text guidance field.
FR-41: Client Nutrition Tab (read-only) — Today's macro targets, meals breakdown (5 meals), per-meal food items + quantities + macros, water intake target. Read-only v1.
FR-42: Authentication — Google Sign-In, Apple Sign-In (iOS required), Phone OTP via Firebase Auth. Backend validates Firebase JWT every request.
FR-43: Access Gate by Role — Solo path: any authenticated user → solo onboarding. Gym path: match on phone/email against brand roster; no match → "pending activation". PT lapse → "PT membership expired" screen.

**Total FRs: 43**

### Non-Functional Requirements

NFR-1 (Internationalization): Weight canonical kg, height canonical m; ISO 8601 internal dates; per-locale display (en-IN DD/MM/YYYY, en-US MM/DD/YYYY); per-user time zones with branch-default; multi-currency-ready data model; English-only v1 with i18n keys structured.
NFR-2 (Real-Time Sync): WebSocket transport for Co-Edit. LWW per Set row. Disconnect rate >5% trips SM-C2. Offline queue persists; reconnect drains within 5s.
NFR-3 (AI): Trainer-only v1. Claude proxy with prompt caching; cache ≥80% hit rate on (trainer × client × exercise) repeats within 7 days. Cost ceiling $0.40/WAU/mo (SM-C1). Circuit-breaker at $0.80/WAU (throttle to 1 suggestion/24h). $1/WAU = pivot. API failure fallback: last-prescribed value with stale-ai indicator.
NFR-4 (Compliance/Privacy): DPDPA consent at onboarding; right-to-erasure in Profile → Account → Delete; GDPR portability surfaces; body-health data encrypted at rest; never logged in plaintext. DPDPA tickets >0.5% WAU/wk = SM-C4.
NFR-5 (Performance): Set save p95 ≤250ms (SM-C6 trips at >1.5s). Equipment-Aware Logger FCR ≤200ms. Idempotency: client-generated UUID per Set write; server dedupes. App cold start ≤2.0s on Pixel 6a / Galaxy A54. Admin FCP ≤1.5s, LCP ≤2.5s on broadband.
NFR-6 (Notifications): v1 FCM push + email transactional. Events: PT expiry, plan-block expiry T-2/T-0, missed session, trainer client-request, PR shareable. SMS v2.
NFR-7 (Accessibility): WCAG 2.1 AA on Admin Web v1. VoiceOver/TalkBack labels on every interactive element. Min 44×44pt hit targets. Reduce-motion OS preference respected; orb degrades gracefully to static states.
NFR-8 (Observability): Counter-metric stream wired day 1 (SM-C1 through SM-C10). Per-Session distributed trace device→backend→DB for FR-1. Crash-free ≥99.0% (SM-C3). Real-time dashboard: WebSocket disconnect rate, AI cost/WAU, p95 set-save latency, crash-free rate.
NFR-9 (Operational): Single-region Cloud Run asia-south1. PostgreSQL daily snapshots + WAL streaming. RPO 5 min; RTO 4h v1.
NFR-10 (Security/Audit): Audit log append-only at DB level; no DELETE grant for any application role; body/health data never logged in plaintext; encrypted at rest.

**Total NFRs: 10**

### Additional Requirements / Constraints

- Six distinct role-contexts ship day 1: Solo Trainer, Solo Client, Gym Trainer, Gym Client, Gym Staff, Gym Owner.
- RBAC: branch-scoped for all gym roles except Owner (brand-wide). Solo path: no branch scope.
- Trainer dual-context: Solo Trainer + Gym Trainer roles simultaneously (Open Question 5 — unresolved).
- AI Progression is trainer-only v1; solo clients get AI Bootstrap defaults then self-direct.
- Voice-input fallback (FR-8) — scope unconfirmed, tagged [ASSUMPTION].
- Spotter-assisted half-credit in Progression Index — unconfirmed, tagged [ASSUMPTION].
- PT inactive threshold for PtEnded→NoPt transition — unset Open Question 1.
- Progression Index weight defaults per goal — unset Open Question 9.

### PRD Completeness Assessment

PRD is thorough and well-structured with stable FR IDs (FR-1 through FR-43), testable consequences, and explicit non-goals. 10 open questions remain, 3 of which are architecture-blockers (dual-context conflict FR-12/OQ-5, voice-input scope FR-8/OQ-3, inactive-threshold FR-28/OQ-1). These are flagged but non-blocking per PRD. No missing feature blocks detected relative to MVP scope §6.1.

---

## Epic Coverage Validation

> **Note:** No BMad-format epics exist. Phase plans in `docs/superpowers/plans/` serve as the implementation plan substitute. Plans analysed: `2026-05-04-phase-0-foundation.md`, `2026-05-04-admin-web.md`, `2026-05-04-trainer-app.md`, `2026-05-04-client-app.md`, `2026-05-18-progressive-overload.md`.

### Coverage Matrix

| FR | Requirement (short) | Plan Coverage | Status |
|---|---|---|---|
| FR-1 | Co-Edit Session (WebSocket, LWW) | Trainer-app plan uses **5s polling** — explicit deviation from PRD WebSocket mandate | ❌ CRITICAL DEVIATION |
| FR-2 | Warm-up vs Working Set | Not found in any plan | ❌ MISSING |
| FR-3 | Prescribed vs Actual fields per Set | ExerciseTarget + ExerciseSessionLog in progressive-overload plan; partial schema only | ⚠️ PARTIAL |
| FR-4 | Failure-intent flag + derived RPE | Not found in any plan | ❌ MISSING |
| FR-5 | Per-rep Assistance tagging | Not found in any plan | ❌ MISSING |
| FR-6 | Superset + Drop-Set composition | Not found in any plan | ❌ MISSING |
| FR-7 | Rest Timer | Not found in any plan | ❌ MISSING |
| FR-8 | Equipment-Aware Logger (6 types) | Not found in any plan | ❌ MISSING — CRITICAL |
| FR-9 | Template Marketplace for Solo Clients | Trainer templates exist; no client-facing template browse planned | ❌ MISSING |
| FR-10 | PR Detection (4 dimensions) | Not found in any plan | ❌ MISSING |
| FR-11 | PR Moment Screen + Card Composer | Not found in any plan | ❌ MISSING |
| FR-12 | AI Progression Rule (Claude proxy) | progressive-overload plan (ClaudeOverloadAdapter, OverloadService) ✓ | ✓ COVERED |
| FR-13 | AI Bootstrap (cold start) | progressive-overload plan (AI_BOOTSTRAP source on ExerciseTarget) ✓ | ✓ COVERED |
| FR-14 | Ghost overlay + ai_locked toggle | progressive-overload plan (AiSetChip, UpNextRow) — trainer-side; Logger ghost overlay not detailed | ⚠️ PARTIAL |
| FR-15 | Member CSV / Excel Import | admin-web plan Task 9 (ImportController, ngx-csv-parser) ✓ | ✓ COVERED |
| FR-16 | PT Activation | admin-web plan Task 3/4 (ActivatePtRequest, PtMembership) ✓ | ✓ COVERED |
| FR-17 | In-person Payment Logging | admin-web plan (LogPaymentRequest, PaymentLog) ✓ | ✓ COVERED |
| FR-18 | Reminder Cadence (FCM/WhatsApp templates) | admin-web plan Task 5 (NotificationService stub only) | ⚠️ PARTIAL — stub |
| FR-19 | Trainer Cert Verification | TrainerProfile model exists; cert upload + approval flow not explicitly planned | ⚠️ PARTIAL |
| FR-20 | Cross-Branch Revenue Dashboard | admin-web plan Task 8 (owner-dashboard, StatsController) ✓ | ✓ COVERED |
| FR-21 | Trainer Leaderboard | client-app plan Task 6 (leaderboard + rating submission) ✓ | ✓ COVERED |
| FR-22 | Branch Health Snapshot | admin-web plan Task 8 (branch-dashboard, BranchStatsResponse) ✓ | ✓ COVERED |
| FR-23 | Brand-Wide Audit Log Read | Not found in any plan | ❌ MISSING |
| FR-24 | Trainer Public Profile Card | admin-web plan (TrainerProfileResponse, trainer-list) ✓ | ✓ COVERED |
| FR-25 | Client-Chosen Trainer Assignment | client-app onboarding has TrainerLeaderboardScreen; accept/decline state machine not planned | ⚠️ PARTIAL |
| FR-26 | Audit Log P0 (append-only, no DELETE) | Not found in any plan | ❌ MISSING — CRITICAL |
| FR-27 | Onboarding Questionnaire (~10 fields) | client-app Task 8 (BodyProfileScreen) — exists but full 10-field spec not confirmed | ⚠️ PARTIAL |
| FR-28 | PT Relationship State Machine | PtMembership model + activate/suspend; mid-session freeze + state transitions not planned | ⚠️ PARTIAL |
| FR-29 | Plan Builder (Flat or Phased) | trainer-app plan (PlanController, WorkoutPlan) — flat plans only; phased not explicit | ⚠️ PARTIAL |
| FR-30 | Plan-Block Expiry Reminders (FCM T-2/T-0) | NotificationService stub present; specific FCM expiry cadence not detailed | ⚠️ PARTIAL |
| FR-31 | Plan Templates (3 sources) | Trainer-private + Vis starter present; brand-shared library not planned | ⚠️ PARTIAL |
| FR-32 | Cardio Logging | Not found in any plan | ❌ MISSING |
| FR-33 | Solo-Trainer Invite Link | Not found in any plan | ❌ MISSING |
| FR-34 | Trainer Marketplace (opt-in) | Correctly deferred to v1.5 per PRD | ✓ DEFERRED (OK) |
| FR-35 | Muscle Recovery Status | client-app plan Task 3/11 (RecoveryService, RecoveryChips) ✓ | ✓ COVERED |
| FR-36 | Per-Muscle Weekly Set Count (Schoenfeld) | Not found in any plan | ❌ MISSING |
| FR-37 | ACWR Trainer Alert | Not found in any plan | ❌ MISSING |
| FR-38 | Progress Tab | client-app Task 13 — body measurements + e1RM; ACWR sparkline + set-count strip absent | ⚠️ PARTIAL |
| FR-39 | Profile Tab | client-app Task 14 — basic profile; PT expiry display, visit-time overrides not confirmed | ⚠️ PARTIAL |
| FR-40 | Trainer Macro Plan Authoring | trainer-app plan (NutritionPlan, NutritionController) ✓ | ✓ COVERED |
| FR-41 | Client Nutrition Tab (read-only) | client-app plan Task 14 (NutritionScreen) ✓ | ✓ COVERED |
| FR-42 | Authentication | Phase 0 (Google, Apple, Phone OTP, Firebase Auth filter) ✓ | ✓ COVERED |
| FR-43 | Access Gate by Role | Phase 0 (PendingScreen, role-gated routing) ✓ | ✓ COVERED |

### Missing Requirements

#### Critical (blocking sprint start)

**FR-1 — Co-Edit Session deviation:** Trainer-app plan explicitly uses 5-second polling, contradicting the PRD's WebSocket mandate. The entire Co-Edit structural moat (SM-3: ≥25% co-edit sessions by D90) depends on WebSocket + LWW. Decision needed before implementing the Active Session view.

**FR-8 — Equipment-Aware Logger (6 types):** The signature differentiator of the whole product. No plan covers cable pin-stack, barbell-and-plates, dumbbell slider, machine pin-stack, bodyweight, kettlebell, or bands loggers. This is not a back-end concern — it is the primary UX investment.

**FR-26 — Audit Log P0:** PRD marks this P0 for compliance + integrity. DB-level no-DELETE enforcement not present in any migration plan. Required for DPDPA compliance.

#### High Priority (significant gaps)

**FR-2, FR-4, FR-5, FR-6, FR-7 — Set-level features:** Warm-up flag, failure intent, per-rep assistance tagging, superset/drop-set, rest timer are all absent. These are core set-logging capabilities tightly coupled to FR-3 (schema) and FR-8 (logger).

**FR-10, FR-11 — PR Detection + PR Card Composer:** No plan covers PR detection (4 dimensions) or the cinematic moment screen + card composer (SM-5 target: ≥30% share rate).

**FR-36, FR-37 — Weekly Set Count + ACWR:** Both are in §6.1 In Scope. Recovery chips are planned (FR-35 ✓) but the Schoenfeld set-count ceiling and ACWR trainer alert are absent.

**FR-23 — Brand-Wide Audit Log Read (Owner):** Depends on FR-26. Owner dashboard plan exists but audit log read view is not planned.

#### Medium Priority (incomplete coverage)

**FR-9 — Solo Client Template Marketplace:** Trainer plans have template logic but no client-facing browse/pick/assign flow.

**FR-32 — Cardio Logging:** Explicitly in §6.1 scope; no plan.

**FR-33 — Solo-Trainer Invite Link:** Explicitly in §6.1 as v1 launch feature; no plan.

**FR-28 — PT State Machine completeness:** Mid-session PT-end freeze + `PtEnded→NoPt` threshold unplanned.

**FR-29 — Phased Plans:** PRD calls phased plans the primary model; plans only cover flat.

#### Technology Deviation (requires architecture decision)

**Trainer-app plan uses OpenAI API for AI suggestions** (AiService proxying to OpenAI gpt-3.5-turbo). PRD mandates Claude proxy with prompt caching (≥80% cache hit, $0.40/WAU ceiling). Progressive-overload plan correctly uses Anthropic SDK. These two plans are in conflict.

### Coverage Statistics

- Total PRD FRs: 43 (excluding FR-34 deferred)
- Fully covered: 14 FRs (33%)
- Partially covered: 12 FRs (28%)
- Not covered / critical deviation: 16 FRs (37%)
- Deferred (OK per PRD): 1 FR (FR-34)
- **Effective coverage: 61% (fully + partial combined)**

---

## UX Alignment Assessment

### UX Document Status

**Found** — `_bmad-output/planning-artifacts/ux-design-specification.md` (124K, completed 2026-05-23, 14 steps, all stepsCompleted).

### UX ↔ PRD Alignment

Strong alignment. The UX spec was authored from PRD inputs and explicitly references FR numbers throughout.

| Area | Status | Notes |
|---|---|---|
| Equipment-Aware Logger (FR-8, 6 types) | ✓ Aligned | "Anchor 1" of the entire spec. Full gesture vocabulary (drag, snap, slide, pick) for all 6 variants. |
| Co-Edit WebSocket (FR-1) | ✓ Aligned | UX spec mandates WebSocket with 10s polling fallback. Explicitly differs from pre-pivot phase plans. |
| PR Detection + Card Composer (FR-10, FR-11) | ✓ Aligned | PR Moment composer, 3 aspect ratios, watermark-default-ON, native share-sheet all specified. |
| AI Ghost Overlay + ai_locked (FR-12, FR-14) | ✓ Aligned | Confidence display, one-tap accept, long-press override, staleness self-dismissal, autopilot toggle. |
| Warm-up flag, failure intent, per-rep assistance (FR-2, FR-4, FR-5) | ✓ Aligned | Explicitly in Set-row interaction design. |
| ACWR alert + weekly set count (FR-36, FR-37) | ✓ Aligned | Risk Banner, amber/red states, ACWR sparkline in Progress Tab all specified. |
| Onboarding Questionnaire split paths (FR-27, FR-43) | ✓ Aligned | Shared Questionnaire component with gym/solo path-aware shell specified. |
| Audit log (FR-26) | ✓ Aligned | "Audit-log retention window = full v1 retention; no expiry" explicitly stated. |
| Nutrition (FR-40, FR-41) | ✓ Aligned | Trainer-authored macros + client read-only tab specified. |
| i18n / units (NFR-1) | ✓ Aligned | kg/lb, m/ft+in, per-locale dates, RTL stub from launch. |
| Accessibility (NFR-7) | ✓ Aligned | Thumb-zone discipline, 44×44pt hit targets, reduce-motion specified. |

### UX ↔ Architecture Alignment

Strong alignment. Architecture was authored post-PRD-pivot (completed 2026-05-24) with UX spec as an explicit input.

| Area | Status | Notes |
|---|---|---|
| WebSocket transport (FR-1) | ✓ Aligned | Architecture locks WebSocket + STOMP; server-stamped monotonic LWW; presence via ephemeral topic. |
| Equipment-Aware Logger (FR-8) | ✓ Aligned | Architecture maps `src/loggers/` to 6 subdirs; ProgressionService handles computation. |
| PR Card dual-render (FR-11) | ✓ Aligned | In-app RN+Skia + headless Chromium/Satori worker for shareable PNG; two render paths specified. |
| Audit log DB enforcement (FR-26) | ✓ Aligned | No DELETE grant; Postgres RLS on tenant_id; monthly range partitioning day 1. |
| AI proxy (FR-12) | ✓ Aligned | Claude proxy locked (not OpenAI); circuit-breaker at $0.80/WAU; fallback to last-prescribed. |
| Performance targets (NFR-5) | ✓ Aligned | Set-save p95 ≤250ms, Logger FCR ≤200ms both specified in architecture service contracts. |
| Set-write idempotency (NFR-5) | ✓ Aligned | Client UUID dedupe locked in architecture. |

### Critical Alignment Finding

**The superpowers phase plans (`docs/superpowers/plans/`) are intentionally pre-pivot artifacts.** The architecture document (2026-05-24) explicitly acknowledges they predate the PRD by 2+ weeks and states: *"`bmad-create-epics-and-stories` will rebuild the Phase 1–3 backlog after architecture locks."*

This means the 37% FR gap identified in Step 3 is **expected** — the phase plans were never updated post-PRD. The architecture is the current ground truth, and it explicitly accounts for all uncovered FRs in its scope table. The coverage gap is not an oversight; it is a clean handoff signal: the architecture is complete, and `bmad-create-epics-and-stories` is the correct next action.

### Warnings

1. **WebSocket transport still needs implementation decision details** — architecture locks WebSocket but defers "STOMP vs raw WS vs sticky sessions vs Redis pub-sub" to implementation. This is a **sprint-planning blocker** (must resolve before Active Session story).
2. **PR Card render worker tech** — "Cloud Run Job? Cloud Functions? In-process?" — explicitly deferred in architecture. Must resolve before PR Card story.
3. **Open Question 5 (dual-context AI history)** — resolved in architecture via second party-mode (2026-05-24). Confirm the resolution is captured in the architecture doc before the AI Progression story.


---

## Epic Quality Review

> **Context:** No BMad-format epics exist. Superpowers phase plans serve as the implementation plan substitute. The architecture doc (2026-05-24) explicitly states these plans are pre-pivot artifacts pending replacement by `bmad-create-epics-and-stories`. Quality review applies BMad standards to these plans.

### Epic Structure Validation

#### 🔴 Critical Violations

**1. Technical milestones, not user-value epics.**
Every phase plan is organized as a developer implementation guide ("Task 1: Backend migrations", "Task 2: JPA entities", "Task 3: Service layer"). These are technical milestones, not user-value epics. No plan answers "what can the user now do?" for each block.

**2. Flat structure with no user-story grain.**
Plans contain developer checklist steps, not user stories. There are no acceptance criteria in Given/When/Then format. No story has a stated user value proposition. This makes the plans unusable as sprint input for agents or developers without reinterpretation.

**3. Phase plans predate the PRD by 2+ weeks.** (Confirmed by architecture doc 2026-05-24.) They reflect a pre-pivot product that does not exist. Treating them as implementation backlog would build the wrong product.

**4. Pre-pivot tech conflicts embedded in plans:**
- Trainer-app + Client-app plans use 5-second polling (pre-pivot). PRD + architecture mandate WebSocket.
- Trainer-app plan uses OpenAI API. PRD + architecture mandate Claude proxy (Anthropic).
- Admin-web plan has no audit log enforcement. PRD FR-26 + architecture mandate no-DELETE at DB level.

#### 🟠 Major Issues

**5. No FR traceability.**
Phase plans reference no FR numbers. No way to verify which story implements which requirement without manual analysis (as done in Step 3).

**6. No dependency graph.**
Plans assume sequential execution (Phase 0 → 1 → 2 → 3) but don't map story-level dependencies. Parallel development is not planned.

**7. Incomplete plan status tracking.**
Phase 0 has 92 unchecked tasks and 0 checked tasks — the plan format uses `- [ ]` checkboxes but none are ticked, making completion state opaque. (Architecture doc says Phase 0 is ~90% done per Linear audit; plan file doesn't reflect this.)

**8. Solo-path features entirely absent.**
Solo Trainer invite link (FR-33), Solo Client template marketplace (FR-9), and independent trainer discovery are not planned in any phase. Six role-contexts ship day 1 per PRD §6.1.

#### 🟡 Minor Concerns

**9. Cardio logging, plan-block expiry reminders, trainer cert verification** — each has partial model presence but no dedicated story or task.

**10. Brand-shared plan template library** — noted in PRD §6.2 as v1.5 candidate but the trainer-app plan neither implements nor explicitly defers it.

### Story Sizing Assessment

Phase plans use sub-step checkboxes within Tasks, not user stories. Story sizing is not applicable in the BMad sense. Each Task is more akin to an epic than a story. Sub-steps are too atomic (e.g. "Add Lombok to pom.xml") for sprint-story granularity.

### Overall Quality Verdict

| Check | Status |
|---|---|
| Epics deliver user value | ❌ Fail — technical milestones throughout |
| Epics can function independently | ❌ Fail — all plans depend on pre-pivot spec |
| Stories appropriately sized | ❌ N/A — no BMad stories exist |
| No forward dependencies | ❌ Fail — polling → WebSocket conflict unresolved |
| Database tables created when needed | ⚠️ Partial — migrations are upfront-batched per phase |
| Clear acceptance criteria | ❌ Fail — no Given/When/Then; developer steps only |
| FR traceability maintained | ❌ Fail — no FR references in any plan |

**Conclusion:** Phase plans are not implementation-ready as sprint input. They serve well as the brownfield baseline the architecture doc already references and supersedes. `bmad-create-epics-and-stories` must produce the real implementation backlog.


---

## Summary and Recommendations

### Overall Readiness Status

## ⚠️ NEEDS WORK — Phase 3 complete; Phase 4 sprint planning not yet ready

**PRD:** ✅ Complete, final, well-structured (43 FRs, 10 NFRs, stable IDs, testable consequences).
**UX Spec:** ✅ Complete (14 steps, 124K, 2026-05-23), deeply aligned with PRD, design-quality bar established.
**Architecture:** ✅ Complete (8 steps, 2026-05-24), post-pivot, resolves all pre-pivot technical conflicts.
**Epics:** ❌ None exist. Phase plans are pre-pivot, pre-PRD implementation guides — not sprint-ready stories.

The three planning artifacts (PRD + UX + Architecture) are in excellent alignment. The blocker is that no sprint-ready user stories exist. `bmad-create-epics-and-stories` is the required next action.

---

### Critical Issues Requiring Immediate Action

1. **Run `bmad-create-epics-and-stories` now.** Architecture is complete and unlocks this step. The architecture doc explicitly states this is the next action. The phase plans are intentionally obsolete; new BMad epics must be generated from the post-pivot PRD + architecture.

2. **Decide WebSocket implementation details before the Active Session epic.** Architecture locks WebSocket but defers: STOMP vs raw WS, sticky sessions vs Redis pub-sub for fan-out. Must resolve at sprint planning or the Active Session story cannot be estimated.

3. **Resolve PR Card render worker.** Cloud Run Job vs Cloud Functions vs in-process — deferred in architecture. Impacts FR-11 story scope and cost model.

4. **Confirm Open Question 5 resolution is captured.** Architecture says OQ-5 (dual-context AI history for Solo Trainer + Gym Trainer roles) was resolved in party-mode 2026-05-24. Verify the resolution appears in the architecture doc before the AI Progression story is written.

---

### Recommended Next Steps

1. **[CE] `bmad-create-epics-and-stories`** — Run immediately. Use the architecture doc + PRD as inputs. Produce user-value epics organized by feature block (Co-Edit, Equipment Logger, Progression Engine, Gym Ops, Solo Paths, PR Cards, etc.). No pre-pivot phase plans should be used as input.

2. **[SP] `bmad-sprint-planning`** — After epics exist. Lock WebSocket implementation detail and PR Card render tech as part of sprint planning. Sequence epics: Foundation (Phase 0 already done) → Co-Edit + Equipment Logger → Gym Ops → Solo Paths → Progression + AI → PR Cards + Advanced.

3. **[CS] `bmad-create-story`** — Begin story-by-story implementation starting with the Active Session + Equipment Logger epic (the structural moat, SM-3 gates on it).

4. **Discard or archive the pre-pivot phase plans.** They still live in `docs/superpowers/plans/`. Move to `_archive/` or clearly date-stamp as superseded to prevent confusion during sprint implementation.

---

### Final Note

This assessment found **17 issues** across **4 categories** (FR coverage gaps, epic quality violations, UX→arch alignment findings, and open implementation decisions).

The critical finding is not that the planning is poor — it is the opposite. PRD, UX Spec, and Architecture are in exceptional alignment and reflect a clear, defensible product strategy. The single gap is that **no sprint-ready implementation backlog exists** because the architecture was only completed today (2026-05-24) and the required `bmad-create-epics-and-stories` step has not yet run.

The project is ready to proceed to `bmad-create-epics-and-stories`. All three planning phase outputs are locked and of high quality. Do not implement from the pre-pivot phase plans.

---

**Report generated:** `_bmad-output/planning-artifacts/implementation-readiness-report-2026-05-24.md`
**Assessor:** Gauravprakashshinde
**Date:** 2026-05-24

