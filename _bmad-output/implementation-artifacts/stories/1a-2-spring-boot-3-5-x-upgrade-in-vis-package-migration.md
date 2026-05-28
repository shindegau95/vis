# Story 1a.2: Spring Boot 3.5.x Upgrade + in.vis.* Package Migration

Status: review

## Story

As a developer,
I want the backend on Spring Boot 3.5.x with all code in `in.vis.*`,
so that we are on current stable Spring Boot with zero gymculture references.

## Acceptance Criteria

1. `pom.xml` parent reflects Spring Boot 3.5.x and all dependency resolution succeeds.
2. `mvn test` passes with zero failures after the upgrade.
3. `grep -r "gymculture" src/` returns empty (zero matches).
4. CI pipeline (`backend-ci.yml`) passes green on the upgraded codebase.

## Tasks / Subtasks

- [x] Verify package migration pre-condition (AC: 3)
  - [x] Run `grep -r "gymculture" backend/src/` — expect zero results (already done pre-story)
  - [x] Confirm all Java packages are `in.vis.*` (main + test)
  - [x] Document confirmation in Completion Notes
- [x] Add `spring-boot-properties-migrator` as runtime dependency (AC: 2)
  - [x] Add `<scope>runtime</scope>` dep to pom.xml
  - [x] Run `mvn test` — review startup output for deprecated property warnings
  - [x] Fix any flagged deprecated properties in `application.properties` or `application-test.properties`
- [x] Bump Spring Boot parent to 3.5.x (AC: 1)
  - [x] Edit `pom.xml` parent `<version>` from `3.3.5` to the latest stable 3.5.x (run `mvn versions:display-parent-updates` to find it, target ≥3.5.9)
  - [x] Run `mvn dependency:tree` to verify no resolution conflicts
- [x] Validate upgrade (AC: 2, 4)
  - [x] Run `mvn test` — all tests must pass
  - [x] Fix any compilation errors or test failures caused by 3.3→3.5 API changes
  - [x] Remove `spring-boot-properties-migrator` dependency
  - [x] Run `mvn test` again (final clean run without migrator)
- [x] Optionally migrate `@MockBean` → `@MockitoBean` (AC: 2)
  - [x] Check if `org.springframework.test.context.bean.override.mockito.MockitoBean` is available (Spring Boot 3.4+ ships Spring Framework 6.2)
  - [x] If available, replace `@MockBean` in `AuthControllerIntegrationTest.java` and `BranchControllerIntegrationTest.java`
  - [x] Run `mvn test` to confirm
- [x] Update sprint status + Linear issue GC-136 to In Review

## Dev Notes

### Pre-condition: Package Migration Already Complete

**CRITICAL: The `in.vis.*` package migration is pre-done. Do NOT re-migrate.**

Verified state (confirmed 2026-05-28):
- All 19 main Java files: `package in.vis.*` ✓
- All 5 test Java files: `package in.vis.*` ✓
- `pom.xml` `groupId`: `in.vis` ✓
- `grep -r "gymculture" backend/src/` → zero matches ✓

Task for dev agent: run the grep, confirm zero, note in Completion Notes, mark task done. That is all.

### Spring Boot Upgrade: 3.3.5 → 3.5.x

**Only file to modify for the upgrade: `backend/pom.xml`**

Current parent block (line 7–12 of pom.xml):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
    <relativePath/>
</parent>
```

Target: change `3.3.5` → latest stable `3.5.x`. Find it:
```bash
cd backend
mvn versions:display-parent-updates -DallowMajorUpdates=false
```
Pick the highest `3.5.x` listed. Per architecture doc (verified 2026-05-24), latest known is `3.5.11`. If `mvn versions:display-parent-updates` shows newer, use that.

**DO NOT upgrade to 4.0.x** — architecture explicitly defers Spring Boot 4.0 post-v1. The `-DallowMajorUpdates=false` flag prevents accidental 4.0 resolution.

### Explicitly-Pinned Dependencies — Verify Compatibility

pom.xml pins these outside Boot BOM management:
- `testcontainers.version=1.19.7` — Spring Boot 3.5 BOMs ship a newer Testcontainers version. If `mvn test` fails due to Testcontainers, bump this property to the version Boot 3.5 recommends (run `mvn dependency:tree | grep testcontainers` post-upgrade to see managed version). Do not upgrade speculatively — only if tests fail.
- `firebase-admin.version=9.3.0` — no known Spring Boot 3.5 conflict; leave unchanged.
- `flyway.version=10.10.0` — Spring Boot 3.5 BOM manages a Flyway version too; since we pin explicitly this takes precedence. Leave unchanged unless `mvn test` shows a Flyway conflict.

### spring-boot-properties-migrator Workflow

Temporary dependency to detect deprecated property names. Steps:

1. Add to pom.xml `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
    <scope>runtime</scope>
