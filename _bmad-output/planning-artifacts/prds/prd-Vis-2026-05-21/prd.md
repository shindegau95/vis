---
title: Vis — Product Requirements Document
status: final
created: 2026-05-21
updated: 2026-05-22
owner: Gauravprakashshinde
project: Vis
inputs:
  - docs/superpowers/specs/2026-05-04-vis-design.md
  - docs/superpowers/specs/2026-05-18-progressive-overload-design.md
  - _bmad-output/planning-artifacts/research/domain-vis-fitness-pt-research-2026-05-21.html
  - prototype/
  - PLAN.md
  - CLAUDE.md
companion_html: prd.html
---

# PRD: Vis

> Companion HTML at `prd.html` (human-readable per project HTML-first rule). This `.md` is workflow state-of-record. Depth that belongs to architecture / UX spec / business-model lives in `addendum.md`.

## 0. Document Purpose

This PRD is the canonical product specification for **Vis v1**, the audience being the PM (owner), downstream BMad workflows (UX design, architecture, epics/stories), and any reviewer ramping onto the project. It is structured Glossary-anchored: features grouped, FRs nested with stable global IDs (FR-1 through FR-N), user journeys numbered UJ-1 through UJ-N, success metrics SM-1 through SM-N and counter-metrics SM-C1 through SM-CN. Inline `[ASSUMPTION: …]` tags flag inferences without confirmation; the §10 Assumptions Index lists them all. `[NOTE FOR PM]` callouts mark items emotionally load-bearing that may need revisit at Finalize. The full Discovery audit trail is at `.decision-log.md`; user-contributed depth that belongs downstream sits in `addendum.md`. Prior artifacts this PRD builds on:

- Original spec — `docs/superpowers/specs/2026-05-04-vis-design.md` (multi-branch SaaS framing; superseded in places by Discovery decisions noted inline).
- Progressive-overload addendum spec — `docs/superpowers/specs/2026-05-18-progressive-overload-design.md`.
- Domain research — `_bmad-output/planning-artifacts/research/domain-vis-fitness-pt-research-2026-05-21.html` (training science, JTBD personas, competitive landscape).
- Working prototype — `prototype/` (orb visual language, brand palette, light + dark theme).
- Brand references — `docs/brand-ref/reference_light.png`, `reference_dark.png`.

## 1. Vision

Vis makes progressive overload effortless for anyone who lifts — solo or with a trainer. Where competitors force gym-goers to type weights and remember last week, Vis logs through visual gestures that mimic the equipment in front of you: drag the cable pin, snap plates onto a bar, slide the dumbbell rack. Where competitors silo lifters, trainers, and gyms into three apps, Vis is one product where a trainer and client can edit the same set log live during an in-person session — and where a gym can run that experience across all its branches.

Free at launch. Global from day one. India-anchored, cinematic in feel, evidence-based in method.

Three load-bearing assertions drive every downstream decision:

1. **Primary user pain = remembering + inputting progression.** Visual equipment-aware logging is the differentiator, not the orb aesthetic. Beauty serves usability. *Grounded in domain research §competitive-landscape: existing apps (Hevy, Strong, FitNotes) require typing every weight; AI-coached apps (FitnessAI, Caliber) abstract the lift to text prompts.*
2. **Trainer ↔ client real-time in-person collab + multi-branch gym ops are the structural moats.** *Anti-positioning:* Trainerize treats trainer-client as async messaging + plan handoff (no live co-edit); FitBudd does multi-branch ops but no co-edit; Bevel does in-person device-sharing but no real-time sync. Vis is the first to unify all three.
3. **Free v1; pricing later.** Global units (kg + lb), date formats, time zones, multi-currency-ready data model from day 1.

## 2. Target User

### 2.1 Primary Personas

Vis serves **six role-contexts** across two onboarding paths (*solo* or *gym-attached*); v1 ships both. RBAC scopes data: branch-scoped for everyone except gym owner, who sees all branches within their gym brand.

| Persona | Path | Role | Branch scope |
|---|---|---|---|
| **Aarav** — gym-going client beginning structured PT | gym | Gym Client | single branch |
| **Rohit** — solo strength athlete (no PT, no gym affiliation) | solo | Solo Client | n/a |
| **Priya** — Personal Trainer at a gym chain | gym | Gym Trainer | single branch |
| **Karan** — independent PT operating across multiple gyms / self-employed | solo + gym | Solo Trainer (+ optional Gym Trainer at one or more brands) | per affiliation |
| **Anita** — gym branch staff / front-desk manager | gym | Gym Staff | single branch |
| **Vikram** — gym owner / chain operator | gym | Gym Owner | all branches in brand |

*A trainer may simultaneously hold Solo Trainer plus Gym Trainer roles at multiple brands. A client may hold Gym Client at one brand plus Solo Client of an independent trainer in parallel. The PT-relationship state is orthogonal to gym membership.*

### 2.2 Jobs To Be Done

- **Client (any path):** "Tell me what to do next, and let me log it without typing." Remove cognitive load of remembering last session and friction of numeric input.
- **Client (PT-attached):** "Let me train *with* my trainer, not around them." Real-time set editing, prescribed-vs-actual feedback loops, social PR moments.
- **Trainer:** "Give me one place to author programs, track every client's adherence, and coach during the session — across all my clients whether they're at one gym or scattered." Programming + live coaching + asynchronous monitoring on one screen.
- **Gym Staff:** "Let me run member ops without spreadsheets." Member roster, PT activation, payment logging, reminder cadence, CSV import on day one.
- **Gym Owner:** "Show me which branches and which trainers are working — and which aren't." Cross-branch revenue, adherence, retention dashboards; trainer leaderboard.
- **All:** "Make my data feel mine, my equipment feel mine, my progress feel earned." Cinematic, equipment-aware UI; PR moments worth sharing.

### 2.3 Non-Users (v1)

- Group fitness classes / class-based bookings — out of scope (v2 candidate).
- Apple Watch / Wear OS companion users — explicitly deferred (post-v1; see addendum §5).
- Member-facing web portal — clients are mobile-only.
- Walk-in / non-paying members — only PT-active clients (gym path) or self-onboarded users (solo path) get the Client App.

### 2.4 Key User Journeys

*Three named scenes captured during Discovery (full source: `.decision-log.md` UJ-1/2/3 blocks). FRs reference journeys by ID inline.*

#### UJ-1. Priya and Aarav co-edit a lat-pulldown session, live.

**Persona + context:** Priya is a gym trainer at a single Vis-equipped branch; Aarav is her PT client three months in. Both are on the gym floor.

**Entry state:** Both authenticated. Priya has Aarav set as the focused client on her Trainer App home; Aarav has the Active Session view open on his Client App. Both devices on the same WebSocket session for this scheduled time block.

