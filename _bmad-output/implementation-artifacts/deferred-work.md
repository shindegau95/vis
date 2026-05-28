# Deferred Work

## Deferred from: code review of 1a-1-gh-actions-ci-cd-pipelines (2026-05-28)

- **No timeout on `ng test`** — `.github/workflows/admin-web-ci.yml`: add `timeout-minutes` to prevent hung Karma runner from blocking CI indefinitely.
- **Docker COPY glob `target/*.jar` silent fail** — `backend/Dockerfile`: glob silently picks wrong JAR if multiple JARs exist in target/; consider `COPY target/vis-*.jar app.jar` with explicit name prefix.
- **Testcontainers image not pinned** — `backend/src/test/resources/application-test.properties`: `postgres:16` pulls latest 16.x patch; pin to `postgres:16.3` (or similar) for reproducibility.
- **Double Maven compile** — `.github/workflows/backend-ci.yml`: `mvn test` compiles and runs tests; `mvn package -DskipTests` recompiles. Consider `mvn verify` or separate compile phase to avoid redundant work (non-blocking, minor CI time).
