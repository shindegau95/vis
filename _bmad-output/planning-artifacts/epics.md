---
stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics", "step-03-create-stories"]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
  - _bmad-output/planning-artifacts/architecture.md
  - _bmad-output/planning-artifacts/ux-design-specification.md
  - _bmad-output/planning-artifacts/implementation-readiness-report-2026-05-24.md
---

# Vis - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Vis, decomposing requirements from the PRD, UX Design Spec, and Architecture into implementable stories.

---

## Requirements Inventory

### Functional Requirements

FR-1: Co-edited Session — Trainer + Client edit same Session on two devices via WebSocket. Last-write-wins per Set row (≤500ms sync; offline queue drains within 5s on reconnect; conflict logged).
FR-2: Warm-up vs Working Set — Either party flags any Set row as warm-up. Excluded from Progression Index, total-volume PR detection, and weekly volume metrics.
FR-3: Prescribed vs Actual fields per Set — Every Set stores prescribed_load, prescribed_reps, actual_load, actual_reps, rpe_target, rpe_actual (1.0–10.0, 0.5 steps), source ∈ {TRAINER,AI,AI_BOOTSTRAP}, ai_locked boolean. UI shows prescribed + actual when divergent.
FR-4: Failure-intent flag + derived RPE — Trainer flags Set "to failure"; rpe_target locks 10.0 (RIR=0). rpe_actual from input.
FR-5: Per-rep Assistance tagging — Label each rep: unassisted | spotter_assisted | forced. Via rep-quality bar after Set. Forced reps disqualify load-PR; spotter-assisted = half-credit in Progression Index.
FR-6: Superset + Drop-Set composition by gesture — Tap-to-pair adjacent exercises = Superset. Tap-to-drop + Equipment-Aware Logger = drop set. No typing.
FR-7: Rest Timer — Auto-starts on Set save. Compound default 150s; isolation 75s. Trainer override per (client × exercise). Haptic at 10s remaining; visual progress.
FR-8: Equipment-Aware Logger (6 types) — Cable pin-stack (drag), barbell plate-snap (auto-sum), dumbbell number-line slider, machine pin-stack, bodyweight + vest field, kettlebell discrete picker, bands resistance-tier picker. Numeric keypad fallback always available. Pre-loads last-used weight per (user × exercise). One-tap "same as last set" / "same as last session".
FR-9: Template Marketplace for Solo Clients — ~15 curated Plan Templates (Vis starter library). Client picks → edits → assigns to weekly grid. Blank-slate builder v2.
FR-10: PR Detection (4 dimensions) — 1RM load, e1RM (Epley ≤5 reps; Brzycki 6-10 reps; mean at boundary), total-volume per Session, rep-count-at-given-load. Fires immediately after each Set save.
FR-11: PR Moment Screen + Card Composer — Cinematic amber moment screen on PR detection. 1 Vis template, 3 aspect ratios (square / 9:16 / 4:5). Vis watermark default ON, user-togglable OFF per share (persists). Native share-sheet (Instagram, WhatsApp Status, X, camera roll). In-app RN+Skia render; shareable PNG via headless worker → GCS.
FR-12: AI Progression Rule per (trainer × client × exercise) — Claude proxy with prompt caching. Inputs: last 12 Sets (load, reps, rpe_actual), client goal, exercise class, days-since-last-session. Output: prescribed_load, prescribed_reps, rpe_target, decision ∈ {MAINTAIN,BUMP_REPS,BUMP_WEIGHT,DEMOTE}. Cache target ≥80% hit rate. Trainer-only v1.
FR-13: AI Bootstrap (cold start) — First exercise assignment with no prior Set history: bootstrap from questionnaire (experience, goal, sex, age, exercise class). source = AI_BOOTSTRAP. Conservative heuristic (~50% bodyweight for compounds, scaled by experience).
FR-14: Next-Suggested ghost overlay + ai_locked toggle — Ghost overlay on Equipment-Aware Logger showing AI suggestion. 30s TTL self-dismissal. One-tap accept; long-press override. ai_locked removes override surface (autopilot). Confidence display. Stale-ai indicator on API failure.
FR-15: Member CSV / Excel Import — Staff uploads CSV; maps columns to name, phone, email, membership_type, membership_start, membership_end, pt_start, pt_end, trainer_assigned. Duplicate detection by phone number.
FR-16: PT Activation — Staff activates, suspends, expires member PT. State drives Client App access gate (FR-43).
FR-17: In-person Payment Logging — Staff records cash/UPI/card amount, date, member, PT block extension. Generates audit log entry.
FR-18: Reminder Cadence — WhatsApp / FCM reminder templates: PT expiry, missed sessions, payment overdue. v1 = generate-and-copy (P0); direct-send v2.
FR-19: Trainer Cert Verification — Trainer uploads cert doc; Staff or Owner reviews + approves → verified badge on public profile card. P0.
FR-20: Cross-Branch Revenue Dashboard — Owner sees per-branch + brand-aggregate revenue, PT activation count, churn, trainer utilisation. Configurable date range.
FR-21: Trainer Leaderboard — Per-branch + brand-wide rankings on 3 dimensions: Experience (years), Client Feedback (5-star avg; n≥3 to qualify), Client Progress (median Progression Index Δ over rolling 90-day window).
FR-22: Branch Health Snapshot — Per-branch KPIs: active members, active PT, WALS, adherence avg, trainer-cert-pending count.
FR-23: Brand-Wide Audit Log Read — Owner reads brand-wide audit log. Cannot edit history.
FR-24: Trainer Public Profile Card — Name, photo, certifications + verified badge, years experience, specialty tags, client rating, response-time stat, languages.
FR-25: Client-Chosen Trainer Assignment (gym path) — Client picks top 1–3 trainers priority-ordered; first-accept wins. Server-stamped monotonic order enforces single-active invariant. Trainer can decline + transfer with client consent, carrying questionnaire + plan.
FR-26: Audit Log P0 — Append-only log of member edits, payment edits, role changes, plan-template publishes. Branch-scoped reads (Staff); brand-wide reads (Owner). No DELETE grant for any app role. Monthly range partitions day 1.
FR-27: Onboarding Questionnaire (~10 fields) — Age, sex, height (cm/in), weight (kg/lb), experience, gap (months sedentary), body-type/state, primary goal, injuries (free-text), days/week, equipment access, wearable opt-in. Optional advanced: sleep avg, calorie estimate.
FR-28: PT Relationship State Machine — NoPt | PtActive | PtEnded. Mid-session PT-end freezes writes, flushes WS queue, marks Session terminated_during=true. Prior trainer retains read-only alumni access.
FR-29: Plan Builder (Flat or Phased) — Trainer authors flat OR phased Plan (named blocks with start/end dates). Phase transitions client-visible. Expiry reminders via FCM.
FR-30: Plan-Block Expiry Reminders — FCM push at T-2 days and T-0 until acknowledged or new Plan/Phase assigned.
FR-31: Plan Templates (3 sources) — Vis starter (~15 curated), per-trainer private library, brand-shared library. RBAC on edit/publish rights.
FR-32: Cardio Logging — Treadmill, elliptical, bike, rowing, free-form. Fields: mode + duration + avg HR + perceived effort. Minimal numeric + dropdown UI.
FR-33: Solo-Trainer Invite Link — Personal share link routes new client to trainer's request inbox. Default at launch.
FR-34: Trainer Marketplace (opt-in) — Deferred to v1.5. Ship invite-link only (FR-33) at launch.
FR-35: Muscle Recovery Status — Color-coded chips for 12 muscle groups. Green = recovered; red = hours remaining. Computed from last Session that trained each group.
FR-36: Per-Muscle Weekly Set Count — Working Sets per muscle group per rolling 7-day window vs Schoenfeld ceiling (~20 sets/muscle/week). Amber chip at 16–20; red at >20.
FR-37: ACWR Trainer Alert — Acute:Chronic Workload Ratio per client/week. ACWR >1.5 = amber alert on Trainer App. ACWR >2.0 = red + auto-deload suggestion via ghost overlay (70% loads). Cold-start suppression for first 14 days ("baseline-building day X/14" chip). Preliminary qualifier until day 28.
FR-38: Progress Tab — Body measurements over time (line charts), e1RM per exercise over time, attendance calendar heatmap, per-muscle weekly set-count strip (FR-36), ACWR sparkline (FR-37).
FR-39: Profile Tab — Name, photo, branch (gym path), membership status + PT expiry, default daily visit time + per-day overrides + rest days, fitness goals, body stats summary, assigned trainer card.
FR-40: Trainer Macro Plan Authoring — Per-client daily macros: total kcal, protein (g), carbs (g), fat (g), water (L). Optional weekly variants (e.g. higher-carb on training days). 200-char free-text guidance field.
FR-41: Client Nutrition Tab (read-only) — Today's macro targets, meals breakdown (5 meals: Breakfast, Pre-workout, Lunch, Post-workout, Dinner), per-meal food items + quantities + macros, water intake target. Read-only v1.
FR-42: Authentication — Google Sign-In, Apple Sign-In (iOS required), Phone OTP via Firebase Auth. Backend validates Firebase JWT on every request.
FR-43: Access Gate by Role — Solo path: authenticated user → solo onboarding. Gym path: match phone/email against branch roster; no match → "pending activation". PT lapse → "PT membership expired" screen. Data retained.

**Total FRs: 43**

### NonFunctional Requirements