**Path:**
1. Priya unloads plates from the cable and queues set 1: drags the visual cable-pin to slab 8 (40 kg), sets reps target 12, RIR target 2. The set row appears instantly on Aarav's app with a "warm-up" toggle Priya can flip — she does, marking set 1 as warm-up.
2. Aarav performs the set. Priya watches form, then taps "Done" on her side; Aarav can also tap "Done" — last-write-wins. Auto-rest timer starts: 2:00 default for compounds (cable is configured isolation, so 1:00). Aarav sees the timer on his screen with a haptic cue at 0:10 remaining.
3. Sets 2-4: Priya bumps slab to 10 (50 kg) for set 2, reps 10. Aarav misses on rep 9 — Priya flips the assistance toggle for rep 9 → spotter-assisted; rep 10 stays unrecorded. She then types prescribed 50/10 for set 3 but Aarav lifts only 47.5/10 actual. Both prescribed and actual show.
4. Set 4 is the failure set: Priya flips "to failure" intent → RIR target locks to 0. Aarav hits 8 reps actual, RIR derived = 0. Progression Index recomputes in real time.
5. Mid-session, Priya tap-to-pairs the next exercise (face-pull) as a superset; tap-to-drops bring in a band-only drop set after the failure set.

**Climax:** Set 4 completion triggers the PR detection check; e1RM for lat-pulldown clears the prior best by 1.2 kg. Aarav's app fires the PR moment screen. Both see it.

**Resolution:** Session timer paused; Priya marks the session complete; Aarav lands on home with a refreshed Recovery Status chip (Back: red, 28h remaining). Progression Index, e1RM graph, and weekly volume all update.

**Edge cases:** Network drop on either device → app shows offline banner, queues writes; on reconnect, last-write-wins reconciles. WebSocket disconnect rate > 5% trips counter-metric SM-C2.

#### UJ-2. Rohit attempts a 200 kg squat PR, solo.

**Persona + context:** Rohit is a solo strength athlete, no PT, no gym affiliation; running a Vis-curated 5×3 squat block in week 8.

**Entry state:** Authenticated; opens Vis at the rack. Today's program shows squat 5×3 with prescribed 195 kg (last session's load).

**Path:**
1. Rohit warms up. Marks sets 1-3 as warm-up rows from the set-row long-press menu (excluded from volume + Progression Index).
2. Working sets 1-4 hit 195 kg / 3 clean. Rohit edits the prescribed load on set 5 → 200 kg via the barbell visualizer (snaps two 25 + one 20 + one 10 per side onto the bar; total auto-sums to 200).
3. Set 5: he grinds out 1 rep unassisted, gets 1 rep with a spotter assist (taps rep-quality bar after the set: rep 1 unassisted, rep 2 spotter-assisted). Sets RIR actual = 0.
4. App detects: 1RM load PR (200 kg) AND e1RM PR.

**Climax:** PR moment screen fires. Cinematic amber animation. Composer shows three aspect ratios (square, 9:16, 4:5) with Vis-designed template. Rohit picks 9:16, native share-sheet sends to Instagram Stories.

**Resolution:** Set saved with assistance metadata; PR card archived to his profile; Progression Index recomputes accounting for the spotter-assisted rep (counted as half-credit by the rule v1 — confirm at Finalize).

**Edge cases:** PR detected but user dismisses the moment screen → still archived; surfaces as a "you have an unshared PR" nudge once. SM-C10 watches PR-card generation with no share.

#### UJ-3. Aarav signs up under Priya in his first seven days.

**Persona + context:** Aarav has just paid a gym fee at a Vis-equipped branch; Anita (front-desk) hands him a QR linking to the gym's Vis tenant. He chooses gym path on first open.

**Entry state:** Unauthenticated. Aarav scans QR → signs in (Google) → his record is matched against Anita's pre-imported member roster (CSV imported the day he paid).

**Path:**
1. Onboarding questionnaire: ~10 fields (age, sex, height 175 cm, weight 78 kg, experience: novice, gap: 6 months sedentary, body-type: ectomorph, primary goal: hypertrophy, injuries: none, days/wk: 4, equipment: full gym, wearable opt-in: no). Skippable advanced section (sleep avg, current calories) — Aarav skips.
2. Trainer browse: scrolls available gym trainers at his branch, sees Priya's card (name, photo, certs verified badge, 4 years exp, hypertrophy + novice-coaching specialty tags, 4.8 rating, ~2h response time, Hindi + English). Picks Priya as #1 choice; falls back to two others.
3. Priya gets FCM push: new client request from Aarav. Opens her Trainer App → reviews questionnaire → accepts. Aarav's home unlocks his trainer panel.
4. Priya picks plan template: filters her view to "novice-hypertrophy" → picks the Vis starter "PPL Beginner 12-week" template → edits 3 swap-outs to suit Aarav's history → assigns as a phased plan (phase 1: weeks 1-4 form + volume tolerance, phase 2: weeks 5-8 progressive overload, phase 3: weeks 9-12 intensity peak).
5. Aarav sees today's workout, weekly grid for the next 12 weeks, and the assigned trainer card on home. First session is tomorrow.

**Climax:** Day 7 — Aarav has completed 3 sessions including one in-person with Priya (which played out as UJ-1). Adherence score = 100%; Progression Index trending up; first PR detected.

**Resolution:** Aarav is in steady state. Priya's home shows him as an active client with adherence 100%. Plan-block expiry FCM scheduled at week 4 minus 2 days (T-2 reminder + T-0 reminder until acknowledged).

**Edge cases:** Aarav's #1 trainer (Priya) declines or doesn't respond within 24h → next-in-priority gets the request. If all three decline → marketplace browse path opens. Trainer can decline + transfer with client consent, carrying the questionnaire + program.

## 3. Glossary

Downstream workflows and readers must use these terms exactly. FRs, UJs, and SMs use Glossary terms verbatim.

