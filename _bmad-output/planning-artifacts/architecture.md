---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
lastStep: 8
status: 'complete'
completedAt: '2026-05-24'
inputDocuments:
  - CLAUDE.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/addendum.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/reconcile-original-spec.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/reconcile-domain-research.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/reconcile-progression-spec.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/review-adversarial.md
  - _bmad-output/planning-artifacts/prds/prd-Vis-2026-05-21/review-edge-cases.md
  - _bmad-output/planning-artifacts/ux-design-specification.md
  - _bmad-output/planning-artifacts/research/domain-vis-fitness-pt-research-2026-05-21.md
  - docs/superpowers/specs/2026-05-04-vis-design.md
  - docs/superpowers/specs/2026-05-18-progressive-overload-design.md
  - docs/superpowers/plans/2026-05-04-phase-0-foundation.md
  - docs/superpowers/plans/2026-05-04-admin-web.md
  - docs/superpowers/plans/2026-05-04-trainer-app.md
  - docs/superpowers/plans/2026-05-04-client-app.md
  - docs/superpowers/plans/2026-05-07-software-design-document.md
  - docs/superpowers/plans/2026-05-18-progressive-overload.md
  - prototype/
workflowType: 'architecture'
project_name: 'Vis'
user_name: 'Gauravprakashshinde'
date: '2026-05-23'
status: 'in-progress'
brownfield: true
companion_html: architecture.html
---

# Architecture Decision Document — Vis

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

> Brownfield project. CLAUDE.md + existing `backend/`, `prototype/`, and superpowers plans already encode several locked technical choices. Architecture phase will **confirm** those and **fill gaps** (real-time sync transport, file storage, push, deployment pipeline, AI proxy design, etc.).

## Pre-existing Linear backlog state (audited 2026-05-23)

**5 projects, 49 issues.** Phase 0 Foundation effectively done (14/16 Done, 2 In Review: GC-18 Flyway V1+V2, GC-30 smoke test). Phases 1–3 backlogs (30 issues) were authored 2026-05-07 — **two weeks before the PRD pivot** of 2026-05-21/22. They reflect the pre-pivot spec.

**Decision (user, 2026-05-23):** leave Linear untouched during architecture phase. `bmad-create-epics-and-stories` will rebuild the Phase 1–3 backlog after architecture locks. Phase 0 issues (foundation) stay as-is.

**Architecturally-relevant conflicts surfaced from the audit (must resolve in this workflow):**

| Pre-pivot issue | Pre-pivot plan | New PRD direction | Resolution venue |
|---|---|---|---|
| GC-44, GC-56 — Active Session sync | 5-second polling | WebSocket co-edit per PRD §7.2 + Glossary `Co-Edit` (server-stamped monotonic last-write-wins) | Step: Real-time transport |
| GC-42, GC-45 — AI suggestions | OpenAI proxy + macro generation | Claude proxy with prompt caching, trainer-only, $0.40/WAU ceiling per PRD §7.3, §FR-12, §FR-13 | Step: AI proxy design |
| Backend package | `in.gymculture.*` | `in.vis.*` (already in `backend/` code per CLAUDE.md) | Step: Tech-stack confirmation |
| Phase 2 scope | No Equipment-Aware Logger | 6 loggers (cable / barbell / dumbbell / machine / bodyweight / kettlebell / bands) per PRD FR-8 — biggest UX investment v1 | Sprint planning (post-arch) |
| Phase 3 scope | No PR / e1RM / Recovery / ACWR / Progression Index UI | All v1 per PRD FR-10, 11, 35, 36, 37, 38 | Sprint planning (post-arch) |
| All phases | No Solo paths, no Marketplace, no Audit Log, no Trainer-authored Nutrition, no Cardio, no Plan Templates, no plan-block expiry reminders | All v1 per PRD §6.1 | Sprint planning (post-arch) |

**Architecture phase action items distilled from this audit:**

1. Lock **WebSocket** as Co-Edit transport. Define server design (single Spring Boot WS endpoint? STOMP? raw WS? sticky sessions? Redis pub-sub for fan-out?).
2. Lock **Claude proxy** architecture (server-side caching, fallback to last-prescribed on API failure per PRD §7.3, circuit-breaker at $0.80/WAU).
3. Lock **package naming**: `in.vis.*` everywhere; no `gymculture` references in new code.
4. Decide **idempotency** strategy for Set writes (client UUID dedupe per PRD §7.5).
5. Decide **audit-log enforcement** at DB role level (no DELETE grant per Glossary).
6. Confirm **single region** (Cloud Run `asia-south1`) + backups (RPO 5 min / RTO 4 h per PRD §7.9).

**Out of scope this workflow** (handled by `bmad-create-epics-and-stories`):