NFR-1 (Internationalization): Weight stored canonical kg; height canonical m; ISO 8601 internal dates; per-locale display (en-IN DD/MM/YYYY, en-US MM/DD/YYYY); per-user time zones with branch-default; multi-currency-ready data model; English-only v1 with i18n keys structured for future translations.
NFR-2 (Real-Time Sync): WebSocket transport; raw @ServerEndpoint + Redis Pub/Sub (Path A + session-affinity); LWW per Set row with server-stamped monotonic version; disconnect rate >5% trips SM-C2; offline queue persists; reconnect drains within 5s; 55-min Cloud Run forced reconnect cycle.
NFR-3 (AI): Trainer-only v1. Claude proxy with prompt caching; stable cacheable prefix (client_id + exercise_id + 90-day set log hash) + dynamic suffix; cache ≥80% hit. Cost ceiling $0.40/WAU/mo; circuit-breaker at $0.80 (throttle 1/24h); $1.00 = pivot. API failure fallback: last-prescribed + stale-ai indicator. Ghost overlay 30s TTL hard discard.
NFR-4 (Offline-First): WatermelonDB on op-sqlite. sync_state: local_only → queued → acked → reconciled. Client UUID idempotency on Set writes; server dedupes by UUID.
NFR-5 (Compliance/Privacy): DPDPA 8 obligations + GDPR 6 endpoints. Consent capture at onboarding. Right-to-erasure in Profile → Account → Delete (/api/v1/me/delete). Data portability export (/api/v1/me/export). Body/health data encrypted at rest; never logged in plaintext. DPDPA tickets >0.5% WAU/wk = SM-C4. Min-age gate (DPDPA §9 — policy decision open).
NFR-6 (Performance): Set save p95 ≤250ms (SM-C6 trips at >1.5s). Equipment-Aware Logger FCR ≤200ms. App cold start ≤2.0s on Pixel 6a / Galaxy A54. Admin FCP ≤1.5s; LCP ≤2.5s on broadband.
NFR-7 (Notifications): FCM via Firebase Admin SDK. Events: PT expiry, plan-block expiry T-2/T-0, missed session, trainer client-request, PR shareable nudge. Device tokens in user_devices table. SMS v2.
NFR-8 (Accessibility): WCAG 2.1 AA on Admin Web. VoiceOver/TalkBack labels on every interactive element. Min 44×44pt hit targets. Reduce-motion + reduce-transparency OS-preference respected; orb degrades gracefully to static states.
NFR-9 (Observability): Counter-metric stream day 1 (SM-C1 through SM-C10). Per-session distributed trace device→backend→DB. Crash-free ≥99.0% (SM-C3). Real-time dashboard: WS disconnect rate, AI cost/WAU, p95 set-save latency. ai.cost.wau_spend custom Cloud Monitoring metric per trainer.
NFR-10 (Operational): Cloud Run asia-south1. PostgreSQL daily snapshots + WAL streaming. RPO 5 min; RTO 4h. Flyway migrations. GH Actions monorepo CI + EAS Build mobile. Secrets via Google Secret Manager.

**Total NFRs: 10**

### Additional Requirements

**Infrastructure / Setup (must be done before development begins):**
- ARCH-1: Bump Spring Boot from 3.3.5 → 3.5.x before Phase 1 backend work (`mvn versions:update-parent`).
- ARCH-2: Upgrade Angular from 17 → 21 before Phase 1 admin-web work (`ng update @angular/core@21 @angular/cli@21`; Karma → Vitest migration included).
- ARCH-3: Expo prebuild migration for both trainer-app and client-app before Phase 2 mobile work (install expo + expo-modules-core; patch AppDelegate.swift + MainApplication.kt; `npx expo prebuild`).
- ARCH-4: Add WatermelonDB + op-sqlite to both RN apps; register op-sqlite as Expo plugin in app.json.
- ARCH-5: Add Redis to docker-compose.yml (local dev); add `tc:redis:7:///` to application-test.properties (Testcontainers); provision Cloud Memorystore in infra story.
- ARCH-6: Configure GCS bucket (names, IAM, CDN config) + GcsConfig.java bean before file storage features.
- ARCH-7: Configure FCM HTTP v1 API (service account scope) before push notification features.
- ARCH-8: Produce `ai-cost-model.html` with verified Claude pricing before AI Progression stories.
- ARCH-9: GH Actions CI pipelines for all 4 stacks (backend-ci.yml, trainer-app-eas.yml, client-app-eas.yml, admin-web-ci.yml, pact-verify.yml).

**Architecture constraints agents must enforce:**
- ARCH-10: DB weight always DECIMAL(6,2) in kg. Never store lb. API always kg in/out.
- ARCH-11: All new code in `in.vis.*` package. Zero gymculture references.
- ARCH-12: Versioned DTOs: `Accept: application/vnd.vis.v1+json`. Backend supports N + N-1.
- ARCH-13: Angular state: Signals + services only. No NgRx v1.
- ARCH-14: Mobile local state: WatermelonDB for domain data; MMKV for auth tokens + feature flags only.
- ARCH-15: WS event types are exhaustive (SET_UPDATE, SET_DELETE, PRESENCE_JOIN, PRESENCE_LEAVE, SESSION_END, AI_SUGGESTION, SYNC_ACK). Agents must not invent new types.
- ARCH-16: PR Card render: in-app = RN + Skia (zero latency). Shareable PNG = headless worker → GCS. Two separate paths. Worker tech TBD in Phase 3 planning.
- ARCH-17: AI history scope = Hybrid per Q5 Decision: raw set logs = human-identity 90-day rolling; AI suggestion history + prescription = trainer-identity. Client consent gate for full history.
- ARCH-18: Audit log = append-only; app DB role has no DELETE grant; migration role has DDL+DELETE. Monthly range partitions.
- ARCH-19: Cloud Armor + Bucket4j per-Firebase-UID rate limiting on all endpoints. Auth-on-upgrade for WebSocket.

### UX Design Requirements

UX-DR1: Connection Orb component — tension stem between two amber spheres in Active Session header. 4 states: At rest (static pulse), Active co-edit (stem pulses on each remote write), Disconnected/Stale (offline banner surfaces). Trainer + client avatars visible when both present.
UX-DR2: Equipment-Aware Logger family (6 variants) — Cable drag-to-pin, Barbell plate-snap with auto-sum, Dumbbell number-line slider, Machine pin-stack, Bodyweight silhouette + vest field, Kettlebell/Bands discrete picker. Shared gesture grammar: drag / snap / slide / pick. First-use 2-second animated hint per logger (dismissed after first successful gesture). Pre-loads last-used weight. Numeric keypad fallback always available.
UX-DR3: Set Row component — prescribed-vs-actual divergence tint (amber), warm-up flag chip, failure-intent toggle, per-rep assistance bar (after Set), sync-state badge (local_only/queued/acked/reconciled), last-write provenance chip (who-just-edited). All primary controls (Done toggle, +reps, weight, rest-timer dismiss) in 56mm bottom-quadrant thumb-zone.
UX-DR4: Ghost Overlay / AI Suggestion chip — async skeleton while Claude proxy pending (median 200ms hot, up to 6s cold). 30s TTL hard self-dismissal. One-tap accept (local-instant). Long-press = override. ai_locked toggle removes override surface. Confidence display. Stale-ai indicator. Context source chip ("Based on your training history" vs "Based on sessions with [Trainer]").
UX-DR5: PR Moment Screen — cinematic amber animation on PR detection. Composer with 3 aspect ratios. Watermark default ON, toggle OFF (persists per user). Native share-sheet. "Earned not celebrated" tone.
UX-DR6: Orb component — 5 tension states (At rest, Under load, Peak tension, Recovered, Disconnected) with named animations. Reduce-motion OS-preference: degrades to static states (not removed). Reduce-transparency: glass overlay removed.
UX-DR7: SyncStatusBar component — Set-row-level visibility of local_only / queued / acked / reconciled. User must never wonder "did this save?". Offline banner in Active Session header when WS disconnected.
UX-DR8: Onboarding Questionnaire component — shared between gym and solo paths. Path-aware shell (gym: QR → roster-match → trainer browse vs solo: sign-up → templates → AI bootstrap). ~10-field form. Optional advanced section (skippable).
UX-DR9: Branch-Scope Chip — always visible in nav, glanceable. Identity clarity for multi-role trainers switching brands.
UX-DR10: ACWR Risk Banner — amber (ACWR >1.5) / red (ACWR >2.0) states. Phrasing: "consider deload" never "you're overtraining". Auto-deload ghost overlay for red state.
UX-DR11: Recovery Chips (12 muscle groups) — color-coded per group. Green = recovered; red = hours remaining. Consistent size + layout on Home screen.
UX-DR12: Light + dark theme parity — ships v1 as quality bar. Token system shared across RN apps, Angular, and PR Card render worker. user_preferences.accent_color_hex, reduce_motion, reduce_transparency all persisted server-side and respected across all surfaces.
UX-DR13: Presence Avatar Stack — visible in Active Session header when co-edit is live. Distinct avatars for Trainer and Client.
UX-DR14: Skip-Set reason capture — explicit Skip affordance on Set row (Fatigue / Equipment busy / Injury / No time / Other). Optional, never required. No guilt copy. Progression Index excludes; adherence dashboard reflects.
UX-DR15: Empty States set — per surface: empty logger (first session), empty PR feed, empty branch dashboard, empty client list, empty trainer roster. Actionable CTA in each.
UX-DR16: Trainer Public Profile Card — name, photo, verified badge, specialty tags, rating, response time, languages. Consistent display in browse + assigned client's home.
UX-DR17: i18n unit affordance — every numeric input + display carries a unit affordance (kg/lb toggle, m/ft+in) without cluttering the row. PR cards render in user's preferred unit.

**Total UX-DRs: 17**

### FR Coverage Map

| Epic | FRs | NFRs | ARCH | UX-DRs |
|------|-----|------|------|--------|
| E1a Backend Infrastructure | — | NFR-9, NFR-10 | ARCH-1, ARCH-5, ARCH-9, ARCH-11, ARCH-12, ARCH-19 | — |
| E1b Mobile Infrastructure | — | NFR-4, NFR-8, NFR-10 | ARCH-3, ARCH-4, ARCH-14 | UX-DR12 |
| E1c Admin-Web Infrastructure | — | NFR-8, NFR-10 | ARCH-2, ARCH-13 | UX-DR12 |
| E1d Auth & Access | FR-27, FR-42, FR-43 | NFR-1, NFR-5 | ARCH-7, ARCH-11 | UX-DR8 |
| E2 Gym Operations | FR-15–24, FR-26 | NFR-9 | ARCH-18 | UX-DR9, UX-DR15, UX-DR16 |
| E3 Trainer-Client Pairing & Plans | FR-25, FR-28–31, FR-33 | NFR-7 | — | UX-DR8, UX-DR15 |
| E4 Equipment-Aware Logger | FR-2–8, FR-32 | NFR-2, NFR-4, NFR-6 | ARCH-10, ARCH-11, ARCH-15 | UX-DR2, UX-DR3, UX-DR6, UX-DR7, UX-DR14, UX-DR15, UX-DR17 |
| E5 Live Session Real-Time Co-Edit | FR-1, FR-28 (mid-session) | NFR-2, NFR-6 | ARCH-5, ARCH-15 | UX-DR1, UX-DR6, UX-DR7, UX-DR13 |
| E6 Progress & Recovery Analytics | FR-35–39 | NFR-6 | — | UX-DR10, UX-DR11 |
| E7 AI Progression Engine | FR-12–14 | NFR-3 | ARCH-8, ARCH-17 | UX-DR4 |
| E8 PR Moments & Social Sharing | FR-10–11 | NFR-6 | ARCH-6, ARCH-16 | UX-DR5, UX-DR12, UX-DR17 |
| E9 Nutrition | FR-40–41 | — | — | — |
| E10 Solo Paths & Template Marketplace | FR-9 | — | — | UX-DR15 |