- **Tenant** — A gym brand (e.g. "Vis Fitness", "Iron Den") OR an independent trainer operating in solo path. Top-level isolation boundary. Solo tenants have a single virtual branch.
- **Brand** — A multi-branch gym chain. Tenant of type `gym`. Has 1..N **Branches**.
- **Branch** — A physical gym location within a Brand. All gym-path data scoped to one Branch.
- **Gym Trainer** — Trainer affiliated to a Brand at one or more Branches. Authored by Brand admin.
- **Solo Trainer** — Trainer operating independently. Self-onboarded. May additionally hold Gym Trainer roles.
- **Gym Client** — Client paying a Brand for gym + optional PT membership. Created by Gym Staff (CSV import or manual) or self-onboarded via Brand QR.
- **Solo Client** — Client not affiliated to any Brand. Self-onboarded. Picks Solo Trainer via invite-link or marketplace.
- **PT Relationship** — A trainer ↔ client engagement, with state `NoPt | PtActive | PtEnded`. Independent of gym membership. Carries a per-(trainer × client × exercise) **Progression Rule**.
- **Plan** — A trainer-authored or template-derived training program assigned to a client. Flat OR phased.
- **Phase** — A named block within a phased Plan with explicit start/end dates and an FCM-scheduled expiry reminder cadence.
- **Plan Template** — A reusable Plan blueprint. Three sources: Vis starter library, per-trainer private, brand-shared.
- **Session** — A scheduled or ad-hoc workout instance. Contains Exercises; each Exercise contains Sets.
- **Set** — A single load × reps record with prescribed and actual values. Carries warm-up flag, failure intent, RPE target + actual (1.0–10.0 in 0.5 steps), per-rep assistance tags. `RIR = 10 − RPE` is derived for display where novices prefer it.
- **RPE** — Rate of Perceived Exertion. Scale 1.0–10.0 in 0.5 steps (Tuchscherer / RP convention). Canonical exertion metric. `rpe_target` + `rpe_actual` on every working Set.
- **RIR** — Reps in Reserve. Derived from RPE for display: `RIR = round(10 − RPE)`. Not stored; computed on the fly.
- **Progression Index** — A per-(client × exercise) score computed as `w_e1rm · Δe1RM + w_vol · ΔVol + w_quality · ΔRPE_eff`, where weights `(w_e1rm, w_vol, w_quality)` are trainer-tunable per (client × goal). Drives suggested loads. Excludes warm-up Sets. Source: domain research §progression-mechanics.
- **Progression Rule** — AI-suggested rule applied per (trainer × client × exercise) v1. Other rule types (double-progression, linear, RPE-based, DUP) deferred to v1.5.
- **e1RM** — Estimated one-rep max. Formula by rep range: Epley (`load × (1 + reps/30)`) for reps ≤ 5; Brzycki (`load × 36 / (37 − reps)`) for reps 6–10; arithmetic mean of Epley + Brzycki at boundary cases. Source: domain research §e1rm-formula.
- **PR (Personal Record)** — Detected across four dimensions in v1: 1RM load, e1RM, total-volume, rep-count-at-given-load. Triggers PR moment screen + composer.
- **PR Card** — Shareable image rendered from one Vis template in three aspect ratios (square, 9:16, 4:5). Native share-sheet.
- **Equipment-Aware Logger** — UI pattern that mimics the physical machine for set entry. Six equipment types v1: cable pin-stack, barbell-and-plates, dumbbell rack, machine pin-stack, bodyweight (+ optional weighted vest field), kettlebell, bands.
- **Onboarding Questionnaire** — ~10-field intake (age, sex, height, weight, experience, gap, body-type/state, primary goal, injuries, days/wk, equipment, wearable opt-in) plus optional advanced section.
- **Trainer Marketplace** — Vis-wide opt-in directory of Solo Trainers, filterable by specialty, certification, language, rating, response time. Moderated.
- **Audit Log** — Append-only record of member edits, payment edits, role changes, plan-template publishes. Branch-scoped reads (Owner reads brand-wide). Enforced at DB level: no `DELETE` grant for any application role; only `INSERT` and `SELECT`.
- **Co-Edit** — Live multi-device editing of the same Session via WebSocket. Server-stamped monotonic last-write-wins per Set row: clients carry client-clock for local ordering, server stamps a monotonic version on receipt, reconciliation uses server-version.
- **Adherence** — Per-client metric. Numerator: number of prescribed Sets logged within the assigned Session's time window (± 12 h). Denominator: total prescribed Sets across active Plan. Expressed as a percentage. Drives FR-22 branch KPI and SM-7 trainer-monitoring target. Reset weekly for surface metrics; stored full-history for trend lines.
- **WAU (Weekly Active User)** — Distinct user IDs that authenticated at least once in a rolling 7-day window. Used in counter-metric denominators (AI cost / WAU, DPDPA tickets / WAU). Distinct from **WALS** which counts Sessions with ≥ 1 Set logged: a user can be WAU without being WALS-contributing.

## 4. Features

*Feature blocks A-K as accepted during Discovery (`.decision-log.md` decisions 36-45 + reconciliation 50-53). FRs numbered globally so downstream artifacts can reference stably. **Numbering is NOT a build-order signal** — FR-1 (Co-Edit) structurally depends on FR-3 (Set schema), FR-7 (Rest Timer), FR-8 (Equipment-Aware Logger), and FR-12 (AI Progression). Epic / sprint sequencing decisions belong to the architecture + sprint-planning phase. Inline UJ references mark which journey a capability realizes.*

### 4.1 Block A — Trainer↔Client In-Person Sessions

**Description:** The signature surface. Trainer and client edit the same Session on two devices simultaneously during the workout. All set rows accept prescribed AND actual values, warm-up flag, failure intent, RIR target, per-rep assistance tags. Progression Index updates after every working set. Realizes UJ-1, UJ-2.

**Functional Requirements:**

#### FR-1: Co-edited Session

Trainer and Client can edit the same Session on two devices via WebSocket. Last-write-wins per Set row.

**Consequences (testable):**
- Trainer edits Set 1 reps from 10 → 12; within ≤ 500 ms Client's UI reflects 12.
- Both devices write to the same Set within 50 ms of each other → final value matches the later server-received write; conflict logged.
- WebSocket disconnect on either side → app shows offline banner; writes queue; on reconnect, sync resumes within 5 s.

#### FR-2: Warm-up vs Working Set distinction

Either party can flag any Set row as warm-up. Warm-up Sets are excluded from Progression Index, total-volume PR detection, and weekly volume metrics. Realizes UJ-1.

**Consequences (testable):**
- Flagging Set 1 warm-up reduces session "working volume" but not "session volume" metric.
- Progression Index recomputes after every working-Set save; warm-up saves are no-ops to the Index.

#### FR-3: Prescribed vs Actual fields per Set

Every Set stores `prescribed_load`, `prescribed_reps`, `actual_load`, `actual_reps`, `rpe_target`, `rpe_actual` (1.0–10.0 in 0.5 steps), `source ∈ {TRAINER, AI, AI_BOOTSTRAP}`, `ai_locked` boolean. Progression Index reads `actual`. UI shows prescribed + actual when divergent. UI displays RIR (`10 − RPE`) alongside RPE per user-toggle preference. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Set with prescribed 50/10 @ RPE 8 and actual 47.5/10 @ RPE 9 displays both values with divergence cue.
- Progression Index uses only `actual_load`, `actual_reps`, `rpe_actual`.
- `source = AI` marks the Set as AI-prescribed; UI shows the ghost-overlay provenance.
- `ai_locked = true` removes the trainer-override surface (used for fully autopilot clients).

#### FR-4: Failure-intent flag + derived RPE

Trainer can flag a Set as "to failure". `rpe_target` locks to 10.0 (RIR = 0). `rpe_actual` measured from rep velocity or trainer/user input. Realizes UJ-1, UJ-2.

#### FR-5: Per-rep Assistance tagging

