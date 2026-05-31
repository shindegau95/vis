# Story 1a.5: Versioned DTO Content-Negotiation

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want every backend endpoint to negotiate the `application/vnd.vis.vN+json` media type with N + N-1 coexistence,
so that mobile and admin-web clients can roll forward without coordinated lockstep releases and we can deprecate old DTO shapes cleanly.

## Acceptance Criteria

1. A client sending `Accept: application/vnd.vis.v1+json` against any controller endpoint (`/auth/me`, `/auth/register`, `/branches`, `/branches/{id}`) receives `Content-Type: application/vnd.vis.v1+json` and the v1 body shape. No 406s on a happy path.
2. A client sending `Accept: application/json` or `Accept: */*` (the absence of a `Vis-versioned` header should not break legacy callers, including curl with no headers, the Pact stub, and `actuator/info`) defaults to v1. `Content-Type` returned is `application/vnd.vis.v1+json` (the negotiated server-side default is v1; we never silently emit the bare `application/json` content type for `/auth` or `/branches` traffic).
3. **v2 coexistence is proven by code, not just claimed.** Ship a real `dto/v2` package plus a single v2 controller endpoint (the simplest: `GET /branches` with a v2 `BranchResponse` that adds one field — e.g., `slug` — so the shape difference is observable). `Accept: application/vnd.vis.v2+json` MUST route to the v2 controller and return `Content-Type: application/vnd.vis.v2+json` with the v2 body. v1 callers must continue to receive v1 unchanged.
4. An unsupported version (e.g. `Accept: application/vnd.vis.v3+json`) returns HTTP 406 Not Acceptable. We do NOT silently downgrade to v1 — that would mask client bugs.
5. DTO records that were under `in.vis.dto.*` are relocated to `in.vis.dto.v1.*`. New v2 DTOs live under `in.vis.dto.v2.*`. All callers updated. No imports remain pointing at `in.vis.dto.Something` (the bare package).
6. Media-type constants are centralized: `in.vis.MediaTypes` (or `in.vis.config.MediaTypes`) declares `APPLICATION_VND_VIS_V1_JSON_VALUE`, `APPLICATION_VND_VIS_V2_JSON_VALUE`, and the matching `MediaType` instances. No raw strings in `produces`/`consumes` attributes elsewhere.
7. Existing 17 backend tests + new tests pass. New tests cover:
   - Accept v1 → 200 + Content-Type v1 + v1 body shape
   - Accept */* (no header) → 200 + Content-Type v1 (default)
   - Accept application/json → 200 + Content-Type v1
   - Accept v2 → 200 + Content-Type v2 + v2 body shape (has the v2-only field)
   - Accept v3 → 406 Not Acceptable
   - v1 caller observing the v2-introduced field is NOT present in the v1 response (proves the routing is real, not just a header rename)
8. `grep -r "gymculture" backend/src/` empty. All new code under `in.vis.*`.
9. `backend-ci.yml` stays green.

### Explicitly OUT OF SCOPE for this story (do NOT implement)

- ❌ **Pact contract tests with real consumer fixtures.** Story 1a.1 shipped a `pact-verify.yml` stub. This story does NOT add real Pact contracts — that's a downstream story owned by E1d/E2 (when consumer apps exist). We may emit a comment in `pact-verify.yml` or a `README.md` note pointing future stories at the v1/v2 split.
- ❌ **Deprecation `Sunset` / `Deprecation` HTTP headers** for the older version. RFC 8594 / 9745 headers belong in the story that actually deprecates v1 (after v2 ships in production). Out of scope here.
- ❌ **Schema migration of any existing DTO field.** v1 stays the EXACT current shape (`BranchResponse(id, name, city)`, `UserResponse(...)`, `RegisterRequest(...)`). v2 is purely additive on `BranchResponse` (one extra field) — no field rename, no field removal, no semantic change.
- ❌ **Custom `Accept` parsing for arbitrary version numbers.** Spring's `ProducesRequestCondition` already routes by `Accept` once `produces` is declared per controller method. We do NOT roll our own version resolver.
- ❌ **OpenAPI/Swagger emission of versioned media types.** Springdoc-openapi work belongs in Story 1a.6 (observability + docs) or a separate docs story.
- ❌ **Versioning of the WebSocket protocol** (E5 territory) — REST DTOs only here.
- ❌ **Versioning of `/actuator/**` endpoints** — actuator stays JSON; it is infra/ops, not domain API.

## Tasks / Subtasks

- [x] Create `in.vis.MediaTypes` constants class (AC: 6)
  - [x] `public static final String APPLICATION_VND_VIS_V1_JSON_VALUE = "application/vnd.vis.v1+json";`
  - [x] `public static final String APPLICATION_VND_VIS_V2_JSON_VALUE = "application/vnd.vis.v2+json";`
  - [x] `public static final MediaType APPLICATION_VND_VIS_V1_JSON = MediaType.valueOf(APPLICATION_VND_VIS_V1_JSON_VALUE);`
  - [x] `public static final MediaType APPLICATION_VND_VIS_V2_JSON = MediaType.valueOf(APPLICATION_VND_VIS_V2_JSON_VALUE);`
  - [x] `private MediaTypes() {}` — utility class, no instantiation
  - [x] Location: `in.vis.MediaTypes` (root package) — this is a cross-cutting API constant, not a config bean. Mirror `MediaType` from `org.springframework.http.MediaType`.
- [x] Relocate v1 DTOs to `in.vis.dto.v1.*` (AC: 5)
  - [x] Move `backend/src/main/java/in/vis/dto/BranchResponse.java` → `.../dto/v1/BranchResponse.java` (change `package in.vis.dto;` → `package in.vis.dto.v1;`)
  - [x] Move `backend/src/main/java/in/vis/dto/UserResponse.java` → `.../dto/v1/UserResponse.java`
  - [x] Move `backend/src/main/java/in/vis/dto/RegisterRequest.java` → `.../dto/v1/RegisterRequest.java`
  - [x] Update every importer:
    - `AuthController` (`/auth`) — imports `RegisterRequest`, `UserResponse`
    - `BranchController` (`/branches`) — imports `BranchResponse`
    - `UserService` — returns `UserResponse`, accepts `RegisterRequest`
    - `AuthControllerIntegrationTest`, `BranchControllerIntegrationTest`, `UserServiceTest` — all imports updated to `in.vis.dto.v1.*`
  - [x] Verify with `grep -rn "import in.vis.dto\." backend/src/ | grep -v "in.vis.dto.v"` → must be empty.
- [x] Wire v1 `produces` on every existing controller endpoint (AC: 1, 2)
  - [x] On `AuthController` and `BranchController`, declare `produces = { MediaTypes.APPLICATION_VND_VIS_V1_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE }` at the class level (`@RequestMapping`) OR per-method (preference: class-level — less repetition).
  - [x] Declare `consumes` on the POST endpoint (`/auth/register`): same two-value list. Body type is `RegisterRequest` (now `in.vis.dto.v1.RegisterRequest`).
  - [x] When a client sends NO `Accept` header (curl default) OR `*/*`, Spring will pick the FIRST declared `produces` entry — keep `APPLICATION_VND_VIS_V1_JSON_VALUE` FIRST so the default response Content-Type is the versioned one, never the bare `application/json`. (AC 2 demands the negotiated content type even on default.)
- [x] Configure default content negotiation in `WebConfig` (AC: 2)
  - [x] Create `in.vis.config.WebConfig` implementing `WebMvcConfigurer`
  - [x] Override `configureContentNegotiation(ContentNegotiationConfigurer configurer)`:
    - `configurer.defaultContentType(MediaTypes.APPLICATION_VND_VIS_V1_JSON);`
    - `configurer.ignoreAcceptHeader(false);`
    - `configurer.favorParameter(false);`
  - [x] `@Configuration @Profile("!test")` for parity with existing config classes — OR drop the profile guard if having content negotiation in tests matches what we want for `@WebMvcTest`. **Decision:** drop the `@Profile` guard here. `WebConfig` is pure Spring MVC config, has no Redis/Firebase dependencies, and we WANT the same negotiation behavior in tests as in prod. The existing `@Profile("!test")` pattern on `SecurityConfig`/`FirebaseConfig`/`RedisConfig`/`RateLimitConfig` exists because those beans need infra (Firebase SDK, Redis); `WebConfig` does not.
- [x] Implement v2 controller demonstrating coexistence (AC: 3)
  - [x] Create `in.vis.dto.v2.BranchResponse` — copy v1 shape and add ONE extra field: `String slug`. Static `from(Branch)` factory derives `slug` from `name.toLowerCase().replace(' ', '-')` (cheap heuristic — fine for the demo, real slug logic is out of scope).
  - [x] Decision on controller structure — pick ONE of these two, document the choice in dev notes when implementing:
    - **Option A (preferred):** add a v2 method INSIDE `BranchController` with `produces = MediaTypes.APPLICATION_VND_VIS_V2_JSON_VALUE` and matching `@GetMapping`. Spring's `ProducesRequestCondition` routes by `Accept`. Pro: minimal new files. Con: controller fans out as more endpoints get v2.
    - **Option B:** new `in.vis.controller.v2.BranchControllerV2` class with `@RequestMapping("/branches", produces = MediaTypes.APPLICATION_VND_VIS_V2_JSON_VALUE)`. Pro: clean separation when v2 grows. Con: empty stub for v1-only endpoints.
    - **Pick Option A** for v1; if v2 grows to ≥3 endpoints in a future story, refactor to Option B then.
  - [x] v2 endpoint MUST return `Content-Type: application/vnd.vis.v2+json` (Spring sets this automatically when the method declares `produces` for that media type).
  - [x] v2 method body: `branchRepository.findAll().stream().map(in.vis.dto.v2.BranchResponse::from).toList()`. No service-layer change — this is a routing/serialization demo.
- [x] 406 contract for unsupported versions (AC: 4)
  - [x] No code change needed — Spring's `ProducesRequestCondition` returns 406 automatically when no `produces` matches. Verify with a test that hits `Accept: application/vnd.vis.v3+json` against `/branches` and asserts status 406.
  - [x] Confirm `GlobalExceptionHandler` does NOT swallow 406 into a different status code (audit `GlobalExceptionHandler.java` — if it catches `HttpMediaTypeNotAcceptableException` or its parent, ensure status stays 406).
- [x] Tests (AC: 7)
  - [x] Update existing `BranchControllerIntegrationTest` + `AuthControllerIntegrationTest` to assert the response `Content-Type` is `application/vnd.vis.v1+json` on the existing happy paths (proves AC 2 default behavior).
  - [x] Update `UserServiceTest` imports to `in.vis.dto.v1.*` (no behavior change).
  - [x] New test class `BranchVersioningIntegrationTest` (`backend/src/test/java/in/vis/controller/`):
    - `@WebMvcTest(BranchController.class)` (or `@SpringBootTest` if you need the repository — pick `@SpringBootTest` to avoid mocking the repo; the existing pattern in `BranchControllerIntegrationTest` already does this against Testcontainers Postgres)
    - 6 test methods, one per AC 7 row:
      1. `accept_v1_returns_v1_content_type`
      2. `no_accept_header_defaults_to_v1`
      3. `accept_plain_json_returns_v1_content_type`
      4. `accept_v2_returns_v2_content_type_and_body_has_slug`
      5. `accept_v3_returns_406_not_acceptable`
      6. `v1_response_body_does_not_contain_slug_field` (regression — proves routing isolation)
  - [x] Use `MockMvc`. Assert headers via `header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.vis.v1+json")`. Assert body shape via `jsonPath("$[0].slug").doesNotExist()` for v1 and `jsonPath("$[0].slug").isString()` for v2.