- Rewriting Phase 1–3 Linear backlog.
- New epic/story tree under projects like "Vis v1 — Equipment-Aware Logger" / "Vis v1 — Co-Edit Session" / "Vis v1 — Progression Engine" / "Vis v1 — Solo Paths" / "Vis v1 — Gym Ops".

---

## Step 2: Project Context Analysis

_Completed 2026-05-24. Party-mode stress-tested by 4 agents (Winston, Amelia, Mary, Sally); 29 critiques accepted. Open Q5 resolved 2026-05-24 via second party-mode round (Winston, John, Mary, Sally)._

### Architectural Drivers

13 drivers extracted from PRD, UX spec, addendum, and brownfield audit.

| # | Driver | Core Constraint | Source |
|---|---|---|---|
| 1 | **Real-time Co-Edit** | WebSocket, server-stamped monotonic LWW, ≤500ms propagation. Presence: ephemeral WS topic `presence:session:{id}`, fire-and-forget, no persistence. Provenance: `last_actor_id`, `last_actor_role`, `server_stamped_at` (NTP-corrected) on every Set write. | PRD §7.2, FR-1 |
| 2 | **AI Progression Engine** | Claude proxy + prompt caching ≥80% hit. Ghost overlay TTL 30s hard. Cost: $0.40/WAU ceiling; $0.80 throttle; $1.00 pivot. Per-trainer rate limit. **Q5 RESOLVED 2026-05-24:** hybrid history scope — see §Q5 Decision. | PRD §7.3, FR-12/13 |
| 3 | **Offline-First Mobile** | WatermelonDB on op-sqlite. `sync_state`: `local_only \| queued \| acked \| reconciled`. Client UUID idempotency on Set writes. 5s reconnect drain. | PRD §7.5, NFR-4 |
| 4 | **Multi-Role Multi-Tenant Identity** | 6 role-contexts (CLIENT, TRAINER, STAFF, OWNER, SOLO_CLIENT, GUEST). Branch scoping at service layer. PT lifecycle: NoPt → PtActive → PtEnded → PtActive\|NoPt. Two onboarding paths (gym, solo). | PRD §4, CLAUDE.md |
| 5 | **Equipment-Aware Logger** | 6 variants: cable / barbell / dumbbell / machine / bodyweight / kettlebell+bands. Visual weight pickers (drag, plate stacker, slider). Pre-loaded last-used weight. Biggest UX investment v1. | PRD FR-8, addendum §1 |
| 6 | **PR Card Dual-Render** | In-app: RN + Skia (zero latency). Shareable PNG: headless Chromium/Satori worker. Two separate render paths. | PRD FR-11, UX spec |
| 7 | **Schema Versioning + API Compat** | Versioned DTOs: `Accept: application/vnd.vis.vN+json`. Backend supports N + N-1. Pact contract tests in CI. | PRD §7.8 |
| 8 | **Presence Channel** | Ephemeral WS topic per session. Fire-and-forget. Zero DB writes for presence events. | PRD §7.2, UX spec |
| 9 | **Set-Write Provenance** | `last_actor_id`, `last_actor_role`, `server_stamped_at` on every Set row. Audit trail at row level. | PRD §7.2 Glossary |
| 10 | **Audit Log Enforcement** | Separate DB roles: app = no DELETE; migration = DDL+DELETE. Postgres RLS on `tenant_id`. Monthly range partitioning day 1. | PRD Glossary, addendum |
| 11 | **Compliance (DPDPA + GDPR)** | DPDPA 8 obligations + GDPR 6 endpoints. Data classification matrix: PII / sensitive-health / operational. Min-age gate (DPDPA §9, teen gym clients). | PRD §7.10 |
| 12 | **Deployment + Migration** | Cloud Run `asia-south1`. RPO 5 min / RTO 4 h. Flyway migrations. GH Actions monorepo + EAS Build mobile. Secrets: Google Secret Manager (not in Docker image). | PRD §7.9, CLAUDE.md |
| 13 | **User Preferences + Theme Tokens** | `user_preferences` table (`accent_color_hex`, `reduce_motion`, `reduce_transparency`, `unit_system`, `locale`). Shared by RN + Angular + PR card render worker. | CLAUDE.md, UX spec |

### Q5 Decision: AI History Scope (BLOCKER → RESOLVED 2026-05-24)

**Decision: Hybrid**

| Layer | Scope | Rationale |
|---|---|---|
| Raw set logs (weight, reps, RIR) | **Human-identity, 90-day rolling window** | Client's physiological record. Bounded window = stable cache key + efficient query. |
| AI suggestion history (what was recommended / accepted) | **Trainer-identity** | DPDPA/GDPR: aggregating behavioral history across relationships requires explicit consent. Trainer-identity is the safe default. |
| Prescription notes / program structure | **Trainer-identity** | Trainer's methodology/IP. Excluded from cross-trainer AI context. |

**Claude prompt structure:**