User (Trainer or solo Client) can label each rep within a Set as `unassisted | spotter_assisted | forced`. UI is a rep-quality bar after the Set. Affects PR detection (forced reps disqualify load-PR; spotter-assisted halves Progression Index credit `[ASSUMPTION: half-credit; confirm at Finalize]`). Realizes UJ-2.

#### FR-6: Superset + Drop-Set composition by gesture

Tap-to-pair adjacent exercises composes a Superset. Tap-to-drop on a Set then engaging the Equipment-Aware Logger picks the drop weight. No typing. Realizes UJ-1.

#### FR-7: Rest Timer

Auto-starts on Set save. Defaults: compound 2-3 min, isolation 60-90 s. Trainer override per (client × exercise). Haptic cue at 10 s remaining; visual progress.

**Consequences (testable):**
- Saving a Set on a compound lift starts a 150 s timer (default).
- Trainer override of "120 s" persists across sessions for that (client × exercise).

#### FR-8: Equipment-Aware Logger (six equipment types)

User logs Set load via the visualizer that matches the equipment:
- **Cable pin-stack** — drag visual pin to slab position.
- **Barbell** — tap plates onto each side; auto-sum to total.
- **Dumbbell** — number-line slider with kg/lb toggle; common-rack increments snap.
- **Machine pin-stack** — same pattern as cable.
- **Bodyweight** — body silhouette with optional weight-vest field.
- **Kettlebell** — discrete-step picker matching standard kettlebell weights.
- **Bands** — color-coded resistance-tier picker.

Numeric-keypad fallback always available. Defaults to last-used load for that (user × exercise). One-tap "same as last set" / "same as last session". Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- Opening cable exercise pre-loads the last slab position used.
- Picking 25 kg + 20 kg + 10 kg per side renders barbell visualizer totaling 130 kg (60 kg plates + 20 kg bar).
- Voice fallback (trainer in-session, ≥ 1 s of speech) parses "fifty for ten" → 50 / 10 [ASSUMPTION: voice fallback v1 scope to confirm at Finalize].

**Out of Scope (this FR):** Free-weight rack reservation, machine queuing, equipment recognition via camera.

**Feature-specific NFRs:**
- Set save → server commit ≤ 250 ms p95.
- Visualizer first-contentful render ≤ 200 ms after exercise tap.

**Notes:** `[NOTE FOR PM]` Equipment-aware loggers are the single biggest UX investment in v1. Cutting any one (e.g. bands) reduces the differentiator. Revisit at Finalize only if scope-pressure escalates.

### 4.2 Block B — Solo Program Builder + PR Cards

**Description:** Self-directed clients with no PT get a curated template marketplace, the Equipment-Aware Logger, Progression Index, e1RM graph, PR detection, and the PR Card composer. No AI programming (cost containment; v2 candidate). Realizes UJ-2.

**Functional Requirements:**

#### FR-9: Template Marketplace for Solo Clients

Vis ships ~15 curated public Plan Templates v1 (5×5, nSuns, PPL, RP-style hypertrophy, plus Vis-authored starters). User picks → edits → assigns to weekly grid. Blank-slate builder is v2.

#### FR-10: PR Detection (four dimensions)

System detects PRs across: 1RM load, e1RM, total-volume per Session, rep-count-at-given-load. Detection runs immediately after each Set save. Realizes UJ-2.

**Consequences (testable):**
- Set with `actual_load` > prior max `actual_load` for that exercise → 1RM PR fires.
- e1RM exceeds prior max (formula per Glossary: Epley for reps ≤ 5; Brzycki for reps 6–10; mean at boundary) → e1RM PR fires.

#### FR-11: PR Moment Screen + Card Composer

On PR detection, app shows a cinematic moment screen (amber animation, brand voice). One Vis template, three aspect ratios (square / 9:16 / 4:5). Native share-sheet to Instagram, Stories, WhatsApp Status, X, camera roll. **Vis watermark default ON, user-togglable OFF per share.** Toggle state persists per user across future shares. Locked at Finalize 2026-05-22. Realizes UJ-2.

**Consequences (testable):**
- Sharing to Instagram opens the platform native sheet pre-populated with the rendered card.
- First share for a user shows watermark ON; user toggle OFF persists for all future shares until toggled back.

### 4.3 Block C — Progression Engine

**Description:** The brain. AI-suggested progression v1 — Claude proxy with prompt caching writes next prescribed Set per (trainer × client × exercise). Trainer reviews + accepts or overrides via ghost overlay. AI is trainer-only v1 (cost containment); solo clients see AI-bootstrap defaults at first assignment then self-direct. Four additional rule types (double-progression, linear, RPE-based, DUP) deferred to v1.5. Realizes UJ-1, UJ-2, UJ-3.

**Functional Requirements:**

#### FR-12: AI Progression Rule per (trainer × client × exercise)

The system computes next-Set targets per (trainer × client × exercise) via Claude proxy with prompt caching. Inputs to the model: last N=12 Sets' (load, reps, rpe_actual), client goal, exercise class, days-since-last-session. Output: `{prescribed_load, prescribed_reps, rpe_target, decision ∈ {MAINTAIN, BUMP_REPS, BUMP_WEIGHT, DEMOTE}}`. Persisted per (trainer × client × exercise). On client switching trainer, the AI history persists; new trainer adopts or resets. Realizes UJ-1.

**Consequences (testable):**
- Toggling AI on for (Priya, Aarav, squat) → ghost suggestion appears for the next Session's squat exercise.
- Prompt cache hit-rate ≥ 80% on repeat suggestions for same (client × exercise) within 7 days.
- AI cost / WAU > $0.40/mo trips SM-C1.

#### FR-13: AI Bootstrap (first-assignment cold start)

On first exercise assignment with no prior Set history for that (client × exercise), the system computes a bootstrap prescription from: client questionnaire (experience, goal, sex, age), exercise compound/isolation class, conservative starting-load heuristic (≈ 50% bodyweight scaled by experience for compounds; isolation defaults). `source = AI_BOOTSTRAP`. Realizes UJ-3.

**Consequences (testable):**
- Aarav (novice, 78 kg) starts squat → bootstrap prescribed 40 kg × 8 @ RPE 7 (50%-of-bodyweight × experience-factor).
- First completed Set transitions `source` from `AI_BOOTSTRAP` to `AI` for subsequent suggestions.

#### FR-14: Next-Suggested ghost overlay + ai_locked toggle

Next prescribed value rendered as a ghost overlay on the Equipment-Aware Logger. One-tap "use suggestion" or trainer override. Per-(trainer × client × exercise) `ai_locked` toggle removes the override surface (autopilot mode). Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Ghost overlay shows AI suggestion in `--vis-amber-highlight` color; tap = accept; long-press = override input.
- `ai_locked = true` hides override controls; trainer can still unlock per (client × exercise).
- Trainer-override rate > 70% trips SM-C5 (AI failing).

### 4.4 Block D — Gym Staff / Branch Manager Operations

**Description:** Anita's surface. Member lifecycle, payment logging, PT activation, reminders. Branch-scoped. Realizes UJ-3 (the activation step).