- [x] Documentation touch (AC: 5)
  - [x] Append a short note to `backend/README.md` under a new section `## API Versioning` documenting:
    - Supported media types: `application/vnd.vis.v1+json`, `application/vnd.vis.v2+json`
    - Default when `Accept` is missing/`*/*`: v1
    - Unsupported version: 406
    - Cross-link to story 1a.5
- [x] Run verification (AC: 8, 9)
  - [x] `cd backend && mvn test` — confirm prior tests + new versioning tests pass (target ≥ 27 + 6 new = 33 tests)
  - [x] `grep -r "gymculture" backend/src/` → empty
  - [x] `grep -rn "import in.vis.dto\\." backend/src/ | grep -v "in.vis.dto.v"` → empty (no stale bare-package imports)
  - [x] Push to remote → confirm `backend-ci.yml` green
- [x] Update sprint status + Linear mirror to In Review (mirror created at dev-story start in project `E1a — Backend Infrastructure` per CLAUDE.md convention; spec issue is GC-68)

## Dev Notes

### Scope discipline — what this story IS and IS NOT

**IS:**
- Centralized `vnd.vis.v1+json` / `vnd.vis.v2+json` media-type constants.
- DTO package relocation to `dto/v1`.
- One real v2 controller endpoint + one real v2 DTO proving Spring routes by Accept (`BranchResponse` v2 with `slug`).
- Default-content-negotiation config so unheadered clients still get the versioned content type back.
- 406 contract for unknown versions.
- 6 new tests covering all five Accept paths plus a routing-isolation regression.