- Stable cacheable prefix → `client_id + exercise_id + 90-day set log` (human-identity layer)
- Dynamic suffix → current trainer's prescription context (trainer-identity layer)
- Cache key: `hash(client_id + exercise_id + 90day_set_log_hash)` — high stability, high hit rate

**Client consent gate:**

- Default: new trainer sees client's 90-day raw set log
- Full history (pre-90 days): explicit client consent toggle ("Share full training history with [Coach]?")
- Ghost overlay chip: context source shown ("Based on your training history" vs "Based on sessions with [Trainer]")

**v2 upgrade path:** trainer-facing toggle to include prior trainer programs as context — not in v1, door stays open.

### Cross-Cutting Concerns

- **WS auth:** token revalidation on WebSocket upgrade (auth-on-upgrade), not just HTTP handshake
- **API hardening:** Cloud Armor + per-Firebase-UID token bucket (Bucket4j); per-connection message-size limit
- **Secrets:** Google Secret Manager for Firebase SA, Claude API key, Postgres creds — not in Docker image or env file
- **RN paradigm:** Expo prebuild (bare workflow + Expo modules) — not managed Expo, not vanilla RN
- **Local state:** MMKV only for auth tokens + feature flags. Domain data = WatermelonDB only. No exceptions.
- **Package:** `in.vis.*` everywhere — zero `gymculture` references in new code
- **Observability:** cost telemetry day 1 — per-trainer WAU spend emitted as structured log for Cloud Logging / alerting
- **E2E co-edit:** Playwright × 2 admin-web instances on every PR + Maestro Cloud × 2 devices nightly

### Cloud Run WS: Path A vs Path B

_Not yet locked. Carried into Step 4 (Technology Decisions)._

| | Path A: Cloud Run + Redis Pub/Sub | Path B: GKE / Compute Engine |
|---|---|---|
| Stickiness | `--session-affinity` header-hash | True sticky (pod affinity) |
| Fan-out | Redis Pub/Sub (`RedisMessageListenerContainer`) | In-process or Redis optional |
| Forced reconnect | 55-min cycle (Cloud Run 3600s timeout − 5-min buffer) | None |
| Reconnect UX | Client drains 5s queue, resubscribes | Transparent |
| Ops overhead | Low (serverless) | Medium–High (cluster management) |
| v1 concurrent WS estimate | <500 (single gym, early adopter) | same |
| v1 cost | Lower | Higher (cluster floor) |

_Lean: Path A. Redis Pub/Sub adds ~40ms fan-out latency — acceptable at v1 scale. Lock in Step 4._

### Remaining Lock Gates (Step 4 inputs)

| Gate | Status |
|---|---|
| Open Q5 — AI history scope | ✅ RESOLVED 2026-05-24 (Hybrid — see §Q5 Decision) |
| Cloud Run WS Path A vs B | 🔲 Open — carry to Step 4 |
| Cost model math (`ai-cost-model.html`) | 🔲 Open — produce in Step 4 |
| Min-age gate (DPDPA §9) | 🔲 Open — policy decision needed (Product / Legal) |

---

## Step 3: Starter Template Evaluation

_Completed 2026-05-24. Brownfield project — 4 separate stacks, most already initialized._

### Current Versions (verified 2026-05-24)

| Stack | In Repo | Latest Stable | Action |
|---|---|---|---|
| Spring Boot | 3.3.5 (Java 21) | 3.5.11 (3.x line); 4.0.6 (Spring Framework 7, Java 17+ min) | Bump to 3.5.x now. Defer 4.0 post-v1 — breaking changes, no arch benefit at v1 scale. |
| Angular | 17.3 | 21.2.14 (zoneless default, Vitest default) | Upgrade to Angular 21 before Phase 1 development. Karma → Vitest migration cost grows the later it's deferred. |
| React Native (trainer-app) | 0.85.3, React 19.2.3 | RN 0.85.x current | Migrate to Expo bare workflow (see below). |
| React Native (client-app) | 0.85.3, React 19.2.3 | RN 0.85.x current | Migrate to Expo bare workflow (see below). |
| Expo SDK | Not installed | SDK 56 | Install via migration path. |

### Critical Finding: RN Apps Are Vanilla — No Expo

Both `trainer-app/` and `client-app/` are vanilla React Native (confirmed: `app.json` has no `"expo"` key; no `expo`/`@expo` packages installed). This conflicts with the locked cross-cutting concern: _"Expo prebuild (bare workflow + Expo modules)"_. **EAS Build** (locked in CI/CD arch) requires Expo. Migration is required before Phase 2 mobile development starts.

**Migration (not a rewrite — ~30 min per app):**

```bash
# Per app (trainer-app/ and client-app/)
npm install expo
npx expo install expo-modules-core
# Patch AppDelegate.swift (iOS) + MainApplication.kt (Android) per Expo bare docs
npx expo prebuild   # generates /ios and /android from app.json Expo config
```