</dependency>
```
2. Run `mvn test` — look for lines like `WARN ... Property 'X' has been replaced by 'Y'` in startup logs.
3. Fix flagged properties in `application.properties` and `application-test.properties`.
4. **Remove the migrator dependency** before committing — it is a diagnostic tool only.

Known properties in our files to watch: none are expected to be deprecated (the current config is minimal). If warnings appear, fix them; if none appear, proceed.

### Breaking Changes: 3.3 → 3.5 Impact on This Codebase

| Change | Impact on us | Action |
|---|---|---|
| `@MockBean` deprecated since 3.4 (removed in 4.0) | Low — compiles fine in 3.5, only deprecation warnings | Optionally migrate to `@MockitoBean` (see task below) |
| `enableByDefault()` on Actuator annotations deprecated | None — not used | No action |
| Virtual threads opt-in | None — not enabling | No action |
| Security DSL changes | None — our `SecurityConfig` uses stable `HttpSecurity` DSL | No action |

**SecurityConfig and FirebaseAuthFilter are unchanged** — the security config pattern (stateless, custom filter before `UsernamePasswordAuthenticationFilter`) is stable across Spring Security 6.1–6.4 (covered by Boot 3.3–3.5).

### @MockBean → @MockitoBean Migration (Optional)

Two test files use `@MockBean` (deprecated since Spring Boot 3.4):
- `AuthControllerIntegrationTest.java:25` — `@MockBean private UserService userService`
- `BranchControllerIntegrationTest.java:29` — `@MockBean private BranchRepository branchRepository`

**Migration steps (if Spring Framework 6.2 is available in the resolved Boot 3.5 deps):**
- Replace import: `org.springframework.boot.test.mock.mockito.MockBean` → `org.springframework.test.context.bean.override.mockito.MockitoBean`
- Replace annotation: `@MockBean` → `@MockitoBean`
- Run `mvn test` to confirm

If `MockitoBean` import does not resolve, skip — `@MockBean` still works in 3.5 (not removed until 4.0). This is a forward-looking cleanup.

### What NOT to Change

- **Dockerfile** — unchanged. The Dockerfile runs its own `mvn -B -q package -DskipTests`; it will pull the new parent during its own build. No edits needed.
- **CI workflows** — `backend-ci.yml` runs `mvn test` and `mvn package -DskipTests` without version-locking Spring Boot; the pom.xml bump is sufficient.
- **Any Java source files** — unless `@MockBean` migration is pursued.
- **application.properties / application-test.properties** — unless migrator reports deprecated keys.

### Test Verification Command

After all changes, run this locally to confirm the final state:
```bash
cd backend
mvn test
grep -r "gymculture" src/   # must return empty
```

Both must succeed cleanly before marking story done.

### Project Structure Notes

Files modified:
- `backend/pom.xml` (UPDATE — parent version bump, optional @MockBean cleanup)

Files optionally modified:
- `backend/src/test/java/in/vis/controller/AuthControllerIntegrationTest.java` (UPDATE — @MockBean → @MockitoBean)
- `backend/src/test/java/in/vis/controller/BranchControllerIntegrationTest.java` (UPDATE — @MockBean → @MockitoBean)

Files NOT modified:
- All other Java files (packages already `in.vis.*`)
- `application.properties`, `application-test.properties` (unless migrator flags issues)
- `backend/Dockerfile`, `.github/workflows/backend-ci.yml`

### References

- Architecture §Step 3: Current versions table — Spring Boot 3.3.5 → 3.5.x, command `mvn versions:update-parent` [`_bmad-output/planning-artifacts/architecture.md:168–194`]
- Architecture §Cross-cutting concerns: `in.vis.*` everywhere, zero gymculture [`_bmad-output/planning-artifacts/architecture.md:129`]
- Epics Story 1a.2 ACs [`_bmad-output/planning-artifacts/epics.md:214–231`]
- Previous story file (patterns, CI verification) [`_bmad-output/implementation-artifacts/stories/1a-1-gh-actions-ci-cd-pipelines.md`]
- Spring Boot 3.5 properties migrator docs [`https://docs.spring.io/spring-boot/3.5/appendix/deprecated-application-properties/index.html`]
- Linear issue: GC-65

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

- Package migration pre-confirmed: all 19 main + 5 test Java files already in `in.vis.*`; `grep -r "gymculture" src/` returned zero matches.
- `mvn versions:display-parent-updates` identified latest 3.5.x as `3.5.14`; bumped parent from `3.3.5` → `3.5.14`.
- Properties migrator added, ran `mvn test` — zero deprecated property warnings. Migrator removed before commit.
- Full `mvn test` (15/15) passes after upgrade. Docker daemon needed to be started locally for Testcontainers; CI (ubuntu-latest) has Docker pre-installed so `MigrationsIntegrationTest` will pass in CI without any workaround.
- Spring Boot 3.5.14 ships Spring Framework 6.2.18. Migrated both controller tests from deprecated `@MockBean` (spring-boot-test) → `@MockitoBean` (spring-test `org.springframework.test.context.bean.override.mockito`). All 15 tests pass post-migration.
- No other breaking changes found: `SecurityConfig` DSL stable, Flyway 10.10.0 compatible, Testcontainers 1.19.7 compatible, Firebase Admin 9.3.0 unchanged.

### File List

- `backend/pom.xml` (updated — parent version 3.3.5 → 3.5.14)
- `backend/src/test/java/in/vis/controller/AuthControllerIntegrationTest.java` (updated — @MockBean → @MockitoBean)
- `backend/src/test/java/in/vis/controller/BranchControllerIntegrationTest.java` (updated — @MockBean → @MockitoBean)