**Functional Requirements:**

#### FR-15: Member CSV / Excel Import

Branch Staff can upload a CSV mapping columns to: name, phone, email, membership_type, membership_start, membership_end, pt_start, pt_end, trainer_assigned. Duplicate detection by phone number. Realizes UJ-3.

#### FR-16: PT Activation

Staff can activate, suspend, expire a member's PT. State drives Client App access gate (see FR-43).

#### FR-17: In-person Payment Logging

Staff records the cash / UPI / card payment amount, date, member, PT block extension. Generates audit-log entry (FR-26).

#### FR-18: Reminder Cadence

Staff can trigger WhatsApp / FCM reminder templates: PT expiry, missed sessions, payment overdue. v1 = generate-and-copy templates (P0); WhatsApp Business API direct send is P1.

#### FR-19: Trainer Cert Verification (admin gate)

Trainer uploads certification document; gym admin (Staff or Owner) reviews + approves → badge appears on the trainer's public profile card. P0.

### 4.5 Block E — Gym Owner Cross-Branch Operations

**Description:** Vikram's surface. All branches, all trainers, all revenue. Brand-scoped (no cross-brand visibility).

**Functional Requirements:**

#### FR-20: Cross-Branch Revenue Dashboard

Owner sees per-branch + brand-aggregate revenue, PT activation count, churn, trainer utilisation. Configurable date range.

#### FR-21: Trainer Leaderboard

Per-branch + brand-wide rankings on three independent dimensions:
- **Experience** — years active as Trainer in Vis-recognized practice (self-attested + cert-verified per FR-19).
- **Client Feedback** — mean of post-PT-block 5-star ratings; minimum n=3 ratings to qualify.
- **Client Progress** — median Progression Index Δ across the trainer's active clients over a rolling 90-day window. Δ computed per (client × exercise) as `(current Index − Index 90 days ago) / Index 90 days ago × 100`. Aggregated as median across the trainer's active client pool.

Realizes the Client onboarding-browse panel surface.

#### FR-22: Branch Health Snapshot

Per-branch KPIs: active members, active PT, weekly active logged Sessions (WALS), adherence avg, trainer-cert-pending count.

#### FR-23: Brand-Wide Audit Log Read

Owner reads brand-wide audit log (see FR-26). Cannot edit history.

### 4.6 Block F — Cross-Role Gym Surfaces

#### FR-24: Trainer Public Profile Card

Fields: name, photo, certifications + verified badge, years experience, specialty tags, client rating, response-time stat, languages. Visible in client browse + on assigned client's home. Realizes UJ-3.

#### FR-25: Client-Chosen Trainer Assignment (gym path)

Client picks top 1-3 trainers in priority order; first-come-first-serve accept. Trainer can decline + transfer with client consent, carrying the Onboarding Questionnaire + Plan. Realizes UJ-3.

**Consequences (testable):**
- Server enforces a single-active-state invariant on `(client_id, trainer_id, request_status=PENDING)`: simultaneous trainer-decline + client-cancel-and-reissue collapses to one winning write by server-stamped monotonic order (see Co-Edit Glossary entry); the loser receives a no-op + UI banner.
- Trainer accept while client has already cancelled the request → trainer sees "client withdrew" toast, no PtActive created.
- Two trainers accept at the same instant → first server-stamped accept wins; second receives "already assigned" response.

#### FR-26: Audit Log (P0)

Append-only log of: member edits, payment edits, role changes, plan-template publishes. Branch-scoped reads (Staff); brand-wide reads (Owner). Compliance + integrity. P0.

### 4.7 Block G — Onboarding & Plan Lifecycle

**Description:** Two onboarding paths (solo / gym) sharing one Questionnaire; one Plan model serving both flat and phased plans with expiry-aware reminders. Realizes UJ-3.

**Functional Requirements:**

#### FR-27: Onboarding Questionnaire (~10 fields)

Fields: age, sex, height (cm/in), weight (kg/lb), experience level, gap (months sedentary), body-type/state, primary goal, injuries (free-text), days/week, equipment access, wearable opt-in. Optional advanced: sleep avg, calorie estimate. Drives trainer-browse defaults + plan-template suggestions. Realizes UJ-3.

#### FR-28: PT Relationship State Machine

States: `NoPt | PtActive | PtEnded`. Transitions:
- `NoPt → PtActive` on trainer + client mutual accept.
- `PtActive → PtEnded` on either-party termination.
- `PtEnded → PtActive` on new trainer + client mutual accept (any number of times).
- `PtEnded → NoPt` if no new PT for a long inactive period [ASSUMPTION: inactive threshold TBD; track in Open Questions].

On `PtEnded`, the prior Trainer retains **read-only** access to past Plans they authored + alumni list. No re-engagement messaging v1.

**Mid-Session PT-end rule:** if either party terminates the PT relationship while a Co-Edit Session is in flight, server freezes new writes to that Session, flushes the in-flight WebSocket queue (clients receive ack for already-acked writes; pending writes return `409 conflict`), and marks the Session `terminated_during=true` for audit purposes. The Session remains readable to both parties (and the prior trainer, read-only) per FR-28 alumni surface.

#### FR-29: Plan Builder — Flat or Phased

Trainer can author a flat Plan (no phases) OR a phased Plan (named blocks with start + end dates). Phase transitions are explicit and client-visible. Plan validity window with expiry reminders pushed via FCM.

#### FR-30: Plan-Block Expiry Reminders

Trainer receives FCM push at T-2 days before Plan or Phase expiry, and at expiry, until acknowledged or new Plan/Phase assigned. Realizes UJ-3.

#### FR-31: Plan Templates (three sources)

Trainer sees: (1) Vis starter library (~15 curated templates v1), (2) per-trainer private library, (3) brand-shared library. RBAC determines edit / publish rights. Realizes UJ-3.

#### FR-32: Cardio Logging

v1 covers treadmill, elliptical, bike, rowing, free-form. Fields: mode + duration + avg HR + perceived effort. Equipment-Aware Logger doesn't cover these; minimal numeric + dropdown UI.

### 4.8 Block H — Solo Trainer Discovery & Marketplace

#### FR-33: Solo-Trainer Invite Link

Solo Trainer has a personal share link (default). Sharing the link routes a new client into the trainer's direct request inbox.

#### FR-34: Trainer Marketplace (opt-in)

Solo Trainer can opt in to the Vis-wide marketplace. Filters: specialty, certification, language, rating, response time. Trainer-side moderation queue at admin scope flags abuse / fake profiles. Marketplace abuse rate > 1% trips SM-C7. `[NOTE FOR PM]` Marketplace defer-to-v1.5 is a Finalize scope-pressure option (decision-log scope-risk flag).

### 4.9 Block I — Recovery, Volume, Load Monitoring (shared client tabs)

#### FR-35: Muscle Recovery Status