RN 0.85.3 + React 19.2.3 remain unchanged. Only build tooling changes.

### Initialization Commands (locked)

| App | Command |
|---|---|
| `backend/` | `mvn versions:update-parent` (bump to Spring Boot 3.5.x) |
| `admin-web/` | `ng update @angular/core@21 @angular/cli@21` (pre-Phase 1) |
| `trainer-app/` | Expo prebuild migration (pre-Phase 2) |
| `client-app/` | Expo prebuild migration (pre-Phase 2) |

---

## Step 4: Core Architectural Decisions

_Completed 2026-05-24. Decisions locked across all five categories._

### Decision Priority Analysis

**Critical (Block Implementation):** WS server design, Cloud Run WS path, file storage, push notifications
**Important (Shape Architecture):** Angular state management, observability stack
**Deferred Post-v1:** Spring Boot 4.0 migration, trainer-toggle for prior-trainer AI context

### Data Architecture

| Decision | Choice | Rationale |
|---|---|---|
| Primary DB | PostgreSQL (existing) | Locked brownfield |
| Mobile local DB | WatermelonDB on op-sqlite | Locked Step 2 |
| Migrations | Flyway | Locked brownfield |
| DB roles | app = no DELETE; migration = DDL+DELETE | Locked — audit enforcement |
| Audit partitioning | Monthly range partitions day 1 | Locked Step 2 |
| File storage | **GCS + Cloud CDN** | Natural GCP fit; no cross-cloud auth; Cloud CDN for image delivery |

### Authentication & Security

Firebase Auth → `FirebaseAuthFilter` → Spring Security → branch scoping at service layer. Cloud Armor + Bucket4j per-Firebase-UID token bucket. Auth-on-upgrade for WebSocket. All locked from brownfield + Step 2.

### API & Communication Patterns

| Decision | Choice | Rationale |
|---|---|---|
| REST versioning | DTOs `vnd.vis.vN+json`, N + N-1 | Locked |
| Contract tests | Pact in CI | Locked |
| WS server design | **Raw `@ServerEndpoint` + Redis Pub/Sub** | STOMP relay requires RabbitMQ/Artemis (not Redis); raw WS simpler for ≤2-participant session fan-out; ~40ms Redis fan-out acceptable at v1 |
| Cloud Run WS | **Path A** (`--session-affinity` + Redis, 55-min reconnect) | Lower ops overhead; lower cost; <500 concurrent WS at v1 |
| Push notifications | **FCM via Firebase Admin SDK** | Already on Firebase stack; backend sends via FCM Admin SDK; device tokens in `user_devices` table |

### Frontend Architecture

| Decision | Choice | Rationale |
|---|---|---|
| Mobile domain state | WatermelonDB | Locked |
| Mobile auth/flags | MMKV | Locked |
| RN paradigm | Expo prebuild bare | Locked |
| Angular state | **Angular Signals + services (no NgRx v1)** | CRUD-heavy admin UI; NgRx overhead not justified at this scope |

### Infrastructure & Deployment

| Decision | Choice | Rationale |
|---|---|---|
| Cloud | Cloud Run asia-south1 | Locked |
| CI/CD | GH Actions + EAS Build | Locked |
| Secrets | Google Secret Manager | Locked |
| SLOs | RPO 5 min / RTO 4 h | Locked |
| Observability | **Cloud Logging + Cloud Monitoring** | Native GCP; structured JSON logback → Cloud Logging; `ai.cost.wau_spend` custom metric day 1 |

### Remaining Open Gates

| Gate | Status |
|---|---|
| Produce `ai-cost-model.html` (verify Claude pricing, $/call at 80% cache hit) | Open |
| Min-age gate — DPDPA §9, teen gym clients | Open — Policy / Legal decision |

---

## Step 7: Architecture Validation & Completion

_Completed 2026-05-24._

### Coherence Validation ✅

All technology choices compatible. Key note: `op-sqlite` must be registered as Expo plugin in `app.json` (`"plugins": ["@op-engineering/op-sqlite"]`) or Expo prebuild will not generate correct native code for WatermelonDB.

### Requirements Coverage ✅

| FR Group | Coverage |
|---|---|
| FR-1 Real-time Co-Edit | `ws/SessionEndpoint.java` + `wsClient.ts` |
| FR-2–7 Session/Program/Set management | REST + PostgreSQL + service layer |
| FR-8 Equipment Logger (6 variants) | `src/loggers/` — 6 subdirs |
| FR-9 Exercise library | REST + PostgreSQL |
| FR-10 e1RM | `ProgressionService.java` |
| FR-11 PR Card dual-render | `PrCardRenderService.java` + `PrCelebrationScreen.tsx` |
| FR-12/13 AI ghost overlay | `ai/` package + `GhostOverlay.tsx` |
| FR-14–20 Client app features | REST + client-app screens |
| FR-21–30 Trainer workflows | REST + trainer-app screens |
| FR-31–34 Admin/Gym Ops | admin-web features |
| FR-35–38 Progression analytics | `ProgressionService.java` + `ProgressionScreen.tsx` |
| FR-39–43 Compliance/GDPR/DPDPA | `GdprController.java` + audit log patterns |