**Dependency graph:**
```
E1a ─┐
E1b ─┼→ E1d → E2 → E3 → E4 ─┬→ E5
E1c ─┘                        ├→ E6 → E7
                               ├→ E8
                               └→ E10
E9 ← E1d (independent of E2–E4)
```

**Uncovered check:** All 43 FRs covered (FR-34 deferred v1.5 per PRD). All 19 ARCH reqs distributed. All 17 UX-DRs anchored.

## Epic List

| # | Epic | Key FRs / Reqs | Depends On |
|---|------|---------------|------------|
| E1a | Backend Infrastructure | ARCH-1, ARCH-5, ARCH-9, ARCH-11, ARCH-12, ARCH-19; NFR-9, NFR-10. CI/CD pipeline = first story. | — |
| E1b | Mobile Infrastructure | ARCH-3, ARCH-4, ARCH-14; NFR-4. WatermelonDB + op-sqlite, MMKV, Expo prebuild both apps. | — (parallel E1a) |
| E1c | Admin-Web Infrastructure | ARCH-2, ARCH-13; NFR-8. Angular 21 upgrade, Signals baseline, Vitest. | — (parallel E1a) |
| E1d | Auth & Access | FR-27, FR-42, FR-43; NFR-1, NFR-5; ARCH-7. Firebase Auth, questionnaire, access gate, DPDPA consent. | E1a + E1b + E1c |
| E2 | Gym Operations | FR-15–24, FR-26; NFR-9; ARCH-18. Member CSV import, PT activation, payments, admin dashboards, audit log. | E1d |
| E3 | Trainer-Client Pairing & Plans | FR-25, FR-28–31, FR-33; NFR-7. Pairing state machine, plan builder, plan templates, FCM reminders, invite link. | E2 |
| E4 | Equipment-Aware Logger | FR-2–8, FR-32; NFR-2, NFR-4, NFR-6; ARCH-10, ARCH-15. All 6 logger types, set row, sync state, rest timer. **Orb dormant scaffold included.** | E3 |
| E5 | Live Session Real-Time Co-Edit | FR-1, FR-28 (mid-session); NFR-2, NFR-6; ARCH-5, ARCH-15. WebSocket endpoint, Redis pub/sub, Orb activation, presence avatars. | E4 |
| E6 | Progress & Recovery Analytics | FR-35–39; NFR-6. Recovery chips, ACWR, progress tab, body stats, attendance heatmap. Shares ProgressionService with E8. | E4 |
| E7 | AI Progression Engine | FR-12–14; NFR-3; ARCH-8, ARCH-17. Claude proxy gate story first, prompt caching, ghost overlay, ai_locked toggle, cold-start bootstrap. | E4 + E6 |
| E8 | PR Moments & Social Sharing | FR-10–11; NFR-6; ARCH-6, ARCH-16. GCS/CDN gate story first, PR detection (4 dims), cinematic PR screen, card composer, share-sheet. | E4 (not E6) |
| E9 | Nutrition | FR-40–41. Trainer macro authoring, client read-only tab. | E1d |
| E10 | Solo Paths & Template Marketplace | FR-9. 15 curated templates, solo client pick + edit flow. | E4 |

---

## Epic E1a — Backend Infrastructure

**Goal:** All backend infra dependencies wired and CI/CD green before any domain feature work begins.

**Requirements covered:** NFR-9, NFR-10, ARCH-1, ARCH-5, ARCH-9, ARCH-11, ARCH-12, ARCH-19

**Depends on:** None (runs parallel with E1b, E1c)

**Linear issues:** GC-64, GC-65, GC-66, GC-67, GC-68, GC-69

---

### Story 1a.1: GH Actions CI/CD Pipelines (GC-64)

As a developer,
I want automated CI pipelines for all four stacks,
So that every PR is validated before merge.

**Acceptance Criteria:**

**Given** a commit is pushed to any branch
**When** GH Actions triggers
**Then** `backend-ci.yml` runs `mvn test` + `mvn package -DskipTests` + Docker build
**And** `admin-web-ci.yml` runs `ng test --watch=false` + `ng build --configuration production`
**And** `pact-verify.yml` stub exists (passes with no contracts yet)
**And** all pipelines pass on current HEAD with zero code changes

**Given** a PR is opened against main
**When** GH Actions triggers
**Then** all pipelines must be green (branch protection enforced)

---

### Story 1a.2: Spring Boot 3.5.x Upgrade + in.vis.* Package Migration (GC-65)

As a developer,
I want the backend on Spring Boot 3.5.x with all code in `in.vis.*`,
So that we are on current stable Spring Boot with zero gymculture references.

**Acceptance Criteria:**

**Given** the current parent is Spring Boot 3.3.5
**When** `mvn versions:update-parent` targets 3.5.x
**Then** `pom.xml` reflects 3.5.x and all dependency resolution succeeds

**Given** any remaining `in.gymculture.*` package references exist
**When** migration runs
**Then** all packages renamed to `in.vis.*`
**And** `mvn test` passes with zero failures
**And** `grep -r "gymculture" src/` returns empty

---

### Story 1a.3: Redis Integration — Local Dev + Testcontainers (GC-66)

As a developer,
I want Redis available via docker-compose locally and Testcontainers in CI,
So that E5 WebSocket pub/sub can be built without additional infra setup.

**Acceptance Criteria:**

**Given** `docker-compose up -d` runs
**When** backend starts
**Then** Redis 7 container is up and `RedisConfig.java` bean connects
**And** host/port read from environment (not hardcoded)

**Given** `mvn test` runs in CI
**When** any `@ActiveProfiles("test")` test runs
**Then** Testcontainers Redis 7 starts and `RedisHealthIndicator` reports UP in `/actuator/health`

---

### Story 1a.4: Cloud Armor + Bucket4j Per-UID Rate Limiting (GC-67)

As an ops engineer,
I want Cloud Armor WAF rules and Bucket4j per-Firebase-UID rate limiting,
So that abusive clients cannot DoS endpoints or inflate AI costs.

**Acceptance Criteria:**

**Given** a Firebase-authenticated request arrives at `/api/**`
**When** the same UID exceeds the configured ceiling per minute
**Then** backend returns HTTP 429 with `Retry-After` header
**And** limit is configurable via application properties

**Given** Cloud Armor is provisioned in front of Cloud Run
**When** a bad-actor IP matches a WAF rule
**Then** Cloud Armor blocks before reaching backend

**Given** a WebSocket upgrade request arrives
**When** connection is established
**Then** Firebase UID is validated before upgrade completes (ARCH-19 auth-on-upgrade)

---

### Story 1a.5: Versioned DTO Content-Negotiation (GC-68)

As a developer,
I want all endpoints to accept and return `application/vnd.vis.v1+json`,
So that N and N-1 API version coexistence is supported from day one.

**Acceptance Criteria:**

**Given** a client sends `Accept: application/vnd.vis.v1+json`
**When** any `/api/**` endpoint is called
**Then** response includes `Content-Type: application/vnd.vis.v1+json`

**Given** a client sends `Accept: application/json` (no version header)
**When** any endpoint is called
**Then** defaults to v1 (backward-compatible fallback)

**Given** a v2 DTO coexists alongside v1
**When** `Accept: application/vnd.vis.v2+json` is sent
**Then** v2 DTO returned (versioning infra supports without code change)

---

### Story 1a.6: Observability Day-1 — Counter Metrics + Distributed Tracing (GC-69)

As an SRE,
I want SM-C1 through SM-C10 counters, distributed traces, and a Cloud Monitoring dashboard wired from day one,
So that every subsequent epic is observable from its first deployment.

**Acceptance Criteria:**

**Given** backend starts and processes any request
**When** OpenTelemetry auto-instrumentation is active
**Then** traces export to Cloud Trace (device → controller → service → DB)

**Given** SM-C1 through SM-C10 events occur
**When** triggered
**Then** counter increments appear in Cloud Monitoring within 60s
**And** `ai.cost.wau_spend` is a custom metric per trainer, initialized at 0.0

**Given** Cloud Monitoring dashboard template committed to repo
**When** applied to a new project
**Then** all SM-C* panels visible with threshold lines (SM-C2: WS disconnect >5%, SM-C6: set-save >1.5s, SM-C3: crash-free <99%)

---

## Epic E1b — Mobile Infrastructure

**Goal:** Both RN apps on Expo prebuild bare workflow with WatermelonDB + offline sync before any mobile feature work.

**Requirements covered:** NFR-4, NFR-8, ARCH-3, ARCH-4, ARCH-14, UX-DR12

**Depends on:** None (runs parallel with E1a, E1c)

**Linear issues:** GC-70, GC-71, GC-72, GC-73, GC-74

---

### Story 1b.1: Expo Prebuild Migration — trainer-app + client-app (GC-70)

As a developer,
I want both RN apps migrated to Expo prebuild bare workflow,
So that native module linking is deterministic and EAS Build can produce signed binaries.

**Acceptance Criteria:**

**Given** either app runs `npx expo prebuild`
**When** migration completes
**Then** `ios/` and `android/` directories are generated and committed
**And** `AppDelegate.swift` and `MainApplication.kt` are patched as Expo requires
**And** `npx react-native run-ios` and `run-android` succeed on a clean checkout
**And** no Metro bundler errors on startup

---

### Story 1b.2: EAS Build CI Pipelines — trainer-app + client-app (GC-71)

As a developer,
I want `trainer-app-eas.yml` and `client-app-eas.yml` GH Actions pipelines,
So that every PR triggers an EAS build profile check and merges produce development builds.

**Acceptance Criteria:**

**Given** a PR touches `trainer-app/` or `client-app/`
**When** GH Actions triggers
**Then** `eas build --profile preview --non-interactive --no-wait` runs and returns a build URL
**And** pipeline passes (build queued — not required to complete in CI time window)

**Given** a commit merges to main
**When** GH Actions triggers
**Then** `eas build --profile development` runs for both apps
**And** build artifacts available in EAS dashboard

---

### Story 1b.3: WatermelonDB + op-sqlite Integration (GC-72)

