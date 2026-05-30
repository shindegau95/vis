# Story 1a.4: Cloud Armor + Bucket4j Per-UID Rate Limiting

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an ops engineer,
I want Bucket4j per-Firebase-UID token-bucket rate limiting on `/api/**` returning HTTP 429 with `Retry-After`,
so that abusive clients cannot DoS our endpoints or inflate AI costs before Cloud Armor and WS auth-on-upgrade land in their dedicated stories.

## Acceptance Criteria

1. A `RateLimitFilter` placed AFTER `FirebaseAuthFilter` in the security chain rate-limits every authenticated request to `/api/**` using the Firebase UID as the bucket key.
2. When a UID exceeds the per-window ceiling, the filter returns HTTP `429` with `Retry-After: <seconds>` header (RFC 7231 §7.1.3) and a `ProblemDetail` body (RFC 7807) consistent with `GlobalExceptionHandler` style; the next request after the refill window passes.
3. Limits are configurable via `application.properties` under `app.rate-limit.*` (capacity, refill tokens, refill period). Defaults: **120 tokens / 60s / 120 refill**. Properties consumed via `@ConfigurationProperties`-style binding (records or typed POJO — your call), **no hardcoded numbers in `RateLimitFilter`**.
4. Bucket state is stored in Redis via `bucket4j-lettuce` (`LettuceBasedProxyManager`, CAS-based, expiration-after-write tied to refill window) so limits survive Cloud Run instance horizontal scale (per-instance in-memory buckets do NOT meet the AC — would allow N× burst at N instances).
5. Unauthenticated requests (no `Authorization` header, or token rejected by `FirebaseAuthFilter`) are NOT rate-limited by this filter — they short-circuit at 401 inside the auth filter before reaching this filter. The rate-limit filter SKIPS execution when `SecurityContextHolder.getContext().getAuthentication()` is null/anonymous, so this story stays UID-keyed (IP-keyed fallback is explicit non-goal — defer to Cloud Armor infra story).
6. `/actuator/**` paths are exempt from rate limiting (matches existing `SecurityConfig` `permitAll()` rule — health checks must never 429).
7. `mvn test` passes: existing 17 tests + new tests:
   - Unit test: in-process bucket allows N requests, rejects (N+1)th, accepts again after refill window via `TimeMeter` fake-clock.
   - Integration test (`@SpringBootTest` + Testcontainers Redis 7): authenticated request loop hits 429 on (N+1)th, response carries `Retry-After`, distinct UIDs do NOT share buckets.
8. `grep -r "gymculture" backend/src/` returns empty; new code under `in.vis.*`.
9. `backend-ci.yml` stays green.

### Explicitly OUT OF SCOPE for this story (do NOT implement)

- ❌ **Cloud Armor WAF provisioning** (the architecture-spec AC about "Cloud Armor blocks before reaching backend") — this is GCP infra/Terraform, not Java code. Will be picked up by the Cloud Run + infra story alongside Cloud Memorystore (architecture §Gap Analysis row "Redis production instance" — same workstream). Story file MUST flag this as deferred; do NOT attempt to write Terraform here.
- ❌ **WebSocket auth-on-upgrade** (the architecture-spec AC about "Firebase UID validated before WS upgrade completes") — owned by Story 5.1 `5-1-websocket-serverendpoint-auth-on-upgrade` (sprint-status). Touching `WsAuthFilter.java` or `@ServerEndpoint` is out of scope.
- ❌ **Per-trainer AI rate limit** (per-trainer Claude proxy cost ceiling) — owned by Story 7.1 `7-1-claude-proxy-anthropic-sdk-backend-prompt-cache-cost-model`.
- ❌ **Per-IP rate limit fallback** for unauthenticated requests — deferred to Cloud Armor (edge WAF) which handles IP-level abuse before requests reach backend.
- ❌ **Per-endpoint differentiated limits** (e.g. stricter cap on `POST /api/ai/**`). v1 ships one global UID limit; differentiated limits added per call-site need under E7.
- ❌ **Bucket4j Spring Boot Starter** auto-configuration. The starter's annotation-based DSL (`@RateLimiting`) couples policy to method-level annotations — we want a single `OncePerRequestFilter` so the policy is centralized. Use plain `bucket4j-core` + `bucket4j-lettuce` artifacts.

## Tasks / Subtasks

- [x] Add Bucket4j dependencies to `backend/pom.xml` (AC: 4)
  - [x] `com.bucket4j:bucket4j-core` (no version — managed by Bucket4j parent BOM if importing; otherwise pin a property `<bucket4j.version>8.10.1</bucket4j.version>`)
  - [x] `com.bucket4j:bucket4j-lettuce`
  - [x] Confirm `mvn dependency:tree | grep bucket4j` shows both jars
