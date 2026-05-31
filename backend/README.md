# Vis — Backend

Spring Boot 3.3 (Java 21) REST API. Multi-branch, role-based, Firebase-auth. PostgreSQL 16 via Flyway-managed schema.

## Quick start

```bash
# 1. Bring up Postgres + Redis locally
docker compose up -d

# 2. Export env vars (copy from .env.example)
export DATABASE_URL=jdbc:postgresql://localhost:5432/vis
export DATABASE_USER=vis
export DATABASE_PASSWORD=vis
export FIREBASE_CREDENTIALS_PATH=./firebase-service-account.json
export FIREBASE_PROJECT_ID=your-firebase-project-id
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# 3. Run migrations
mvn flyway:migrate

# 4. Boot the app
mvn spring-boot:run
```

## Migrations

Flyway scans `src/main/resources/db/migration` on startup and replays any unapplied `V*__*.sql` in version order. State tracked in `flyway_schema_history` table.

| Version | What                                                                         |
|---------|------------------------------------------------------------------------------|
| V1      | `branches` table + 7 seed rows (Kandivali, Borivali, Mira Road, Malad East, Orlem, Haridwar, Sundar Nagar) |
| V2      | `user_role` enum + `users` table + CHECK `users_non_owner_has_branch` + indexes |

Run manually outside the app: `mvn flyway:migrate` (uses the same env vars).

## Observability

Counter metrics (`SM-C1`–`SM-C10`) + the `ai.cost.wau_spend` gauge are registered at startup by `in.vis.observability.MetricsRegistrar`. Call sites in domain stories increment them via `meterRegistry.counter(VisMetrics.SM_C2_WS_DISCONNECT).increment()`.

**Scrape endpoint:** `GET /actuator/prometheus` (no auth — Cloud Run's Managed Service for Prometheus scrapes externally; in production behind Cloud Run + Cloud Armor).

**Tracing:** Spring Boot's Micrometer Observation API bridges to OpenTelemetry → OTLP/HTTP exporter. Configure via env vars:

| Var | Default | Effect |
|---|---|---|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | _empty_ | Empty = exporter disabled, no log spam in local dev. Cloud Run sidecar injects the real endpoint. |
| `app.tracing.sampling.probability` | `0.1` | 10% sample rate in prod; tests pin `1.0` for determinism. |
| `OBSERVABILITY_ENV` | `dev` | Tags every meter with `env=<value>` (`dev` / `staging` / `prod`). |

**Cloud Monitoring dashboard:** ship the day-1 template via gcloud:

```bash
gcloud monitoring dashboards create \
  --config-from-file=backend/dashboards/vis-observability.json
```

Dashboard ships SM-C1, SM-C2, SM-C3, SM-C6 panels with threshold lines plus placeholder text panels listing remaining counters' owning epics.

## API Versioning

Every REST endpoint negotiates content via vendor media types (RFC 6838 §3.2).

| Client `Accept`                          | Server `Content-Type`             | Body shape |
|------------------------------------------|-----------------------------------|------------|
| `application/vnd.vis.v1+json`            | `application/vnd.vis.v1+json`     | v1         |
| `application/vnd.vis.v2+json`            | `application/vnd.vis.v2+json`     | v2         |
| `application/json`                       | `application/vnd.vis.v1+json`     | v1 (default) |
| `*/*` or header missing                  | `application/vnd.vis.v1+json`     | v1 (default) |
| `application/vnd.vis.v3+json` or any unsupported version | — (`406 Not Acceptable`, empty body) | — |

`/actuator/**` endpoints stay on plain `application/json` — they are infra/ops, not domain API.

v1 = `in.vis.dto.v1.*`. v2 = `in.vis.dto.v2.*`. Media-type constants live in `in.vis.MediaTypes`. Content-Type rewriting (plain `application/json` → versioned default) is handled by `in.vis.config.V1ContentTypeAdvice` (`ResponseBodyAdvice`). Implemented in Story 1a.5 (GC-68 / VIS-139).

## Tests

```bash
mvn test
```

Default datasource = Testcontainers JDBC URL `jdbc:tc:postgresql:16:///vis` — spins a fresh PostgreSQL 16 container per run, no local DB needed.

### Testcontainers + Docker Engine ≥ 29 known issue

Testcontainers 1.19.7's shaded `docker-java` cannot parse the Docker Engine 29.x `_ping` response (returns empty body where the older API returned a payload), so the auto-discovery fails with `JsonMappingException: No content to map`. Docker CLI itself works fine; only TC's startup probe is affected.

**Workaround until TC ships a docker-java with Engine API ≥ 1.45 support**: point the test datasource at the running `docker compose` Postgres:

```bash
docker compose up -d
TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/vis \
TEST_DATABASE_DRIVER=org.postgresql.Driver \
TEST_DATABASE_USER=vis \
TEST_DATABASE_PASSWORD=vis \
mvn test
```

`application-test.properties` reads `TEST_DATABASE_*` env vars and falls back to the Testcontainers URL when unset.

## Project layout

```
src/main/java/in/vis/
  VisApplication.java     # @SpringBootApplication entry point
src/main/resources/
  application.properties         # env-driven config
  db/migration/V*__*.sql         # Flyway migrations
src/test/resources/
  application-test.properties    # Testcontainers / env-overridable datasource
src/test/java/in/vis/
  MigrationsIntegrationTest.java # branches seed + CHECK constraint
docker-compose.yml               # postgres:16 on :5432
```