As a developer,
I want WatermelonDB with op-sqlite adapter registered in both apps,
So that all domain data can be stored offline-first before any feature work begins.

**Acceptance Criteria:**

**Given** `@nozbe/watermelondb` and `op-sqlite` are installed and `app.json` registers the op-sqlite Expo plugin
**When** either app launches
**Then** WatermelonDB initializes with op-sqlite adapter without error
**And** a smoke-test model (`SyncMeta`) can be written and read back in a Jest test

**Given** op-sqlite is registered in `app.json` as an Expo plugin
**When** `npx expo prebuild` runs
**Then** native SQLite bindings are included in iOS and Android builds automatically

---

### Story 1b.4: MMKV Setup + Auth Token Storage (GC-73)

As a developer,
I want MMKV initialized in both apps for auth tokens and feature flags only,
So that sensitive auth state is not stored in WatermelonDB (ARCH-14 constraint).

**Acceptance Criteria:**

**Given** `react-native-mmkv` is installed
**When** Firebase Auth issues a token after sign-in
**Then** the token is stored in MMKV (not AsyncStorage, not WatermelonDB)
**And** token retrieved from MMKV on app restart and passed to backend requests
**And** `MMKV.set('featureFlags', ...)` is the only other permitted use — no domain data in MMKV

**Given** user signs out
**When** sign-out completes
**Then** MMKV auth key is cleared

---

### Story 1b.5: Light/Dark Theme Token System + OS Preference Hooks (GC-74)

As a developer,
I want the Vis token system wired in both RN apps with OS reduce-motion and reduce-transparency hooks,
So that all future components can reference tokens and accessibility preferences from day one.

**Acceptance Criteria:**

**Given** the app starts
**When** OS is in dark mode
**Then** `TOKENS.dark` values are active across all components via theme Context
**And** switching OS theme reflects immediately without app restart

**Given** OS has reduce-motion enabled
**When** any animated component renders
**Then** animations are reduced to static states (not removed entirely per UX-DR6)

**Given** OS has reduce-transparency enabled
**When** glass-overlay components render
**Then** glass overlay replaced with opaque fallback

---

## Epic E1c — Admin-Web Infrastructure

**Goal:** Angular 21 with Signals-only state, Vitest, and WCAG 2.1 AA shell before any admin feature work.

**Requirements covered:** NFR-8, ARCH-2, ARCH-13, UX-DR12

**Depends on:** None (runs parallel with E1a, E1b)

**Linear issues:** GC-75, GC-76, GC-77, GC-78

---

### Story 1c.1: Angular 17 → 21 Upgrade + Karma → Vitest Migration (GC-75)

As a developer,
I want Angular upgraded to v21 and Karma replaced with Vitest,
So that admin-web is on current Angular with a fast modern test runner.

**Acceptance Criteria:**

**Given** `ng update @angular/core@21 @angular/cli@21` runs
**When** migration completes
**Then** `ng serve` starts without errors
**And** `ng build --configuration production` succeeds
**And** all existing tests pass under Vitest (`npm run test`)
**And** `package.json` shows Angular 21 in all `@angular/*` dependencies

---

### Story 1c.2: Signals + Services State Architecture Baseline (GC-76)

As a developer,
I want Angular Signals + services-only state as the enforced pattern,
So that no NgRx is introduced in v1 and future components have a clear pattern to follow.

**Acceptance Criteria:**

**Given** any new feature service is written
**When** it manages state
**Then** it uses `signal()`, `computed()`, and `effect()` — not BehaviorSubject or NgRx store

**Given** existing services use BehaviorSubject
**When** migration runs
**Then** BehaviorSubject patterns are converted to Signals in all existing services
**And** `ng test` passes after conversion

**Given** a developer adds NgRx as a dependency
**When** CI runs
**Then** linting/build fails with a clear error message

---

### Story 1c.3: WCAG 2.1 AA Shell — Focus Management, Skip-Nav, Min Targets (GC-77)

As a developer,
I want the admin-web shell to meet WCAG 2.1 AA baseline before any feature components are added,
So that every feature built on top inherits accessibility for free.

**Acceptance Criteria:**

**Given** admin-web app shell renders
**When** keyboard user navigates
**Then** "Skip to main content" link is first focusable element
**And** focus trapped within modals when open
**And** all interactive elements have min 44×44px hit targets

**Given** screen reader is active
**When** navigating shell chrome
**Then** all landmark regions labeled (`aria-label` or `aria-labelledby`)
**And** page title updates on route change

---

### Story 1c.4: Admin-Web Light/Dark Theme Token System (GC-78)

As a developer,
I want the Vis token system wired in admin-web matching RN token values,
So that brand palette is consistent across all surfaces from day one.

**Acceptance Criteria:**