Color-coded chips per muscle group (Chest, Upper Back, Lower Back, Shoulders, Biceps, Triceps, Forearms, Quads, Hamstrings, Glutes, Calves, Core). Computed from the last Session that trained that group. Green = recovered; red = hours remaining.

#### FR-36: Per-Muscle Weekly Set Count

System tracks working Sets per muscle group per week (rolling 7-day window). Surfaces a chip per muscle group on the Progress Tab: current count vs Schoenfeld-derived ceiling (≈ 20 sets / muscle / week for hypertrophy). Amber state at 16-20; red state at > 20 ("you're overcooking this muscle"). Source: domain research §training-science.

**Consequences (testable):**
- Aarav has logged 14 chest Sets this week → chest chip displays "14 / 20"; green state.
- Logging Set 21 → chest chip flips to red + Trainer App notification surfaces (if PT-attached).

#### FR-37: ACWR Trainer Alert

System computes Acute:Chronic Workload Ratio per client per week (`acute = last 7 days volume; chronic = trailing 28-day rolling average`). When ACWR > 1.5, the Trainer App fires an amber alert on the client's profile card ("Aarav: ACWR 1.7 — injury-risk zone"). Source: IOC-grounded; domain research §training-load. Realizes the trainer-monitoring JTBD.

**Consequences (testable):**
- Aarav: last-7-day volume 12,400 kg, trailing-28 average 7,800 kg → ACWR 1.59 → amber alert.
- ACWR > 2.0 → red alert + automatic deload suggestion via FR-12 ghost overlay (next Session: 70% prescribed loads).
- **Cold-start suppression:** for the first 14 calendar days of any client's logging history, ACWR alerts are suppressed (chronic baseline is not yet representative). Trainer App shows a "baseline-building (day X / 14)" chip instead. After day 14, chronic is computed from the available window even if < 28 days, with a "preliminary" qualifier until day 28.

#### FR-38: Progress Tab

Body Measurements over time (weight, body fat %, chest/waist/hip/arm/thigh — line charts). Strength Progress (e1RM per exercise over time). Attendance Calendar heatmap. Per-muscle weekly set-count strip (FR-36). ACWR sparkline (FR-37) visible to client + trainer.

#### FR-39: Profile Tab

Name, photo, branch (if gym path), membership status + PT expiry (if applicable), default daily visit time + per-day overrides + rest days, fitness goals, current body stats summary, assigned trainer card.

### 4.10 Block J — Nutrition (Trainer-Authored)

**Description:** Trainer authors a per-client daily macro target + meals breakdown. Client reads only. No AI macro generation v1 (cost containment; v2 candidate). Realizes the "tell me what to eat" JTBD restored from original spec §7.6.

**Functional Requirements:**

#### FR-40: Trainer Macro Plan Authoring

Trainer App provides a per-client daily macro editor: total calories, protein (g), carbs (g), fat (g), water (L). Optional weekly variants (e.g. higher-carb on training days). Free-text guidance field (200 char). Realizes the "give clients structured nutrition" trainer JTBD.

**Consequences (testable):**
- Trainer saves Aarav's macros: 2600 kcal / 180P / 280C / 80F → reflected on Aarav's Nutrition tab within next app open.
- Weekly variant "training-day" overrides default on assigned workout days only.

#### FR-41: Client Nutrition Tab (read-only)

Client App Nutrition tab displays: today's macro targets, meals breakdown (Breakfast, Pre-workout, Lunch, Post-workout, Dinner), per-meal food items + quantities + macros, water intake target. Read-only. No client edits v1.

**Out of Scope (this FR):**
- AI macro generation (v2)
- Food logging / barcode scanning (v2)
- Calorie deficit/surplus tracking (v2)

### 4.11 Block K — Authentication & Access Gate

#### FR-42: Authentication

Google Sign-In, Apple Sign-In (iOS required), Phone OTP via Firebase Auth. Backend (Spring Boot) validates Firebase JWT on every request.

#### FR-43: Access Gate by Role

Solo path: any authenticated user lands on solo onboarding. Gym path: user record matched on phone / email against the brand's roster; if no match → "pending activation" screen until Staff activates them. PT lapse → "PT membership expired" screen (data retained).

## 5. Non-Goals (Explicit)

- **Not** a group-fitness app. Class booking, recurring class registration, and class attendance are out (v2 candidate).
- **Not** a payment gateway. Vis logs payments staff make in-person; Vis does not process them.
- **Not** a wearable / Apple Watch app v1 (deferred — addendum §5).
- **Not** a messaging platform. Trainer ↔ client communication outside Sessions happens on WhatsApp; Vis ships WhatsApp template generation + send-link only.
- **Not** a video content platform. Exercise demos are static images / links v1; trainer-uploaded videos are v2.
- **Not** a member-facing web portal. Clients are mobile-only.
- **Not** writing to Apple Health / Google Fit. Read-only on device; not stored in backend.
- **Not** an AI coach for solo users v1. Cost containment. AI is Trainer-only.
- **Not** multi-region v1. Cloud Run asia-south1 only. Multi-region failover is v2.
- **Not** offering SMS v1. v1 = FCM push + email transactional. SMS is v2.

## 6. MVP Scope

### 6.1 In Scope

- All six role-contexts (solo trainer, solo client, gym trainer, gym client, gym staff, gym owner) ship day 1.
- All six Equipment-Aware Loggers (cable, barbell, dumbbell, machine pin-stack, bodyweight, kettlebell, bands).
- Real-time WebSocket co-edit between Trainer App and Client App with last-write-wins.
- **AI Progression Rule** (Claude proxy with prompt caching) + AI Bootstrap for first assignment + `ai_locked` autopilot toggle. Trainer-only v1; solo clients see AI bootstrap defaults then self-direct.
- Progression Index computed as `w_e1rm·Δe1RM + w_vol·ΔVol + w_quality·ΔRPE_eff` with trainer-tunable weights per (client × goal).
- e1RM formula: Epley for reps ≤ 5; Brzycki for reps 6-10; arithmetic mean at boundary.
- RPE 1.0-10.0 (0.5 steps) primary; RIR (`10 − RPE`) derived for display per user preference.
- PR detection across four dimensions + PR Card composer (1 template, 3 aspect ratios) + native share-sheet.
- **Per-muscle weekly set count** vs Schoenfeld ~20-set ceiling, chip on Progress Tab with amber/red states.
- **ACWR trainer alert** (Acute:Chronic Workload Ratio > 1.5 amber; > 2.0 red + auto-deload via ghost overlay).
- **Trainer-authored Nutrition module** — Trainer macro plan + Client read-only Nutrition tab. No AI macro v1.
- **Solo-trainer invite-link** at launch (FR-33). Trainer Marketplace opt-in (FR-34) deferred to v1.5 (see §6.2).
- Vis starter Plan template library (~15 templates) + per-trainer + brand-shared.
- Flat OR phased Plans with FCM expiry reminders.
- Onboarding Questionnaire shared across both paths.
- Branch Staff CSV import + PT activation + payment logging + WhatsApp / FCM reminder cadence (generate-and-copy templates).
- Trainer cert verification flow.
- Cross-branch Owner dashboards + trainer leaderboard + brand-wide audit log read.
- Cardio logging (treadmill, elliptical, bike, rowing, free-form).
- Global units day 1 (kg + lb, m + ft/in toggle, per-locale dates, time zones).
- i18n keys structured day 1; English-only translation v1.
- Audit log P0 (member, payment, role, plan-template events).