- [x] Create `RateLimitProperties` POJO/record under `in.vis.config` (AC: 3)
  - [x] Three fields: `int capacity`, `int refillTokens`, `Duration refillPeriod` (defaults 120 / 120 / `PT1M`)
  - [x] Use either `@ConfigurationProperties("app.rate-limit")` + `@EnableConfigurationProperties(RateLimitProperties.class)` on `RateLimitConfig`, OR three `@Value`-injected fields on the filter — pick whichever keeps `RateLimitFilter` testable in isolation (a constructor that takes capacity/refill/period plus a `ProxyManager<String>` is the simplest form to unit-test)
- [x] Wire `LettuceBasedProxyManager` under `in.vis.config.RateLimitConfig` (AC: 4)
  - [x] `@Configuration @Profile("!test")` — production form
  - [x] Bean `StatefulRedisConnection<String, byte[]>` derived from existing `LettuceConnectionFactory` (you can grab the underlying `RedisClient` from the connection factory, OR create a sibling `RedisClient` configured from the same `spring.data.redis.*` properties — do whichever the Bucket4j docs recommend; `Bucket4jLettuce.casBasedBuilder(StatefulRedisConnection)` is the canonical entry point)
  - [x] `@Bean ProxyManager<String> rateLimitProxyManager(...)` — `casBasedBuilder` + `ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(refillPeriod * 2)` (TTL ~ 2× refill window so cold UIDs don't leak Redis keys forever, but active UIDs never expire mid-window)
  - [x] A separate `@Configuration @Profile("test")` `RateLimitTestConfig` may export an in-memory `ProxyManager` for tests that don't need a real Redis (see Testing section below)
- [x] Implement `in.vis.filter.RateLimitFilter` (AC: 1, 2, 5, 6)
  - [x] Extends `OncePerRequestFilter`
  - [x] Constructor takes `ProxyManager<String> proxyManager` + `RateLimitProperties props` (or three primitives)
  - [x] `shouldNotFilter(HttpServletRequest req)` returns true for `/actuator/**` (AC 6) — single substring check, fast path
  - [x] `doFilterInternal`:
    - [x] Read `SecurityContextHolder.getContext().getAuthentication()`. If null OR `!authentication.isAuthenticated()` OR principal name blank → `chain.doFilter(req, res); return;` (AC 5 — skip, don't 429 anonymous)
    - [x] Compute `String uid = authentication.getName();`
    - [x] Look up bucket: `Bucket bucket = proxyManager.getProxy(uid, () -> bucketConfig);` where `bucketConfig` is built from props (capacity + `refillGreedy(refillTokens, refillPeriod)`)
    - [x] `ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);`
    - [x] If `probe.isConsumed()` → set header `X-Rate-Limit-Remaining: <probe.getRemainingTokens()>` then `chain.doFilter(req, res)`
    - [x] Else → set `Retry-After: <seconds-rounded-up>`, set status `HTTP_TOO_MANY_REQUESTS (429)`, write `ProblemDetail` JSON body (`type: about:blank`, `title: "Too Many Requests"`, `status: 429`, `detail: "Rate limit exceeded for UID …"` — DO NOT include the raw UID in the detail; log it instead, mask it in body) using `objectMapper` or just hand-rolled JSON via `response.getWriter()` if you want zero deps
  - [x] Compute Retry-After seconds: `Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1)` — round UP, never advertise 0s
- [x] Register `RateLimitFilter` AFTER `FirebaseAuthFilter` in `SecurityConfig` (AC: 1)
  - [x] In `SecurityConfig.securityFilterChain(...)`, add: `.addFilterAfter(rateLimitFilter, FirebaseAuthFilter.class)`
  - [x] Inject the filter as a method parameter; declare its bean via `@Bean public RateLimitFilter rateLimitFilter(ProxyManager<String> pm, RateLimitProperties p) { return new RateLimitFilter(pm, p); }`
  - [x] Verify the existing `addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)` chain still wires correctly with the new filter slotted in between
- [x] Wire `app.rate-limit.*` defaults into `application.properties` (AC: 3)
  - [x] `app.rate-limit.capacity=120`
  - [x] `app.rate-limit.refill-tokens=120`
  - [x] `app.rate-limit.refill-period=PT1M`
- [x] Tests (AC: 7)
  - [x] Unit test `RateLimitFilterTest` in `backend/src/test/java/in/vis/filter/`:
    - Build an in-memory `ProxyManager<String>` via `io.github.bucket4j.distributed.proxy.AsyncOrSync` `Bucket4jHazelcast` — wait, simpler: use `Bucket.builder()` directly in a stub `ProxyManager` OR use `io.github.bucket4j.distributed.proxy.InMemoryProxyManager` if it exists in 8.10. If neither is convenient, mock `ProxyManager` with Mockito returning a `Bucket` built from `Bucket.builder().withMillisecondPrecision().withCustomTimePrecision(new MockTimeMeter())`. Pick the path that lets you fake-advance time without `Thread.sleep`.
    - Test A: N requests allowed (capacity = 3 for the test, set via constructor), (N+1)th returns 429 with `Retry-After ≥ 1`.
    - Test B: advance fake clock by refillPeriod, next request consumed again, headers reset.
    - Test C: requests from two different UIDs do NOT share a bucket — UID A's exhaustion does not 429 UID B.
    - Test D: `/actuator/health` request bypasses entirely (verify by calling `shouldNotFilter` or by full request mock); skip if covered by integration test
    - Test E: anonymous authentication (null `Authentication`) is not rate-limited; chain proceeds
  - [x] Integration test `RateLimitIntegrationTest` in `backend/src/test/java/in/vis/`:
    - `@SpringBootTest(classes = VisApplication.class) @ActiveProfiles("test") @Testcontainers`
    - Reuse `redis:7-alpine` `GenericContainer` + `@DynamicPropertySource` pattern from `RedisIntegrationTest`
    - `MockMvc` POST/GET to a test controller endpoint (or any existing `/api/**` endpoint with auth-mock) loop capacity+1 times → expect 429 on last
    - Verify `Retry-After` header present and parses to int ≥ 1
    - Because `SecurityConfig` is `@Profile("!test")`, the test profile lacks the SecurityFilterChain. You will need EITHER: (a) a test-only `@TestConfiguration` that builds a minimal filter chain wiring just `RateLimitFilter` + a fake auth supplier, OR (b) a slim `RateLimitFilter` test that bypasses Spring Security entirely and invokes `doFilterInternal` directly with a `MockHttpServletRequest` carrying a pre-populated `SecurityContextHolder`. Option (b) is simpler — Option (a) blurs the unit/integration boundary. Combined with the Redis Testcontainer, (b) still proves the Lettuce path.
- [x] Verification (AC: 8, 9)
  - [x] `cd backend && mvn test` — confirm 17 prior + new RateLimit tests pass
  - [x] `grep -r "gymculture" backend/src/` → empty (exit 1)
  - [x] Push to remote → confirm `backend-ci.yml` green
- [x] Update sprint status + Linear mirror to In Review (mirror created at dev-story start in project `E1a — Backend Infrastructure` per CLAUDE.md convention; spec issue is GC-67)

## Dev Notes

### Scope discipline — what this story IS and IS NOT

**IS:**
- A `RateLimitFilter` that returns 429 + `Retry-After` per Firebase UID with Redis-backed buckets.
- `app.rate-limit.*` configuration surface for capacity / refill.
- Tests that prove the limit fires + resets + isolates per-UID.

**IS NOT:**
- Cloud Armor / WAF rules (GCP infra, defer to Cloud Run infra story).
- WS auth-on-upgrade (Story 5.1).
- Per-trainer AI rate limit (Story 7.1).
- Per-endpoint or per-tier differentiated limits.
- An IP-keyed fallback for unauthenticated traffic (Cloud Armor handles).

If you find yourself reaching for `WsAuthFilter`, `@ServerEndpoint`, Terraform, or per-call-site annotations — **stop**; that's not this story.

### Why Bucket4j over Spring Cloud Gateway RateLimiter or resilience4j

- **Spring Cloud Gateway** is a gateway, not a filter — we'd have to introduce a gateway proxy in front of Spring Boot. Architectural overshoot at v1 scale.
- **Resilience4j RateLimiter** is per-instance only, no native distributed backend. With Cloud Run scaling to N instances, a 120/min limit becomes effective `N×120/min` — exactly the failure mode AC 4 calls out.
- **Bucket4j-Lettuce** gives us distributed buckets keyed by string (the Firebase UID), Lettuce-native (we already use Lettuce 6.6 from Story 1a.3), and the `ConsumptionProbe` API exposes nanos-to-refill which maps cleanly to `Retry-After`.

### Why `OncePerRequestFilter` AFTER `FirebaseAuthFilter` (not BEFORE)

- AC 1 says UID-keyed. UID is only available after `FirebaseAuthFilter` populates `SecurityContextHolder`.
- Putting the rate limiter BEFORE auth means we'd have to parse the JWT twice (waste) or fall back to IP-keyed for everyone — defeats the per-UID guarantee.
- Trade-off acknowledged: an attacker who can complete Firebase JWT verification still costs us one full `verifyIdToken` call per blocked request. That's fine — Firebase's verification is cached against the public key set; the bucket reject is microseconds. Cloud Armor handles the pre-auth abuse layer.

### Why `@Profile("!test")` on the prod config + test-only path

- `@Profile("!test")` on `RateLimitConfig` mirrors the existing pattern (`SecurityConfig`, `FirebaseConfig`, `RedisConfig`). Tests get a clean wire-up via direct construction.
- The integration test uses the Lettuce path against a real Testcontainers Redis — proves CAS-based bucket logic works end-to-end against Redis.
- The unit test does NOT need Redis; constructs the filter with an in-memory or mock `ProxyManager` so it runs in <100ms.

### Bucket4j 8.10.x specifics (verified 2026-05-30 via context7 docs)

- Group ID: `com.bucket4j`. Artifacts: `bucket4j-core`, `bucket4j-redis`, `bucket4j-lettuce`.
- `bucket4j-lettuce` depends on `bucket4j-redis` which depends on `bucket4j-core` — only declare `bucket4j-lettuce` in `pom.xml`; the others are transitive.
- API surface used: `Bucket4jLettuce.casBasedBuilder(StatefulRedisConnection)`, `LettuceBasedProxyManager`, `ProxyManager.getProxy(key, Supplier<BucketConfiguration>)`, `Bucket.tryConsumeAndReturnRemaining(long)`, `ConsumptionProbe.isConsumed()`, `ConsumptionProbe.getRemainingTokens()`, `ConsumptionProbe.getNanosToWaitForRefill()`.
- `BucketConfiguration.builder().addLimit(limit -> limit.capacity(N).refillGreedy(N, Duration.ofMinutes(1))).build()` — `refillGreedy` ≠ `refillIntervally`; greedy refills continuously, intervally refills in lumps at period boundary. Greedy is what HTTP-API rate limits typically want (smoother behavior).
- `ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration)` — TTL strategy that keeps the Redis key alive only as long as needed; pass `refillPeriod.multipliedBy(2)` so keys self-evict for cold UIDs.

### Spring Boot 3 / `jakarta.servlet` adjustment

The Bucket4j docs use `javax.servlet.*` (pre-Jakarta). Spring Boot 3.5 is on `jakarta.servlet.*`. Use `jakarta.servlet.http.HttpServletRequest` / `HttpServletResponse` and `org.springframework.web.filter.OncePerRequestFilter`. The semantics are identical; only the import package changes.

Example skeleton (adapt — do not blindly paste):

```java
package in.vis.filter;

import in.vis.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final RateLimitProperties props;

    public RateLimitFilter(ProxyManager<String> proxyManager, RateLimitProperties props) {
        this.proxyManager = proxyManager;
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isBlank()) {
            chain.doFilter(req, res);
            return;
        }

        String uid = auth.getName();
        Bucket bucket = proxyManager.getProxy(uid, () -> bucketConfig());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            res.setHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            chain.doFilter(req, res);
            return;
        }

        long retryAfterSeconds = Math.max(1L,
                TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        res.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        res.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.getWriter().write(
            "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429," +
            "\"detail\":\"Rate limit exceeded. Retry after " + retryAfterSeconds + "s.\"}"
        );
    }

    private BucketConfiguration bucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(props.capacity())
                        .refillGreedy(props.refillTokens(), props.refillPeriod()))
                .build();
    }
}
```

### `RateLimitConfig` skeleton (production wiring)

```java
package in.vis.config;

import in.vis.filter.RateLimitFilter;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.github.bucket4j.distributed.expiration.ExpirationAfterWriteStrategy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient bucket4jRedisClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        RedisURI.Builder uri = RedisURI.builder().withHost(host).withPort(port);
        if (password != null && !password.isBlank()) {
            uri.withPassword(password.toCharArray());
        }
        return RedisClient.create(uri.build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> bucket4jRedisConnection(RedisClient client) {
        return client.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    public ProxyManager<String> rateLimitProxyManager(
            StatefulRedisConnection<String, byte[]> connection,
            RateLimitProperties props) {
        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(props.refillPeriod().multipliedBy(2)))
                .build();
    }

    @Bean
    public RateLimitFilter rateLimitFilter(ProxyManager<String> proxyManager, RateLimitProperties props) {
        return new RateLimitFilter(proxyManager, props);
    }
}
```

`RateLimitProperties` record:

```java
package in.vis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
        int capacity,
        int refillTokens,
        Duration refillPeriod
) {
    public RateLimitProperties {
        if (capacity <= 0) throw new IllegalArgumentException("app.rate-limit.capacity must be > 0");
        if (refillTokens <= 0) throw new IllegalArgumentException("app.rate-limit.refill-tokens must be > 0");
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("app.rate-limit.refill-period must be a positive ISO-8601 duration");
        }
    }
}
```

NOTE: Spring Boot 3.x supports `record` `@ConfigurationProperties` natively — no `@ConstructorBinding` needed. Defaults come from `application.properties`, not from the record's compact constructor.

### Updating `SecurityConfig` (one-line addition)

Current chain (Story 1a.3 verified state):

```java
.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
.addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
.build();
```

Story change — inject `RateLimitFilter` as a method parameter and slot AFTER `FirebaseAuthFilter`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               FirebaseAuthFilter firebaseAuthFilter,
                                               RateLimitFilter rateLimitFilter,
                                               CorsFilter corsFilter) throws Exception {
    return http
            .cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(eh -> eh
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, FirebaseAuthFilter.class)
            .build();
}
```

### Current backend state (verified 2026-05-30 — post Story 1a.3 merge)

- Spring Boot **3.5.14**, Java 21 (CI) / dev JDK may vary.
- `RedisConfig` (`@Profile("!test")`) provides `LettuceConnectionFactory` + `StringRedisTemplate`. **Reuse Lettuce 6.6 dep — do NOT add a separate Redis client library.** Bucket4j-Lettuce wants a raw `StatefulRedisConnection`; create it from the same `RedisClient` you build in `RateLimitConfig` (don't try to share the connection factory's connection — Lettuce connections are stateful per-codec, and Bucket4j needs `<String, byte[]>` whereas `LettuceConnectionFactory` uses its own codec).
- `FirebaseAuthFilter` sets `SecurityContextHolder` authentication with `decoded.getUid()` as principal name → `authentication.getName()` returns the Firebase UID. Confirmed in `backend/src/main/java/in/vis/filter/FirebaseAuthFilter.java:42-45`.
- `SecurityConfig` is `@Profile("!test")`. Tests don't load the security chain at all (Spring autoconfigure exclusions in `application-test.properties` cover this). Integration tests for the rate limiter must wire the filter directly OR enable a minimal test profile chain.
- `application-test.properties` UNCHANGED is the goal — do NOT add rate-limit overrides there unless absolutely necessary.

### Testcontainers Redis — reuse pattern from `RedisIntegrationTest`

Identical `@Testcontainers` + `GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379)` + `@DynamicPropertySource` shape. If multiple integration tests want Redis, the architecture said an `AbstractRedisIntegrationTestBase` would be appropriate — but per Story 1a.3 we explicitly DEFERRED that. For this story, copy the inline pattern; refactor across stories ONLY when ≥3 tests share it.

### Per-UID isolation test pattern

The strongest test of AC 4's distributed claim is: two distinct UIDs, the second one is NOT throttled when the first is exhausted. Skeleton:

```java
@Test
void distinct_uids_have_independent_buckets() {
    // Authenticate as UID "A", drain bucket
    authenticate("uid-a");
    for (int i = 0; i < CAPACITY; i++) {
        mockMvc.perform(get("/api/me")).andExpect(status().isOk());  // or 401 if no controller stub
    }
    mockMvc.perform(get("/api/me")).andExpect(status().isTooManyRequests());

    // Switch to UID "B", should NOT be throttled
    authenticate("uid-b");
    mockMvc.perform(get("/api/me")).andExpect(status().isOk());
}
```

If you don't want to stand up a controller, hit `/error` or `/actuator/info` (NOT health — that's exempt) to trigger the filter chain without a real handler. Pick whichever needs the smallest test fixture.

### Files to be modified / created

**UPDATE:**
- `backend/pom.xml` — add `bucket4j-lettuce` (transitively pulls `bucket4j-core` + `bucket4j-redis`).
- `backend/src/main/java/in/vis/config/SecurityConfig.java` — inject `RateLimitFilter`, add `.addFilterAfter(rateLimitFilter, FirebaseAuthFilter.class)`.
- `backend/src/main/resources/application.properties` — add `app.rate-limit.capacity/refill-tokens/refill-period` defaults.

**NEW:**
- `backend/src/main/java/in/vis/config/RateLimitProperties.java`
- `backend/src/main/java/in/vis/config/RateLimitConfig.java`
- `backend/src/main/java/in/vis/filter/RateLimitFilter.java`
- `backend/src/test/java/in/vis/filter/RateLimitFilterTest.java`
- `backend/src/test/java/in/vis/RateLimitIntegrationTest.java`

### What NOT to change

- `RedisConfig.java`, `FirebaseConfig.java`, `CorsConfig.java`, `FirebaseAuthFilter.java` — unchanged.
- `backend-ci.yml` — unchanged. Testcontainers handles Redis lifecycle in test JVM.
- `Dockerfile` — unchanged.
- `application-test.properties` — unchanged (Story 1a.3 disciplined this; preserve).
- `docker-compose.yml` — unchanged (Redis service already there from Story 1a.3).
- Any existing controller/service/model — unchanged.

### Previous story intelligence (Story 1a.3 — Redis Integration, merged 2026-05-28)

From Story 1a.3 Completion Notes and code review (commits `4fe3838` + `afa3438`):

- `LettuceConnectionFactory` is wired in `in.vis.config.RedisConfig`. Bucket4j uses a **separate** `StatefulRedisConnection<String, byte[]>` (different codec). Do not try to reuse the Spring Data Redis connection.
- `RedisIntegrationTest` proves Testcontainers `redis:7-alpine` + `@DynamicPropertySource` works on CI (JDK 21, ubuntu-latest). Follow the same pattern.
- `maven-compiler-plugin` `annotationProcessorPaths` for Lombok was added bundled in 1a.3 — do NOT touch it.
- Deferred work registry (`_bmad-output/implementation-artifacts/deferred-work.md`) already lists Cloud Memorystore provisioning under "Production Redis hardening" — Cloud Armor falls under the same infra-story workstream. No duplicate defer entry needed; cross-reference is enough.
- Local Docker on Mac with Engine 29.x can stall (`docker ps` hangs); restart Docker Desktop if Testcontainers fails. CI uses ubuntu-latest, unaffected.

### Git intelligence (last 5 commits, relevance)

- `afa3438` Story 1a.3 marked done after CI green — no code touched, status flip only.
- `4fe3838` Story 1a.3 Redis integration — adds Lettuce 6.6, Testcontainers pattern, `RedisConfig.java`. **Foundation for this story.**
- `ef6aac2` Boot 3.5.14 upgrade — Spring Data Redis 3.5.x → Bucket4j-Lettuce 8.10.x compatible.
- `db1dd30` / `a77c9e4` CI pipelines — unaffected by this story.
- `97e06fb` / `07b48b1` prototype frontend — unrelated.

### Latest tech info (2026-05-30)

- Bucket4j current stable: **8.10.1** (verified via context7 docs). 8.10.x is the active branch on Spring Boot 3.x and Java 21+. Earlier 7.x predates Jakarta migration; do NOT pin 7.x.
- Lettuce 6.6.x (already in classpath via Boot 3.5.14) is compatible with `bucket4j-lettuce` 8.10.x — they target Lettuce ≥6.2.
- Redis 7 (already running via docker-compose from Story 1a.3) supports the EVAL/EVALSHA + WATCH/MULTI primitives Bucket4j uses for CAS-based buckets.
- Spring Boot 3.5 record-based `@ConfigurationProperties` is stable since 3.2; no special config required beyond `@EnableConfigurationProperties(RateLimitProperties.class)` or `@ConfigurationPropertiesScan`.

### Project Structure Notes

- `RateLimitFilter.java` slots into `in.vis.filter/` next to `FirebaseAuthFilter.java` — matches architecture §Step 6 backend structure (`filter/` package houses both `FirebaseAuthFilter.java` and `WsAuthFilter.java`; `WsAuthFilter` is Story 5.1, untouched here).
- `RateLimitConfig.java` and `RateLimitProperties.java` slot into `in.vis.config/` next to `RedisConfig.java`. Pattern matches: one config class + one properties record per feature.
- Test files mirror main package layout: `RateLimitFilterTest` under `backend/src/test/java/in/vis/filter/`, integration test at the top of `backend/src/test/java/in/vis/` (matching `MigrationsIntegrationTest.java` / `RedisIntegrationTest.java` placement).

### Risk register for the dev session

| Risk | Likelihood | Mitigation |
|---|---|---|
| `@Profile("!test")` on `RateLimitConfig` means tests cannot autowire `RateLimitFilter` from the prod chain | High | Use direct-construction in unit tests (no Spring) + minimal `@TestConfiguration` if integration test needs DI |
| Bucket4j-Lettuce 8.10 API moves between minor versions | Low | Pin `<bucket4j.version>8.10.1</bucket4j.version>` in `pom.xml` |
| `Retry-After` formatted as 0 if `nanosToWaitForRefill` rounds down | Med | `Math.max(1L, ... + 1)` in the filter (already specified) |
| Two-UID isolation test is flaky if Testcontainers Redis is slow to flush keys between tests | Low | Each test uses its own UID strings (e.g., UUID-prefixed); Redis TTL ≪ test runtime |
| Existing integration tests (`MigrationsIntegrationTest`, `AuthControllerIntegrationTest`, `RedisIntegrationTest`) start failing because the new `@Configuration` `RateLimitConfig` requires Redis at context-load even in tests that don't exercise it | Med | `@Profile("!test")` on `RateLimitConfig` keeps it out of test context, mirroring the existing pattern |
| Cloud Run instance-affinity confusion (a UID could land on different instances over time, each with its own Redis connection) | Low | Bucket4j-Lettuce stores state in Redis, not in-process — distinct connections see the same bucket via CAS |

### References

- Architecture §Step 4 "Cross-Cutting Concerns": Cloud Armor + Bucket4j per-Firebase-UID token bucket [`_bmad-output/planning-artifacts/architecture.md:125`]
- Architecture §Step 4 Authentication & Security [`architecture.md:222–224`]
- Architecture §Step 5 Gap Analysis row "Redis production instance" → infra story (Cloud Armor in the same workstream) [`architecture.md:300`]
- Architecture §Step 6 backend directory tree — `RateLimitFilter` location (`filter/`), `RateLimitConfig` location (`config/`) [`architecture.md:381–407`]
- Epic spec ARCH-19 + Story 1a.4 ACs [`_bmad-output/planning-artifacts/epics.md:107, 253–273`]
- Sprint cross-references: Story 5.1 (WS auth-on-upgrade), Story 7.1 (Claude proxy + cost ceiling)
- Previous story (Redis integration verified) [`_bmad-output/implementation-artifacts/stories/1a-3-redis-integration-local-dev-testcontainers.md`]
- Bucket4j 8.x quick-start (servlet filter pattern, `ConsumptionProbe` API) [`https://bucket4j.github.io/8.10.1/toc.html#_quick_start`]
- Bucket4j Lettuce backend (`Bucket4jLettuce.casBasedBuilder`, `LettuceBasedProxyManager`, `ExpirationAfterWriteStrategy`) [`https://bucket4j.github.io/8.10.1/toc.html#_lettuce_integration`]
- RFC 7231 §7.1.3 `Retry-After` header [`https://datatracker.ietf.org/doc/html/rfc7231#section-7.1.3`]
- RFC 7807 `application/problem+json` body shape [`https://datatracker.ietf.org/doc/html/rfc7807`]
- Linear spec issue: GC-67 (E1a project). Per CLAUDE.md, a mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>` (see GC-137 / commit `4fe3838` for the 1a.3 precedent).

## Dev Agent Record

### Agent Model Used

claude-opus-4-7 (Claude Code)

### Debug Log References

- Initial Bucket4j coordinates `com.bucket4j:bucket4j-lettuce:8.10.1` failed to resolve — that artifact does not exist on Maven Central. Bucket4j 8.x is published per JDK target with underscored names: `bucket4j_jdk11-*` and `bucket4j_jdk17-*`. Switched to `com.bucket4j:bucket4j_jdk17-lettuce:8.14.0` (latest stable, JDK 17+ baseline matches our Java 21 toolchain). API surface (`Bucket4jLettuce.casBasedBuilder`, `LettuceBasedProxyManager`, `ProxyManager.getProxy`, `BucketProxy`, `ConsumptionProbe`, `ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax`) is identical to the 8.10.x docs we consulted.
- `HttpServletResponse.SC_TOO_MANY_REQUESTS` does not exist in `jakarta.servlet`. Replaced with `org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value()`.
- First unit-test pass cast-failed: Mockito mocked `Bucket` while `ProxyManager.getProxy` returns `BucketProxy` (extends `Bucket`). Mock the concrete `BucketProxy` interface and stub via `doReturn`/`doAnswer` so Mockito doesn't dispatch into the default-method body that calls `builder().build(...)`.
- Local Docker daemon stalled mid-session (same Engine 29.x issue from Story 1a.3). Force-killed `com.docker` / `Docker Desktop` processes + `open -a Docker` restarted; Testcontainers Redis lifecycle ran cleanly on retry.

### Completion Notes List

- All 9 ACs satisfied (1–9). Out-of-scope items (Cloud Armor WAF, WS auth-on-upgrade, AI per-trainer limit, IP fallback) untouched and explicitly cross-referenced in the story for future stories to pick up.
- 27 tests pass locally (15 prior backend tests + 2 Redis tests from Story 1a.3 + **7 new `RateLimitFilterTest` unit tests** + **3 new `RateLimitIntegrationTest` Testcontainers Redis tests** = 27).
- `grep -r "gymculture" backend/src/` → empty (exit 1).
- Bucket4j 8.14.0 (`bucket4j_jdk17-lettuce`) chosen over the 8.10.1 figure in story file Dev Notes — 8.10.x is no longer current on Central; 8.14.0 is the latest stable, same JDK 17+ artifact line. Story file Dev Notes carries the now-stale "8.10.1" reference; not worth amending — version is pinned in `pom.xml`.
- `RateLimitFilter` placed after `FirebaseAuthFilter` via `.addFilterAfter(rateLimitFilter, FirebaseAuthFilter.class)` in `SecurityConfig`.
- `RateLimitConfig` is `@Profile("!test")` — production path uses a dedicated `RedisClient` + `StatefulRedisConnection<String, byte[]>` (separate from Spring Data Redis's connection factory, which uses a different codec); test path constructs the filter directly via `RateLimitIntegrationTest`'s `@BeforeAll` against a fresh Testcontainers Redis.
- ProblemDetail JSON body is hand-rolled (no Jackson dependency in the filter) to keep the 429 path allocation-cheap and avoid pulling `ObjectMapper` into the filter constructor. UID is intentionally omitted from the body — it's already in the auth context, no need to echo it back to the client.
- `Retry-After` rounded UP via `Math.max(1L, NANOSECONDS.toSeconds(nanos) + 1)` so we never advertise 0s.
- Linear mirror **GC-138** created (In Progress at start; flipped to In Review on completion). Spec issue GC-67 untouched.
- Commit prefix for this story: `VIS-138`.

### File List

- `backend/pom.xml` — UPDATE: added `bucket4j.version=8.14.0` property + `com.bucket4j:bucket4j_jdk17-lettuce` dependency.
- `backend/src/main/resources/application.properties` — UPDATE: added `app.rate-limit.capacity=120`, `app.rate-limit.refill-tokens=120`, `app.rate-limit.refill-period=PT1M`.
- `backend/src/main/java/in/vis/config/RateLimitProperties.java` — NEW: `@ConfigurationProperties("app.rate-limit")` record with compact-constructor validation.
- `backend/src/main/java/in/vis/config/RateLimitConfig.java` — NEW: `@Profile("!test")` config wiring `RedisClient`, `StatefulRedisConnection<String, byte[]>`, `LettuceBasedProxyManager`, and the `RateLimitFilter` bean.
- `backend/src/main/java/in/vis/filter/RateLimitFilter.java` — NEW: `OncePerRequestFilter` with `/actuator/**` bypass, anonymous-skip, UID-keyed bucket lookup, 429 + `Retry-After` + `application/problem+json` body.
- `backend/src/main/java/in/vis/config/SecurityConfig.java` — UPDATE: inject `RateLimitFilter` + `.addFilterAfter(rateLimitFilter, FirebaseAuthFilter.class)`.
- `backend/src/test/java/in/vis/filter/RateLimitFilterTest.java` — NEW: 7 Mockito-backed unit tests covering capacity exhaust + 429 contract + refill reset + per-UID isolation + `/actuator` bypass + anonymous skip + null-auth skip + properties validation.
- `backend/src/test/java/in/vis/RateLimitIntegrationTest.java` — NEW: 3 Testcontainers Redis tests proving end-to-end behavior against a real `LettuceBasedProxyManager` (capacity → 429 with `Retry-After`, distinct UIDs independent across the same Redis backend, `/actuator/health` bypass).

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-30 | Initial implementation: Bucket4j-Lettuce 8.14.0 dependency, `RateLimitProperties`, `RateLimitConfig`, `RateLimitFilter`, `SecurityConfig` wiring, `application.properties` defaults, unit + integration tests. |
| 2026-05-30 | Bucket4j coordinates corrected from `bucket4j-lettuce:8.10.1` (does not exist on Central) to `bucket4j_jdk17-lettuce:8.14.0` after Maven resolution failure. |
| 2026-05-30 | Linear mirror GC-138 created (In Progress → In Review) under E1a project; related to spec GC-67. |
