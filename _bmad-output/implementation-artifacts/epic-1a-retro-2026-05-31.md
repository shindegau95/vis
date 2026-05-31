# Epic E1a — Backend Infrastructure: Retrospective

**Date:** 2026-05-31
**Facilitator:** Amelia (Developer)
**Project Lead:** Gauravprakashshinde
**Epic Status:** ✅ done (6/6 stories shipped)
**Retro Status:** done

---

## 1. Epic Summary

### Delivery Metrics

| Metric | Value |
|---|---|
| Stories planned | 6 |
| Stories shipped | 6 (100%) |
| Linear spec issues | GC-64..69 |
| Linear dev mirrors | GC-136..GC-140 (GC-140 pending re-auth at retro time) |
| Commits (first → last) | `ef6aac2` (1a.2 first dev work) → `f7ad9c7` (epic close) |
| Backend test count | 15 (baseline) → **41** (+26) |
| CI status (every story) | Backend ✓ / Admin Web ✓ / Pact ✓ / Deploy Prototype ✓ |
| Production incidents | 0 (no prod yet — infrastructure-only epic) |

### Story-by-Story

| # | Title | Mirror | Commit | New tests |
|---|---|---|---|---|
| 1a.1 | GH Actions CI/CD pipelines | GC-64 (existed) | `a77c9e4` / `db1dd30` | 0 (CI scaffolding) |
| 1a.2 | Spring Boot 3.5.14 + `in.vis.*` rename | VIS-136 / GC-136 | `ef6aac2` | 0 (`@MockBean` → `@MockitoBean` migration only) |
| 1a.3 | Redis 7 + Testcontainers | VIS-137 / GC-137 | `4fe3838` + `afa3438` | +2 (`RedisIntegrationTest`) |
| 1a.4 | Bucket4j per-Firebase-UID rate limit | VIS-138 / GC-138 | `14cdc66` + `c7b5b86` | +10 (7 unit + 3 integration) |
| 1a.5 | Versioned DTO content-negotiation | VIS-139 / GC-139 | `30a3ccc` + `256f58d` | +6 (`BranchVersioningIntegrationTest`) |
| 1a.6 | Observability day-1 (SM-C1..C10 + OTel) | VIS-140 / GC-140 (mirror pending) | `321ac7f` + `f7ad9c7` | +8 (5 unit + 3 integration tracing/prom) |

---

## 2. Successes (what landed clean)

### S1. Story-file context discipline carried the epic

Every story file shipped with explicit **IS / IS NOT** scope sections, plus a deferred-work pointer for anything that surfaced mid-implementation. Result: zero scope creep across six stories.

**Evidence:** Every story shipped with the original ACs unchanged. Where work surfaced that didn't belong (prod Redis hardening in 1a.3, SSL/circuit-breaker in 1a.4, real per-trainer AI cost in 1a.6) it went to `deferred-work.md` instead of growing the story.

### S2. Triple-flip status pattern stabilized

`story-file → sprint-status → Linear mirror` flipping `ready-for-dev → in-progress → review → done` worked on every story. Commit prefix `VIS-<mirror#>` made provenance trivial in `git log`.

### S3. Testcontainers `GenericContainer` + `@DynamicPropertySource` is the canonical non-JDBC TC pattern

Established in 1a.3 (`RedisIntegrationTest`), reused unchanged in 1a.4 (`RateLimitIntegrationTest`), and didn't need re-invention. The `tc:redis:7:///` JDBC-URL shorthand from architecture was correctly recognized as conceptual, not literal.

### S4. `@Profile("!test")` on infra-touching configs is the right default

`SecurityConfig`, `FirebaseConfig`, `RedisConfig`, `RateLimitConfig` all use this guard. `WebConfig` correctly DROPS the guard (1a.5 dev note) because it's pure MVC config with no infra deps. Pattern is now four-config wide and stable.

### S5. Review patches stayed tiny — story files were comprehensive

Across stories that ran through `bmad-code-review` (1a.3, partial on 1a.4–1a.6 via internal triage), patches per story averaged ~2 small items (test-assertion tightening, defensive checks). Indicates story Dev Notes were absorbing the design weight upfront.

### S6. Linear hygiene held even when MCP token died

