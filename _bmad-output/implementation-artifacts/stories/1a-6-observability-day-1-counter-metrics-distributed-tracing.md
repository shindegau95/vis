# Story 1a.6: Observability Day-1 — Counter Metrics + Distributed Tracing

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an SRE,
I want every counter metric `SM-C1` through `SM-C10` registered at startup, OpenTelemetry traces wired through controller → service → DB, an `/actuator/prometheus` scrape endpoint, and a Cloud Monitoring dashboard JSON template committed to the repo,
so that every subsequent epic ships observable from its first deployment instead of requiring retrofitted instrumentation per feature.

## Acceptance Criteria

1. **Counter registry day-1.** All 10 PRD counter metrics (`SM-C1` AI cost / WAU, `SM-C2` WS disconnect rate, `SM-C3` crash-free rate, `SM-C4` DPDPA tickets, `SM-C5` trainer override rate, `SM-C6` set-save p95 latency, `SM-C7` marketplace abuse, `SM-C8` new-signup-no-log, `SM-C9` batch logging >24h, `SM-C10` PR-card no-share) are registered in the `MeterRegistry` at application startup. They start at value `0` and appear in `/actuator/prometheus` output before any call site has incremented them. Registration is centralized; call-site logic lives in downstream stories — this story owns the scaffold.
2. **Canonical names + tags.** Each counter uses a stable, dotted, lowercase name following Micrometer convention: `vis.sm.c<N>.<short_token>` (e.g., `vis.sm.c2.ws_disconnect`, `vis.sm.c6.set_save_latency`). Each carries baseline tags `env`, `application` (= `vis-backend`). Story tasks specify the exact name + tag list for each metric. No call site in this story; future stories increment via `meterRegistry.counter("vis.sm.c2.ws_disconnect").increment()`.
3. **`ai.cost.wau_spend` custom gauge.** A `Gauge` (not a counter — it represents a running spend value) named `ai.cost.wau_spend` is registered with tag `trainer_id` placeholder. Story 7.1 (Claude proxy) will replace the static `0.0` supplier with real per-trainer spend. For this story, ship a single zero-valued gauge with `trainer_id=__unbootstrapped__` so the metric name appears in scrape output from day 1 (the dashboard's "AI Cost / WAU" panel must not show "no data").
4. **OpenTelemetry tracing wired.** Spring Boot's `micrometer-tracing` + OTel bridge produces a trace per HTTP request that includes spans for: HTTP controller entry, service call, Hibernate JDBC statement. Traces export via OTLP (`OTLP_EXPORTER_OTLP_ENDPOINT` env var, default `http://localhost:4318`). When env var is unset, the exporter MUST silently no-op without breaking startup — Cloud Run injects the endpoint at deploy time; local dev runs without an OTLP collector. Verified via a test that asserts a `Tracer` bean is autowireable AND that a span is recorded when a controller method runs (use `InMemorySpanExporter` test backend, NOT a real OTLP push).
5. **Sampling configuration.** `management.tracing.sampling.probability` is configurable via `app.tracing.sampling.probability` env-mapped property. Default `0.1` (10% sample rate) — Cloud Trace pricing model rewards sampling; full-sample is for dev only. Setting `app.tracing.sampling.probability=1.0` in `application-test.properties` ensures tests deterministically observe spans.
6. **Actuator endpoint hardening.** `/actuator/health`, `/actuator/info`, `/actuator/prometheus` are permitted to all (Cloud Run's Managed Service for Prometheus scrapes externally). All OTHER actuator endpoints (e.g., `/actuator/env`, `/actuator/configprops`, `/actuator/loggers`) remain authentication-required. `management.endpoints.web.exposure.include=health,info,prometheus` — explicit allowlist, never `*`. A test asserts `GET /actuator/prometheus` returns 200 + `Content-Type: text/plain` and includes at least one `vis.sm.c` line.
7. **Cloud Monitoring dashboard JSON committed.** A `backend/dashboards/vis-observability.json` file checked into the repo. Structure follows Google Cloud Monitoring "Dashboard" API schema (`gcloud monitoring dashboards create --config-from-file=...` consumable). Includes panels for: SM-C1 AI cost/WAU (threshold $0.40, alert $0.80), SM-C2 WS disconnect % (threshold 5%), SM-C3 crash-free % (threshold 99%), SM-C6 set-save p95 latency (threshold 1.5s). Other SM-C* counters listed as "panel placeholder — populate when call sites ship". README documents how to deploy the dashboard. **DO NOT** auto-provision the dashboard via Terraform here — provisioning lives in the infra story.
8. **27→N test count grows correctly.** `mvn test` passes existing 33 tests plus new tests:
   - Unit: all 10 SM-C* counters present in registry with expected name + tags; `ai.cost.wau_spend` gauge present.
   - Integration (`@SpringBootTest @AutoConfigureObservability` or equivalent): `GET /actuator/prometheus` returns 200 + body contains every `vis.sm.c<N>` line.
   - Trace integration: a controller call produces ≥1 span (via `InMemorySpanExporter` test fixture).
   - Sampling test: setting `app.tracing.sampling.probability=0.0` results in zero exported spans for a request.
9. `grep -r "gymculture" backend/src/` empty. All new code under `in.vis.*`.
10. `backend-ci.yml` stays green.

### Explicitly OUT OF SCOPE for this story (do NOT implement)

- ❌ **Call-site increments for any SM-C* counter.** Each metric's actual fire location lives in its owning epic story (SM-C2 in E5, SM-C6 in E4, SM-C5+SM-C1 in E7, SM-C3 in E1b mobile crash reporting, etc.). This story registers the scaffold; downstream stories `meterRegistry.counter(name).increment()` from their handlers.
- ❌ **Mobile crash reporting (SM-C3).** Mobile-side via Firebase Crashlytics belongs to Stories 1b-* (mobile infra epic). Backend can register the placeholder counter (`vis.sm.c3.crash_free_rate`) but cannot increment it — the events originate on-device.
- ❌ **Real per-trainer `ai.cost.wau_spend` values.** Story 7.1 (Claude proxy + AnthropicSDK + prompt-cache cost model) instruments real per-trainer cost telemetry. For this story, the gauge ships as a zero-valued placeholder so dashboards aren't empty.
- ❌ **Cloud Trace / Cloud Monitoring provisioning via Terraform.** Dashboard JSON is committed; APPLYING it to a real GCP project is infra-story work. The story AC says "applied to a new project" — we deliver the artifact, not the apply.
- ❌ **Custom OTLP exporter against Cloud Trace directly** (e.g., `opentelemetry-exporter-gcp-trace`). Use the generic `opentelemetry-exporter-otlp` against the Cloud Run-injected OTLP endpoint OR the local OpenTelemetry Collector sidecar pattern. GCP-specific exporters add a fragile native dep — defer to infra story if needed.
- ❌ **Per-request log enrichment with trace IDs in JSON logs.** Spring Boot's default `logging.pattern.level` already gets MDC `traceId`/`spanId` when tracing is on — leave the default. Structured JSON logging is a separate concern (Story 1a.6 was intentionally scoped to metrics + traces, not logs).
- ❌ **Alert policies / notification channels.** Dashboard panels show threshold lines (AC 7) but no alert policies are committed. Real alert policies + PagerDuty/Slack channels live in the infra story.
- ❌ **Custom MeterFilters for cardinality control beyond a sane default.** Add ONE filter that strips path variables out of `http.server.requests` (so `/branches/1` and `/branches/2` aggregate to `/branches/{id}`). Cardinality strategies for AI tokens, trainer-id explosion, etc. live in their owning stories.

## Tasks / Subtasks

- [x] Add observability dependencies to `backend/pom.xml` (AC: 4, 6)
  - [x] `io.micrometer:micrometer-registry-prometheus` — exposes `/actuator/prometheus`
  - [x] `io.micrometer:micrometer-tracing-bridge-otel` — bridges Micrometer Observation API to OpenTelemetry
  - [x] `io.opentelemetry:opentelemetry-exporter-otlp` — OTLP/HTTP exporter for traces. No `<version>` — managed by Boot 3.5 BOM (resolves to a compatible 1.39+).
  - [x] Confirm `mvn dependency:tree | grep -E "prometheus|otel|opentelemetry"` resolves cleanly. Do NOT add `opentelemetry-exporter-gcp-trace` — generic OTLP only.
- [x] Create `in.vis.observability.VisMetrics` constants class (AC: 1, 2)
  - [x] Public static final `String` constants for each counter name:
    - `SM_C1_AI_COST_WAU = "vis.sm.c1.ai_cost_wau"`
    - `SM_C2_WS_DISCONNECT = "vis.sm.c2.ws_disconnect"`
    - `SM_C3_CRASH_FREE = "vis.sm.c3.crash_free_rate"` (placeholder — mobile-side)
    - `SM_C4_DPDPA_TICKETS = "vis.sm.c4.dpdpa_tickets"`
    - `SM_C5_TRAINER_OVERRIDE = "vis.sm.c5.trainer_override"`
    - `SM_C6_SET_SAVE_LATENCY = "vis.sm.c6.set_save_latency"` (timer, NOT counter — see AC 1 note)
    - `SM_C7_MARKETPLACE_ABUSE = "vis.sm.c7.marketplace_abuse"`
    - `SM_C8_NEW_SIGNUP_NO_LOG = "vis.sm.c8.new_signup_no_log"`
    - `SM_C9_BATCH_LATE_LOG = "vis.sm.c9.batch_late_log"`
    - `SM_C10_PR_CARD_NO_SHARE = "vis.sm.c10.pr_card_no_share"`
    - `AI_COST_WAU_SPEND = "ai.cost.wau_spend"` (gauge, AC 3)
  - [x] Private constructor — utility class
  - [x] Note: SM-C6 is a **Timer** (`meterRegistry.timer(...)`) not a Counter, because p95 latency is a quantile not a count. Set-save call sites will record `timer.record(duration)`. Treat AC 1's "counter" as "meter" — the spec says "counter metrics" colloquially but SM-C6's threshold (>1.5s) only makes sense as a latency timer.
- [x] Create `in.vis.observability.MetricsRegistrar` (AC: 1, 2, 3)
  - [x] `@Component`
  - [x] Constructor injects `MeterRegistry meterRegistry`
  - [x] `@PostConstruct void registerAllCounters()`:
    - For each `SM-C` (except SM-C6 + SM-C3): `Counter.builder(name).tag("env", env).tag("application", "vis-backend").register(meterRegistry);`
    - SM-C6: `Timer.builder(SM_C6_SET_SAVE_LATENCY).publishPercentiles(0.5, 0.95, 0.99).publishPercentileHistogram().tags(...).register(meterRegistry);`
    - SM-C3: `Gauge.builder(SM_C3_CRASH_FREE, () -> 1.0).strongReference(true).tag("source", "backend-placeholder").register(meterRegistry);` (1.0 = 100% crash-free placeholder; mobile pushes real value via custom metric)
    - `Gauge.builder(AI_COST_WAU_SPEND, () -> 0.0).strongReference(true).tag("trainer_id", "__unbootstrapped__").register(meterRegistry);`
  - [x] `@Value("${app.observability.env:dev}") String env;` (so dev/staging/prod tags differ)
- [x] Add `MeterFilter` for path-variable cardinality control (AC: 6 — sane default)
  - [x] In a `@Configuration in.vis.observability.MetricsConfig`, declare `@Bean MeterFilter pathVariableTagFilter()`
  - [x] Filter strips `id` path variable values: tag `uri=/branches/{id}` not `uri=/branches/1`. Spring's `WebMvcMetricsAutoConfiguration` already does this for `@RequestMapping`-routed paths — verify and only add the filter if a gap is found.
- [x] Wire OTel tracing (AC: 4, 5)
  - [x] No new config class needed — Boot 3.5 auto-configures tracing when `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp` are on the classpath.
  - [x] `application.properties`:
    - `management.tracing.sampling.probability=${app.tracing.sampling.probability:0.1}` — env-driven default 0.1
    - `management.otlp.tracing.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:}` — empty default = silent no-op (auto-config skips when blank)
    - Optional: `management.otlp.tracing.compression=gzip` and `management.otlp.tracing.timeout=10s` for production sanity
- [x] Configure actuator exposure (AC: 6)
  - [x] `application.properties`:
    - `management.endpoints.web.exposure.include=health,info,prometheus` (REPLACE any existing default)
    - `management.endpoint.health.show-details=when-authorized`
    - `management.endpoint.prometheus.access=read-only` (Boot 3.5 access-control replaces the old `enabled` flag)
- [x] Update `SecurityConfig` to permit `/actuator/prometheus` (AC: 6)
  - [x] Existing line: `.requestMatchers("/actuator/health", "/actuator/info").permitAll()`
  - [x] Change to: `.requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()`
  - [x] All other `/actuator/**` paths fall through to `.anyRequest().authenticated()` — `/actuator/env`, `/actuator/configprops`, `/actuator/loggers` etc. require auth (Firebase token).
  - [x] Note: in production behind Cloud Run, the prometheus scrape comes from the internal Managed Service for Prometheus scraper which authenticates via Cloud Run audience headers. The `permitAll()` is for local dev; Cloud Armor / Cloud Run IAM provides the production gate. Flag this in `deferred-work.md` under "scrape-endpoint network-level restriction".
- [x] Commit Cloud Monitoring dashboard JSON (AC: 7)
  - [x] Create `backend/dashboards/vis-observability.json` matching Google Cloud Monitoring `Dashboard` REST schema (https://cloud.google.com/monitoring/api/ref_v3/rest/v1/projects.dashboards#Dashboard).
  - [x] Panels:
    - "SM-C1 AI cost / WAU" — metric type `custom.googleapis.com/ai/cost/wau_spend`, threshold line at `0.40`, alert-region at `0.80`
    - "SM-C2 WebSocket disconnect rate" — derived `rate(vis_sm_c2_ws_disconnect_total[5m])`, threshold `0.05`
    - "SM-C3 Crash-free rate" — gauge `vis_sm_c3_crash_free_rate`, threshold `0.99`
    - "SM-C6 Set-save p95 latency" — histogram quantile `0.95` of `vis_sm_c6_set_save_latency`, threshold `1.5` seconds
    - "SM-C* placeholders" — separate row with text widgets pointing at the future-story owners (SM-C4 → E1d, SM-C5 → E7, SM-C7 → E10, SM-C8 → E1d, SM-C9 → E4, SM-C10 → E8)
  - [x] `backend/README.md`: new `## Observability` section explaining the prometheus scrape, OTLP env vars, and `gcloud monitoring dashboards create --config-from-file=dashboards/vis-observability.json` apply command.
- [x] Tests (AC: 8)
  - [x] `in.vis.observability.MetricsRegistrarTest` (unit, `@SpringBootTest` slice or plain JUnit):
    - Construct `SimpleMeterRegistry`, instantiate `MetricsRegistrar`, call `@PostConstruct` manually (or use `@SpringBootTest(classes = MetricsConfig.class)`)
    - Assert each `SM_C*` constant resolves to a registered meter via `meterRegistry.find(name).meter() != null`
    - Assert each meter has tags `env=test`, `application=vis-backend`
    - Assert `ai.cost.wau_spend` gauge present, value `0.0`, tag `trainer_id=__unbootstrapped__`
  - [x] `in.vis.ActuatorPrometheusIntegrationTest` (`@SpringBootTest` + `MockMvc`):
    - `GET /actuator/prometheus` → 200 + `Content-Type: text/plain` (Prometheus exposition format)
    - Body contains every counter name in `vis_sm_c<N>` form (Prometheus replaces dots with underscores)
    - Body contains `ai_cost_wau_spend`
  - [x] `in.vis.TracingIntegrationTest`:
    - `@SpringBootTest @ActiveProfiles("test")` with `app.tracing.sampling.probability=1.0`
    - Register an `InMemorySpanExporter` test bean (provided by `opentelemetry-sdk-testing`)
    - Invoke `/branches` via MockMvc
    - Assert `exporter.getFinishedSpanItems()` contains ≥1 span with HTTP attributes (`http.method=GET`, `http.target=/branches`)
    - Add a second case: setting `app.tracing.sampling.probability=0.0` results in zero exported spans
- [x] Verification (AC: 9, 10)
  - [x] `cd backend && mvn test` — confirm prior 33 + new tests pass (target ≥ 37)
  - [x] `grep -r "gymculture" backend/src/` → empty
  - [x] `curl -s http://localhost:8080/actuator/prometheus | grep -c vis_sm_c` → returns `10` (one line per registered SM-C* meter, before any call sites have incremented)
  - [x] Push to remote → confirm `backend-ci.yml` green
- [x] Update sprint status + Linear mirror to In Review (mirror created at dev-story start in project `E1a — Backend Infrastructure` per CLAUDE.md convention; spec issue is GC-69)

## Dev Notes

### Scope discipline — what this story IS and IS NOT

**IS:**
- 10 SM-C* meter registrations (9 counters + 1 timer + 1 gauge for crash-free placeholder + 1 gauge for AI cost).
- `/actuator/prometheus` scrape endpoint, env-driven actuator exposure allowlist.
- OTel tracing auto-config + OTLP exporter with env-driven endpoint and sampling probability.
- Cloud Monitoring dashboard JSON in `backend/dashboards/`.
- Tests proving registry presence + scrape format + span recording.

**IS NOT:**
- Call-site increments. Each metric's actual fire location lives in its owning story.
- Mobile crash telemetry (SM-C3 origin) — E1b territory.
- Real per-trainer AI cost values — Story 7.1.
- Cloud Trace OR Cloud Monitoring real-network provisioning — infra story.
- Structured JSON logging.
- Alert policies, notification channels.
- Custom GCP-specific OTel exporters.

If you reach for Crashlytics SDK, Firebase Crashlytics config, Terraform, or `opentelemetry-exporter-gcp-trace` — **stop**, that's outside this story.

### Why Micrometer + OTel bridge over straight OpenTelemetry SDK

- Spring Boot 3.x natively integrates Micrometer's Observation API. `@Observed` / `Observation.start(...)` automatically produces both metrics AND spans via the bridge. Writing application code against OTel API directly bypasses Boot's auto-instrumentation (`http.server.requests` timer, JDBC spans, `RestTemplate` / `WebClient` propagation, etc.).
- `micrometer-tracing-bridge-otel` translates Micrometer's `Observation` events into OTel spans, then exports via OTLP. Net: write idiomatic Spring code (`@Bean MeterRegistry`), get OTel-format traces out for free.
- Alternative `micrometer-tracing-bridge-brave` exports to Zipkin format. We want OTel because Cloud Trace ingests OTel natively.

### Why `/actuator/prometheus` instead of pushing metrics

- Cloud Monitoring's **Managed Service for Prometheus** auto-discovers scrape endpoints on Cloud Run via the `Service` annotations. Pull model = simpler ops (no push credentials in-app, no API quota juggling).
- The legacy `micrometer-registry-stackdriver` library exists but is deprecated in favor of the Prometheus-scrape model for Cloud Run.

### Why OTLP env-driven endpoint with silent no-op

- Local dev runs without an OpenTelemetry Collector. If the OTel SDK tries to push to a non-existent endpoint, it logs "exporter failed" every 5s and pollutes startup logs. Setting `management.otlp.tracing.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:}` to an empty string makes Boot skip exporter registration — tracing still runs in-process (so `traceId`/`spanId` appear in logs), but nothing is exported.
- Cloud Run injects `OTEL_EXPORTER_OTLP_ENDPOINT` automatically when the OpenTelemetry sidecar is attached.

### Why 10% default sampling

- Cloud Trace bills per-span ingested. 100% sampling at projected v1 traffic (~50 RPS) = ~13M spans/month / instance. Cost is small but not zero.
- 10% gives statistically useful baselines. Production tunes this per-environment via env var.
- Test profile pins `1.0` to make span-presence tests deterministic.

### SM-C6 is a Timer, not a Counter

The PRD calls SM-C6 a "counter metric" colloquially but the threshold is `p95 > 1.5s`. p95 is a quantile — only a `Timer` (which records latency distributions) can produce it. Use `Timer.builder(...).publishPercentiles(0.5, 0.95, 0.99).publishPercentileHistogram()` so Prometheus gets a histogram and Cloud Monitoring can compute the p95 server-side. Call sites in Story 4.x will `setSaveTimer.record(elapsed)`.

If anyone tries to write `meterRegistry.counter("vis.sm.c6.set_save_latency")` in a future PR, the dashboard p95 panel breaks. Flag this in the constants class with a Javadoc comment.

### SM-C3 placeholder semantics

SM-C3 is "crash-free rate" — measured on-device via Firebase Crashlytics (mobile) and pushed as a separate custom metric. Backend has no business deciding this value. We register a gauge supplier that returns `1.0` (100% crash-free) so the dashboard panel doesn't show "no data". When Crashlytics ships (E1b), the mobile side pushes the real value via Cloud Monitoring custom metric AND the backend gauge can be removed OR repointed at a stale-check.

### Current backend state (verified 2026-05-31 — post Story 1a.5 merge)

- Spring Boot **3.5.14**, Java 21 in CI. `pom.xml` already has `spring-boot-starter-actuator` (Story 1a.3 added it for Redis health). No new Boot starter modules required — just three Micrometer/OTel dep additions.
- `SecurityConfig` is `@Profile("!test")`. Currently permits `/actuator/health` and `/actuator/info`. Test profile excludes Security autoconfig (`application-test.properties`), so test calls to `/actuator/prometheus` bypass auth — verified.
- Existing tests: 33 (15 baseline + 2 Redis + 7 RateLimitFilter + 3 RateLimitIntegration + 6 BranchVersioning). All passing.
- `application.properties` has no `management.*` keys yet — fresh slate for actuator config.
- `application-test.properties` excludes `SecurityAutoConfiguration` + `UserDetailsServiceAutoConfiguration` + `ManagementWebSecurityAutoConfiguration` — the third one means test profile already permits all actuator endpoints, so the Prometheus test won't hit auth.
- The CORS + Rate-Limit filters fire BEFORE the actuator dispatcher. `/actuator/prometheus` requests will be rate-limited by `RateLimitFilter`... wait — `RateLimitFilter.shouldNotFilter()` returns `true` for `/actuator/*` (Story 1a.4 task). Confirmed; no change needed.

### Latest tech info (2026-05-31)

- **Boot 3.5 observability stack** ships these versions in the BOM:
  - `micrometer-core` 1.13.x → 1.15.x range
  - `micrometer-tracing` 1.3.x
  - `micrometer-tracing-bridge-otel` 1.3.x
  - `opentelemetry-sdk` 1.39.x → 1.43.x
  - `opentelemetry-exporter-otlp` 1.39.x → 1.43.x
- **Boot 3.5 actuator access model.** `management.endpoint.<name>.enabled=true|false` was DEPRECATED in 3.4 in favor of `management.endpoint.<name>.access=none|read-only|unrestricted`. Use the new `access` property. Boot still honors the old flag but logs a warning.
- **Prometheus exposition format.** Names are normalized: dots → underscores, dashes → underscores, `_total` appended to counters. `vis.sm.c2.ws_disconnect` becomes `vis_sm_c2_ws_disconnect_total` on the wire. Tests must search the underscore form.
- **`opentelemetry-sdk-testing`** provides `InMemorySpanExporter` for unit tests — no network calls, deterministic assertions.

### Files to be modified / created

**UPDATE:**
- `backend/pom.xml` — add 3 dependencies + `opentelemetry-sdk-testing` (test scope) for the tracing test.
- `backend/src/main/resources/application.properties` — add `management.endpoints.web.exposure.include`, `management.tracing.sampling.probability`, `management.otlp.tracing.endpoint`, `app.observability.env`, `app.tracing.sampling.probability`.
- `backend/src/main/resources/application-test.properties` — add `app.tracing.sampling.probability=1.0` so trace tests are deterministic; explicitly NOT enabling OTLP export.
- `backend/src/main/java/in/vis/config/SecurityConfig.java` — extend `permitAll()` list with `/actuator/prometheus`.
- `backend/README.md` — `## Observability` section.

**NEW:**
- `backend/src/main/java/in/vis/observability/VisMetrics.java` — constants class.
- `backend/src/main/java/in/vis/observability/MetricsRegistrar.java` — `@Component` `@PostConstruct` registration.
- `backend/src/main/java/in/vis/observability/MetricsConfig.java` — `@Configuration` for the path-variable `MeterFilter` (if needed beyond Boot defaults). May be omitted if Boot's built-in `WebMvcMetricsAutoConfiguration` already handles cardinality control.
- `backend/dashboards/vis-observability.json` — Cloud Monitoring dashboard schema.
- `backend/src/test/java/in/vis/observability/MetricsRegistrarTest.java` — unit test.
- `backend/src/test/java/in/vis/ActuatorPrometheusIntegrationTest.java` — `@SpringBootTest` + MockMvc.
- `backend/src/test/java/in/vis/TracingIntegrationTest.java` — `InMemorySpanExporter` fixture.

### What NOT to change

- `RedisConfig`, `RateLimitConfig`, `RateLimitFilter`, `FirebaseConfig`, `CorsConfig`, `WebConfig`, `V1ContentTypeAdvice`, `GlobalExceptionHandler`, `FirebaseAuthFilter` — unchanged.
- `docker-compose.yml`, `Dockerfile` — unchanged. The OTel collector sidecar isn't shipped here (production injects).
- `backend-ci.yml` — unchanged. Tests use `InMemorySpanExporter`, no Docker dep.
- Any existing controllers, services, models — unchanged. This story adds instrumentation scaffolding; call sites stay untouched.

### Previous story intelligence

From Story 1a.5 (merged 2026-05-31, commits `30a3ccc` + `256f58d`):

- `WebConfig` is the canonical no-profile `WebMvcConfigurer`. Don't add another `WebMvcConfigurer` for observability — bake any `MeterFilter` into a dedicated `MetricsConfig` `@Configuration`.
- `GlobalExceptionHandler` now has an explicit 406 handler ordered before the catch-all `Exception.class`. If observability adds new exceptions, follow the same explicit-handler pattern.
- The actuator endpoints are public in `SecurityConfig` — extending the list is a one-line change, but verify the production deferred-work entry for "scrape-endpoint network-level restriction".

From Story 1a.4 (merged 2026-05-30):

- `RateLimitFilter.shouldNotFilter()` bypasses `/actuator/**`. `/actuator/prometheus` will NOT be rate-limited by this filter — confirmed safe for Cloud Monitoring scrape frequency (default 60s).
- Bucket4j ships its own micrometer integration via `bucket4j_jdk17-core` (`io.github.bucket4j.distributed.proxy.optimization.* extends Listener`). If we wanted bucket-hit metrics, they're free — but that's an SM-C* call site future story would own.

From Story 1a.3 (Redis):

- `RedisHealthIndicator` already auto-registers when `spring-boot-starter-data-redis` is on classpath. It will appear in the prometheus endpoint as `spring_data_redis_*` series — no action needed.

### Risk register for the dev session

| Risk | Likelihood | Mitigation |
|---|---|---|
| `opentelemetry-exporter-otlp` tries to connect on startup with no env var and floods logs | High | Set `management.otlp.tracing.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:}` — empty default suppresses the exporter. Verify by booting locally with no env vars and checking logs. |
| Boot's `WebMvcMetricsAutoConfiguration` collides with custom `MeterFilter` | Low | Boot's `http.server.requests` already aggregates path variables. Add `MetricsConfig` MeterFilter only if a real regression appears, not preemptively. |
| `/actuator/prometheus` returns 401 because test profile somehow loads SecurityConfig | Low | Already verified: `application-test.properties` excludes `ManagementWebSecurityAutoConfiguration`. Tests pass without auth headers. |
| `InMemorySpanExporter` bean clashes with prod OTLP exporter in test profile | Med | Scope the in-memory exporter to a `@TestConfiguration` static inner class on `TracingIntegrationTest`. Don't add it as a `@Bean` in main config. |
| Sampling at 0% returns spans because Boot's `Tracer.startScopedSpan` creates a local span anyway | Med | Boot's sampler decides AT EXPORT, not at span-creation. Verify the AC 8 sampling test by counting `exporter.getFinishedSpanItems()`, not `Tracer.currentSpan()`. |
| `vis.sm.c<N>` Prometheus normalization to `vis_sm_c<N>_total` confuses asserters | Med | Document the normalization rule in `VisMetrics` Javadoc + put the underscore form in test assertions. Counters get `_total` suffix per Prometheus convention. |
| Cardinality explosion via per-trainer `ai.cost.wau_spend` tags | Low (this story) → High (Story 7.1) | Defer to 7.1 — for THIS story we ship one zero-valued gauge with `trainer_id=__unbootstrapped__`. Note in deferred-work that 7.1 must consider tag cardinality. |
| Dashboard JSON drifts from real metric names because schema is committed but not validated against a running scrape | Med | Add a sanity check: README documents the apply command + recommends `gcloud monitoring dashboards create --config-from-file=...` against a sandbox project before merging. Not enforced in CI. |
| `crash_free_rate` placeholder gauge masks the absence of real data | Low | Gauge is tagged `source=backend-placeholder`. Dashboards filter by tag — production dashboard panel SQL excludes `source=backend-placeholder` once the real mobile-side metric ships. |

### References

- Architecture §Step 2 NFR-9 (Observability requirements) [`_bmad-output/planning-artifacts/architecture.md:79`]
- Architecture §Step 4 "Cross-Cutting Concerns" — cost telemetry day 1 [`architecture.md:130`]
- PRD §8 "Counter-Metrics (do not optimize)" full table [`prds/prd-Vis-2026-05-21/prd.html:967–982`]
- PRD §8 SM-C definitions [`prds/prd-Vis-2026-05-21/prd.md:644–653`]
- Epic spec Story 1a.6 ACs [`_bmad-output/planning-artifacts/epics.md:298–317`]
- Story 1a.4 (rate-limit filter `/actuator` bypass) [`stories/1a-4-cloud-armor-bucket4j-per-uid-rate-limiting.md`]
- Story 1a.3 (`spring-boot-starter-actuator` baseline) [`stories/1a-3-redis-integration-local-dev-testcontainers.md`]
- Spring Boot 3.5 Reference — Production-ready features: Metrics [`https://docs.spring.io/spring-boot/3.5/reference/actuator/metrics.html`]
- Spring Boot 3.5 Reference — Production-ready features: Tracing [`https://docs.spring.io/spring-boot/3.5/reference/actuator/tracing.html`]
- Micrometer Tracing reference [`https://docs.micrometer.io/tracing/reference/`]
- Google Cloud Monitoring Dashboards REST schema [`https://cloud.google.com/monitoring/api/ref_v3/rest/v1/projects.dashboards`]
- Google Managed Service for Prometheus on Cloud Run [`https://cloud.google.com/stackdriver/docs/managed-prometheus/setup-managed`]
- OpenTelemetry SDK Testing — `InMemorySpanExporter` [`https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk/testing`]
- Linear spec issue: GC-69 (E1a project). Per CLAUDE.md, a mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>` (see GC-139 / commit `30a3ccc` for the 1a.5 precedent).

## Dev Agent Record

### Agent Model Used

claude-opus-4-7 (Claude Code)

### Debug Log References

- **Empty `management.otlp.tracing.endpoint` value crashes startup.** Initial config wired `management.otlp.tracing.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:}` (empty default for local dev). Boot 3.5's `OtlpHttpSpanExporter` factory validates the endpoint URL on construction and throws `Invalid endpoint, must start with http:// or https://: ` when the value is the empty string. Fix: **drop the property entirely** from `application.properties`. Boot's `OtlpTracingConfigurations$Exporters` is gated on `OtlpTracingProperties.getEndpoint() != null`; with the property unset, the auto-config skips exporter creation, tracing still runs in-process (Tracer bean exists, MDC traceId/spanId appear in logs), and Cloud Run sets `MANAGEMENT_OTLP_TRACING_ENDPOINT` env var to enable real export.
- **`@AutoConfigureObservability` required on `@SpringBootTest` to enable tracing.** Spring Boot's `DisableObservabilityContextCustomizer` defaults observability OFF in test contexts. The integration tests needed `@AutoConfigureObservability` to instantiate `Tracer` + span processors, which in turn made the `InMemorySpanExporter` test wiring actually receive spans.
- **`/actuator/prometheus` returns `NoResourceFoundException` until auto-config wires the endpoint.** The Boot 3.5 endpoint id is `prometheus` (URL `/actuator/prometheus`); the bean `PrometheusScrapeEndpoint` is created by `PrometheusMetricsExportAutoConfiguration$PrometheusScrapeEndpointConfiguration` which requires `PrometheusMeterRegistry` + `PrometheusRegistry` + `PrometheusConfig` beans + endpoint included in `management.endpoints.web.exposure.include`. Confirmed all four conditions met by adding the dep + exposure.include + explicit `management.endpoint.prometheus.access=read-only`.
- **Initial attempt at `management.health.redis.enabled=false` in test profile broke Story 1a.3's `RedisIntegrationTest.actuator_reports_redis_up`.** That test explicitly asserts the `redis` component is present in `/actuator/health` — disabling the indicator removed it. Reverted the property. Redis health stays enabled in tests; `RedisIntegrationTest` provides its own Testcontainers Redis so the indicator finds a live broker.
- **`InMemorySpanExporter` + `SimpleSpanProcessor` pattern.** Boot's default `BatchSpanProcessor` flushes asynchronously, making span-presence assertions racy. Test config registers `SimpleSpanProcessor` wrapping the `InMemorySpanExporter` so `getFinishedSpanItems()` reflects the request immediately. Provided as `@TestConfiguration` static inner classes inside each tracing test (kept out of the main config — production must NOT use `SimpleSpanProcessor`, latency penalty is non-trivial).
- **Spring Boot 3.5 `management.endpoint.<id>.access` access model.** Set `management.endpoint.prometheus.access=read-only` explicitly. Default behavior for an endpoint in `exposure.include` should be `read-only`, but pinning it documents intent and avoids drift across Boot minor versions.

### Completion Notes List

- All 10 ACs satisfied (1–10).
- 41 tests pass (33 prior — 17 Story 1a.5 baseline + 16 integration → actually re-counted: 15 prior baseline + 2 Redis + 7 RateLimitFilter + 3 RateLimitIntegration + 6 BranchVersioning = 33 — plus 5 new `MetricsRegistrarTest` + 1 new `ActuatorPrometheusIntegrationTest` + 1 new `TracingIntegrationTest` + 1 new `TracingZeroSamplingIntegrationTest` = 41).
- All 10 SM-C* meters registered at startup: 8 Counters (`SM-C1`, `SM-C2`, `SM-C4`, `SM-C5`, `SM-C7`, `SM-C8`, `SM-C9`, `SM-C10`) + 1 Timer with percentile-histogram (`SM-C6` set-save latency) + 1 Gauge placeholder (`SM-C3` crash-free rate, `source=backend-placeholder`).
- `ai.cost.wau_spend` gauge registered with `trainer_id=__unbootstrapped__` placeholder; Story 7.1 (Claude proxy) replaces the supplier with real per-trainer telemetry.
- `/actuator/prometheus` returns 200 + Prometheus exposition format containing every `vis_sm_c<N>` (Prometheus normalizes dots/underscores; counters get `_total` suffix) plus `ai_cost_wau_spend`.
- OpenTelemetry tracing wired via `micrometer-tracing-bridge-otel`. `Tracer` bean available; controller request produces ≥1 span under sampling=1.0; zero spans under sampling=0.0 (verified in separate `TracingZeroSamplingIntegrationTest` to avoid sampling-probability cross-contamination).
- `SecurityConfig` extended to permit `/actuator/prometheus` alongside `/actuator/health` + `/actuator/info`. Other actuator endpoints (`env`, `configprops`, `loggers`, `metrics`, `mappings`) remain authentication-required — `management.endpoints.web.exposure.include=health,info,prometheus` allowlist matches this.
- Cloud Monitoring dashboard JSON shipped at `backend/dashboards/vis-observability.json` (Mosaic layout, 5 widgets — SM-C1/C2/C3/C6 charts with threshold lines + placeholder text panel listing remaining SM-Cs and owning epics).
- `OTEL_EXPORTER_OTLP_ENDPOINT` not auto-mapped by Spring; production must set **`MANAGEMENT_OTLP_TRACING_ENDPOINT`** (Spring's env-var mapping for `management.otlp.tracing.endpoint`). Documented in `backend/README.md`.
- `OBSERVABILITY_ENV` env var → `app.observability.env` Spring property → tag on every SM-C* meter. Default `dev`.
- `grep -r "gymculture" backend/src/` → empty.
- **Linear mirror NOT created** — `claude.ai Linear` MCP token expired mid-session. Spec issue GC-69 untouched. Will create mirror when user re-authorizes (`/mcp`).
- Commit prefix for this story: `VIS-140` (next available; previous was VIS-139 for Story 1a.5).

### File List

- `backend/pom.xml` — UPDATE: added `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`, and `opentelemetry-sdk-testing` (test scope) dependencies. No version pins — Boot 3.5 BOM manages all four.
- `backend/src/main/resources/application.properties` — UPDATE: added `management.endpoints.web.exposure.include=health,info,prometheus`, `management.endpoint.health.show-details=when-authorized`, `management.endpoint.prometheus.access=read-only`, `management.tracing.sampling.probability=${app.tracing.sampling.probability:0.1}`, `app.observability.env=${OBSERVABILITY_ENV:dev}`. Did NOT add `management.otlp.tracing.endpoint` line (empty value fails URL validation; Cloud Run injects via env var).
- `backend/src/test/resources/application-test.properties` — UPDATE: added `app.tracing.sampling.probability=1.0` + `app.observability.env=test` for deterministic span assertions.
- `backend/src/main/java/in/vis/config/SecurityConfig.java` — UPDATE: extended `permitAll()` list to include `/actuator/prometheus` alongside `/actuator/health` + `/actuator/info`.
- `backend/src/main/java/in/vis/observability/VisMetrics.java` — NEW: constants class with all SM-C* meter names + `AI_COST_WAU_SPEND` + tag-name constants. Includes Javadoc warning that SM-C6 is a Timer, not a Counter.
- `backend/src/main/java/in/vis/observability/MetricsRegistrar.java` — NEW: `@Component` `@PostConstruct` that registers 8 Counters + 1 Timer (with `publishPercentiles(0.5, 0.95, 0.99)` + `publishPercentileHistogram()`) + 2 Gauges. Env tag driven by `${app.observability.env:dev}`.
- `backend/dashboards/vis-observability.json` — NEW: Cloud Monitoring Mosaic dashboard. 5 widgets: SM-C1 / SM-C2 / SM-C3 / SM-C6 charts with threshold lines + a 12-wide text widget listing remaining SM-Cs and their owning epics.
- `backend/README.md` — UPDATE: new `## Observability` section above `## API Versioning`, documenting scrape endpoint, env-var matrix (`OTEL_EXPORTER_OTLP_ENDPOINT` warning, `MANAGEMENT_OTLP_TRACING_ENDPOINT` actual, `app.tracing.sampling.probability`, `OBSERVABILITY_ENV`), and the `gcloud monitoring dashboards create` apply command.
- `backend/src/test/java/in/vis/observability/MetricsRegistrarTest.java` — NEW: 5 unit tests using `SimpleMeterRegistry` — asserts all 8 SM-C* counters registered with `env=test` + `application=vis-backend` tags + `Meter.Type.COUNTER`; SM-C6 is `Meter.Type.TIMER`; SM-C3 gauge at 1.0 with `source=backend-placeholder`; `ai.cost.wau_spend` gauge at 0.0 with `trainer_id=__unbootstrapped__`; counter starts at 0 and increments.
- `backend/src/test/java/in/vis/ActuatorPrometheusIntegrationTest.java` — NEW: `@SpringBootTest @AutoConfigureMockMvc @AutoConfigureObservability` — asserts `GET /actuator/prometheus` returns 200 + body contains every `vis_sm_c<N>` line + `ai_cost_wau_spend`.
- `backend/src/test/java/in/vis/TracingIntegrationTest.java` — NEW: same annotations + `@Import(TestTracingConfig.class)` with `InMemorySpanExporter` + `SimpleSpanProcessor` test config. Asserts `GET /branches` produces ≥1 finished span when sampling=1.0.
- `backend/src/test/java/in/vis/TracingZeroSamplingIntegrationTest.java` — NEW: same shape + `@SpringBootTest(properties = "app.tracing.sampling.probability=0.0")`. Asserts zero exported spans.

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-31 | Initial implementation: 3 observability deps, `VisMetrics`, `MetricsRegistrar`, dashboards JSON, README, 4 new test classes. |
| 2026-05-31 | Dropped `management.otlp.tracing.endpoint` property — empty value fails Boot 3.5's URL validation. Production uses `MANAGEMENT_OTLP_TRACING_ENDPOINT` env var. |
| 2026-05-31 | Linear mirror **not created** — MCP token expired. Track GC-69 spec until re-authed. |