All 8 NFR groups addressed (Performance, Security, Scalability, Offline, AI cost, Compliance, Availability, Maintainability).

### Gap Analysis

**Critical (resolve before Phase 1 implementation starts):**

| Gap | Action |
|---|---|
| `ai-cost-model.html` not produced | Produce with verified Claude pricing before arch freeze |
| Min-age gate policy (DPDPA §9) | Product / Legal decision needed before registration flow stories |
| PR Card render worker tech (Cloud Run Job? Cloud Functions? In-process?) | Decide in Phase 3 planning |
| Redis production instance (Cloud Memorystore vs self-hosted) | Specify in infra setup story |

**Important (resolve before Phase 2):**

| Gap | Action |
|---|---|
| op-sqlite in Expo `app.json` plugins | Add to app.json in Expo migration story |
| FCM HTTP v1 API setup | Add service account scope config to infra story |
| GCS bucket structure (names, IAM, CDN config) | Specify in file storage story |
| Testcontainers Redis config | Add `tc:redis:7:///` to `application-test.properties` |
| React Navigation version pin | Pin in package.json |

### Architecture Completeness Checklist

- [x] Project context analyzed (13 drivers, party-mode validated)
- [x] Scale + complexity assessed (<500 concurrent WS, single region, v1 free)
- [x] Technical constraints identified (DPDPA, cost ceilings, offline-first, multi-tenant)
- [x] Cross-cutting concerns mapped (8 documented)
- [x] Critical decisions documented with versions
- [x] Technology stack fully specified (4 stacks)
- [x] Integration patterns defined (REST + WS + FCM + GCS)
- [x] Performance considerations addressed
- [x] Naming conventions established (6 categories)
- [x] Structure patterns defined
- [x] Communication patterns specified (WS event exhaustive list)
- [x] Process patterns documented (branch scoping, idempotency, soft-delete, weight unit)
- [x] Complete directory structure defined (all 4 stacks)
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

**Overall Status: READY WITH MINOR GAPS**
**Confidence: High** — core architecture coherent, covers all 43 FRs + 8 NFR groups.

### Implementation Handoff

**AI agents MUST read sections:**
1. Step 2 §Q5 Decision — AI history scope (hybrid)
2. Step 5 §Enforcement Guidelines — 8 mandatory patterns + anti-patterns
3. Step 6 — directory structure for their target stack

**First implementation priority per stack:**
- `backend/`: bump Spring Boot to 3.5.x → implement Flyway baseline migration → `FirebaseAuthFilter` already done
- `trainer-app/` + `client-app/`: Expo prebuild migration → add op-sqlite plugin → pin React Navigation
- `admin-web/`: `ng update @angular/core@21 @angular/cli@21`

---

## Step 6: Project Structure & Boundaries

_Completed 2026-05-24. Complete directory tree for all 4 stacks, integration boundaries, FR→structure mapping._

### Monorepo Root

```
Vis/
├── backend/                Spring Boot 3.x REST API + WebSocket
├── trainer-app/            React Native (Expo prebuild) — Trainer app
├── client-app/             React Native (Expo prebuild) — Client app
├── admin-web/              Angular 21 — Branch Staff/Owner web
├── prototype/              Reference prototype (visual source of truth — do not modify)
├── docs/
│   ├── brand-ref/          reference_light.png, reference_dark.png
│   └── superpowers/        Specs + plans
├── _bmad-output/           Planning artifacts
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── trainer-app-eas.yml
│       ├── client-app-eas.yml
│       ├── admin-web-ci.yml
│       └── pact-verify.yml
└── CLAUDE.md
```

### `backend/` Structure

