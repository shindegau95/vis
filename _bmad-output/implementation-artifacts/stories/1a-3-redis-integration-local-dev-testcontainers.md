# Story 1a.3: Redis Integration — Local Dev + Testcontainers

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want Redis 7 available via `docker-compose` locally and Testcontainers in CI,
so that Epic E5 WebSocket Pub/Sub fan-out can be built on a real Redis without additional infra setup, and `/actuator/health` reports Redis status from day one.

## Acceptance Criteria

1. `docker-compose up -d` brings up a `redis:7-alpine` container alongside Postgres; host/port match dev defaults consumed by Spring Boot.
2. Backend `RedisConfig.java` provides a `LettuceConnectionFactory` (and a `StringRedisTemplate` bean) configured from environment variables (`REDIS_HOST`, `REDIS_PORT`, optional `REDIS_PASSWORD`) — **no hardcoded host/port**.
3. `mvn test` with `@ActiveProfiles("test")` starts a Testcontainers Redis 7 container, wires its host/port into Spring via `@DynamicPropertySource`, and `RedisHealthIndicator` reports `UP` from `/actuator/health`.
4. A round-trip integration test (`set` → `get` on `StringRedisTemplate`) passes against the Testcontainers Redis instance.
5. `grep -r "gymculture" backend/src/` still returns empty; all new code under `in.vis.*`.
6. `mvn test` passes (existing 15 + new Redis test(s)); `backend-ci.yml` stays green.

## Tasks / Subtasks

- [x] Add `spring-boot-starter-data-redis` dependency to `backend/pom.xml` (AC: 2, 3)
  - [x] Insert under `<dependencies>` block (group: `org.springframework.boot`, no explicit version — managed by Boot 3.5.14 BOM)
  - [x] Run `mvn dependency:tree | grep -i redis` and confirm `lettuce-core` resolves (default client)
- [x] Add Redis service to `backend/docker-compose.yml` (AC: 1)
  - [x] Add `redis` service: `image: redis:7-alpine`, `container_name: vis-redis`, `restart: unless-stopped`, `ports: ["6379:6379"]`, named volume `vis-redis:/data`, healthcheck `redis-cli ping` → `PONG`
  - [x] Append `vis-redis:` to top-level `volumes:` block
- [x] Create `RedisConfig.java` under `in.vis.config` (AC: 2)
  - [x] `@Configuration`, `@Profile("!test")` mirroring `SecurityConfig`/`FirebaseConfig` pattern (test profile uses `@DynamicPropertySource`-driven Spring Boot auto-config)
  - [x] `@Bean LettuceConnectionFactory redisConnectionFactory(...)` reading `${REDIS_HOST}`, `${REDIS_PORT:6379}`, `${REDIS_PASSWORD:}` via `@Value` or `RedisProperties` injection
  - [x] `@Bean StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf)` — used by E5 Pub/Sub story and by the round-trip test in this story
  - [x] Do **not** add `RedisMessageListenerContainer` or pub/sub topic beans — those belong to Story 5.2
- [x] Wire Redis properties in `application.properties` (AC: 2)
  - [x] Add `spring.data.redis.host=${REDIS_HOST:localhost}`
  - [x] Add `spring.data.redis.port=${REDIS_PORT:6379}`
  - [x] Add `spring.data.redis.password=${REDIS_PASSWORD:}`
  - [x] Add `spring.data.redis.timeout=2s`
  - [x] (Optional, defensive) `management.health.redis.enabled=true` — Boot defaults to `true` when starter is present, but explicit pins intent