When the `claude.ai Linear` MCP token expired mid-1a.5, the convention "create mirror at story start, flip at end" still produced a tracking record — the only loss was the final In Review → Done flip on GC-139 and GC-140, both flagged in their commit messages for manual close.

---

## 3. Challenges (where time went)

### C1. Local Docker Engine 29.x is unreliable

**Manifested in:** 1a.3 (first), 1a.4, 1a.5 — docker daemon hung 4 separate times.

**Pattern that worked:**
```bash
pkill -9 -f "com.docker" 2>&1
pkill -9 -f "Docker Desktop" 2>&1
sleep 3
open -a Docker
# then Monitor: curl -s --max-time 3 --unix-socket /var/run/docker.sock http://localhost/_ping
```

**Root cause hypothesis (story 1a.3 deferred-work):** Engine 29.x `_ping` response shape changed; Testcontainers' shaded `docker-java` mis-parses. CI (ubuntu-latest, older Docker) is unaffected.

**Action:** document the restart loop in `CLAUDE.md` so it's not re-discovered next session.

### C2. Upstream docs cite stale artifact coordinates / API names

**Manifested in:**
- **1a.4 Bucket4j** — official docs cite `com.bucket4j:bucket4j-lettuce`. Maven Central has NO such artifact at any version. Real coordinates: `com.bucket4j:bucket4j_jdk17-lettuce` (note the `_jdk17` JDK-tagged suffix). Cost: one mvn resolution failure + a Maven Central search to discover.
- **1a.6 OpenTelemetry exporter** — `management.otlp.tracing.endpoint=` (empty string) was expected to be a silent no-op per most published guides. Spring Boot 3.5 actually validates the URL on bean construction and crashes startup with `Invalid endpoint, must start with http:// or https://: `. Fix: drop the property entirely.

**Action:** when pinning a dependency or property from external docs, do a one-line Maven Central / Boot source verification before committing — added as ongoing rule.

### C3. JDK drift between local box and CI

**Manifested in:** 1a.3 — `mvn clean compile` failed locally because the dev JDK is 23/25 and `maven-compiler-plugin` 3.14 stopped auto-discovering Lombok annotation processors. CI runs JDK 21 (where the auto-discovery still works).

**Resolution:** bundled an `<annotationProcessorPaths>` config into 1a.3's pom (forward-compat for any JDK ≥ 21). User explicitly approved the bundled scope expansion.

**Stayed clean afterwards:** no JDK drift issues in 1a.4–1a.6 because the pom config now covers the gap.

### C4. Mockito interface default methods + concrete return type subclasses

**Manifested in:** 1a.4 — mocked `Bucket` for `ProxyManager.getProxy(...)` return value. `ProxyManager.getProxy` is a Java `default` method that returns `BucketProxy` (which extends `Bucket`). Stubbing via `when(...).thenReturn(...)` invoked the default method body, which called an un-stubbed `builder()` chain → ClassCastException.

**Fix:** mock `BucketProxy` directly (the concrete declared return type) and stub via `doReturn(...).when(...)` so Mockito doesn't dispatch into the default body.

**Generalized lesson:** when mocking a Java interface that uses `default` methods, prefer `doReturn`/`doAnswer` over `when(...).thenReturn(...)`. Also: mock the most-specific declared return type, not its parent.

### C5. Default content negotiation + Spring Security's HTML-vs-JSON classifier interact subtly

**Manifested in:** 1a.5 — initial `WebConfig.defaultContentType(APPLICATION_VND_VIS_V1_JSON)` made `@WebMvcTest` 401 expectations return 302 instead. Spring Security's `DelegatingAuthenticationEntryPoint` classifies clients by Accept header; `vnd.vis.v1+json` wasn't in its "JSON client" list, so it picked the HTML redirect entry point.

**Fix:** drop `defaultContentType`. Spring's built-in `*/*` → first-produces fallback already lands on v1 because we declared `produces=[vnd.vis.v1+json, application/json]` with v1 first.

**Lesson:** changes that look local to one layer (content negotiation) can break orthogonal layers (security entry-point selection). Always run the full test suite after MVC config tweaks.

### C6. Linear MCP token expired mid-session

**Manifested in:** 1a.5 final flip, 1a.6 mirror creation. Cost: manual cleanup of two issues.

