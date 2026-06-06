# Story 1b.2: EAS Build CI Pipelines — trainer-app + client-app

Status: review

## Story

As a developer,
I want `trainer-app-eas.yml` and `client-app-eas.yml` GH Actions pipelines,
so that every PR triggers an EAS Build profile check and merges to `main` produce development builds without local infra.

## Acceptance Criteria

1. **`eas.json` present in both apps** with `preview` / `development` / `production` build profiles. CLI version pinned `>= 17.0.0`. `appVersionSource: "remote"` so EAS owns version bumping.
2. **`trainer-app-eas.yml` + `client-app-eas.yml` workflows** under `.github/workflows/`.
3. **PR trigger** runs `eas build --profile preview --non-interactive --no-wait --platform all`. `--no-wait` returns immediately with the build URL; CI does not block on EAS queue time.
4. **`main` trigger** runs `eas build --profile development --non-interactive --no-wait --platform all`.
5. **`test` job** runs `npm ci` + `npm test -- --ci` before the EAS Build step so broken tests block the build queue.
6. **Path filters** scope each workflow to its app folder + the workflow file itself — no cross-trigger on the other app.
7. **`EXPO_TOKEN` secret** consumed via `expo/expo-github-action@v8` — user is responsible for setting the secret in repo Settings → Secrets and variables → Actions (not in scope for this story; documented in commit message).

## Out of Scope

- TestFlight / Play Store submission (covered by `eas submit` profile; wired but not invoked).
- Build artifacts upload to GCS / Slack notification.
- Caching `node_modules/.cache` between runs.
- Setting `EXPO_TOKEN` itself — user-owned secret.

## Dev Notes

- `eas.json` `appVersionSource: "remote"` lets EAS own `expo.version` autoincrement; local commits stay at `0.1.0`.
- `--no-wait` is critical — full EAS builds take 15-30 min, which exceeds GH Actions free-tier budget per run. The PR workflow's value is "did EAS accept the build request?", not "is the binary signed?".
- `expo/expo-github-action@v8` is the canonical first-party action for installing EAS CLI in CI.
- Workflows will FAIL on first run until `EXPO_TOKEN` repo secret is added — accepted and documented.

## Dev Agent Record

### Files Created

- `trainer-app/eas.json` — preview / development / production profiles.
- `client-app/eas.json` — same.
- `.github/workflows/trainer-app-eas.yml` — test job + eas-build job (PR=preview, main=development), `--no-wait`.
- `.github/workflows/client-app-eas.yml` — same shape, client-app scope.

### Files Modified

- `_bmad-output/implementation-artifacts/sprint-status.yaml` — `1b-2-...: backlog → review`.

### Completion Notes

- PR + `main` triggers wired. Path-scoped to each app folder.
- `EXPO_TOKEN` GH secret is a one-time user setup — not blocking this commit; workflows will fail until then.
- Native EAS Build execution is out of scope for the dev session (no EXPO_TOKEN available).

### Change Log

| Date | Change |
|------|--------|
| 2026-06-06 | Story created + implemented in single dev session under blanket auth. Status `backlog → review`. |