**IS NOT:**
- Pact contract tests (Story 1a.1 left a stub; real contracts wait for consumer apps).
- Deprecation / `Sunset` HTTP headers (belongs to whichever story actually retires v1).
- Schema migration of any v1 field (no rename, no removal — additive-only).
- OpenAPI/Swagger emission (1a.6 or later docs story).
- WebSocket protocol versioning (E5).
- Versioning of `/actuator/**` (stays JSON, never used by clients).

If you reach for `RequestMappingHandlerMapping`, a custom `HandlerMapping`, a custom Accept parser, springdoc-openapi, or `Sunset`/`Deprecation` headers — **stop**, that's outside this story.

### Why Spring's built-in `produces` is enough

- Spring MVC's `RequestMappingHandlerMapping` already evaluates `Accept` against every `@RequestMapping(produces = ...)` via `ProducesRequestCondition`. Two methods with the same URL but different `produces` values are routed by `Accept` automatically. We do not need a custom HandlerMapping, custom version resolver, or a media-type strategy.
- `ContentNegotiationConfigurer.defaultContentType(...)` covers AC 2: when the client sends no `Accept` header or `*/*`, Spring serves the configured default. With one line we get the "no header → v1" contract.
- `HttpMediaTypeNotAcceptableException` is the built-in Spring exception thrown when `produces` doesn't match `Accept`. The default Spring Boot error handler maps it to 406 with a `ProblemDetail` body (Boot 3.x default). Verify our `GlobalExceptionHandler` doesn't override this — if it does, add an explicit `@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)` that returns `ResponseEntity.status(406).build()` (or let Spring's default handle it).

### Why a real v2 DTO instead of a marker

AC 3 explicitly asks for v2 coexistence proven, not claimed. A real v2 `BranchResponse` with one extra field (`slug`) gives us:

- An observable shape difference in tests (`jsonPath` assertions can distinguish v1 vs v2 bodies).
- A reference pattern for downstream stories adding v2 endpoints for `/auth`, `/sessions`, etc.
- Zero risk to existing v1 callers — Spring's content negotiation isolates the routes.

The `slug = name.toLowerCase().replace(' ', '-')` derivation is intentionally trivial. A "real" slug needs Unicode normalization, conflict resolution, persistence, etc. — out of scope. The point is the wire shape, not the slug semantics.

### Current backend state (verified 2026-05-30 — post Story 1a.4 merge)

- Spring Boot **3.5.14**, Java 21 in CI. `pom.xml` has Bucket4j 8.14.0 (story 1a.4), Redis (Lettuce) starter (story 1a.3), Boot 3.5 BOM (story 1a.2), CI workflows (story 1a.1). No new dependencies are needed for content negotiation — it's pure Spring MVC.
- Existing controllers (verified):
  - `AuthController` @ `/auth` — `GET /me`, `POST /register`
  - `BranchController` @ `/branches` — `GET /`, `GET /{id}`
- Existing DTOs under `in.vis.dto.*` (3 files): `BranchResponse`, `UserResponse`, `RegisterRequest`. ALL are `record`s. Moving them to `in.vis.dto.v1.*` is purely package-level — no field changes.
- `SecurityConfig` is `@Profile("!test")`. Existing controller tests boot via `@SpringBootTest` and bypass the security chain via `application-test.properties` excludes (`SecurityAutoConfiguration`, `UserDetailsServiceAutoConfiguration`, `ManagementWebSecurityAutoConfiguration`). Versioning behavior is independent of security — content negotiation happens BEFORE security in Spring's filter chain order, but for `@SpringBootTest` MockMvc the security chain is excluded entirely.
- `RateLimitFilter` from Story 1a.4 fires AFTER `FirebaseAuthFilter` and BEFORE the dispatcher servlet. Content negotiation runs INSIDE the dispatcher servlet on the matched handler method. The rate limit filter is orthogonal — it doesn't care about Accept headers.
- Architecture spec uses `/api/**` as the future URL prefix; current controllers do NOT have that prefix. **Do NOT introduce `/api` prefix in this story** — that's a separate refactor (and would break 1a.4's rate-limit AC reference to `/api/**` until both ship together). Defer the prefix migration to a dedicated story.

