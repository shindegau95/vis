# Story 1b.4: MMKV Setup + Auth Token Storage — trainer-app + client-app

Status: review

## Story

As a developer,
I want MMKV initialized in both apps for auth tokens and feature flags only,
so that sensitive auth state is not stored in WatermelonDB (ARCH-14 constraint: WatermelonDB for domain data; MMKV for auth tokens + feature flags only).

## Acceptance Criteria

1. **`react-native-mmkv@^4.3.1` installed** in both apps' `dependencies`.
2. **MMKV-backed secure-store utility** at `src/storage/secure-store.ts` exposes:
   - `setFirebaseIdToken(token)` / `getFirebaseIdToken()`.
   - `setFirebaseRefreshToken(token)` / `getFirebaseRefreshToken()`.
   - `clearAuthTokens()` to wipe both keys on sign-out.
3. **Separate MMKV instance IDs** per app (`vis-trainer-secure` / `vis-client-secure`) so the two apps installed side-by-side on a dev's device do not share auth state.
4. **Jest test** asserts round-trip + clear behavior via a mocked `react-native-mmkv` Map adapter.
5. **Tokens NEVER stored in WatermelonDB or AsyncStorage.** No `import AsyncStorage` anywhere in `src/`. No `database.collections.get('auth_tokens')` model. (Verified by grep — see commit message.)

## Out of Scope

- Wiring the secure store into `apiService.ts` Axios interceptor → owned by Story 1d.1 (Firebase Auth integration).
- Feature flag storage layer (MMKV is the substrate; flag schema + helpers land in a later infra story).
- Encryption of the MMKV file at rest (MMKV ships AES-256 GCM out of the box; defaults are fine — explicit `encryptionKey` config deferred unless DPDP/GDPR review surfaces it).
- Biometric gate on token read (deferred to Story 1d.5 — DPDPA/GDPR compliance).

## Dev Notes

- MMKV uses JSI on RN 0.70+ — zero bridge cost, sync reads. Suitable for the hot path (every API request reads the ID token).
- v4 of `react-native-mmkv` requires React Native 0.74+. We are on 0.85.3 — fine.
- The test mocks MMKV with a `Map<string,string>` so jest can exercise the API surface without the native module.
- Keys are namespaced under `auth.*` so a future `flags.*` namespace doesn't collide.

## Files Created

trainer-app:
- `src/storage/secure-store.ts` — MMKV wrapper with set/get/clear helpers.
- `__tests__/secure-store.test.ts` — 3 tests (round-trip ID, round-trip refresh, clear-both).

client-app: identical scaffold with `vis-client-secure` instance ID.

## Files Modified

- `trainer-app/package.json` — added `react-native-mmkv@^4.3.1`.
- `client-app/package.json` — same.
- Both apps' `package-lock.json` — regenerated.
- `_bmad-output/implementation-artifacts/sprint-status.yaml` — `1b-4-...: backlog → review`.

## Change Log

| Date | Change |
|------|--------|
| 2026-06-06 | Story created + implemented in single dev session under blanket user auth. Status `backlog → review`. |