```
backend/
├── pom.xml
├── Dockerfile
├── docker-compose.yml              PostgreSQL + Redis (local dev)
└── src/
    ├── main/java/in/vis/
    │   ├── config/
    │   │   ├── FirebaseConfig.java
    │   │   ├── SecurityConfig.java
    │   │   ├── WebSocketConfig.java       @ServerEndpoint registration
    │   │   ├── RedisConfig.java           Redis Pub/Sub beans
    │   │   └── GcsConfig.java             GCS client bean
    │   ├── filter/
    │   │   ├── FirebaseAuthFilter.java
    │   │   └── WsAuthFilter.java          Auth-on-upgrade check
    │   ├── ws/
    │   │   ├── SessionEndpoint.java        @ServerEndpoint — session co-edit
    │   │   ├── PresenceEndpoint.java       @ServerEndpoint — presence channel
    │   │   ├── WsSessionRegistry.java     In-memory connection map
    │   │   └── RedisPubSubListener.java   Redis subscribe → fan-out to local connections
    │   ├── ai/
    │   │   ├── ClaudeProxyService.java    API calls + cache-hit tracking
    │   │   ├── PromptBuilder.java         Stable prefix + dynamic suffix assembly
    │   │   ├── AiCostTracker.java         Per-trainer WAU spend → Cloud Monitoring metric
    │   │   └── AiCircuitBreaker.java      $0.80 throttle, $1.00 pivot
    │   ├── model/
    │   │   ├── Branch.java
    │   │   ├── User.java
    │   │   ├── WorkoutSession.java
    │   │   ├── ExerciseSet.java            last_actor_id, last_actor_role, server_stamped_at
    │   │   ├── Exercise.java
    │   │   ├── Program.java
    │   │   ├── UserDevice.java            FCM tokens
    │   │   └── UserPreferences.java       accent_color_hex, unit_system, locale, …
    │   ├── enums/
    │   │   ├── Role.java
    │   │   ├── SyncState.java
    │   │   ├── ActorRole.java
    │   │   └── EquipmentType.java
    │   ├── repository/
    │   ├── service/
    │   │   ├── ExerciseSetService.java     idempotency check + branch scope
    │   │   ├── ProgressionService.java     e1RM, ACWR, PR detection, Progression Index
    │   │   ├── PrCardRenderService.java    headless render worker → GCS
    │   │   ├── FcmService.java            Firebase Admin SDK push
    │   │   ├── SyncService.java           queued write drain + ack
    │   │   └── UserPreferencesService.java
    │   ├── controller/
    │   │   ├── SessionController.java
    │   │   ├── ExerciseSetController.java
    │   │   ├── ProgressionController.java
    │   │   ├── SyncController.java
    │   │   ├── UserPreferencesController.java
    │   │   └── GdprController.java         /api/v1/me/export, /delete
    │   ├── dto/v1/                         V1 request/response records
    │   └── exception/
    │       ├── GlobalExceptionHandler.java → ProblemDetail RFC 7807
    │       └── VisException.java
    ├── main/resources/
    │   ├── application.properties
    │   ├── application-test.properties     Testcontainers override
    │   └── db/migration/                   Flyway scripts
    └── test/java/in/vis/
        ├── unit/
        ├── controller/                     @WebMvcTest slice tests
        └── integration/                    Testcontainers PostgreSQL + Redis
```

### `trainer-app/` and `client-app/` (identical layout)

```
{app}/
├── app.json                     Expo config
├── package.json
├── tsconfig.json
├── ios/                         Expo prebuild generated — do not hand-edit
├── android/                     Expo prebuild generated — do not hand-edit
└── src/
    ├── screens/
    │   ├── HomeScreen.tsx
    │   ├── ActiveSessionScreen.tsx
    │   ├── SessionHistoryScreen.tsx
    │   ├── ProgressionScreen.tsx
    │   ├── PrCelebrationScreen.tsx  RN + Skia
    │   └── ProfileScreen.tsx
    ├── loggers/
    │   ├── CableLogger/             drag-to-pin visual
    │   ├── BarbellLogger/
    │   ├── DumbbellLogger/
    │   ├── MachineLogger/
    │   ├── BodyweightLogger/
    │   └── BandsKettlebellLogger/
    ├── components/
    │   ├── GhostOverlay.tsx         AI suggestion chip
    │   ├── PresenceIndicator.tsx
    │   └── SyncStatusBar.tsx        local_only/queued/acked/reconciled
    ├── hooks/
    │   ├── useSession.ts
    │   ├── useSyncQueue.ts
    │   └── useAiSuggestion.ts       30s TTL discard
    ├── store/
    │   ├── schema.ts                WatermelonDB schema
    │   ├── models/
    │   └── sync/
    │       ├── syncQueue.ts
    │       └── reconcile.ts
    ├── services/
    │   ├── apiClient.ts             vnd.vis.v1+json header
    │   ├── wsClient.ts              heartbeat + reconnect
    │   └── fcmClient.ts
    ├── navigation/
    │   └── AppNavigator.tsx
    └── utils/
        ├── weightConverter.ts       display only — never store lb
        ├── dateFormatter.ts         per user_preferences.locale
        └── idempotencyKey.ts        uuid v4 for Set writes
```

### `admin-web/` (Angular 21)

```
admin-web/src/app/
├── features/
│   ├── sessions/     Signals-based; session detail + history
│   ├── clients/
│   ├── trainers/
│   ├── branches/
│   └── reports/
├── shared/
│   ├── components/   notification/, loading/
│   └── services/     notification.service.ts
├── core/
│   ├── guards/       auth.guard.ts
│   ├── interceptors/ auth.interceptor.ts, error.interceptor.ts
│   └── services/     firebase-auth.service.ts
└── environments/
```