- [x] Add Testcontainers Redis test fixture (AC: 3, 4)
  - [x] Add test dependency to `pom.xml`: `org.testcontainers:testcontainers` (already pulled by `junit-jupiter`/`postgresql` Testcontainers BOM at 1.19.7 — verify no new dep needed)
  - [x] Do **NOT** add `tc:redis:7:///` to `application-test.properties` — that JDBC URL scheme is Postgres-only; Redis is not JDBC. The architecture's `tc:redis:7:///` hint refers to *intent* (Testcontainers Redis 7), not a literal URL. Implement via `@DynamicPropertySource`.
  - [x] Create `src/test/java/in/vis/RedisIntegrationTest.java`:
    - `@SpringBootTest`, `@ActiveProfiles("test")`, `@Testcontainers`
    - `@Container static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379)`
    - `@DynamicPropertySource` static method that registers `spring.data.redis.host` → `redis.getHost()`, `spring.data.redis.port` → `redis.getFirstMappedPort()`
    - Test A: autowire `StringRedisTemplate`, write key, read key back, assert equality
    - Test B: autowire `HealthEndpoint` (or perform `MockMvc` GET `/actuator/health`); assert `Status.UP` and that the components map contains a `redis` entry reporting `UP`
- [x] Document Redis env vars (AC: 1, 2)
  - [x] Append `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` to the local-dev export block in `backend/README.md` (mirroring `DATABASE_URL` style)
- [x] Run verification (AC: 5, 6)
  - [x] `cd backend && mvn test` — confirm 15 prior tests + new Redis test(s) pass
  - [x] `grep -r "gymculture" backend/src/` → empty
  - [x] `docker-compose up -d` locally → confirm `vis-redis` healthy + `redis-cli -h localhost ping` returns `PONG`
  - [x] Start backend with `REDIS_HOST=localhost mvn spring-boot:run`, curl `http://localhost:8080/actuator/health` → `redis` component `UP`
- [x] Update sprint status + Linear mirror issue to In Review (mirror created at dev-story start in project `E1a — Backend Infrastructure` per CLAUDE.md convention; spec issue is GC-66)

## Dev Notes

### Scope discipline — what this story is NOT

This story delivers **Redis as managed infrastructure + health visibility**. It does **NOT** deliver:

- ❌ `RedisMessageListenerContainer` / pub-sub topic beans → Story 5.2 (Pub/Sub fan-out + LWW)
- ❌ `RedisPubSubListener.java` → Story 5.2
- ❌ Session-affinity config for Cloud Run → Story 5.1 (WebSocket endpoint) / infra story
- ❌ Cloud Memorystore provisioning → infra/Terraform story (architecture §Gap Analysis row "Redis production instance")
- ❌ Bucket4j rate-limit backing store on Redis → Story 1a.4 (it may or may not use Redis; deferred to that story)

If you reach for `@EnableRedisRepositories`, a `ChannelTopic`, or any pub/sub primitive, **stop** — that is Story 5.2 territory. This story is the foundation: connection factory + health + dev/test parity.

### Current backend state (verified 2026-05-28)

- Spring Boot **3.5.14** (post-Story 1a.2), Java 21
- `pom.xml` pins: `testcontainers.version=1.19.7`, `flyway.version=10.10.0`, `firebase-admin.version=9.3.0`
- `backend/docker-compose.yml` runs **Postgres 16 only** (no Redis yet)
- `backend/src/main/resources/application.properties` — no Redis section
- `backend/src/test/resources/application-test.properties` — Postgres-only Testcontainers, autoconfig excludes for Security
- `RedisConfig.java` does **not** exist yet (only `CorsConfig`, `FirebaseConfig`, `SecurityConfig` in `config/`)
- Actuator dep already present (`spring-boot-starter-actuator`); `/actuator/health` and `/actuator/info` are public in `SecurityConfig`
- `SecurityConfig` is `@Profile("!test")`; test profile leaves Spring's default health endpoint unsecured — no extra rule needed for Redis to appear in `/actuator/health` in tests

### Spring Boot 3.5 Redis specifics

