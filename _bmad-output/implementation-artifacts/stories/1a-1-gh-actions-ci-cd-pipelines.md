# Story 1a.1: GH Actions CI/CD Pipelines

Status: ready-for-dev

## Story

As a developer,
I want automated CI pipelines for all backend, admin-web, and contract-test stacks,
so that every PR is validated before merge and branch protection is enforced.

## Acceptance Criteria

1. `backend-ci.yml` runs on push/PR: `mvn test` → `mvn package -DskipTests` → Docker build — all must pass.
2. `admin-web-ci.yml` runs on push/PR: `ng test --watch=false` → `ng build --configuration production` — all must pass.
3. `pact-verify.yml` stub exists and passes with no contracts yet.
4. All three pipelines pass on current repo HEAD with zero code changes.
5. Branch protection enforced: all pipelines must be green before merge to `main`.

**Out of scope for this story:** EAS Build pipelines for trainer-app/client-app — those are Story 1b.2.

## Tasks / Subtasks

- [ ] Create `.github/workflows/backend-ci.yml` (AC: 1, 4)
  - [ ] Trigger: `push` and `pull_request` on all branches
  - [ ] Steps: checkout → Java 21 setup → Maven cache → `mvn test` → `mvn package -DskipTests` → Docker build (no push)
  - [ ] Set `SPRING_PROFILES_ACTIVE=test` so Testcontainers config is active; no real DB needed
  - [ ] Confirm `MigrationsIntegrationTest`, controller/, filter/, service/ tests all pass in GH Actions Ubuntu runner
- [ ] Create `.github/workflows/admin-web-ci.yml` (AC: 2, 4)
  - [ ] Trigger: `push` and `pull_request` on all branches
  - [ ] Steps: checkout → Node 20 setup → `npm ci` in `admin-web/` → `ng test --watch=false --browsers=ChromeHeadless` → `ng build --configuration production`
  - [ ] Use `working-directory: admin-web` for all steps
- [ ] Create `.github/workflows/pact-verify.yml` stub (AC: 3, 4)
  - [ ] Stub only: single job that echoes "No Pact contracts yet — stub passes" and exits 0
  - [ ] Trigger: `push` and `pull_request` on all branches
- [ ] Enable GitHub branch protection rule on `main` requiring all 3 workflows to pass (AC: 5)

## Dev Notes

### What already exists

- `.github/workflows/deploy-prototype.yml` — prototype GH Pages deploy. **Do not modify it.** New workflows are additions, not replacements.
- `backend/Dockerfile` — multi-stage build (maven:3.9-eclipse-temurin-21-alpine → eclipse-temurin:21-jre-alpine). The Docker build step in CI is `docker build -t vis-backend .` from the `backend/` directory; no push needed in CI.
- `backend/docker-compose.yml` — PostgreSQL only (no Redis yet; Redis added in Story 1a.3). CI uses Testcontainers, not docker-compose.
- `backend/src/main/resources/application-test.properties` — Testcontainers override profile. Tests annotated with `@ActiveProfiles("test")` use this; CI must set profile so the right DataSource URL activates.

### Backend CI — critical details

**Java version:** Java 21 (`actions/setup-java@v4` with `distribution: 'temurin'`, `java-version: '21'`).

**Maven cache:** Use `cache: 'maven'` on `actions/setup-java` to cache `~/.m2`. This avoids re-downloading deps on every run.

**Firebase in tests:** Controller tests use `@MockBean FirebaseAuthFilter` — no real Firebase creds needed. Do NOT add Firebase service account secrets to CI for this story.

**Testcontainers in GH Actions:** Ubuntu runners have Docker pre-installed. Testcontainers will auto-pull `postgres:16` during the test run. No `docker-compose up` needed.

**Maven commands order (match AC exactly):**
```bash
mvn test                   # runs all tests (unit + integration via Testcontainers)
mvn package -DskipTests    # produces JAR in target/
```

**Docker build:** After `mvn package`, run:
```bash
docker build -t vis-backend .
```
from `backend/` working directory. The Dockerfile runs its own internal `mvn package -DskipTests` — this duplication is acceptable per the story AC. Do not try to reuse the CI-built JAR inside Docker; leave Dockerfile unchanged.

**Environment variables for CI:**
- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` are overridden by Testcontainers in `application-test.properties` — no need to set them as GH secrets.
- `FIREBASE_CREDENTIALS_PATH` and `FIREBASE_PROJECT_ID` — mock in tests, but `application.properties` reads them as `${FIREBASE_CREDENTIALS_PATH}`. Set dummy values as env vars in the workflow to prevent Spring Boot from failing to start:
  ```yaml
  env:
    FIREBASE_CREDENTIALS_PATH: /dev/null
    FIREBASE_PROJECT_ID: test-project
  ```

### Admin Web CI — critical details

**Node version:** 20 (`actions/setup-node@v4` with `node-version: 20`, `cache: 'npm'`, `cache-dependency-path: admin-web/package-lock.json`).

**Karma + headless Chrome:** Angular 17 uses Karma/Jasmine. GH Actions Ubuntu runners have Chrome but no display. Must pass `--browsers=ChromeHeadless`:
```bash
ng test --watch=false --browsers=ChromeHeadless
```

**Working directory:** All `run` steps under the `admin-web/` job must use `working-directory: admin-web`.

**`ng build` output:** Angular 17 outputs to `admin-web/dist/`. Do not upload artifacts in this story — build verification only.

### Pact stub

```yaml
jobs:
  pact-verify:
    runs-on: ubuntu-latest
    steps:
      - run: echo "No Pact contracts yet — stub passes"
```

No checkout needed. Passes immediately. Will be replaced when contract tests are introduced (post-E1d).

### Branch protection

After all 3 workflows exist and pass on `main`:
- Go to GitHub repo → Settings → Branches → Add rule for `main`
- Enable "Require status checks to pass before merging"
- Add checks: `backend-ci / build`, `admin-web-ci / build`, `pact-verify / pact-verify`
- The status check names must match the GH Actions **job IDs** exactly (not workflow names).

### Project Structure Notes

Files to create (all NEW — no existing files modified):
```
.github/
  workflows/
    backend-ci.yml       NEW
    admin-web-ci.yml     NEW
    pact-verify.yml      NEW
```

Existing `deploy-prototype.yml` remains unchanged.

### References

- Existing Dockerfile: `backend/Dockerfile`
- Existing docker-compose: `backend/docker-compose.yml` (PostgreSQL only, no Redis)
- Existing workflow pattern: `.github/workflows/deploy-prototype.yml` (uses `actions/checkout@v4`, `actions/setup-node@v4`)
- Architecture §Step 6 monorepo structure: `.github/workflows/` filenames locked
- Architecture §Step 4 CI/CD: "GH Actions + EAS Build" locked; EAS pipelines = Story 1b.2
- Epics Story 1a.1 ACs: `_bmad-output/planning-artifacts/epics.md` §Epic E1a Story 1a.1
- CLAUDE.md build commands: `mvn test`, `ng test --watch=false`, `ng build --configuration production`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