### 6.2 Out of Scope for MVP

- **v1.5 candidates** (Finalize scope-pressure first-cut):
  - Trainer Marketplace opt-in (defer; ship invite-link only at launch) `[NOTE FOR PM]`
  - Brand-shared Plan template library (ship Vis starter + per-trainer only) `[NOTE FOR PM]`
  - Flat Plan toggle (default to phased) `[NOTE FOR PM]`
  - **Additional Progression Rules** (double-progression, linear, RPE-based, DUP). AI-only v1 covers the autopilot path; rule-based options for trainers who reject AI come in v1.5.
- **v2:**
  - AI macro generation (nutrition).
  - Client-side food logging / barcode scanning.
  - Calorie deficit/surplus tracking.
  - AI programming for solo users.
  - Blank-slate solo program builder (template-only at launch).
  - WhatsApp Business API direct-send.
  - SMS notifications.
  - Trainer-uploaded exercise demo videos.
  - Re-engagement messaging on `PtEnded`.
  - Multi-region failover.
  - Apple Watch / Wear OS companion app (addendum §5).
- **Never (v1 firm):**
  - Group-class booking.
  - Vis-processed payments.
  - Member-facing web portal.
  - Apple Health / Google Fit write-back.

## 7. Cross-Cutting NFRs

System-wide quality attributes from Discovery decisions G-N (`.decision-log.md` decision 41-45 + addendum §4).

### 7.1 Internationalization

- **Weight:** stored canonical kg; UI converts to kg or lb per user preference.
- **Distance / height:** stored canonical m; UI converts to m or ft+in.
- **Dates:** ISO 8601 internal; per-locale display (en-IN → DD/MM/YYYY; en-US → MM/DD/YYYY).
- **Time zones:** per-user with branch-default fallback.
- **Currency:** multi-currency-ready data model (no billing surface v1).
- **Language:** English-only translation v1; i18n keys structured for future translations.

### 7.2 Real-Time Sync

- WebSocket transport for Co-Edit sessions. Reconciliation strategy: last-write-wins per Set row.
- WebSocket disconnect rate > 5% trips counter-metric SM-C2.
- Offline queue persists pending writes; reconnect drains within 5 s.

### 7.3 AI

- Trainer-only feature v1. Solo users do not see AI surfaces.
- Implementation: Claude proxy with prompt caching; cache target ≥ 80% hit rate on (trainer × client × exercise) repeats.
- Cost ceiling: $0.40 / WAU / month (SM-C1). **Runtime circuit-breaker:** at $0.80 / WAU rolling-7-day average, system auto-throttles by reducing inference frequency (cap one suggestion per (client × exercise) per 24 h, served from cache otherwise). $1 / WAU triggers pivot.
- **API failure fallback:** on Claude API error (empty / null / HTTP 5xx), ghost overlay falls back to last-prescribed value with `stale-ai` indicator. Trainer sees the indicator and can manually override.

### 7.4 Compliance + Privacy