- `spring-boot-starter-data-redis` brings **Lettuce** by default (Spring Boot has migrated off Jedis as default since 2.0). Do not add Jedis.
- Auto-configured beans when starter is on classpath: `LettuceConnectionFactory`, `RedisTemplate<Object,Object>`, `StringRedisTemplate`, `RedisHealthIndicator`.
- Property prefix is **`spring.data.redis.*`** (NOT `spring.redis.*` — that prefix was deprecated in Boot 2.7 and removed in 3.0). Hex use only `spring.data.redis.host/port/password/timeout`.
- `RedisHealthIndicator` runs a `PING` against the configured connection and reports `UP` / `DOWN`. It auto-registers when starter present and `management.health.redis.enabled=true` (default).
- Boot 3.5.14's managed `spring-data-redis` is 3.4.x, `lettuce-core` is 6.4.x — no manual version pin needed; let the BOM manage.

### Why `RedisConfig.java` if Boot auto-configures everything?

Two reasons the architecture explicitly calls out a `RedisConfig` bean class:

1. **Future home for pub/sub beans** in Story 5.2 — keeping the file present + minimal now means Story 5.2 just adds `RedisMessageListenerContainer` + `ChannelTopic` beans inside the same class.
2. **Explicit `@Profile("!test")` parity with `SecurityConfig`** — keeps prod profile composition explicit and discoverable.

If Boot auto-config gives you everything for now, **`RedisConfig.java` may contain only `@Configuration @Profile("!test") public class RedisConfig {}`** as a placeholder, OR may declare the connection factory explicitly. Prefer the **explicit-bean** form (per AC 2) so prod config is grep-able and not buried in auto-config defaults.

Example explicit form:

```java
package in.vis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
@Profile("!test")
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.timeout:2s}") Duration timeout) {

        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            standalone.setPassword(password);
        }

        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(timeout)
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder().connectTimeout(timeout).build())
                        .build())
                .build();

        return new LettuceConnectionFactory(standalone, client);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
```

Under `test` profile, Spring Boot's auto-config wires the same beans from `spring.data.redis.*` properties (now driven by `@DynamicPropertySource`).

### Testcontainers — why NOT `tc:redis:7:///`

Architecture line 309 says "Add `tc:redis:7:///` to `application-test.properties`". That string is a **conceptual shorthand**, not a literal property. The `tc:` JDBC URL prefix is Testcontainers' JDBC-only mechanism (`ContainerDatabaseDriver`); Redis is not a JDBC datasource, so `spring.data.redis.url=tc:redis:7:///` is not parseable.

Correct pattern: `@Testcontainers` + `@Container static GenericContainer` + `@DynamicPropertySource`. This is the canonical Testcontainers approach for non-JDBC services (Kafka, Redis, RabbitMQ).

Reference test skeleton:

```java
package in.vis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = VisApplication.class)
@ActiveProfiles("test")
@Testcontainers
class RedisIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", REDIS::getHost);
        r.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired StringRedisTemplate redis;
    @Autowired HealthEndpoint healthEndpoint;

    @Test
    void round_trip_set_get() {
        redis.opsForValue().set("vis:test:key", "pong");
        assertThat(redis.opsForValue().get("vis:test:key")).isEqualTo("pong");
    }

    @Test
    void actuator_reports_redis_up() {
        var status = healthEndpoint.healthForPath("redis").getStatus();
        assertThat(status).isEqualTo(Status.UP);
    }
}
```

Note: Testcontainers 1.19.7 ships `GenericContainer` — no extra Maven dep needed beyond the existing `testcontainers-bom`. The `org.testcontainers:testcontainers` artifact is already on the test classpath transitively via `junit-jupiter`. If `GenericContainer` import fails, add `<dependency><groupId>org.testcontainers</groupId><artifactId>testcontainers</artifactId><scope>test</scope></dependency>` explicitly.

### docker-compose.yml delta

Append (do not rewrite Postgres block):

```yaml
  redis:
    image: redis:7-alpine
    container_name: vis-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - vis-redis:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  vis-pg:
  vis-redis:
```

The trailing `volumes:` block already exists with `vis-pg:`; add `vis-redis:` alongside it. Do not duplicate the `volumes:` top-level key.

### application.properties delta

Insert under existing Datasource block:

```properties
# Redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.timeout=2s
```

### application-test.properties — no change needed