**Mitigation:** flag in commit messages when the flip didn't happen. Re-auth `/mcp` and back-fill at session boundary.

---

## 4. Previous-retro follow-through

Not applicable — Epic E1a is the first epic; no prior retro to reference.

This retro establishes the baseline for E1b / E1c / E1d follow-through.

---

## 5. Significant discoveries that affect downstream epics

### D1. Cloud Monitoring dashboard JSON pre-commits the `prometheus.googleapis.com/*` metric type assumption

`backend/dashboards/vis-observability.json` queries metrics via `prometheus.googleapis.com/vis_sm_c<N>_total/counter` — this assumes **Google Managed Service for Prometheus** is provisioned. If the infra story picks a different metrics ingestion (e.g., OTel collector → Cloud Monitoring direct write), the dashboard JSON needs filter rewrites.

**Owner of follow-up:** whoever picks up the production-hardening infra story.

### D2. `/api/**` URL prefix discrepancy

Epic spec ACs for 1a.4 (`/api/**`) and 1a.5 cite `/api/**` as the rate-limited path. Actual controllers (`AuthController`, `BranchController`) sit at `/auth/*` and `/branches/*` — no `/api` prefix. Story 1a.4 / 1a.5 explicitly DID NOT introduce the prefix migration to avoid scope creep.

**Implication for E1d (Auth & Access):** the `/api/**` prefix migration should land alongside E1d's first endpoint additions, OR before, as a tiny mechanical pre-story. Until then, the rate-limit filter rate-limits `/auth/*` + `/branches/*` (which is fine functionally, off by name in the ACs).

### D3. Deferred-work backlog is now substantial — schedule a dedicated infra-hardening story

`deferred-work.md` accumulated 10+ items across the epic, all production-hardening:

- Redis: AUTH/TLS, SSL, password whitespace/special-char hardening, shutdown timeout, AOF persistence
- Bucket4j: split connect vs command timeout, circuit-breaker around `StringRedisTemplate`, prom production scrape network restriction
- Cloud Armor WAF provisioning
- Cloud Memorystore provisioning
- Cloud Trace / Cloud Monitoring real-network apply
- Alert policies / notification channels
- OpenTelemetry GCP-specific exporter decision

Most of these belong in a **single "Production Hardening" infra story** scheduled before public launch — NOT before E1b/E1c/E1d. Risk of leaving them all unbundled: they'll get cherry-picked into feature stories and pollute scope.

**Owner:** new story (TBD — recommend slotting after E1d's auth lands).

---

## 6. Action Items

| # | Action | Owner | When | Success criteria |
|---|---|---|---|---|
| A1 | Document Docker Engine 29.x restart loop pattern in `CLAUDE.md` "Lessons Learned" | Gaurav | Before next epic dev session | Future session does not rediscover; greps `CLAUDE.md` for "docker" finds the recipe |
| A2 | Add ongoing rule to `CLAUDE.md`: "verify Maven Central artifact coordinates before pinning version from upstream docs" | Gaurav | Before next epic dev session | Rule visible alongside the existing Linear / commit-prefix rules |
| A3 | Create dedicated **"Production hardening"** infra story consolidating `deferred-work.md` backlog | Gaurav (via `bmad-create-story` after E1d) | After E1d ships (auth real, traffic plausible) | Story file lists every entry in `deferred-work.md` as a sub-task |
| A4 | Back-fill Linear mirrors GC-139 → Done and GC-140 (create + Done) once MCP token re-authorized | Gaurav | Next session start | Both issues in Done state under E1a project |
| A5 | Resolve `/api/**` URL-prefix discrepancy before E1d's first endpoint lands (small mechanical pre-story OR fold into E1d's first story) | E1d planning | Before E1d story 1 | All `@RequestMapping` paths share `/api/v1` prefix; rate-limit AC + spec ACs aligned |
| A6 | When mocking Java interfaces with `default` methods, use `doReturn`/`doAnswer` and mock the concrete declared return type | All future dev stories | Ongoing | Code pattern; no specific milestone |

---

## 7. Next-epic preparation (E1b + E1c run in parallel)

Both E1b and E1c can start immediately — no remaining E1a dependencies.

### E1b — Mobile Infrastructure (5 stories backlog)