- **DPDPA (India)** — consent capture at onboarding; right-to-erasure flow visible in Profile → Account → Delete.
- **GDPR readiness** — same erasure + portability surfaces enabled globally (free-text export of user's own data).
- DPDPA support tickets > 0.5% WAU / wk trips SM-C4.
- Body measurement + health-derived data classified Sensitive; encrypted at rest; never logged in plaintext.

### 7.5 Performance

- Set save → server commit p95 ≤ 250 ms (SM-C6).
- Equipment-Aware Logger first-contentful render ≤ 200 ms.
- **Idempotency:** every Set write carries a client-generated UUID; server dedupes by UUID; on logger crash + retry the same UUID returns the prior commit (no double-writes, no silent loss).
- App cold start ≤ 2.0 s on mid-tier Android (target: Pixel 6a, Galaxy A54).
- Web (Admin) FCP ≤ 1.5 s on broadband; LCP ≤ 2.5 s.

### 7.6 Notifications

- v1: FCM push + email transactional.
- Reminder events: PT expiry, plan-block expiry T-2 / T-0, missed session, trainer client-request, PR shareable.
- SMS, WhatsApp Business API direct-send → v2 / P1.

### 7.7 Accessibility

- WCAG 2.1 AA target on Admin Web v1.
- Mobile apps: VoiceOver / TalkBack labels on every interactive element; minimum hit target 44 × 44 pt; reduce-motion respects OS preference; the orb visual language degrades gracefully to static states under reduce-motion.

### 7.8 Observability

- Counter-metric stream wired day 1 (see §8 SM-C1 through SM-C10).
- Per-Session distributed trace including device → backend → DB write for FR-1.
- Crash-free target ≥ 99.0% (SM-C5).
- Real-time dashboard for: WebSocket disconnect rate, AI cost/WAU, p95 set-save latency, crash-free.

### 7.9 Operational

- Single-region v1 (Cloud Run asia-south1).
- Backups: PostgreSQL daily snapshots + WAL streaming. RPO 5 min; RTO 4 h v1.
- On-call expectations not yet defined — see Open Questions.

## 8. Success Metrics

*Primary metrics validate FRs. Counter-metrics counterbalance: if any tracks against target, the optimization is going wrong.*

### Primary

- **SM-1 (North Star): Weekly Active Logged Sessions (WALS)** — Sessions with ≥ 1 Set logged in a week. **Target:** 2,500 by D90; 12,000 by D180. Validates the whole product.
- **SM-2: D7 retention** — % of new signups still logging in week 2. **Target:** ≥ 35% by D90. Validates FR-1, FR-8 differentiators.
- **SM-3: Real-Time Co-Edit usage** — % of Trainer-led Sessions with at least one Co-Edit write from each side. **Target:** ≥ 25% by D90. Validates FR-1 (the structural moat).
- **SM-4: Solo D30 retention** — % of solo signups still logging at D30. **Target:** ≥ 10% by D180 (pivot trigger if below). Validates FR-9, FR-10, FR-11.

### Secondary

- **SM-5: PR Card share rate** — % of detected PRs that the user shares to any platform. **Target:** ≥ 30% by D180. Validates FR-11 (social moat).
- **SM-6: Onboarding completion** — % of new signups completing Questionnaire + first Set within 24 h. **Target:** ≥ 60% by D90. Validates FR-27, FR-38, FR-39.
- **SM-7: Trainer adherence-monitoring usage** — % of Gym Trainers checking client adherence at least 3×/week. **Target:** ≥ 50% by D180. Validates FR-22.
- **SM-8: Brand-wide audit-log query** — Owner queries / brand / week. **Target:** ≥ 2 / week steady-state. Validates FR-23, FR-26.

### Counter-metrics (do not optimize)

- **SM-C1: AI cost / WAU** > $0.40 / mo — counterbalances SM-1 (don't grow WALS by leaning AI heavier). Pivot trigger > $1.
- **SM-C2: WebSocket disconnect rate** > 5% — counterbalances SM-3 (don't claim Co-Edit if it drops).
- **SM-C3: Crash-free rate** < 99.0% — counterbalances SM-1, SM-2 (don't ship features at quality cost).
- **SM-C4: DPDPA support tickets** > 0.5% WAU / wk — counterbalances SM-1 (don't grow at compliance cost).
- **SM-C5: Trainer-override rate** > 70% on AI suggestions — counterbalances SM-1 (AI suggestions failing if trainer overrides most of them).
- **SM-C6: Set-save p95 latency** > 1.5 s — counterbalances FR-1, FR-8.
- **SM-C7: Marketplace abuse** > 1% of profiles — counterbalances FR-34.
- **SM-C8: New-signup-no-log rate** > 50% — counterbalances SM-6 (signups without first Set are dead weight).
- **SM-C9: Batch logging > 24h after Session** > 30% of Sessions — counterbalances FR-1 (Sessions should be logged live, not after).
- **SM-C10: PR-card generation with no share** > 80% — counterbalances SM-5 (composer is hollow if nobody shares).

### Pivot Triggers

- D7 retention < 20% sustained for 8 weeks → reconsider differentiator hypothesis.
- Real-time Co-Edit usage < 10% by D180 → reconsider structural moat claim.
- AI cost / WAU > $1 sustained → kill AI suggestion or restrict tighter than trainer-only.
- Solo D30 < 10% by D180 → solo path doesn't carry; refocus on gym path.

## 9. Open Questions

*Deferred items with owner + revisit condition. Phase-blockers resolved at Finalize; remaining items are non-blocking for UX / architecture / epics phase but should be revisited by the named owner before the listed condition.*

1. **PT inactive-threshold:** how long must `PtEnded` persist with no new trainer + no client activity before transitioning to `NoPt`? *Owner: PM. Revisit: before first alumni-retention sweep.*
2. **Spotter-assisted rep weighting in Progression Index** (FR-5): half-credit assumed v1; alternatively exclude entirely or RPE-penalize. *Owner: PM + trainer-coach reviewer. Revisit: at first PR-card-with-assisted-rep edge case in beta.*
3. **Voice-input fallback scope v1** (FR-8): is "≥ 1 second of speech → weight + reps" inside v1 or v1.5? *Owner: PM + Architect. Revisit: at architecture phase scope gate.*
4. **On-call expectations:** single-owner shop until first hire. *Owner: PM. Revisit: before WAU > 1k.*
5. **Trainer dual-context conflict resolution:** when a Solo Trainer is also a Gym Trainer at brand X and engages the same client through both contexts, which (trainer × client × exercise) AI history wins? *Owner: Architect. Revisit: at architecture phase, FR-12 design.*
6. **Marketplace moderation SLA:** response-time target for abuse reports. Affects SM-C7 design. *Owner: PM. Revisit: at v1.5 marketplace re-introduction.*
7. **Plan-block expiry FCM cadence beyond T-2 + T-0:** do we escalate (T+2, T+7) if trainer never acknowledges? *Owner: PM. Revisit: after first 90 days of FCM-expiry telemetry.*
8. **Branch-level data residency:** brand-wide opt-in for region pinning (EU customer brands). Affects multi-region v2 timing. *Owner: Architect + Legal. Revisit: at first EU brand sign-up signal.*
9. **Progression Index weight defaults** per goal (hypertrophy / strength / endurance / general): defaults need locking even though trainer-tunable. *Owner: PM + trainer-coach reviewer. Revisit: before first solo-client bootstrap goes live.*
10. **WALS / D7 / D30 target derivation:** current targets (WALS 2,500 D90; D7 35%) are aspirational, not benchmarked. Anchor in Hevy / Trainerize / FitBudd public funnels OR TAM × adoption-rate × channel-mix. *Owner: PM + GTM. Revisit: at first 30 days of pre-launch landing-page signups.* `[ASSUMPTION]`

## 10. Assumptions Index

Every inline `[ASSUMPTION: …]` surfaced for confirmation:

- §4.1 FR-5 — Spotter-assisted rep counts as half-credit in Progression Index. Could be excluded entirely or RPE-penalized.
- §4.1 FR-8 — Voice fallback ("≥ 1 s of speech → weight + reps") is in v1 scope.
- §4.7 FR-28 — Inactive threshold for `PtEnded → NoPt` transition unset (Open Question 1).
- §4.4 FR-19 — Cert verification by either Staff or Owner; division between them not specified.
- §4.3 FR-13 — Conservative starting-load heuristic ("≈ 50% bodyweight scaled by experience for compounds") — needs trainer-coach validation.
- §7.5 — Mid-tier Android target Pixel 6a / Galaxy A54.
- §7.9 — RPO 5 min, RTO 4 h v1.

## 11. Aesthetic + Tone (PRD-level pointer)

Vis carries a deliberate visual + voice identity that downstream UX work must honor. This PRD specifies the *capability* surface; visual + animation + copy detail lives in the UX spec phase, sourced from these canonical references:

- **Original spec §7.4** — Orb-as-organism tension states (At rest / Under load / Peak tension / Recovered) with named animations; stacked-orb weekly view; trainer-client connection-orb in live Sessions.
- **Original spec §17.1-17.6** — Liquid Glass material principles, warm-orange palette tokens, Geist typography scale, motion vocabulary (easing, durations, breathing loop, shimmer), light + dark theme strategy.
- **CLAUDE.md "Brand Palette"** — Canonical hex tokens (`--vis-amber-*`, `--gc-*`). Use CSS vars; never hard-code hex.
- **`docs/brand-ref/reference_light.png` + `reference_dark.png`** — Canonical UI references (home card, recovery orb, list cards, login). Read before any UI work.
- **`prototype/`** — Working implementation. Single source of truth for orb-fill recipe, gradient stacks, motion timing.

**Tone anchors:** restraint over showiness; warm not cool; cinematic-amber molten emissive, never neon gaming orange; one icon only per surface; "earned" beats "celebrated" for progress moments. Page bg is warm ivory, never pure white (cards may use `#FFFFFF` as exception). Shadows are peach-tinted, never neutral black. Light + dark theme parity is a v1 deliverable, not a v2 enhancement.

**Light + dark theme parity** ships v1 (in scope §6.1; not v1.5).

---

*Discovery audit + non-PRD depth → `.decision-log.md` + `addendum.md`. Companion HTML → `prd.html`. Status flips to `final` at Finalize step 7.*
