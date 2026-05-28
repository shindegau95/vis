# Deferred Work

## Deferred from: code review of 1a-3-redis-integration-local-dev-testcontainers (2026-05-28)

- **Production Redis hardening** — `backend/docker-compose.yml`, `backend/src/main/resources/application.properties`: add `requirepass`, bind to loopback, fail-fast when `REDIS_PASSWORD` blank outside local profile. Defer to Cloud Memorystore infra story (architecture §Gap Analysis — Memorystore deferred).
- **Split connect-timeout vs command-timeout** — `backend/src/main/java/in/vis/config/RedisConfig.java`: current code reuses `spring.data.redis.timeout` (2s) for both `connectTimeout` and `commandTimeout`. Decouple when tuning under load.
- **Lettuce SSL/TLS support** — `backend/src/main/java/in/vis/config/RedisConfig.java`: add `spring.data.redis.ssl.enabled` toggle and `.useSsl()` on the Lettuce client config. Required for Cloud Memorystore TLS.
- **Redis dev persistence** — `backend/docker-compose.yml`: either drop the `vis-redis` volume (default Redis 7 RDB snapshots add little) or enable AOF via `command: ["redis-server","--appendonly","yes"]`. Minor dev-UX.
- **Resilience4j circuit-breaker around `StringRedisTemplate`** — pending E5 / Story 5.2 callsites: wrap pub/sub usage in CircuitBreaker or catch `RedisConnectionFailureException`. Nothing to wrap today.
- **Password whitespace + special-char hardening** — `backend/src/main/java/in/vis/config/RedisConfig.java`: trim/validate env-var input; switch to `RedisPassword.of(char[])` to avoid `String` interning of secrets. Production concern; defer to Memorystore infra story.
- **Lettuce shutdown timeout / quiet-period** — `backend/src/main/java/in/vis/config/RedisConfig.java`: `.shutdownTimeout(Duration.ofMillis(100)).shutdownQuietPeriod(Duration.ZERO)` to prevent slow container SIGTERM → SIGKILL. Revisit at Cloud Run deploy story.

## Deferred from: code review of 1a-2-spring-boot-3-5-x-upgrade-in-vis-package-migration (2026-05-28)

- **Testcontainers 1.19.7 pin stale** — `backend/pom.xml:23`: Boot 3.5 BOM manages a newer Testcontainers version; explicit pin takes precedence. Tests pass at 1.19.7. Revisit when upgrading Testcontainers intentionally or if tests start failing.

## Deferred from: code review of 1a-1-gh-actions-ci-cd-pipelines (2026-05-28)

- **No timeout on `ng test`** — `.github/workflows/admin-web-ci.yml`: add `timeout-minutes` to prevent hung Karma runner from blocking CI indefinitely.
- **Docker COPY glob `target/*.jar` silent fail** — `backend/Dockerfile`: glob silently picks wrong JAR if multiple JARs exist in target/; consider `COPY target/vis-*.jar app.jar` with explicit name prefix.
- **Testcontainers image not pinned** — `backend/src/test/resources/application-test.properties`: `postgres:16` pulls latest 16.x patch; pin to `postgres:16.3` (or similar) for reproducibility.
- **Double Maven compile** — `.github/workflows/backend-ci.yml`: `mvn test` compiles and runs tests; `mvn package -DskipTests` recompiles. Consider `mvn verify` or separate compile phase to avoid redundant work (non-blocking, minor CI time).