### `GlobalExceptionHandler` audit checklist

Before declaring AC 4 done, open `backend/src/main/java/in/vis/exception/GlobalExceptionHandler.java` and verify:

- No `@ExceptionHandler` exists for `HttpMediaTypeNotAcceptableException` (Boot's default handler should run).
- No catch-all `@ExceptionHandler(Exception.class)` returns 500 ahead of the framework-resolved 406. If a generic `Exception.class` handler exists, ADD a more specific `@ExceptionHandler({HttpMediaTypeNotAcceptableException.class})` that returns `ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()` so the 406 isn't shadowed.

### Files to be modified / created

**UPDATE:**
- `backend/src/main/java/in/vis/controller/AuthController.java` — class-level `produces`/`consumes` declaring v1 + `application/json`.
- `backend/src/main/java/in/vis/controller/BranchController.java` — class-level `produces`; add a v2 `@GetMapping` method per Option A.
- `backend/src/main/java/in/vis/service/UserService.java` — imports moved to `in.vis.dto.v1.*` (signature shape unchanged).
- `backend/src/test/java/in/vis/controller/AuthControllerIntegrationTest.java` — imports updated; assert `Content-Type` on existing tests.
- `backend/src/test/java/in/vis/controller/BranchControllerIntegrationTest.java` — imports updated; assert `Content-Type` on existing tests.
- `backend/src/test/java/in/vis/service/UserServiceTest.java` — imports updated.
- `backend/README.md` — `## API Versioning` section.

**MOVE (file rename — git tracks as rename if the diff is small enough):**
- `backend/src/main/java/in/vis/dto/BranchResponse.java` → `backend/src/main/java/in/vis/dto/v1/BranchResponse.java`
- `backend/src/main/java/in/vis/dto/UserResponse.java` → `backend/src/main/java/in/vis/dto/v1/UserResponse.java`
- `backend/src/main/java/in/vis/dto/RegisterRequest.java` → `backend/src/main/java/in/vis/dto/v1/RegisterRequest.java`

**NEW:**
- `backend/src/main/java/in/vis/MediaTypes.java`
- `backend/src/main/java/in/vis/config/WebConfig.java`
- `backend/src/main/java/in/vis/dto/v2/BranchResponse.java`
- `backend/src/test/java/in/vis/controller/BranchVersioningIntegrationTest.java`

### What NOT to change

- `application.properties` — content negotiation is configured in `WebConfig` (Java config), not via `spring.mvc.contentnegotiation.*` properties. Keep both consistent: prefer Java config since it's grep-able and version-controlled with the source.
- `application-test.properties` — unchanged.
- `RedisConfig`, `RateLimitConfig`, `RateLimitFilter`, `FirebaseConfig`, `CorsConfig`, `FirebaseAuthFilter`, `SecurityConfig` — unchanged.
- `backend-ci.yml` — unchanged. Tests pass via `mvn test`, no new infra.
- `Dockerfile`, `docker-compose.yml` — unchanged.
- The `dto/` parent package — it can stay empty after the move; do NOT delete the directory tree (other future v3+ packages will live alongside `v1`/`v2`).

### Previous story intelligence

From Story 1a.4 (merged 2026-05-30, commits `14cdc66` + `c7b5b86`):

- `SecurityConfig.securityFilterChain` signature now takes `RateLimitFilter` as a parameter. Don't accidentally drop or rename that parameter when touching the filter chain — content negotiation work should leave `SecurityConfig` alone.
- The pattern for `@Configuration @Profile("!test")` is established across 4 config classes. **Do NOT apply that profile guard to `WebConfig`**; see Tasks section for rationale.
- Bucket4j-Lettuce 8.x family uses `bucket4j_jdk17-*` artifact line. The general lesson — Maven Central artifact IDs sometimes differ from documentation — applies broadly. Spring's content negotiation is in `spring-web` (already on classpath via Boot starter), no new artifacts needed for this story.
- `application/problem+json` is already used by `RateLimitFilter` for 429 bodies. The same media type style applies to 406 bodies via Boot's default error handler — keep the response shape consistent (`ProblemDetail` per RFC 7807).

From Story 1a.3 (Redis integration, commits `4fe3838` + `afa3438`):

- Tests pass on JDK 21 in CI; local JDK 23/25 also works (annotation processor path was added in 1a.3's pom for Lombok). No JDK action needed here.

### Git intelligence (last 5 commits, relevance)

- `c7b5b86` Story 1a.4 done — status flip only. No code touched.
- `14cdc66` Story 1a.4 rate limit — adds `RateLimitFilter`, `RateLimitConfig`, `RateLimitProperties`, modifies `SecurityConfig`. Sets the pattern: small, scoped backend additions with `@Profile("!test")` for infra configs.
- `afa3438` / `4fe3838` Story 1a.3 done + Redis integration — establishes the Testcontainers Redis test pattern. Unused here (versioning is pure MVC, no Redis), but the `@DynamicPropertySource` approach is the canonical reference for any future integration test needing infra.
- `ef6aac2` Boot 3.5.14 upgrade — `ContentNegotiationConfigurer.defaultContentType()` API is stable since Spring Framework 5.x; no breakage.
- Earlier commits unrelated.

### Latest tech info (2026-05-30)

- Spring Boot 3.5.14 includes Spring Framework 6.2.18. `ProducesRequestCondition` + `HttpMediaTypeNotAcceptableException` semantics unchanged from 6.0.
- `MediaType.valueOf("application/vnd.vis.v1+json")` parses vendor media types per RFC 6838 §3.2 — `vnd.<vendor>.<token>+<suffix>`. The `+json` suffix means Spring uses the Jackson HTTP message converter automatically (no custom converter registration needed).
- Default Boot 3.x error handling converts `HttpMediaTypeNotAcceptableException` → 406 with an empty body (no `ProblemDetail` auto-emitted unless `spring.mvc.problemdetails.enabled=true`). For this story, empty 406 body is acceptable — AC 4 only requires the status code. If you want `application/problem+json` 406 bodies, set the property and document it; otherwise leave default.

### Project Structure Notes

- `MediaTypes.java` lives at the root `in.vis` package (one level above `config/`) because it is a public API contract, not a Spring `@Configuration` class. Mirror `org.springframework.http.MediaType`'s placement (top-level of `spring-web`).
- `WebConfig.java` joins `in.vis.config/` next to `RedisConfig`, `RateLimitConfig`, `SecurityConfig`, `FirebaseConfig`, `CorsConfig`. Pattern: one `@Configuration` per concern.
- DTO packages: `in.vis.dto.v1.*` for current, `in.vis.dto.v2.*` for next. Future v3 lives in `in.vis.dto.v3.*`. No `in.vis.dto.common.*` shared sub-package — shared shapes (if ever) live in `in.vis.dto` (parent), but this story does NOT introduce shared shapes.
- v2 controller method placement: per Option A, lives inside `BranchController` next to v1 methods. When v2 grows beyond `/branches`, refactor to `controller/v2/`.

### Risk register for the dev session

| Risk | Likelihood | Mitigation |
|---|---|---|
| Moving DTO files breaks compile because of stale imports | High | After each file move, run `mvn -DskipTests compile` and fix imports immediately. Do NOT batch-move all three then compile. |
| `produces` declared on one method but missing on another → silent fall through to first `produces` match | Med | Declare `produces` at CLASS level via `@RequestMapping(... produces = {...})` so it inherits across methods. Method-level overrides only on the v2 endpoint. |
| `@RestController` + `@RequestMapping` interaction with `produces` doesn't behave as expected in `@WebMvcTest` slice | Low | Use `@SpringBootTest` (matches existing test pattern), not `@WebMvcTest`. The existing integration tests already boot the full context — keep parity. |
| Default `Accept: */*` from `curl` lands on v1, but `Accept: application/json` doesn't (or vice versa) | Med | Add BOTH `application/json` AND `vnd.vis.v1+json` to the `produces` list — Spring picks the first match for `*/*`, and `application/json` is explicit for legacy callers. Order matters: vnd.vis.v1+json FIRST so the response `Content-Type` is always versioned. |
| Existing tests assert `application/json` content type via implicit MockMvc default | Low | The current tests do NOT assert `Content-Type` (verified — `grep -n "MediaType\|content.*type" controller tests` empty). New tests add the assertions; existing tests get updated to assert the versioned content type. |
| 406 path returns 500 because `GlobalExceptionHandler` catches `Exception.class` | Med | Audit `GlobalExceptionHandler` per the checklist above. If a catch-all exists, add an explicit `HttpMediaTypeNotAcceptableException` handler that returns 406. |
| `dto/v2/BranchResponse.from()` introduces a `name`-based slug that's null-unsafe | Low | Guard: `slug = name == null ? null : name.toLowerCase().replace(' ', '-')`. `Branch.name` is `NOT NULL` per schema, but defensive code costs nothing here. |
| Rate-limit filter's `/actuator/*` bypass is unaffected | n/a | Versioning lives in MVC layer, after filters. Confirmed by tracing the call path. |

### References

- Architecture §Step 2 Tech-Driver row 7 "Schema Versioning + API Compat" [`_bmad-output/planning-artifacts/architecture.md:90`]
- Architecture §Step 4 "API & Communication Patterns" — REST versioning `vnd.vis.vN+json`, N + N-1 locked [`architecture.md:230`]
- Architecture §Step 6 backend tree — `dto/v1/` directory placement [`architecture.md:417`]
- Epic spec Story 1a.5 ACs (GC-68) [`_bmad-output/planning-artifacts/epics.md:276–294`]
- Cross-references: Story 1a.6 (Observability + dashboards — may include OpenAPI emission later); Story 1a.1 (`pact-verify.yml` stub awaits real contracts)
- Previous stories: 1a.4 (rate limit) [`stories/1a-4-cloud-armor-bucket4j-per-uid-rate-limiting.md`], 1a.3 (Redis) [`stories/1a-3-redis-integration-local-dev-testcontainers.md`]
- Spring Framework Reference — Content Negotiation [`https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/content-negotiation.html`]
- Spring Framework Reference — Producible Media Types in `@RequestMapping` [`https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html#mvc-ann-requestmapping-produces`]
- RFC 6838 §3.2 Vendor Tree media types (`application/vnd.<vendor>.<token>+<suffix>`) [`https://datatracker.ietf.org/doc/html/rfc6838#section-3.2`]
- Linear spec issue: GC-68 (E1a project). Per CLAUDE.md, a mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>` (see GC-138 / commit `14cdc66` for the 1a.4 precedent).

## Dev Agent Record

### Agent Model Used

claude-opus-4-7 (Claude Code)

### Debug Log References

- Initial `WebConfig.defaultContentType(vnd.vis.v1+json)` broke existing 401 tests — Spring Security classifier saw the negotiated default and returned `302` redirect (HTML-client behavior) instead of `401`. Removed `defaultContentType` line; Spring's built-in behavior (no Accept = `*/*` = first `produces` match) gives the same outcome because `vnd.vis.v1+json` is declared FIRST in the produces list on every v1 controller. Net: same AC 2 behavior, no security regression.
- `application/json` Accept handling: Spring's `ProducesRequestCondition` picks the exact `application/json` match → response Content-Type becomes `application/json`. To force the versioned Content-Type on this path, added `in.vis.config.V1ContentTypeAdvice` — a `ResponseBodyAdvice` scoped to `in.vis.controller` package that rewrites Content-Type to `vnd.vis.v1+json` when Spring selects `application/json`. v2 endpoints (which declare `produces=vnd.vis.v2+json` only) never trigger the rewrite because the selected Content-Type is `vnd.vis.v2+json`, not `application/json`.
- `GlobalExceptionHandler` audit confirmed the catch-all `Exception.class` handler would shadow Spring's framework-resolved 406 → 500. Added explicit `@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)` that returns 406 with an empty body, placed before the `Exception.class` handler (Spring picks the most specific handler regardless of declaration order, but explicit ordering documents intent).
- `@WebMvcTest` slice loads `WebConfig` (`WebMvcConfigurer`) and `V1ContentTypeAdvice` (`@ControllerAdvice`) only when `@Import` lists them — verified by checking each existing controller test still passes with `@Import({WebConfig.class, V1ContentTypeAdvice.class})` added.
- Followed existing controller-test convention (`@WebMvcTest(Controller.class)`) rather than the `@SpringBootTest` mention in Dev Notes. The existing `BranchControllerIntegrationTest` and `AuthControllerIntegrationTest` are `@WebMvcTest` slices; `@SpringBootTest` would have added Testcontainers Postgres dependency to versioning tests for no functional gain. Story Dev Notes carried a stale assumption from architecture file; live code is the source of truth.

### Completion Notes List

- All 9 ACs satisfied.
- 33 tests pass locally (27 prior — 15 baseline + 2 Redis + 7 RateLimitFilter + 3 RateLimitIntegration — plus 6 new in `BranchVersioningIntegrationTest`).
- `grep -r "gymculture" backend/src/` → empty (exit 1).
- `grep -rn "import in.vis.dto\." backend/src/ | grep -v "in.vis.dto.v"` → empty (no stale bare-package imports).
- DTO records relocated to `in.vis.dto.v1.*` via `git mv` (rename tracked, not delete+add).
- v2 demo via Option A: inline v2 `@GetMapping` on `BranchController` returning `in.vis.dto.v2.BranchResponse` with `slug` field. v1 callers continue to see v1 unchanged — proven by `v1_response_body_does_not_contain_slug_field` regression test.
- 406 path verified end-to-end via `accept_v3_returns_406_not_acceptable` — Spring's `HttpMediaTypeNotAcceptableException` is thrown by `ProducesRequestCondition`, then caught by the explicit `GlobalExceptionHandler.handleNotAcceptable` → 406 empty body. No body required by AC 4.
- `WebConfig` deliberately has NO `@Profile("!test")` guard — pure MVC config, want same negotiation behavior in tests. Mirrors the Dev Notes decision.
- Linear mirror **GC-139** created (In Progress at start; will flip to In Review on completion). Spec issue GC-68 untouched.
- Commit prefix for this story: `VIS-139`.

### File List

- `backend/src/main/java/in/vis/MediaTypes.java` — NEW: `APPLICATION_VND_VIS_V1_JSON` + `APPLICATION_VND_VIS_V2_JSON` constants (string + `MediaType` forms).
- `backend/src/main/java/in/vis/dto/v1/BranchResponse.java` — MOVE from `dto/BranchResponse.java`; package updated to `in.vis.dto.v1`.
- `backend/src/main/java/in/vis/dto/v1/UserResponse.java` — MOVE from `dto/UserResponse.java`; package updated.
- `backend/src/main/java/in/vis/dto/v1/RegisterRequest.java` — MOVE from `dto/RegisterRequest.java`; package updated.
- `backend/src/main/java/in/vis/dto/v2/BranchResponse.java` — NEW: v2 record with extra `slug` field; `slugify(name)` static helper (`name.toLowerCase().replace(' ', '-')`).
- `backend/src/main/java/in/vis/config/WebConfig.java` — NEW: `WebMvcConfigurer` with explicit `ignoreAcceptHeader(false)` + `favorParameter(false)`. No `defaultContentType` (intentional — see Debug Log).
- `backend/src/main/java/in/vis/config/V1ContentTypeAdvice.java` — NEW: `ResponseBodyAdvice` scoped to `in.vis.controller` that rewrites Content-Type `application/json` → `application/vnd.vis.v1+json`.
- `backend/src/main/java/in/vis/controller/AuthController.java` — UPDATE: class-level `produces = {vnd.vis.v1+json, application/json}`; `consumes` same list on `POST /register`; imports moved to `dto.v1`.
- `backend/src/main/java/in/vis/controller/BranchController.java` — UPDATE: class-level `produces = {vnd.vis.v1+json, application/json}`; new `@GetMapping(produces = vnd.vis.v2+json) listV2()` returning `dto/v2/BranchResponse`; imports moved to `dto.v1`.
- `backend/src/main/java/in/vis/service/UserService.java` — UPDATE: imports moved to `dto.v1` (no behavior change).
- `backend/src/main/java/in/vis/exception/GlobalExceptionHandler.java` — UPDATE: new `@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)` returning 406 empty body.
- `backend/src/test/java/in/vis/controller/AuthControllerIntegrationTest.java` — UPDATE: `@Import({WebConfig, V1ContentTypeAdvice})`; assert `Content-Type: vnd.vis.v1+json` on happy path; imports moved to `dto.v1`.
- `backend/src/test/java/in/vis/controller/BranchControllerIntegrationTest.java` — UPDATE: `@Import({GlobalExceptionHandler, WebConfig, V1ContentTypeAdvice})`; assert `Content-Type` on happy path.
- `backend/src/test/java/in/vis/controller/BranchVersioningIntegrationTest.java` — NEW: 6 tests (accept_v1, no_accept, plain_json, v2_with_slug, v3_406, v1_isolation).
- `backend/src/test/java/in/vis/service/UserServiceTest.java` — UPDATE: imports moved to `dto.v1`.
- `backend/README.md` — UPDATE: new `## API Versioning` section with matrix table.

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-30 | Initial implementation: `MediaTypes`, DTO relocation to `dto/v1`, v2 `BranchResponse`, v2 controller method, `WebConfig`, `V1ContentTypeAdvice`, `produces`/`consumes` on controllers, `GlobalExceptionHandler` 406 path, 6 versioning tests + Content-Type asserts on existing tests, README API Versioning section. |
| 2026-05-31 | Dropped `WebConfig.defaultContentType(vnd.vis.v1+json)` — broke `@WebMvcTest` 401 expectations (Spring Security 302 redirect on HTML-classified clients). Relied on `produces` order (v1 first) for `*/*` default. Functional behavior unchanged. |
| 2026-05-31 | Linear mirror GC-139 created (In Progress → In Review) under E1a project; related to spec GC-68. |