| Story | Dependency check |
|---|---|
| 1b.1 Expo prebuild migration (trainer + client) | Independent — no backend coupling |
| 1b.2 EAS Build CI pipelines | Depends on 1b.1 |
| 1b.3 WatermelonDB + op-sqlite | Depends on 1b.1 |
| 1b.4 MMKV auth-token storage | Independent of backend (token shape is the Firebase JWT we already validate) |
| 1b.5 Light/dark theme tokens | Independent — design tokens only |

**Preparation:** none from E1a's side. E1b owns its own infra (Expo, native modules).

### E1c — Admin-Web Infrastructure (4 stories backlog)

| Story | Dependency check |
|---|---|
| 1c.1 Angular 17 → 21 + Karma → Vitest | Independent |
| 1c.2 Signals + services state baseline | Depends on 1c.1 |
| 1c.3 WCAG 2.1 AA shell + focus management + skip-nav + min-targets | Independent |
| 1c.4 Admin-web light/dark theme tokens | Independent (shares token source with E1b's 1b.5) |

**Preparation:** none from E1a's side.

### Shared preparation across E1b + E1c

- **Theme token source-of-truth.** 1b.5 and 1c.4 both consume the same token table. The `_bmad/...` planning artifacts already define one (`prototype/src/tokens.js` + `CLAUDE.md` brand palette). Recommend: ship the JSON/JS token source once and have both mobile + admin-web import it, instead of each redefining.

### E1d (downstream — blocks on E1b + E1c + E1a)

E1d (Auth & Access) is the first feature-touching epic. It depends on all three infra epics (E1a ✓, E1b for the mobile auth UX, E1c for the admin-web onboarding screens). Until E1b and E1c land, E1d can't start.

---

## 8. Readiness assessment for E1b / E1c kickoff

| Dimension | Status |
|---|---|
| E1a stories all `done` in sprint-status | ✅ |
| All 6 stories' CI green on the merge commit | ✅ |
| All 6 stories' Linear mirrors closed | ⚠️ GC-139 In Review, GC-140 mirror not created (MCP expired) — Action A4 |
| Deferred-work registry consolidated | ✅ (`deferred-work.md` has all entries; Action A3 schedules the dedicated story) |
| Codebase stable + idiomatic | ✅ — 41 tests, no `gymculture`, no stale `in.vis.dto.*` imports, all `@Profile("!test")` infra configs aligned |
| Production-readiness of E1a code | ❌ — explicit, scoped to deferred-work. Acceptable because E1a deploys nothing to production yet; first prod deploy is gated on E1d shipping. |
| Critical blockers for E1b / E1c | None |

**Verdict:** E1b and E1c can start immediately in parallel. The Linear mirror cleanup (A4) and `CLAUDE.md` documentation updates (A1, A2) are small carry-forwards, not blockers.

---

## 9. Key takeaways (one-line each)

1. **Story scope discipline + deferred-work registry** is the load-bearing pattern. Six stories shipped with zero scope creep because every "should we add this?" decision flowed into `deferred-work.md` instead of into the story.
2. **Triple-flip status (story-file / sprint-status / Linear) + commit prefix `VIS-<mirror#>`** is now muscle memory and worked unchanged across six stories.
3. **External library docs lag reality** — Bucket4j artifact name, OTLP empty-endpoint validation, Mockito + interface default methods. Verify Maven Central + Spring Boot source before pinning.
4. **Local Docker Engine 29.x is the single biggest source of friction** — solved by force-restart loop; needs documentation so future sessions skip the diagnosis phase.
5. **`@Profile("!test")` baseline on infra-touching configs holds; `WebConfig` correctly drops the guard** — pattern is now reliable.
6. **Deferred-work has accumulated enough mass to warrant a dedicated production-hardening story** — schedule after E1d, before public traffic.

---

## 10. Commitments

- Action items A1–A6 will be reviewed at the start of the next dev session before any E1b/E1c story creation begins.
- Action A4 (Linear back-fill) executes first thing after `/mcp` re-auth.
- This retro lives at `_bmad-output/implementation-artifacts/epic-1a-retro-2026-05-31.md` and is referenced by `sprint-status.yaml` `epic-1a-retrospective: done`.

**Amelia (Developer):** "Solid epic, Gaurav. The discipline held. Let's start E1b and E1c when you're ready."