Do **not** add Redis properties to `application-test.properties`. `@DynamicPropertySource` in `RedisIntegrationTest` registers host/port at runtime; tests that don't need Redis pay zero cost (the `GenericContainer` only starts in test classes that declare it).

If a future story has Redis usage across many integration tests, extract the `@Testcontainers` + `@DynamicPropertySource` block into an abstract `RedisAwareIntegrationTestBase` and have those tests extend it. Out of scope for this story.

### What NOT to change

- **`Dockerfile`** — unchanged. Connection is env-driven; container reads `REDIS_HOST` at runtime.
- **`backend-ci.yml`** — unchanged. CI runs `mvn test`; Testcontainers handles Redis lifecycle inside the test JVM. No service container needed in the workflow file.
- **`SecurityConfig.java`** — unchanged. `/actuator/health` is already `permitAll()`.
- **Any existing controller/service/model files** — none touch Redis yet.
- **`CorsConfig.java`, `FirebaseConfig.java`** — unchanged.
- **Production Memorystore provisioning** — explicit out-of-scope; lives in infra story.

### Files modified (UPDATE)

- `backend/pom.xml` — add `spring-boot-starter-data-redis` (optionally explicit `org.testcontainers:testcontainers` test dep if `GenericContainer` import fails)
- `backend/docker-compose.yml` — add `redis` service + `vis-redis` named volume
- `backend/src/main/resources/application.properties` — add `spring.data.redis.*` block
- `backend/README.md` — document `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` env vars

### Files created (NEW)

- `backend/src/main/java/in/vis/config/RedisConfig.java`
- `backend/src/test/java/in/vis/RedisIntegrationTest.java`

### Previous story intelligence (Story 1a.2 — Spring Boot 3.5.14)

From Story 1a.2 Completion Notes (2026-05-28):

- Spring Boot 3.5.14 ships **Spring Framework 6.2.18**, **spring-data-* 3.4.x** — auto-configured Redis beans available with current BOM.
- Testcontainers 1.19.7 pin tested OK with Boot 3.5; **do not bump speculatively**. Only revisit if `mvn test` fails (matches deferred-work.md note).
- Dev needs Docker Desktop running locally for Testcontainers to start `redis:7-alpine` and `postgres:16`. CI (`ubuntu-latest`) has Docker pre-installed — no workaround needed.
- `@MockBean` → `@MockitoBean` migration done; for future test work use `@MockitoBean` (org.springframework.test.context.bean.override.mockito), never `@MockBean`.
- Convention: integration tests live under `src/test/java/in/vis/` directly (see `MigrationsIntegrationTest`), not under a `/integration/` subfolder yet.

### Git intelligence (last 5 commits, relevance to this story)

- `ef6aac2` Boot 3.5 upgrade → resolves Spring Data Redis at 3.4.x via BOM, Lettuce 6.4.x. Foundation for this story.
- `db1dd30` / `a77c9e4` CI pipelines done → backend-ci runs `mvn test`; no Redis-specific workflow change needed.
- `97e06fb` / `07b48b1` prototype frontend changes → unrelated; do not touch `prototype/`.

### Latest tech info (2026-05-28)

- `redis:7-alpine` is currently Redis 7.4.x (post-LICENSE change to RSALv2/SSPLv1 by Redis Inc.). For backend Pub/Sub usage we are well within permitted use. If license sensitivity becomes a concern post-v1, drop-in replacements are Valkey 7 (BSD) or KeyDB — same wire protocol, no client change required.
- `spring-boot-starter-data-redis` 3.4.x — no breaking API changes vs prior Boot 3.x for `LettuceConnectionFactory` / `StringRedisTemplate`.
- Testcontainers 1.19.7 + `redis:7-alpine` is a routinely-used combination; Testcontainers' image-pull cache means first run downloads ~5 MB and subsequent runs are warm.

### Project Structure Notes