### Integration Boundaries & Data Flow

```
trainer-app ──REST──► backend/controller/ ──► service/ ──► PostgreSQL
client-app  ──WS────► backend/ws/         ──► Redis Pub/Sub ──► fan-out to both apps
admin-web   ──REST──► backend/controller/

backend/ai/       ──HTTPS──► Claude API  (stable prefix cached; dynamic suffix appended)
backend/          ──FCM────► Firebase Cloud Messaging ──► iOS / Android devices
backend/          ──GCS────► profile photos, PR card PNGs ──► Cloud CDN ──► all apps
```

### FR → Directory Mapping

| Feature | Backend | Mobile | Admin |
|---|---|---|---|
| FR-1 Co-Edit | `ws/SessionEndpoint.java` + `ws/RedisPubSubListener.java` | `services/wsClient.ts` + `hooks/useSession.ts` | — |
| FR-8 Equipment Logger | `service/ExerciseSetService.java` | `src/loggers/*` | — |
| FR-11 PR Card | `service/PrCardRenderService.java` | `screens/PrCelebrationScreen.tsx` | — |
| FR-12/13 AI | `ai/ClaudeProxyService.java` + `ai/PromptBuilder.java` | `hooks/useAiSuggestion.ts` + `components/GhostOverlay.tsx` | — |
| FR-10/35-38 Progression | `service/ProgressionService.java` | `screens/ProgressionScreen.tsx` | — |
| GDPR/DPDPA | `controller/GdprController.java` | — | — |
| User preferences | `service/UserPreferencesService.java` | `utils/weightConverter.ts` + locale | features/profile/ |
| Audit log | `model/*.java` deleted_at + Flyway + DB role grants | — | — |

---

## Step 5: Implementation Patterns & Consistency Rules

_Completed 2026-05-24. Rules preventing cross-agent implementation conflicts across 4 stacks._

### Naming Patterns

**Database (PostgreSQL):**

| Convention | Rule | Example |
|---|---|---|
| Tables | `snake_case` plural | `workout_sessions`, `exercise_sets`, `user_devices` |
| Columns | `snake_case` | `branch_id`, `server_stamped_at`, `sync_state` |
| Foreign keys | `{singular_entity}_id` | `trainer_id`, `client_id`, `session_id` |
| Indexes | `idx_{table}_{col(s)}` | `idx_exercise_sets_session_id` |
| Postgres enum types | `snake_case` | `sync_state_enum`, `user_role_enum` |
| Soft-delete column | `deleted_at TIMESTAMPTZ` (nullable) | filter: `WHERE deleted_at IS NULL` |

**REST API:**

| Convention | Rule | Example |
|---|---|---|
| Base path | `/api/v1/` | `/api/v1/sessions` |
| Resources | `snake_case` plural | `/exercise_sets`, `/workout_sessions` |
| Path params | `camelCase` | `/{sessionId}`, `/{userId}` |
| Query params | `camelCase` | `?trainerId=`, `?startDate=` |
| Custom headers | `X-Vis-*` | `X-Vis-Request-Id`, `X-Vis-Actor-Role` |

**Code:**

| Layer | Convention | Example |
|---|---|---|
| Java classes | `PascalCase` | `WorkoutSessionService` |
| Java methods/vars | `camelCase` | `findByBranchId()`, `sessionId` |
| TS/RN components | `PascalCase` file + function | `CableLogger.tsx` |
| TS/RN utils | `camelCase` file | `syncQueue.ts`, `weightConverter.ts` |
| Angular components | `PascalCase` class, `kebab-case` selector | `app-session-list` |
| CSS Module classes | `camelCase` | `.orbContainer`, `.weightSlider` |

### Structure Patterns

**Backend (`in.vis.*`):**
```
controller/   REST endpoints — no business logic
service/      Business logic + branch enforcement (every method checks branch)
repository/   Spring Data JPA + custom queries
model/        JPA entities
dto/          Versioned request/response records (SetResponseV1, SetResponseV2)
filter/       HTTP + WS filters (FirebaseAuthFilter, WsAuthFilter)
ws/           @ServerEndpoint handlers + Redis pub/sub listener
ai/           Claude proxy, prompt builder, cost tracker
config/       Spring config classes
exception/    GlobalExceptionHandler + typed exceptions
```

**React Native (trainer-app/, client-app/):**
```
src/
  screens/      Full screens (one file = one screen)
  components/   Reusable UI (co-located .module.css)
  loggers/      Equipment-Aware Logger variants (CableLogger/, BarbellLogger/, etc.)
  hooks/        Custom hooks (useSession, useSyncQueue)
  store/        WatermelonDB schema + models + sync logic
  services/     API client, WS client, FCM client
  navigation/   React Navigation setup
  utils/        Formatters, weight converters (always → kg), date helpers
```