**Given** admin-web starts in dark mode
**When** OS or user preference is dark
**Then** CSS vars from TOKENS.dark active via `prefers-color-scheme` media query
**And** warm ivory (#F9F6F0) used in light mode — never pure white for page bg

**Given** new component references color
**When** it uses hardcoded hex
**Then** CI lint check flags it

---

## Epic E1d — Auth & Access

**Goal:** End-to-end auth across all 3 apps, role-gated access, onboarding questionnaire, DPDPA consent.

**Requirements covered:** FR-27, FR-42, FR-43, NFR-1, NFR-5, ARCH-7, UX-DR8

**Depends on:** E1a + E1b + E1c

**Linear issues:** GC-79, GC-80, GC-81, GC-82, GC-83, GC-84

---

### Story 1d.1: Firebase Auth — Google, Apple, Phone OTP (all 3 apps) (GC-79)

As a user,
I want to sign in with Google, Apple (iOS), or Phone OTP,
So that I can access the app without creating a separate password.

**Acceptance Criteria:**

**Given** user taps "Sign in with Google"
**When** Google OAuth completes
**Then** Firebase Auth issues valid ID token stored in MMKV
**And** user navigated to appropriate post-auth screen

**Given** user taps "Sign in with Apple" on iOS
**When** Apple Sign-In completes
**Then** Firebase Auth issues valid ID token
**And** Apple Sign-In button present on iOS (App Store requirement)

**Given** user enters phone number and receives OTP
**When** OTP submitted correctly
**Then** Firebase Auth issues valid ID token
**And** incorrect OTP shows error without logging out

---

### Story 1d.2: Backend JWT Validation + Role Extraction (FirebaseAuthFilter) (GC-80)

As an API consumer,
I want every request validated against Firebase JWT with role and branch in Security Context,
So that downstream controllers enforce branch-scoped access without repeating auth logic.

**Acceptance Criteria:**

**Given** request arrives with valid Firebase ID token
**When** FirebaseAuthFilter processes it
**Then** Firebase UID extracted and matched to user record in PostgreSQL
**And** role (CLIENT | TRAINER | STAFF | OWNER) and branch_id attached to Security Context

**Given** request arrives with expired or invalid token
**When** FirebaseAuthFilter processes it
**Then** response is HTTP 401

**Given** valid token but no matching DB record
**When** FirebaseAuthFilter processes it
**Then** HTTP 403 with `{"status":"PENDING_ACTIVATION"}`

---

### Story 1d.3: Onboarding Questionnaire — ~10 Fields, Path-Aware Shell (GC-81)

As a new user,
I want to complete a short onboarding questionnaire after signing in,
So that the app can personalise my experience and AI can bootstrap initial workout suggestions.

**Acceptance Criteria:**

**Given** new user with no existing questionnaire data
**When** app loads
**Then** onboarding questionnaire shown before main app
**And** collects: age, sex, height (cm/in), weight (kg/lb), experience, sedentary gap, primary goal, injuries, days/week, equipment access, wearable opt-in
**And** optional advanced section (sleep avg, calorie estimate) collapsible/skippable

**Given** user completes questionnaire
**When** submitted
**Then** data persisted via POST `/api/v1/me/questionnaire`
**And** weight in kg canonical, height in m canonical (NFR-1)

---

### Story 1d.4: Role-Based Access Gate — Gym Path, Solo Path, Pending, PT-Lapse (GC-82)

As an authenticated user,
I want the app to route me to the correct experience based on role and membership status,
So that I never see features I am not entitled to.

**Acceptance Criteria:**

**Given** user's phone/email matches a branch roster record
**When** app loads
**Then** user enters gym path with assigned role

**Given** user has no matching roster record
**When** app loads
**Then** user sees "Pending Activation" screen

**Given** CLIENT whose PT membership has lapsed
**When** app loads
**Then** user sees "PT Membership Expired" screen — data retained, no delete

**Given** user with no roster match and no prior gym path
**When** app loads after questionnaire
**Then** user enters solo path

---

### Story 1d.5: DPDPA/GDPR Compliance — Consent, Right-to-Erasure, Data Export (GC-83)

As a user,
I want to give explicit consent at onboarding and control my data,
So that the platform meets DPDPA 8 obligations and GDPR 6 endpoints.

**Acceptance Criteria:**

**Given** new user reaches onboarding questionnaire
**When** questionnaire displays
**Then** explicit consent checkbox appears before health/body data fields
**And** submission blocked if consent not given

**Given** user navigates to Profile → Account → Delete
**When** they confirm deletion
**Then** POST `/api/v1/me/delete` called
**And** all personal data erased within 30 days

**Given** user requests data portability
**When** GET `/api/v1/me/export` called
**Then** JSON export of all user data returned

---

### Story 1d.6: FCM HTTP v1 API Service Account Config (GC-84)

As a developer,
I want FCM HTTP v1 API wired with a service account before any push notification features are built,
So that E3 plan-block reminders and all future FCM events have a working send path.

**Acceptance Criteria:**

**Given** Firebase Admin SDK added to backend
**When** backend starts
**Then** `FirebaseMessaging` bean initialises using service account from Google Secret Manager

**Given** device registers after sign-in
**When** FCM token received
**Then** token stored in `user_devices` table (device_token, platform, user_id, updated_at)

**Given** stored device token is no longer valid (FCM returns UNREGISTERED)
**When** send attempted
**Then** stale token removed from `user_devices`

---

## Epic E2 — Gym Operations

**Goal:** Full gym operations in Admin Web — member import, PT lifecycle, payments, dashboards, audit log.

**Requirements covered:** FR-15–24, FR-26, NFR-9, ARCH-18, UX-DR9, UX-DR15, UX-DR16

**Depends on:** E1d

**Linear issues:** GC-85, GC-86, GC-87, GC-88, GC-89, GC-90, GC-91, GC-92, GC-93, GC-94

---

### Story 2.1: Member CSV/Excel Import with Duplicate Detection (GC-85)

As a branch staff member,
I want to upload a CSV or Excel file to bulk-import members,
So that I do not have to enter hundreds of records manually.

**Acceptance Criteria:**

**Given** staff uploads CSV with columns: name, phone, email, membership_type, membership_start, membership_end, pt_start, pt_end, trainer_assigned
**When** import processes
**Then** all valid rows create member records in the branch
**And** duplicate detection by phone number flags existing members without overwriting
**And** import summary shows: created, skipped (duplicates), failed (validation errors) counts

**Given** a row has missing required fields (name or phone)
**When** import processes
**Then** that row is skipped with a validation error in the summary

**Given** staff uploads an Excel (.xlsx) file
**When** import processes
**Then** accepted and processed identically to CSV

---

### Story 2.2: PT Activation, Suspension, and Expiry State Machine (GC-86)

As a branch staff member,
I want to activate, suspend, or expire a member's PT membership,
So that client app access is correctly gated based on current membership status.

**Acceptance Criteria:**

**Given** member with no active PT
**When** staff clicks "Activate PT" and sets start/end dates
**Then** PT state transitions to ACTIVE
**And** client app access gate allows member in on next load

**Given** member with ACTIVE PT
**When** staff clicks "Suspend PT"
**Then** PT state transitions to SUSPENDED
**And** client app shows "PT Membership Expired" screen on next load

**Given** member's PT end date passes
**When** scheduled job runs
**Then** PT state transitions to EXPIRED
**And** audit log entry created for state change

---

### Story 2.3: In-Person Payment Logging (Cash/UPI/Card) (GC-87)

As a branch staff member,
I want to log in-person payments for PT membership,
So that there is a traceable payment record and PT block extensions are tracked.

**Acceptance Criteria:**

**Given** staff selects member and clicks "Log Payment"
**When** they enter amount, method (cash/UPI/card), date, and optionally extend PT end date
**Then** payment record created in DB
**And** audit log entry created (ARCH-18 append-only)
**And** if PT end date extended, member's PT record updated accordingly

**Given** staff views member's payment history
**When** list loads
**Then** all past payments shown in reverse-chronological order with amount, method, date, and staff name

---

### Story 2.4: WhatsApp/FCM Reminder Templates (GC-88)

As a branch staff member,
I want to generate reminder messages for PT expiry, missed sessions, and overdue payments,
So that I can paste them into WhatsApp without typing from scratch.

**Acceptance Criteria:**

**Given** member's PT expires within 7 days
**When** staff opens reminder panel
**Then** pre-filled WhatsApp message shown with member name, expiry date, branch contact
**And** "Copy to clipboard" button copies message

**Given** member has missed 2+ consecutive sessions
**When** staff views missed-sessions list
**Then** each member shows "Copy reminder" button with pre-filled message

---

### Story 2.5: Trainer Cert Verification — Upload, Review, Verified Badge (GC-89)

As a trainer,
I want to upload my certification document for verification,
So that a verified badge appears on my public profile card.

**Acceptance Criteria:**

**Given** trainer uploads cert document (PDF or image)
**When** uploaded
**Then** document stored in GCS and verification record created with status PENDING
**And** branch staff/owner sees pending cert in verification queue

**Given** staff or owner approves cert
**When** approved
**Then** trainer's profile gets verified_at timestamp and verified badge (UX-DR16)
**And** if rejected, trainer receives rejection reason and can re-upload

---

### Story 2.6: Cross-Branch Revenue Dashboard (Owner) (GC-90)

As an owner,
I want a cross-branch revenue dashboard with configurable date ranges,
So that I can monitor revenue, PT activations, churn, and trainer utilisation across all branches.

**Acceptance Criteria:**

**Given** owner opens Revenue Dashboard
**When** page loads with default date range (current month)
**Then** per-branch revenue totals shown alongside brand-aggregate
**And** PT activation count, churn rate, trainer utilisation shown per branch

**Given** owner with STAFF role attempts access
**When** request made
**Then** HTTP 403 (owner-only endpoint)

---

### Story 2.7: Trainer Leaderboard (Experience, Feedback, Client Progress) (GC-91)

As an owner or staff member,
I want to see a trainer leaderboard ranked on three dimensions,
So that top performers are visible and branch management is data-driven.

**Acceptance Criteria:**

**Given** owner opens Trainer Leaderboard
**When** page loads
**Then** trainers ranked on: Experience (years), Client Feedback (5-star avg, n≥3 qualifier), Client Progress (median Progression Index Δ over rolling 90-day window)
**And** per-branch and brand-wide views toggleable

**Given** trainer has fewer than 3 feedback ratings
**When** leaderboard renders
**Then** feedback score shows "Insufficient data"

---

### Story 2.8: Branch Health Snapshot KPIs (GC-92)

As a branch staff member or owner,
I want a branch health snapshot with key KPIs,
So that I can quickly assess branch status without digging into reports.

**Acceptance Criteria:**

**Given** staff or owner opens Branch Health dashboard
**When** page loads
**Then** KPIs shown: active members, active PT count, WALS, adherence average, trainer-cert-pending count

**Given** owner views snapshot
**When** it loads
**Then** they can toggle between individual branches and brand-aggregate view

---

### Story 2.9: Audit Log — Append-Only DDL + Branch/Brand-Scoped Read (GC-93)

As a branch staff member or owner,
I want an append-only audit log of all member edits, payments, role changes, and plan-template publishes,
So that there is a tamper-proof record of all administrative actions.

**Acceptance Criteria:**

**Given** any member record edited, payment logged, role changed, or plan template published
**When** action completes
**Then** audit log entry appended with: action_type, entity_id, actor_id, branch_id, timestamp, before/after snapshot (JSON)

**Given** app DB role attempts DELETE on audit_log
**When** statement executes
**Then** it fails — no DELETE grant on audit_log (ARCH-18)

**Given** staff views audit log
**When** list loads
**Then** only entries for their own branch shown

**Given** owner views audit log
**When** list loads
**Then** all branches shown (brand-wide read)

**Given** audit_log table created via Flyway
**When** migration runs
**Then** monthly range partitions created (partition by month on timestamp)

---

### Story 2.10: Trainer Public Profile Card (GC-94)

As a client or staff member,
I want to view a trainer's public profile card with credentials and ratings,
So that clients can make an informed trainer selection.

**Acceptance Criteria:**

**Given** client browses trainers in gym path
**When** they tap a trainer
**Then** public profile card shows: name, photo, certifications + verified badge, years experience, specialty tags, client rating (5-star avg, n≥3), response-time stat, languages

**Given** trainer has fewer than 3 reviews
**When** profile card renders
**Then** rating shows "New Trainer" instead of star score

**Given** trainer updates their profile
**When** saved
**Then** public profile card reflects updated information immediately

---

## Epic E3 — Trainer-Client Pairing & Plans

**Goal:** Trainer-client pairing state machine, plan builder (flat + phased), templates, FCM reminders, invite link.

**Requirements covered:** FR-25, FR-28–31, FR-33, NFR-7, UX-DR8, UX-DR15

**Depends on:** E2

**Linear issues:** GC-95, GC-96, GC-97, GC-98, GC-99, GC-100

---

### Story 3.1: Client-Chosen Trainer Assignment — Priority Order + Single-Active Invariant (GC-95)

As a client on the gym path,
I want to pick my top 1–3 preferred trainers in priority order,
So that the first trainer who accepts becomes my assigned trainer.

**Acceptance Criteria:**

**Given** client completes onboarding questionnaire on gym path
**When** they reach the trainer browse screen
**Then** they can select 1–3 trainers in priority order
**And** ranked list submitted via POST `/api/v1/me/trainer-requests`

**Given** a trainer receives a client request and accepts
**When** they accept
**Then** server stamps a monotonic assignment timestamp
**And** client-trainer pairing created (single-active invariant enforced)
**And** lower-priority pending requests for that client automatically cancelled

**Given** a trainer declines with optional transfer
**When** declined
**Then** request moves to next priority trainer
**And** questionnaire and plan data carry over

---

### Story 3.2: PT Relationship State Machine — NoPt / PtActive / PtEnded (GC-96)

As a trainer,
I want the PT relationship state machine enforced on the backend,
So that client access and session permissions always reflect current pairing status.

**Acceptance Criteria:**

**Given** a client-trainer pairing is active (PtActive)
**When** PT relationship ends
**Then** state transitions to PtEnded
**And** prior trainer retains read-only alumni access to client's historical data

**Given** PT relationship ends mid-session
**When** state transitions to PtEnded while session in progress
**Then** WebSocket writes frozen, offline queue flushed, session marked terminated_during=true

**Given** client has never had a trainer
**When** their record is queried
**Then** PT state is NoPt

---

### Story 3.3: Plan Builder — Flat Plan Creation and Assignment (GC-97)

As a trainer,
I want to build a flat (non-phased) workout plan and assign it to a client,
So that the client has a structured programme to follow in sessions.

**Acceptance Criteria:**

**Given** trainer opens Plan Builder for a client
**When** they create a flat plan
**Then** they can add exercises to each day of the week with prescribed sets, reps, load, RPE target
**And** plan can be saved as draft or published immediately

**Given** trainer publishes a flat plan
**When** client opens their Workout tab
**Then** today's exercises are visible based on weekly schedule

---

### Story 3.4: Plan Builder — Phased Plan + Phase Transitions + Client-Visible Progress (GC-98)

As a trainer,
I want to build phased plans with named blocks and start/end dates,
So that clients experience a periodised programme with visible phase transitions.

**Acceptance Criteria:**

**Given** trainer creates a phased plan with phases (named blocks with start/end dates)
**When** saved
**Then** phases are ordered sequentially and cannot overlap dates

**Given** a phase transition date arrives
**When** client opens their Workout tab
**Then** they see the new phase's exercises
**And** a phase transition banner is shown

---

### Story 3.5: Plan Templates — 3 Sources + RBAC (GC-99)

As a trainer,
I want to browse, save, and publish plan templates from three sources,
So that I can reuse proven programmes across clients without rebuilding from scratch.

**Acceptance Criteria:**

**Given** trainer opens the template library
**When** it loads
**Then** three sections visible: Vis starter (~15 curated), My Private Templates, Brand-Shared Templates

**Given** trainer with publish rights publishes a template to brand-shared
**When** published
**Then** all trainers in the brand can browse and clone it

**Given** trainer clones a template
**When** applied to a client
**Then** creates new plan for that client (clone does not affect original)

---

### Story 3.6: FCM Plan-Block Expiry Reminders + Solo Trainer Invite Link (GC-100)

As a trainer,
I want FCM reminders sent to clients when their plan phase is about to expire, and a personal invite link to bring new clients directly to my inbox,
So that clients are prompted to continue and new client acquisition is frictionless.

**Acceptance Criteria:**

**Given** plan phase end date is 2 days away
**When** scheduled job runs
**Then** FCM push sent to client: "Your [Phase Name] programme ends in 2 days — check in with [Trainer Name]"

**Given** plan phase end date arrives (T-0)
**When** scheduled job runs
**Then** FCM push sent again until acknowledged or new plan/phase assigned

**Given** trainer opens their profile settings
**When** they view their invite link
**Then** personal shareable URL shown (e.g., `vis.app/join/[trainer-handle]`)
**And** new client tapping link has that trainer auto-populated as first-priority choice

---

## Epic E4 — Equipment-Aware Logger

**Goal:** Full workout logging — 6 logger types, Set Row field model, sync badges, rest timer, Orb dormant scaffold.

**Requirements covered:** FR-2–8, FR-32, NFR-2, NFR-4, NFR-6, ARCH-10, ARCH-11, ARCH-15, UX-DR2, UX-DR3, UX-DR6, UX-DR7, UX-DR14, UX-DR15, UX-DR17

**Depends on:** E3

**Linear issues:** GC-101, GC-102, GC-103, GC-104, GC-105, GC-106, GC-107, GC-108, GC-109

---

### Story 4.1: ExerciseSet DB Schema + Backend CRUD API (GC-101)

As a developer,
I want the ExerciseSet DB schema and backend CRUD API in place,
So that all logger stories can persist data without schema conflicts.

**Acceptance Criteria:**

**Given** Flyway migration runs
**When** applied
**Then** `exercise_sets` table exists with all prescribed/actual fields, sync_state, client_uuid, server_version (monotonic)

**Given** client sends POST `/api/v1/sessions/{id}/sets` with a client_uuid
**When** processed
**Then** set created and server_version assigned
**And** duplicate client_uuid for same session idempotently ignored (NFR-4)

---

### Story 4.2: Set Row Component — Prescribed/Actual Fields, Failure Intent, Per-Rep Assistance (GC-102)

As a trainer or client logging a session,
I want the Set Row component to show prescribed vs actual fields, failure-intent toggle, and per-rep assistance bar,
So that every set captures full fidelity of what was prescribed and what actually happened.

**Acceptance Criteria:**

**Given** a set has prescribed_load and actual_load that differ
**When** Set Row renders
**Then** both values shown; row tinted amber (UX-DR3)

**Given** trainer flags set as "to failure"
**When** failure_intent toggled
**Then** rpe_target locks to 10.0

**Given** set saved
**When** per-rep assistance bar appears
**Then** user can label each rep: unassisted | spotter_assisted | forced

**Given** set in sync_state: local_only / queued / acked / reconciled
**When** Set Row renders
**Then** sync-state badge visible + last-write provenance chip

---

### Story 4.3: Warm-Up Flag + Skip-Set Affordance (GC-103)

As a trainer or client,
I want to flag any set as warm-up or skip it with a reason,
So that warm-up volume is excluded from progression metrics and skips are tracked without guilt.

**Acceptance Criteria:**

**Given** either party taps warm-up chip
**When** flagged
**Then** is_warmup=true persisted; set excluded from Progression Index, volume PR, weekly volume metrics

**Given** user taps Skip affordance
**When** skip menu appears
**Then** reason options: Fatigue / Equipment busy / Injury / No time / Other
**And** skip is optional — dismissible without selecting

---

### Story 4.4: Equipment-Aware Logger — Cable, Barbell, Dumbbell Variants (GC-104)

As a trainer or client logging a set,
I want cable, barbell, and dumbbell-specific weight input controls,
So that weight entry matches physical equipment and is faster than numeric keypad.

**Acceptance Criteria:**

**Given** exercise tagged as cable → drag-to-pin stack selector shown
**Given** exercise tagged as barbell → plate-snap with auto-sum + "Same as last set" one-tap
**Given** exercise tagged as dumbbell → number-line slider + first-use 2s animated hint (UX-DR2)
**And** numeric keypad fallback always available via "Type" button

---

### Story 4.5: Equipment-Aware Logger — Machine, Bodyweight, Kettlebell/Bands Variants (GC-105)

As a trainer or client logging a set,
I want machine, bodyweight, kettlebell, and bands-specific weight input controls,
So that all 6 Equipment-Aware Logger types are complete.

**Acceptance Criteria:**

**Given** exercise tagged as machine → pin-stack selector
**Given** exercise tagged as bodyweight → silhouette + optional vest/extra-weight field
**Given** exercise tagged as kettlebell or bands → discrete picker
**And** "Same as last set" / "Same as last session" available across all 6 types

---

### Story 4.6: Superset + Drop-Set Composition by Gesture (GC-106)

As a trainer or client,
I want to create supersets and drop sets by gesture without typing,
So that complex set structures are logged as fast as simple sets.

**Acceptance Criteria:**

**Given** two adjacent exercises visible
**When** user taps-to-pair
**Then** linked as superset with visual indicator; rest timer runs after both complete

**Given** user taps-to-drop on Equipment-Aware Logger
**When** confirmed
**Then** drop-set row appended with lower weight pre-filled; visually distinct from regular set row

---

### Story 4.7: Rest Timer — Auto-Start, Compound/Isolation Defaults, Haptic (GC-107)

As a trainer or client,
I want the rest timer to auto-start after each set save with exercise-type defaults,
So that rest periods are tracked without any manual action.

**Acceptance Criteria:**

**Given** set saved → timer auto-starts: compound 150s / isolation 75s default
**Given** timer hits 10s remaining → haptic fires
**Given** trainer has custom rest override for client × exercise → custom duration used
**Given** user taps "Done resting" or timer expires → next set row becomes active

---

### Story 4.8: Cardio Logger — Treadmill, Elliptical, Bike, Rowing, Free-Form (GC-108)

As a trainer or client,
I want a minimal cardio logger for common cardio machines and free-form cardio,
So that cardio sessions are tracked alongside strength work.

**Acceptance Criteria:**

**Given** exercise tagged as cardio
**When** logger opens
**Then** mode picker shown: Treadmill | Elliptical | Bike | Rowing | Free-form
**And** fields: duration, avg HR (optional), perceived effort (1–10)

---

### Story 4.9: Connection Orb — Dormant Scaffold (Static Pulse State) (GC-109)

As a developer,
I want the Connection Orb component scaffolded in dormant state in the Active Session header,
So that E5 can activate it without structural changes to the session screen.

**Acceptance Criteria:**

**Given** active session screen renders
**When** header loads
**Then** Orb present showing static pulse (At rest — no WS wired)
**And** renders using OrbFill visual recipe (radial gradient, cinematic glow)
**And** reduce-motion → static state; reduce-transparency → glass overlay removed
**And** second avatar slot empty/placeholder (single participant)

---

## Epic E5 — Live Session Real-Time Co-Edit

**Goal:** WebSocket co-edit, Redis Pub/Sub fan-out, LWW conflict resolution, Orb activation, offline queue drain.

**Requirements covered:** FR-1, FR-28 (mid-session), NFR-2, NFR-6, ARCH-5, ARCH-15, UX-DR1, UX-DR6, UX-DR7, UX-DR13

**Depends on:** E4

**Linear issues:** GC-110, GC-111, GC-112, GC-113, GC-114

---

### Story 5.1: WebSocket @ServerEndpoint + Auth-on-Upgrade (GC-110)

As a developer,
I want a raw WebSocket @ServerEndpoint with Firebase auth validated on upgrade,
So that E5 real-time co-edit has a secure, authenticated transport layer.

**Acceptance Criteria:**

**Given** client sends WS upgrade with `Authorization: Bearer <token>`
**When** server processes upgrade
**Then** Firebase UID validated before upgrade completes (ARCH-19)
**And** unauthenticated upgrades receive HTTP 401

**Given** authenticated WS connection established
**When** server tracks it
**Then** connection keyed by (session_id, user_id) in in-memory registry

**Given** 55 minutes pass (Cloud Run forced reconnect)
**When** client reconnects
**Then** fresh WS established within 5s with no data loss

---

### Story 5.2: Redis Pub/Sub Session Fan-Out + LWW Conflict Resolution (GC-111)

As a trainer and client co-editing the same session,
I want set updates to sync between both devices in ≤500ms via Redis Pub/Sub,
So that both parties always see the latest state.

**Acceptance Criteria:**

**Given** trainer saves a set
**When** SET_UPDATE published to Redis `session:{id}`
**Then** update appears on client's device within ≤500ms

**Given** both parties edit same set row simultaneously
**When** both updates arrive
**Then** LWW using server_version (monotonic); losing write discarded; conflict logged

**Given** disconnect rate exceeds 5%
**When** SM-C2 threshold trips
**Then** alert fires in Cloud Monitoring

---

### Story 5.3: Mobile WS Client + Offline Queue Drain (55-min Reconnect) (GC-112)

As a trainer or client in an active session,
I want the mobile app to maintain WS with automatic reconnect and offline queue drain,
So that sets logged offline sync within 5s of reconnection.

**Acceptance Criteria:**

**Given** network drops mid-session
**When** WS disconnects
**Then** offline banner shows (UX-DR7); new writes queued in WatermelonDB sync_state=queued

**Given** network reconnects
**When** WS re-establishes
**Then** queued sets drain within 5s; each transitions local_only → queued → acked → reconciled

**Given** 55 min pass
**When** Cloud Run reconnect cycle triggers
**Then** client auto-reconnects; 10s polling fallback activates if WS reconnect takes >10s

---

### Story 5.4: Connection Orb — Active Co-Edit, Peak Tension, Disconnected States (GC-113)

As a trainer or client in a co-edit session,
I want the Connection Orb to animate based on real-time co-edit state,
So that both participants have a glanceable signal of sync and connection health.

**Acceptance Criteria:**

**Given** PRESENCE_JOIN received → both avatars appear in Orb header (UX-DR13); Orb → Active co-edit (stem pulses)
**Given** SET_UPDATE from other participant → stem pulses once with amber animation (UX-DR1)
**Given** WS disconnects → Orb → Disconnected state; offline banner appears (UX-DR7)
**Given** reduce-motion active → all states are static (not removed)

---

### Story 5.5: Mid-Session PT-End Handling + SyncStatusBar Offline Banner (GC-114)

As a trainer whose PT relationship ends mid-session,
I want the session to gracefully terminate flushing the WS queue,
So that no data is lost and the client is informed.

**Acceptance Criteria:**

**Given** PT relationship transitions to PtEnded while session in progress
**When** SESSION_END broadcast
**Then** WS writes frozen; offline queue flushed; session marked terminated_during=true

**Given** SESSION_END received on mobile
**When** processed
**Then** non-dismissible "Session ended by trainer" banner shown; all set data retained

**Given** any set has sync_state=local_only or queued
**When** SyncStatusBar renders
**Then** count of unsynced sets shown; no empty save state visible (UX-DR7)

---

## Epic 6: Progress & Recovery Analytics

**Goal:** Build ProgressionService (the single source of truth for all strength metrics), surface recovery chips, ACWR load tracking, progress tab visualizations, and profile tab — giving trainers and clients data-driven insight into performance and injury risk.

---

### Story 6.1: ProgressionService — PR Detection, Volume, Estimated 1RM (GC-115)

As a trainer or client reviewing performance data,
I want a ProgressionService that computes PRs, weekly volume, and estimated 1RM from workout history,
So that all downstream features (E7 AI, E8 PR Moments, E10 solo) share one authoritative source.

**Acceptance Criteria:**

**Given** a completed session with sets logged
**When** ProgressionService.compute(userId, exerciseId) called
**Then** returns: personalRecord (weight × reps), weeklyVolume (kg), estimatedOneRM (Epley formula)

**Given** a new PR is detected
**When** compute() runs
**Then** pr_events row inserted; prior PR for same exercise retained in history (append-only)

**Given** no prior history for exercise
**When** compute() runs
**Then** current session set treated as first PR; no null pointer; estimated1RM calculated from first set

**And** ProgressionService is the ONLY writer of pr_events; E7 AI and E8 PR Moments read via its interface

---

### Story 6.2: Recovery Chips — DOMS Model + Rest-Day Recommendation (GC-116)

As a client viewing my workout history,
I want recovery chips showing which muscle groups are fatigued,
So that I can see rest-day recommendations and avoid overtraining.

**Acceptance Criteria:**

**Given** a session is completed with muscle-group-tagged exercises
**When** recovery engine runs (DOMS model: 24h peak, 48h half-life per muscle group)
**Then** recovery chips appear per muscle group: Fresh / Recovering / Fatigued

**Given** a muscle group is Fatigued
**When** trainer creates next session plan
**Then** rest-day chip surfaced in plan view; trainer can override with note

**Given** client opens Orb home screen
**When** any muscle group is Recovering or Fatigued
**Then** chip rendered in the recovery row (UX-DR11); "Fully recovered" shown when all groups Fresh

---

### Story 6.3: ACWR Backend — Acute:Chronic Workload Ratio Computation (GC-117)

As a trainer monitoring injury risk for a client,
I want the backend to compute the 1:4 Acute:Chronic Workload Ratio weekly,
So that elevated injury risk can be flagged before it causes harm.

**Acceptance Criteria:**

**Given** a client completes sessions over ≥4 weeks
**When** ACWR job runs (weekly cron)
**Then** acwr_snapshots row written: acute_load (7-day), chronic_load (28-day avg), ratio, risk_band (optimal/caution/danger)

**Given** ratio > 1.5 and ≤ 2.0
**When** snapshot written
**Then** risk_band = caution; acwr_alerts row inserted (amber severity)

**Given** ratio > 2.0
**When** snapshot written
**Then** risk_band = danger; acwr_alerts row inserted (red severity); auto-deload flag set (suggested load = 70% of recent avg)

**Given** fewer than 14 days of session history (cold start)
**When** ACWR computed
**Then** risk_band = cold_start; "Baseline-building day X/14" label returned; no alert fired (FR-37 suppression)

**Given** fewer than 4 weeks (but ≥14 days) of history
**When** ACWR computed
**Then** chronic_load uses available weeks; risk_band computed normally with "preliminary" qualifier; risk_band never null on crash

---

### Story 6.4: ACWR Risk Banner in Trainer Session Planning (GC-118)

As a trainer planning a session for a client at elevated injury risk,
I want to see an ACWR risk banner before confirming the plan,
So that I can adjust load before the session starts.

**Acceptance Criteria:**

**Given** client's current ACWR risk_band = caution (>1.5)
**When** trainer opens session plan creation for that client
**Then** amber banner rendered: ratio value + "Consider a deload this week" (UX-DR10); phrasing never "you're overtraining"

**Given** client's current ACWR risk_band = danger (>2.0)
**When** trainer opens session plan creation
**Then** red banner rendered + auto-deload ghost overlay shown with 70% loads pre-populated on each set (UX-DR10)

**Given** trainer taps "Override"
**When** confirmation dialog shown
**Then** override logged with trainer note; banner dismissed; session proceeds with original loads

**Given** risk_band = optimal, cold_start, or insufficient_data
**When** session plan opened
**Then** no banner shown; no performance degradation

---

### Story 6.5: Progress Tab — Strength Chart + Volume Sparklines (GC-119)

As a client or trainer reviewing progress,
I want a Progress tab with a strength chart per exercise and weekly volume sparklines,
So that I can see trends over time at a glance.

**Acceptance Criteria:**

**Given** client has ≥2 sessions with same exercise logged
**When** Progress tab opened
**Then** line chart shows estimated 1RM over time (Epley, from ProgressionService); x-axis = session dates

**Given** client selects "Volume" toggle
**When** view updates
**Then** bar chart shows weekly total kg lifted per muscle group; last 8 weeks visible

**Given** client opens "Body" tab in Progress
**When** rendered
**Then** line charts shown per tracked measurement (weight, body fat %, waist cm, etc.); x-axis = log dates (FR-38)

**Given** client opens "Attendance" tab in Progress
**When** rendered
**Then** calendar heatmap shows days with logged sessions (green) vs rest days vs missed planned days (last 12 weeks) (FR-38)

**Given** client opens "Muscles" tab in Progress
**When** rendered
**Then** per-muscle weekly set-count strip shown; amber chip when 16–20 sets/week; red chip when >20 sets/week (FR-36 Schoenfeld ceiling)

**Given** ACWR data available
**When** Progress tab header renders
**Then** ACWR sparkline shown (last 8 weeks); color-coded by risk_band (UX-DR10); FR-38

**Given** no history yet for selected exercise
**When** chart renders
**Then** empty state: "Log your first [exercise] to start tracking" (UX-DR15)

**And** chart data sourced exclusively from ProgressionService (not raw sets)

---

### Story 6.6: Profile Tab — Lifetime Stats + Goal Progress (GC-120)

As a client,
I want a Profile tab showing lifetime stats, current goal, and achievement badges,
So that I feel a sense of long-term progress and motivation.

**Acceptance Criteria:**

**Given** client opens Profile tab
**When** rendered
**Then** shows: name, profile photo, branch (gym path), membership status + PT expiry date, assigned trainer card (FR-39)

**Given** trainer card rendered
**When** PT is assigned
**Then** shows trainer name, photo, verified badge (if any); tapping routes to trainer public profile

**Given** client opens "Stats" section of Profile
**When** rendered
**Then** shows: total sessions, total kg lifted (lifetime), longest streak (days), current goal title + % progress, body stats summary (last logged weight, body fat %) (FR-39)

**Given** client opens "Schedule" section of Profile
**When** rendered
**Then** default daily visit time shown; per-day overrides editable; rest days configurable (FR-39)

**Given** client has earned a badge (e.g., "First PR", "30-day streak")
**When** Profile tab renders
**Then** badge grid shown; new badges highlighted with pulse animation (UX-DR12)

**Given** no sessions yet
**When** Profile tab renders
**Then** empty state with onboarding nudge: "Complete your first session to start tracking" (not blank screen)


---

## Epic 7: AI Progression Engine

**Goal:** Build the Claude-proxy backend and mobile AI suggestion layer, delivering in-session ghost overlays and AI-assisted progression rules — with cost-model guardrails (circuit-breaker $0.80, pivot $1.00) and feature flags to disable when cost thresholds trip.

---

### Story 7.1: Claude Proxy — Anthropic SDK Backend + Prompt Cache + Cost Model (GC-121) [GATE]

As a backend engineer establishing the AI infrastructure,
I want a Claude proxy service with prompt caching (≥80% hit rate target), per-user cost tracking, and circuit-breaker at $0.80/WAU/mo,
So that AI features run within budget and can be killed centrally.

**Acceptance Criteria:**

**Given** an AI suggestion request arrives
**When** Claude proxy processes it
**Then** Anthropic SDK used (NOT OpenAI); system prompt served from cache; cache_hit tracked in ai_cost_events

**Given** per-WAU cost reaches $0.80
**When** circuit-breaker trips
**Then** all AI endpoints return 503 with body: {"error":"ai_budget_exceeded"}; ai_locked flag set true in feature_flags table

**Given** ai_locked = true
**When** any AI endpoint called
**Then** 503 returned immediately without calling Anthropic; no cost incurred

**Given** monthly spend reviewed
**When** cost > $1.00/WAU
**Then** pivot_alert fires to ops Slack channel; ai_cost_model.html dashboard updated

**And** this story is a GATE — GC-122 through GC-125 must not be started until GC-121 is complete and proxy is deployed

---

### Story 7.2: AI Progression Rule Engine — Suggest Next Set Parameters (GC-122)

As a trainer reviewing a client's session in progress,
I want the AI progression engine to suggest the next set's weight and reps based on history and ACWR,
So that I can make evidence-based micro-progressions without manual calculation.

**Acceptance Criteria:**

**Given** ≥3 prior sessions logged for the exercise
**When** trainer opens set entry for that exercise
**Then** AI suggestion computed: {weight_kg, reps, confidence} based on recent trend + ACWR risk band

**Given** ACWR risk_band = danger
**When** AI suggestion generated
**Then** suggestion shows reduced load (deload week protocol); confidence = "conservative"

**Given** ai_locked = true
**When** set entry opened
**Then** ghost overlay hidden; no suggestion shown; no Anthropic call made

---

### Story 7.3: AI Bootstrap — Cold-Start Suggestions for New Clients (GC-123)

As a trainer onboarding a brand-new client with no history,
I want the AI to provide evidence-based starting weights based on stated goal and body metrics,
So that the first session is appropriately challenging, not a shot in the dark.

**Acceptance Criteria:**

**Given** client has 0 sessions logged
**When** trainer opens first session plan
**Then** AI bootstrap call made with: goal (strength/hypertrophy/endurance), body weight, age; response includes starting weight + rep scheme per exercise

**Given** bootstrap completes
**When** trainer views plan
**Then** "AI Starting Point" badge shown on each suggested set; trainer can accept or override each independently

**Given** ai_locked = true
**When** new client session opened
**Then** no AI bootstrap call; trainer sees standard empty set entry; no error shown

---

### Story 7.4: Ghost Overlay — In-Session AI Target Visualization (GC-124)

As a trainer or client watching a set being logged,
I want ghost weight/rep targets to appear as a semi-transparent overlay on the active set row,
So that both parties can see the AI target without interrupting the logging flow.

**Acceptance Criteria:**

**Given** AI suggestion available for current exercise
**When** active set row renders
**Then** ghost overlay shown: semi-transparent weight + rep bubble anchored above input field (UX-DR1 animation)

**Given** actual logged value equals AI target (±5%)
**When** set saved
**Then** ghost overlay pulses green once then fades; micro-celebration animation (UX-DR12)

**Given** reduce-motion system setting active
**When** ghost overlay renders
**Then** overlay is static; no animation; bubble still visible

**Given** ai_locked = true
**When** set row renders
**Then** ghost overlay absent; no empty space left where it would have been

---

### Story 7.5: ai_locked Feature Flag Toggle + Trainer Notification (GC-125)

As a trainer using AI features,
I want to see a clear in-app notification when the AI budget circuit-breaker trips,
So that I understand why AI suggestions have disappeared and can plan accordingly.

**Acceptance Criteria:**

**Given** ai_locked transitions from false → true
**When** trainer opens any session
**Then** one-time dismissible banner: "AI suggestions paused — monthly budget limit reached. They'll return next billing cycle."

**Given** ai_locked = true
**When** trainer dismisses banner
**Then** banner not shown again until next lock event; no repeat on each screen visit

**Given** ai_locked transitions true → false (new billing cycle reset)
**When** trainer opens next session
**Then** AI suggestions resume silently; no notification needed


---

## Epic 8: PR Moments & Social Sharing

**Goal:** Detect personal records, render in-app celebration moments, and produce shareable PNG cards via a headless worker → GCS pipeline — giving clients a motivational reward loop and a social sharing artifact.

---

### Story 8.1: GCS Bucket + CDN + Headless Worker Infrastructure (GC-126) [GATE]

As a backend engineer setting up the PR card sharing pipeline,
I want a GCS bucket with Cloud CDN, signed URL generation, and a headless PNG render worker scaffolded,
So that all file storage and shareable card generation has a working foundation before PR detection logic is built.

**Acceptance Criteria:**

**Given** GCS bucket created with correct IAM roles
**When** headless worker calls renderCard(prEvent)
**Then** PNG written to gs://vis-pr-cards/{userId}/{prEventId}.png; signed URL returned; TTL = 72h

**Given** CDN configured
**When** signed URL accessed
**Then** response headers include Cache-Control: public, max-age=259200; TTFB < 500ms globally

**Given** worker crashes during render
**When** retry policy triggers
**Then** max 3 retries with exponential backoff; after 3 failures pr_share_events.status = render_failed

**And** this story is a GATE — GC-127 through GC-130 must not be started until GC-126 is deployed and a test PNG successfully written to GCS

---

### Story 8.2: PR Detection + pr_events Write (GC-127)

As a client completing a session,
I want the system to automatically detect when I've set a personal record,
So that my achievements are captured without manual input.

**Acceptance Criteria:**

**Given** session completes (SESSION_END broadcast)
**When** ProgressionService.compute() runs (owned by E6)
**Then** if new PR detected: pr_events row inserted {userId, exerciseId, weight_kg, reps, estimated1RM, session_id, created_at}

**Given** same exercise already has a PR row
**When** new PR detected
**Then** new row inserted; old row NOT updated or deleted (append-only audit requirement)

**Given** no PR (performance same or lower)
**When** compute() runs
**Then** no pr_events row written; no false positive

---

### Story 8.3: In-App PR Moment Screen + Confetti Animation (GC-128)

As a client who just set a personal record,
I want a full-screen PR Moment celebration to appear after my session ends,
So that I feel genuinely rewarded and motivated to keep training.

**Acceptance Criteria:**

**Given** pr_events row written for current client
**When** session summary screen loads
**Then** PR Moment screen auto-presents: exercise name, new PR weight × reps, improvement delta vs previous PR

**Given** PR Moment screen shown
**When** rendered (reduce-motion OFF)
**Then** confetti particle animation plays (UX-DR12); amber-glow Orb pulse in background (UX-DR1)

**Given** reduce-motion system setting ON
**When** PR Moment shown
**Then** static celebration card only; no particles, no animation; data still shown

**Given** no PR this session
**When** session summary loads
**Then** no PR Moment screen; standard session summary renders

---

### Story 8.4: PR Card Composer — RN+Skia In-App Render (GC-129)

As a client viewing a PR Moment,
I want to see a beautifully designed PR card I can share,
So that I can post my achievement to social media.

**Acceptance Criteria:**

**Given** PR Moment screen shown
**When** "Share" button tapped
**Then** RN+Skia card rendered in-app (ARCH-16 in-app path): Vis brand, exercise name, PR weight, improvement delta, client first name

**Given** card rendered
**When** share sheet opened
**Then** native iOS/Android share sheet opens with PNG payload; "Copy image" and social targets available

**Given** Skia render fails (cold device, low memory)
**When** fallback triggers
**Then** headless GCS PNG URL used instead (ARCH-16 fallback path); share sheet still opens

---

### Story 8.5: Headless PNG Worker — Server-Side PR Card Render to GCS (GC-130)

As a backend service generating shareable PR cards,
I want a headless worker to render PR card PNGs server-side and store them in GCS,
So that a permanent shareable link exists even after the client closes the app.

**Acceptance Criteria:**

**Given** pr_events row inserted
**When** headless worker triggered
**Then** PR card PNG rendered server-side (same brand template as RN+Skia in-app); written to gs://vis-pr-cards/{userId}/{prEventId}.png

**Given** PNG written successfully
**When** share link requested by client
**Then** signed URL returned (72h TTL from GC-126); pr_share_events.status = ready

**Given** worker triggered but GCS write fails
**When** max retries exhausted
**Then** pr_share_events.status = render_failed; in-app Skia card still works (no user-facing error)


---

## Epic 9: Nutrition Tracking

**Goal:** Allow trainers to author macro targets per client and enable clients to log daily nutrition — giving both parties visibility into the nutrition side of the training equation.

---

### Story 9.1: Trainer Macro Authoring — Set Daily Targets per Client (GC-131)

As a trainer,
I want to set daily macro targets (protein, carbs, fat, calories) for each of my clients,
So that clients have clear nutrition goals alongside their training plan.

**Acceptance Criteria:**

**Given** trainer opens a client's profile in the trainer app
**When** "Nutrition" tab selected
**Then** macro target form shown: protein_g, carbs_g, fat_g, calories (auto-calc from macros); save writes to client_nutrition_targets table

**Given** trainer saves targets
**When** client opens app
**Then** targets visible in client Nutrition tab within 30s (no app restart needed)

**Given** trainer has not set targets
**When** client opens Nutrition tab
**Then** empty state: "Your trainer hasn't set nutrition targets yet" (not crash, not null values)

---

### Story 9.2: Client Nutrition Tab — Daily Log + Macro Ring (GC-132)

As a client,
I want to log my daily meals and see a macro ring showing progress toward my targets,
So that I can stay on track with my nutrition goals.

**Acceptance Criteria:**

**Given** trainer has set macro targets for client
**When** client opens Nutrition tab
**Then** macro ring shown: four arcs (protein/carbs/fat/calories); filled % based on today's logged meals

**Given** client taps "+ Add Meal"
**When** meal log form opens
**Then** fields: meal_name, protein_g, carbs_g, fat_g, calories; save appends to nutrition_logs table; ring updates immediately

**Given** no targets set by trainer
**When** client opens Nutrition tab
**Then** log form still available; ring hidden; message: "Waiting for your trainer to set targets" shown above log

**Given** client logs nutrition for the day
**When** day changes (midnight)
**Then** new log period starts; yesterday's log retained in history; ring resets to 0%

---

## Epic 10: Solo Paths & Template Marketplace

**Goal:** Enable solo clients (no assigned trainer) to browse and fork curated workout templates, and give trainer clients a "solo day" option — ensuring the app has utility beyond supervised sessions.

---

### Story 10.1: Template Browse + Fork — Solo Client Workout Selection (GC-133)

As a solo client or trainer client on a solo day,
I want to browse a library of 15 curated workout templates and fork one as my plan,
So that I have a structured workout to follow without needing a trainer.

**Acceptance Criteria:**

**Given** client opens "Templates" tab (solo mode or solo day)
**When** rendered
**Then** grid of ≥15 curated templates shown: name, goal tag (strength/hypertrophy/endurance), exercise count, estimated duration

**Given** client taps a template
**When** detail view opens
**Then** full exercise list shown with default sets × reps × rest; "Use This Template" CTA visible

**Given** client taps "Use This Template"
**When** fork action triggers
**Then** new session_plan row created from template (deep copy, not reference); client owns the fork; original template unchanged

**Given** no internet connection
**When** templates tab opened
**Then** previously cached templates shown from WatermelonDB; "Offline — showing cached templates" banner

---

### Story 10.2: Template Edit + Solo Session Logging Grid (GC-134)

As a solo client who has forked a template,
I want to edit the template's sets/reps/weights and then log a session against it,
So that I can customize the workout to my level and track my performance.

**Acceptance Criteria:**

**Given** client has a forked template plan
**When** "Edit Plan" opened
**Then** each exercise row editable: sets, reps, weight_kg; changes save to forked session_plan (not original template)

**Given** client starts a solo session from their forked plan
**When** session logging grid opens
**Then** same equipment-aware grid from E4 (GC-108 component) used; Connection Orb shows Solo state (dormant, no co-edit indicators)

**Given** client completes solo session
**When** SESSION_END triggered
**Then** ProgressionService.compute() runs as normal; PR detection active; solo sessions count toward all analytics (E6, E7, E8)

**Given** client is a trainer-assigned client using "solo day" mode
**When** solo session completes
**Then** session visible in trainer's client history with "Solo" badge; trainer cannot edit retroactively