- New `RedisConfig.java` slots into `in.vis.config/` next to `FirebaseConfig` / `SecurityConfig` / `CorsConfig` — matches the directory tree in architecture.md §Step 6 backend structure.
- Test file `RedisIntegrationTest.java` lives at `backend/src/test/java/in/vis/` (top-level), matching `MigrationsIntegrationTest.java` placement. Do not invent a `redis/` test subpackage.

### References

- Architecture §Step 3 versions table — Spring Boot 3.5.x baseline [`_bmad-output/planning-artifacts/architecture.md:168`]
- Architecture §Step 4 API & Communication — Raw `@ServerEndpoint` + Redis Pub/Sub decision [`_bmad-output/planning-artifacts/architecture.md:232`]
- Architecture §Step 5 Gap Analysis — Memorystore deferred; Testcontainers Redis required [`_bmad-output/planning-artifacts/architecture.md:300, 309`]
- Architecture §Step 6 backend directory tree — `RedisConfig.java` location [`_bmad-output/planning-artifacts/architecture.md:381–388`]
- Epics ARCH-5 + Story 1a.3 ACs [`_bmad-output/planning-artifacts/epics.md:91, 234–249`]
- Cross-story: Story 5.2 Redis Pub/Sub fan-out [`_bmad-output/planning-artifacts/epics.md:1255`]
- Previous story (Boot 3.5.14, Testcontainers 1.19.7 verified) [`_bmad-output/implementation-artifacts/stories/1a-2-spring-boot-3-5-x-upgrade-in-vis-package-migration.md`]
- Deferred-work registry (Testcontainers pin policy) [`_bmad-output/implementation-artifacts/deferred-work.md`]
- Spring Data Redis ref — `spring.data.redis.*` property prefix [`https://docs.spring.io/spring-boot/3.5/reference/data/nosql.html#data.nosql.redis`]
- Testcontainers Redis pattern — `GenericContainer` + `@DynamicPropertySource` [`https://java.testcontainers.org/modules/databases/redis/`]
- Linear spec issue: GC-66 (E1a project). Per CLAUDE.md, a mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>` (see GC-136 / commit `ef6aac2` for the 1a.2 precedent).

## Dev Agent Record

### Agent Model Used

claude-opus-4-7 (Claude Code)

### Debug Log References

- Local mvn build initially failed on `clean compile` (JDK 23 + maven-compiler-plugin 3.14.1 + Lombok 1.18.46): annotation processor not auto-discovered from classpath. Added explicit `<annotationProcessorPaths>` to `maven-compiler-plugin` configuration in `backend/pom.xml`. CI (JDK 21) was unaffected, but fix is forward-compatible and future-proofs the build for any JDK ≥21.
- Local Docker daemon hung during initial probe (`docker ps` timeout). Force-killed Docker Desktop processes + reopened; daemon came back, Testcontainers Redis + Postgres lifecycles worked normally.
- Final `mvn test` ran on JDK 25.0.2 locally (Maven Toolchains picked latest installed JDK) — passed cleanly. CI pins JDK 21 via `setup-java@v4`.
- Two `ConnectionWatchdog` "Connection reset" warnings appear after `RedisIntegrationTest` completes — Lettuce auto-reconnect attempts post-container-stop. Non-blocking, expected for short-lived test containers.

### Completion Notes List

- All 6 ACs satisfied.
- 17 tests pass (15 prior + 2 new in `RedisIntegrationTest`: `round_trip_set_get`, `actuator_reports_redis_up`).
- `grep -r "gymculture" backend/src/` → empty (exit 1).
- `spring.data.redis.*` properties env-driven via `${REDIS_HOST:localhost}` / `${REDIS_PORT:6379}` / `${REDIS_PASSWORD:}` — no hardcoded values.
- `RedisConfig` placed under `in.vis.config` next to `SecurityConfig` / `FirebaseConfig` / `CorsConfig`. Annotated `@Profile("!test")` mirroring existing pattern.
- `RedisIntegrationTest` uses `@Testcontainers` + `GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379)` + `@DynamicPropertySource` for host/port wiring (per Dev Notes guidance — **not** `tc:redis:7:///` JDBC URL).
- `application-test.properties` left unchanged (Dev Notes guidance).
- Bundled pre-existing Lombok build fix: added `annotationProcessorPaths` to `maven-compiler-plugin` in `pom.xml` (called out separately above; user confirmed before applying).
- Linear mirror issue **GC-137** created in project E1a — Backend Infrastructure (status: In Progress; will flip to In Review at code-review handoff). Spec issue GC-66 unchanged.
- Commit prefix for this story: `VIS-137`.