**Angular (admin-web/src/app/):**
```
features/     Feature modules (sessions/, clients/, branches/, trainers/, reports/)
shared/       Shared components + services (notification, loading)
core/         Auth guards, HTTP interceptors, Firebase auth service
environments/ environment.ts, environment.prod.ts
```

### Format Patterns

**API success response — direct (no wrapper):**
```json
{ "id": "uuid", "weight": 80.0, "reps": 5, "serverStampedAt": "2026-05-24T10:30:00Z" }
```

**API error response — RFC 7807 ProblemDetail:**
```json
{
  "type": "https://vis.in/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Weight must be positive",
  "instance": "/api/v1/sessions/abc/sets"
}
```

**WebSocket message (all directions):**
```json
{
  "type": "SET_UPDATE",
  "sessionId": "uuid",
  "serverTs": 1716549000000,
  "actorId": "firebase-uid",
  "actorRole": "TRAINER",
  "payload": { ... }
}
```

**Date/time rules:**
- DB: `TIMESTAMPTZ` for timestamps; `DATE` for date-only
- JSON API: ISO 8601 strings — never Unix ms in REST responses
- WS messages: Unix ms (`serverTs`) for monotonic ordering
- Display: per `user_preferences.locale` — never format on backend

**Weight/unit rules (non-negotiable):**
- DB: always `DECIMAL(6,2)` in kg. Never store lb.
- API: always kg in/out. Client converts for display per `unit_system`.
- No unit parameter in DTOs — weight is always kg at the API boundary.

### Communication Patterns

**WebSocket event types (exhaustive — agents must not invent new types):**

| Type | Direction | Meaning |
|---|---|---|
| `SET_UPDATE` | Server → both | Set row created/updated; LWW |
| `SET_DELETE` | Server → both | Set soft-deleted |
| `PRESENCE_JOIN` | Server → both | Actor joined session |
| `PRESENCE_LEAVE` | Server → both | Actor left/disconnected |
| `SESSION_END` | Server → both | Session marked complete |
| `AI_SUGGESTION` | Server → trainer | Ghost overlay payload |
| `SYNC_ACK` | Server → originator | Queued write acknowledged |

**Mobile sync state transitions (strict order):**
```
local_only → queued → acked → reconciled
```
Never skip states. Never write `reconciled` from client side.

**Angular Signals pattern:**
- Services own signals: `private readonly _sessions = signal<Session[]>([])`
- Expose read-only: `readonly sessions = this._sessions.asReadonly()`
- Derived state: `computed(() => this._sessions().filter(s => s.active))`
- Never mutate signal value — always `.set()` or `.update()`

### Process Patterns

**Branch scoping (backend — mandatory):**
```java
// Every service method querying tenant data MUST verify branch:
session = sessionRepo.findById(sessionId)
    .filter(s -> s.getBranchId().equals(auth.getBranchId()))
    .orElseThrow(() -> new AccessDeniedException("Branch mismatch"));
```

**Set-write idempotency (mandatory):**
```java
if (setRepo.existsByClientIdempotencyKey(dto.getClientKey())) {
    return setRepo.findByClientIdempotencyKey(dto.getClientKey());
}
// Never insert without checking idempotency key first.
```

**Soft deletes — no hard deletes on core entities:**
```sql
UPDATE exercise_sets SET deleted_at = NOW() WHERE id = ?;
-- All reads: WHERE deleted_at IS NULL
```

**Error handling:**
- Backend: `GlobalExceptionHandler` → ProblemDetail for all exceptions
- RN: per-screen `ErrorBoundary` + global `onUnhandledRejection` → structured log
- Angular: `HttpInterceptor` catches HTTP errors → `NotificationService` toast
- Never swallow errors silently

**Optimistic updates (RN logger):**
- Write to WatermelonDB immediately, show result
- Never block logger UI on network
- Reconcile on `SYNC_ACK` / `reconciled` state

### Enforcement Guidelines

**All agents MUST:**
1. Apply `WHERE deleted_at IS NULL` to every entity query
2. Apply branch scope check in every service method touching tenant data
3. Check idempotency key before every Set insert
4. Store weights as `DECIMAL(6,2)` kg — never lb in DB or API
5. Use `snake_case` plural for table names; `camelCase` for JSON fields
6. Use ISO 8601 strings in REST JSON; Unix ms only in WS `serverTs`
7. Use ProblemDetail (RFC 7807) for all API error responses
8. Write Set rows with `last_actor_id`, `last_actor_role`, `server_stamped_at`

**Anti-patterns (agents MUST NOT):**
- Hard `DELETE` from core tables
- Store `unit_system` in Set rows (weight is always kg)
- Invent new WS event types outside the exhaustive list
- Write Angular feature state with NgRx
- Use MMKV for domain data (only auth tokens + feature flags)
- Put business logic in controllers
