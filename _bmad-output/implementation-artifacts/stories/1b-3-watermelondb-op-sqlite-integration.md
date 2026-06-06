# Story 1b.3: WatermelonDB + op-sqlite Integration — trainer-app + client-app

Status: review

## Story

As a developer,
I want WatermelonDB with op-sqlite adapter registered in both apps,
so that all domain data can be stored offline-first before any feature work begins.

## Acceptance Criteria

1. **`@nozbe/watermelondb` and `@op-engineering/op-sqlite` installed** in both apps' `dependencies`.
2. **WatermelonDB Database initialized** with `SQLiteAdapter({ jsi: true })` in `src/db/index.ts` of both apps.
3. **`SyncMeta` smoke-test model** defined with `key` (indexed), `value`, `updated_at` fields in `src/db/SyncMeta.ts`.
4. **Schema declared** in `src/db/schema.ts` at version 1.
5. **Jest test** validates the schema structure (`db.schema.test.ts` passes in both apps).
6. **`npx expo prebuild --clean --no-install` succeeds** with op-sqlite present in `dependencies`. op-sqlite autolinks via RN community CLI — **no Expo plugin entry needed** (op-sqlite ships no `app.plugin.js`).

## Deviation from Epic Spec

Epic AC said: "Given op-sqlite is registered in `app.json` as an Expo plugin / When `npx expo prebuild` runs / Then native SQLite bindings are included."

Reality: `@op-engineering/op-sqlite@^16.2.0` ships no Expo config plugin (`app.plugin.js` absent). Native bindings are picked up via React Native autolinking (`react-native.config.js` of the package), which Expo prebuild respects automatically — without a `plugins[]` entry. Adding `"@op-engineering/op-sqlite"` to `expo.plugins` caused prebuild to crash with `resolveConfigPluginFunction` failure.

Outcome: AC intent met (native bindings link automatically). Story Epic-spec literal AC about plugin registration is technically unsatisfiable for this package version — flagged here.

## Out of Scope

- Real WatermelonDB write/read native integration test (requires running on device/simulator; Jest cannot exercise the JSI op-sqlite adapter). Smoke test asserts schema structure only.
- Domain models beyond `SyncMeta` (Equipment, Set, Session, etc. — owned by feature stories).
- Migrations (schema version 1 is the floor; story 1b.3.1 or later owns migration framework).

## Dev Notes

- `Adapter` config uses `jsi: true` for the modern JSI bridge that op-sqlite provides.
- `onSetUpError` logs failures during the JSI bootstrap; no graceful fallback (offline-first means DB must initialize).
- The schema test uses the structural API (`dbSchema.tables.sync_meta.columns.key.isIndexed`) to validate without running the native adapter.
- Decorators support added via `@babel/plugin-proposal-decorators` (devDep). Currently relied on by `@field`, `@date` decorators in `SyncMeta.ts`. The RN babel preset includes proposal-decorators by default, so no additional `babel.config.js` change required.

## Files Created

trainer-app:
- `src/db/schema.ts` — appSchema(version 1) with `sync_meta` table.
- `src/db/SyncMeta.ts` — Model with `@field('key')`, `@field('value')`, `@date('updated_at')`.
- `src/db/index.ts` — Database init with op-sqlite JSI adapter.
- `__tests__/db.schema.test.ts` — 2 tests; both pass.

client-app: identical scaffold.

## Files Modified

- `trainer-app/package.json` — added `@nozbe/watermelondb@^0.28.0`, `@op-engineering/op-sqlite@^16.2.0`, devDep `@babel/plugin-proposal-decorators`.
- `client-app/package.json` — same.
- Both apps' `package-lock.json` — regenerated.
- Both apps' `ios/` + `android/` — regenerated via `npx expo prebuild --clean --no-install` (op-sqlite autolinks).
- `_bmad-output/implementation-artifacts/sprint-status.yaml` — `1b-3-...: backlog → review`.

## Change Log

| Date | Change |
|------|--------|
| 2026-06-06 | Story created + implemented in single dev session under blanket user auth. Status `backlog → review`. Epic-spec deviation: op-sqlite has no Expo config plugin; autolinking covers the native bindings without a `plugins[]` entry. |