### File List

- `backend/pom.xml` — UPDATE: added `spring-boot-starter-data-redis` dependency; added `maven-compiler-plugin` `annotationProcessorPaths` block for Lombok.
- `backend/docker-compose.yml` — UPDATE: added `redis` service (`redis:7-alpine`, healthcheck, `vis-redis` named volume).
- `backend/src/main/resources/application.properties` — UPDATE: added `spring.data.redis.host/port/password/timeout` block.
- `backend/src/main/java/in/vis/config/RedisConfig.java` — NEW: explicit `LettuceConnectionFactory` + `StringRedisTemplate` beans, `@Profile("!test")`.
- `backend/src/test/java/in/vis/RedisIntegrationTest.java` — NEW: Testcontainers Redis 7 round-trip + actuator UP assertions.
- `backend/README.md` — UPDATE: documented `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` env vars; quick-start note updated to "Postgres + Redis".

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-28 | Initial implementation: docker-compose Redis service, `RedisConfig`, `RedisIntegrationTest`, env-driven props, README env-var docs. |
| 2026-05-28 | Pom: added `maven-compiler-plugin` `annotationProcessorPaths` (bundled pre-existing Lombok fix). |
| 2026-05-28 | Linear mirror GC-137 created (In Progress) under E1a project; related to spec GC-66. |

### Review Findings

- [x] [Review][Patch] Tighten `actuator_reports_redis_up` to also assert `components` map contains a `redis` entry [`backend/src/test/java/in/vis/RedisIntegrationTest.java`] — applied: `assertThat(health).isNotNull()` now proves component presence before `getStatus()`.
- [x] [Review][Patch] Defensive null check before `getStatus()` on `healthForPath("redis")` [`backend/src/test/java/in/vis/RedisIntegrationTest.java`] — applied: same `isNotNull()` guard.
- [x] [Review][Defer] Production Redis hardening — `requirepass` + bind to loopback + AUTH-enforced compose, plus fail-fast when password is blank outside local profile [`backend/docker-compose.yml`, `application.properties`] — deferred to Cloud Memorystore infra story per Dev Notes scope discipline.
- [x] [Review][Defer] Split `connect-timeout` vs `command-timeout` for finer control [`backend/src/main/java/in/vis/config/RedisConfig.java`] — current code reuses `spring.data.redis.timeout` (2s) for both; works for v1, decouple when tuning under load.
- [x] [Review][Defer] Lettuce SSL/TLS support (`rediss://`) for Cloud Memorystore [`RedisConfig.java`] — deferred to infra story (TLS is required for managed Redis).
- [x] [Review][Defer] Redis AOF persistence or drop the `vis-redis` volume in local compose [`backend/docker-compose.yml`] — RDB snapshots are the default; volume currently buys little. Minor dev-UX.
- [x] [Review][Defer] Circuit-breaker / retry around `StringRedisTemplate` calls (Resilience4j) [pending E5 callsites] — defer to Story 5.2+ when actual pub/sub call paths exist; nothing to wrap today.
- [x] [Review][Defer] Password whitespace + special-character hardening (env-var stripping, `RedisPassword.of(char[])` to avoid String interning of secrets) [`RedisConfig.java`] — production-only concern; defer to Memorystore infra story.
- [x] [Review][Defer] Lettuce shutdown timeout / quiet-period config to prevent slow container SIGTERM → SIGKILL [`RedisConfig.java`] — minor; revisit when Cloud Run deploy story lands.
