# Story 1c.1: Angular 17 → 21 Upgrade + Karma → Vitest Migration

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an admin-web platform engineer,
I want `admin-web/` upgraded from Angular 17.3 to Angular 21.x (latest stable) and the test runner swapped from Karma + Jasmine to Vitest,
so that every subsequent Epic E1c story (Signals baseline, WCAG shell, theme tokens) and every Epic E2+ admin feature is built on a modern Angular foundation with a fast, ESM-native test runner instead of the deprecated Karma stack.

## Acceptance Criteria

1. **Angular 21 across the board.** `admin-web/package.json` shows every `@angular/*` runtime dependency (`animations`, `common`, `compiler`, `core`, `forms`, `platform-browser`, `platform-browser-dynamic`, `router`) at the resolved `^21.x` major. Devkit packages (`@angular-devkit/build-angular`, `@angular/cli`, `@angular/compiler-cli`) also at `^21.x`. No `@angular/*` package below 21 remains in either `package.json` or `package-lock.json`. Verified via `grep -E '"@angular/[^"]+":' admin-web/package.json` and `npm ls @angular/core --depth=0`.
2. **`ng serve` boots clean.** `cd admin-web && npx ng serve` starts the dev server on `http://localhost:4200`, compiles successfully, and emits zero ERROR-level log lines. Warnings from third-party deps acceptable; any Angular-emitted `NG####` error blocks acceptance. Manual smoke check: the root route renders without a runtime exception in the browser console.
3. **Production build green.** `cd admin-web && npx ng build --configuration production` exits 0 and produces `admin-web/dist/admin-web/` with an `index.html` plus hashed JS/CSS bundles. The current 500 KB initial / 1 MB max budget stays in place (no relaxing budgets to make the build pass — if Angular 21's runtime overshoots, the story must surface that as a finding, not silently bump the budget).
4. **Vitest is the test runner.** `cd admin-web && npm test` runs Vitest (not Karma) and exits 0 with all existing specs (currently just `src/app/app.component.spec.ts`) passing. `npm test` continues to be the developer-facing entrypoint — the underlying runner changes, the command does not. `npx ng test` also works (the `test` builder in `angular.json` is wired to the Vitest builder).
5. **No Karma / Jasmine residue.** `grep -E '"(karma|karma-[^"]+|jasmine-core|@types/jasmine)":' admin-web/package.json` returns empty. `grep -E '"(karma|karma-[^"]+|jasmine-core|@types/jasmine)":' admin-web/package-lock.json` returns empty. `admin-web/karma.conf.js` does not exist (verified — there is no `karma.conf.js` in the current tree, so nothing to delete; the AC stays as a guardrail in case the upgrade schematic regenerates one). `tsconfig.spec.json` no longer references `"jasmine"` in `compilerOptions.types`.
6. **CI stays green.** `.github/workflows/admin-web-ci.yml` push run succeeds after the merge. The current workflow runs `npx ng test --watch=false --browsers=ChromeHeadless` and `npx ng build --configuration production`. The `--browsers=ChromeHeadless` flag is Karma-specific and **must be removed** from the test step (Vitest has no browser-launcher concept; specs run in JSDOM by default). The `--watch=false` flag is acceptable for both runners (Vitest also supports `--watch`/`--run`; document the swap in the dev notes). Workflow update is in-scope for this story.

### Explicitly OUT OF SCOPE for this story (do NOT implement)

- ❌ **Signals + services state architecture baseline.** Story 1c.2 territory. This story MUST land on an Angular version that supports Signals (Angular 17+ already does) but MUST NOT introduce any Signal usage in app code or refactor `app.component.ts` to use Signals.
- ❌ **WCAG 2.1 AA shell — focus management, skip-nav, min-target sizes.** Story 1c.3.
- ❌ **Admin-web light/dark theme token system.** Story 1c.4. Do not touch `src/styles.scss` beyond what the `ng update` schematic does automatically.
- ❌ **Firebase Auth SDK integration / login screens.** Epic E1d Story 1d.1. The `firebase: ^10.14.1` dependency stays at exactly its current major — no Firebase SDK bump, no auth wiring, no login UI.
- ❌ **Any admin feature code.** Epic E2 onwards. The app at the end of this story renders the same shell as before (router-outlet + default Angular welcome).
- ❌ **Migrating `npm test` away as the developer-facing command.** `npm test` MUST still work. Vitest replaces the underlying runner; the developer's muscle memory does not change.
- ❌ **Replacing Angular CLI's `ng serve` with raw Vite.** Vite is the test-only concern via Vitest. `ng serve` stays on Angular CLI's own builder (which in Angular 17+ is already esbuild-based via `@angular-devkit/build-angular:application`). Do not introduce a parallel Vite dev server.
- ❌ **New Angular 17+ syntax adoption** — Signals, deferrable views (`@defer`), new control flow (`@if` / `@for` / `@switch`), standalone-by-default schematics — beyond what `ng update` schematics auto-apply. Refactoring `*ngIf` → `@if` is a separate concern. `app.component.spec.ts` may be updated by the upgrade schematics; manual rewriting of templates is out of scope.
- ❌ **ESLint config introduction / migration.** No `@angular-eslint/*` packages added. No Prettier config. These are separate hygiene concerns.
- ❌ **Replacing `firebase` 10.14 with a newer major.** Pin stays at `^10.14.1`. Firebase 10 supports Angular 17 through 21 (verified in dev notes).
- ❌ **Touching `backend/`, `prototype/`, `client-app/`, or `trainer-app/`.** This story is scoped to `admin-web/` only. If a workspace-level dependency bump is tempting (e.g., shared TypeScript pin), resist — admin-web has its own isolated `package.json`.
- ❌ **Removing `zone.js` for zoneless change detection.** Angular 21 supports zoneless but it requires opt-in (`provideExperimentalZonelessChangeDetection` or its stable equivalent). Stay on zone.js — zoneless is a deliberate architecture decision for Story 1c.2 or later.

## Tasks / Subtasks

- [ ] Pre-flight: capture green baseline (AC: 1, 2, 3, 4)
  - [ ] Snapshot the current `admin-web/package-lock.json` (git already tracks it — confirm working tree clean before starting).
  - [ ] Run `cd admin-web && npm ci` to ensure the lock resolves cleanly today. Record the resolved `@angular/core` version (`npx ng version`).
  - [ ] Run `cd admin-web && npx ng test --watch=false --browsers=ChromeHeadless` — confirm exit 0 + record the spec count (expected: 1 spec from `app.component.spec.ts`).
  - [ ] Run `cd admin-web && npx ng build --configuration production` — confirm exit 0 + record bundle size (initial + main hashed JS).
  - [ ] Document the baseline in a one-liner commit comment so post-upgrade diffs are clear.
- [ ] Audit existing specs before the migration (AC: 4, 5)
  - [ ] `find admin-web/src -name "*.spec.ts" -type f` — current expectation: a single file `src/app/app.component.spec.ts` (verified during story drafting). If MORE specs appear at dev time, list each one and tag the Jasmine globals it uses (`describe`, `it`, `expect`, `beforeEach`, `beforeAll`, `afterEach`, `afterAll`, `spyOn`, `spyOnProperty`, `jasmine.createSpy`, `jasmine.createSpyObj`).
  - [ ] If spec count ≤ 3, plan a manual rewrite. If > 3, plan a sed/codemod pass (single regex per Jasmine API). The current state (1 spec, no `spyOn` usage) means manual rewrite is trivial — but the audit gates that assumption.
- [ ] Confirm latest stable Angular major + verify Node/TS requirements (AC: 1)
  - [ ] `npm view @angular/core dist-tags` — confirm `latest` resolves to a `21.x` build. If `latest` has progressed past 21 to a newer major, stop and consult — this story is scoped to 21.x. (Drafting verified: Angular 21 is the stable as of 2026-05-31.)
  - [ ] `npm view @angular/core@21 peerDependencies` — record the required `typescript` range and the required `zone.js` range. Bump `admin-web/package.json` to match at the end of the upgrade.
  - [ ] `npm view @angular/cli@21 engines` — record the required Node range. CI is on Node 20 (verified `admin-web-ci.yml`); Angular 21 supports Node 20 + 22. If Node 20 is dropped by Angular 21, surface as a CI workflow change (out-of-scope alarm).
- [ ] One-major-at-a-time Angular upgrade via `ng update` (AC: 1, 2, 3)

  The official Angular migration path requires one major hop at a time. Each `ng update` runs schematics that auto-patch breaking changes. Commit after each major so a failed schematic can be reset surgically.

  - [ ] **17 → 18:** `cd admin-web && npx ng update @angular/core@18 @angular/cli@18`
    - [ ] Resolve any schematic prompts (accept defaults unless they introduce out-of-scope changes — e.g., control flow migration is OUT OF SCOPE, decline if prompted).
    - [ ] `npx ng test --watch=false --browsers=ChromeHeadless` — pass. (Karma still active at this point.)
    - [ ] `npx ng build --configuration production` — pass.
    - [ ] Commit: `VIS-<mirror>: admin-web Angular 17 → 18 (ng update)`
  - [ ] **18 → 19:** `cd admin-web && npx ng update @angular/core@19 @angular/cli@19`
    - [ ] Same validation: `ng test`, `ng build`.
    - [ ] Commit: `VIS-<mirror>: admin-web Angular 18 → 19 (ng update)`
  - [ ] **19 → 20:** `cd admin-web && npx ng update @angular/core@20 @angular/cli@20`
    - [ ] Same validation.
    - [ ] Commit: `VIS-<mirror>: admin-web Angular 19 → 20 (ng update)`
  - [ ] **20 → 21:** `cd admin-web && npx ng update @angular/core@21 @angular/cli@21`
    - [ ] Same validation.
    - [ ] Commit: `VIS-<mirror>: admin-web Angular 20 → 21 (ng update)`
  - [ ] After all four hops, confirm `npx ng version` reports Angular CLI 21.x + Angular 21.x.
- [ ] Align TypeScript + zone.js + tslib to Angular 21 peer requirements (AC: 1, 3)
  - [ ] Bump `typescript` in `devDependencies` to the version Angular 21 peer-requires (likely `~5.7.x` or `~5.8.x` — confirm via the recorded `npm view` output from the prior task).
  - [ ] Bump `zone.js` to the matching version (Angular 21 may require `~0.15.x`).
  - [ ] Leave `tslib` alone unless the schematic touched it.
  - [ ] Run `npx tsc --noEmit -p tsconfig.json` to surface any latent type errors the new TS version exposes. Fix only what the upgrade introduced — do NOT refactor unrelated code.
- [ ] Remove Karma + Jasmine devDependencies (AC: 5)
  - [ ] `cd admin-web && npm uninstall karma karma-chrome-launcher karma-coverage karma-jasmine karma-jasmine-html-reporter jasmine-core @types/jasmine`
  - [ ] Verify `package.json` no longer lists any `karma*`, `jasmine*`, or `@types/jasmine` entry.
  - [ ] If `karma.conf.js` exists (it does not in the current tree — verified during drafting), `git rm` it.
- [ ] Install Vitest + Angular adapter (AC: 4)
  - [ ] Resolve the current stable Angular-Vitest adapter via `npm view @analogjs/vitest-angular dist-tags`. As of 2026-05-31, `@analogjs/vitest-angular` is the community-standard adapter for Angular 18+. If an official `@angular/build:vitest` or similar first-party builder ships with Angular 21 (verify in the Angular 21 release notes), prefer that. **Decision must be recorded in `Dev Agent Record` with the resolved package name + version.**
  - [ ] `cd admin-web && npm install -D vitest @vitest/coverage-v8 @analogjs/vitest-angular jsdom @types/node`
    - `vitest` — runner
    - `@vitest/coverage-v8` — V8 coverage (replaces `karma-coverage`)
    - `@analogjs/vitest-angular` (or official equivalent) — Angular TestBed adapter
    - `jsdom` — DOM environment (replaces Karma's real-browser fallback)
    - `@types/node` — Vitest expects Node globals available
  - [ ] If the chosen adapter pulls these as transitive deps, skip the direct install — let the adapter own its peer chain. Document the choice.
- [ ] Wire Vitest into `angular.json` (AC: 4)
  - [ ] Replace the `test` target's `"builder": "@angular-devkit/build-angular:karma"` with the Vitest builder path the chosen adapter provides (`"@analogjs/vitest-angular:test"` for the Analog adapter).
  - [ ] Remove Karma-specific options: drop `"polyfills": ["zone.js", "zone.js/testing"]` from the test target (Vitest setup handles zone.js/testing via the adapter's setup file, not via Angular CLI's polyfill loader). Keep `tsConfig`, `inlineStyleLanguage`, `assets`, `styles` — they remain valid for the Vitest builder.
  - [ ] Add a `"runner": "vitest"` / `"include": ["src/**/*.spec.ts"]` block if the adapter expects it (consult the adapter README at dev time).
- [ ] Create `vitest.config.ts` (AC: 4)
  - [ ] At `admin-web/vitest.config.ts`, configure:
    - `test.environment = 'jsdom'`
    - `test.globals = true` (lets specs use `describe`/`it`/`expect` without per-file imports — matches the Jasmine global model)
    - `test.setupFiles = ['src/test-setup.ts']` (or whatever path the adapter's README recommends)
    - Coverage provider `v8`, reporter `text` + `html`, output dir `coverage/`
  - [ ] At `admin-web/src/test-setup.ts`, import the Angular Vitest setup helpers from `@analogjs/vitest-angular/setup-zone` (or the equivalent) — this initializes `zone.js/testing` and `TestBed` for the JSDOM environment.
  - [ ] Add `vitest.config.ts` and `src/test-setup.ts` to `tsconfig.spec.json`'s `include` array if they aren't picked up by the default glob.
- [ ] Update `tsconfig.spec.json` (AC: 4, 5)
  - [ ] Change `compilerOptions.types` from `["jasmine"]` to `["vitest/globals", "node"]`. Drop the `jasmine` entry entirely.
  - [ ] Confirm the `include` array still resolves `src/**/*.spec.ts`. Add `src/test-setup.ts` if the file is referenced from `vitest.config.ts` only and the TS compiler complains.
- [ ] Migrate existing spec(s) from Jasmine to Vitest globals (AC: 4)
  - [ ] `src/app/app.component.spec.ts` currently uses only `describe`, `beforeEach`, `it`, `expect` — all 1:1 in Vitest globals mode. NO `spyOn`, NO `jasmine.createSpy*`. Likely no rewrite needed beyond verifying the file runs under Vitest.
  - [ ] If `RouterTestingModule` from `@angular/router/testing` was removed/renamed in Angular 21 (verify via `npm view @angular/router@21`), swap to the current equivalent (`provideRouter([])` + `Router` testing utilities, or `RouterModule.forRoot([])` in the test imports). This is the most likely Angular-21-specific spec edit.
  - [ ] For any additional spec discovered in the audit step: replace `spyOn(...)` → `vi.spyOn(...)`, `jasmine.createSpy(...)` → `vi.fn(...)`, `jasmine.createSpyObj(...)` → manual `vi.fn()` map. Add `import { describe, it, expect, beforeEach, vi } from 'vitest'` at the top of any spec where globals are NOT enabled (but if `test.globals = true` is set, the imports are optional).
- [ ] Update `package.json` scripts (AC: 4)
  - [ ] Keep `"test": "ng test"` as-is — `ng test` now invokes the Vitest builder via the updated `angular.json` `test` target. The developer-facing `npm test` command does NOT change.
  - [ ] Add `"test:watch": "ng test --watch"` (Vitest's watch mode).
  - [ ] Add `"test:coverage": "ng test --coverage"` (only if the adapter exposes a `--coverage` flag — otherwise use `"test:coverage": "vitest run --coverage"`).
- [ ] Update `.github/workflows/admin-web-ci.yml` (AC: 6)
  - [ ] Change the test step from `npx ng test --watch=false --browsers=ChromeHeadless` to `npx ng test --watch=false`. Remove `--browsers=ChromeHeadless` — Vitest does not use browser launchers; it runs in JSDOM. If the Vitest builder ignores the `--watch=false` flag, swap to `npx vitest run` and document in the dev notes.
  - [ ] Leave the `Set up Node 20` step alone — Angular 21 supports Node 20 per the verified peer-deps check.
  - [ ] Leave the `Build production` step alone.
  - [ ] Do NOT add caching for Vitest's `node_modules/.vitest-cache` directory — premature optimization; revisit if CI runtime becomes a bottleneck.
- [ ] Update `admin-web/README.md` (AC: 4)
  - [ ] Refresh the `## Running unit tests` section: replace "Karma test runner" with "Vitest test runner".
  - [ ] Add a `## Test commands` block listing `npm test`, `npm run test:watch`, `npm run test:coverage`.
  - [ ] Note Node ≥ 20 + Angular 21 baseline up top.
- [ ] End-to-end verification (AC: 1, 2, 3, 4, 5, 6)
  - [ ] `cd admin-web && rm -rf node_modules dist && npm ci` — confirm clean install resolves with zero peer-dep warnings (warnings from unrelated transitive deps acceptable; any `@angular/*` peer mismatch blocks).
  - [ ] `cd admin-web && npx ng version` — confirm Angular CLI 21.x + Angular 21.x.
  - [ ] `cd admin-web && npm test` — exit 0, ≥ 1 spec passing, runtime visibly shorter than the Karma baseline (Vitest is typically 5-10× faster than Karma).
  - [ ] `cd admin-web && npx ng build --configuration production` — exit 0, `dist/admin-web/` populated.
  - [ ] `cd admin-web && npx ng serve` — boots on 4200, no `NG####` errors. Open browser, confirm the root route renders.
  - [ ] `grep -E '"(karma|karma-[^"]+|jasmine-core|@types/jasmine)":' admin-web/package.json admin-web/package-lock.json` — empty.
  - [ ] Push branch → confirm `admin-web-ci.yml` workflow run is green.
- [ ] Update sprint status + Linear mirror to In Review
  - [ ] Flip `1c-1-angular-17-21-upgrade-karma-vitest-migration: ready-for-dev` → `in-progress` at start of dev, → `review` on dev done. (Sprint-status flip to `ready-for-dev` happens at story-creation time — handled by this story-creation pass.)
  - [ ] Linear mirror created at dev-story start in project `E1c — Admin-Web Infrastructure` per CLAUDE.md convention. Spec issue is GC-75. Move mirror In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>`.

## Dev Notes

### Scope discipline — what this story IS and IS NOT

**IS:**
- Angular 17.3 → 21.x runtime + devkit upgrade across `@angular/*` packages.
- TypeScript pin bump to whatever Angular 21 peer-requires (likely `~5.7.x` / `~5.8.x`).
- `zone.js` bump to the Angular 21-compatible minor.
- Karma + Jasmine devDeps removed; Vitest + Angular adapter (`@analogjs/vitest-angular` or first-party equivalent) added.
- `angular.json` `test` target rewired to the Vitest builder.
- `tsconfig.spec.json` types switched from `jasmine` → `vitest/globals` + `node`.
- New `vitest.config.ts` + `src/test-setup.ts`.
- Existing `app.component.spec.ts` migrated (likely zero-touch beyond verifying it runs).
- `.github/workflows/admin-web-ci.yml` test step de-Karma-ized (`--browsers=ChromeHeadless` removed).
- `admin-web/README.md` refresh.

**IS NOT:**
- Signal-based components, services, or state. (Story 1c.2.)
- WCAG focus / skip-nav / min-target work. (Story 1c.3.)
- Light/dark theme tokens. (Story 1c.4.)
- Firebase Auth wiring or login UI. (Epic E1d.)
- Any admin feature. (Epic E2+.)
- `ng serve` replaced with raw Vite.
- `@if` / `@for` control flow migration of existing templates (unless an `ng update` schematic auto-applies it — accept that as side-effect, don't manually expand).
- ESLint / Prettier introduction.
- Firebase SDK 10.14 bump.
- Touching any non-`admin-web/` workspace.
- Zoneless change detection.

If you reach for `@angular-eslint`, `prettier`, `@angular/fire`, `provideExperimentalZonelessChangeDetection`, or a Firebase SDK 11 / 12 bump — **stop**, that's outside this story.

### Why Angular 21 specifically

- Angular 21.x is the current stable release as of 2026-05-31 (verify with `npm view @angular/core dist-tags` at dev time — if it has advanced, stop and re-scope before continuing).
- Angular 17 → 21 is FOUR major hops. Angular's official migration policy requires one major at a time so schematics apply correctly: `17 → 18 → 19 → 20 → 21`. Skipping majors (e.g., `ng update @angular/core@21 @angular/cli@21` from a 17 baseline) is unsupported and will either refuse to run or leave the project in an inconsistent state.
- Each `ng update` runs the schematics shipped with that major. Examples of past auto-migrations the schematics handle: standalone-component conversion (16→17), required `inject()` / `@Component` typing tweaks (17→18), control flow migration is offered as an OPT-IN flag (18→19) — decline if prompted (out of scope), the schematic still completes without it.
- Angular 21 raises the TypeScript baseline (verify exact range via `npm view @angular/core@21 peerDependencies` — likely `~5.7.x` or newer). The current `typescript: ~5.4.2` MUST bump.
- Angular 21 raises the Node baseline to **Node ≥ 20** (verified via release notes pattern — Angular 19 already required Node 20). CI is already on Node 20; no workflow change required. Local-dev `nvm` users should bump too — flag in `admin-web/README.md`.

### Why Vitest over Karma (and over Jest)

- **Karma is in maintenance mode** — the Karma project has been formally deprecated by its maintainers since 2023. Angular continues to ship the `@angular-devkit/build-angular:karma` builder for backward compatibility but new project schematics no longer default to Karma. Continuing on Karma means lockstep with a deprecated runner and the Chrome browser-launcher dance in CI.
- **Vitest is ESM-native and Vite-powered** — startup is 5-10× faster than Karma in typical Angular projects, watch mode reruns are sub-second, and the test runtime is JSDOM (no headless-Chrome process to launch in CI).
- **Why not Jest?** Jest works with Angular (`jest-preset-angular`) but is CJS-first; ESM support has been "experimental" for years and Angular 17+ ships ESM-only ECMAScript modules. Vitest's first-class ESM model removes a category of "this works for Karma but Jest can't resolve the import" friction.
- **The Vite usage is test-only.** Angular CLI's `ng serve` and `ng build` continue to use Angular's own builder (which in Angular 17+ is `@angular-devkit/build-angular:application` — already esbuild-based, ESM, fast). We do NOT introduce a parallel Vite dev server. The mental model: Vitest is a peer-of-Karma swap inside the `test` builder only.

### Why `@analogjs/vitest-angular` (verify at dev time)

- `@analogjs/vitest-angular` is the community-maintained Angular adapter for Vitest. It handles the TestBed initialization, zone.js/testing wiring, and Angular compiler integration that raw Vitest cannot do on its own.
- As of 2026-05-31, it is the de-facto standard adapter for Angular + Vitest projects. The Angular team has hinted at first-party Vitest support in the `@angular/build:unit-test` builder roadmap, but as of this story drafting it is not yet shipped as stable.
- **Verify at dev time.** Run `npm view @analogjs/vitest-angular` and check the Angular peer-dependency range. If Angular 21 has shipped its own first-party Vitest builder (`@angular-devkit/build-angular:vitest` or similar), prefer that — record the decision in `Dev Agent Record`. If `@analogjs/vitest-angular` is still the canonical choice, lock the major version (`^X.Y`) at install time to avoid silent breakage on future minor bumps.

### Current admin-web state (verified 2026-05-31 — pre-story)

- `admin-web/package.json` Angular runtime + devkit are all `^17.3.0` / `^17.3.17`.
- `firebase: ^10.14.1`, `rxjs: ~7.8.0`, `tslib: ^2.3.0`, `zone.js: ~0.14.3`.
- `typescript: ~5.4.2` — must bump to Angular 21 peer minimum.
- DevDeps test stack: `jasmine-core ~5.1.0`, `karma ~6.4.0`, `karma-chrome-launcher ~3.2.0`, `karma-coverage ~2.2.0`, `karma-jasmine ~5.1.0`, `karma-jasmine-html-reporter ~2.1.0`, `@types/jasmine ~5.1.0`.
- Scripts: `ng`, `start: ng serve`, `build: ng build`, `watch: ng build --watch --configuration development`, `test: ng test`.
- `angular.json` `build` target uses `@angular-devkit/build-angular:application` (Angular 17's new builder — good, already on the modern path) with budgets `initial=500KB/1MB`, `anyComponentStyle=2KB/4KB`. `serve` target uses `@angular-devkit/build-angular:dev-server`. `test` target uses `@angular-devkit/build-angular:karma` with `zone.js` + `zone.js/testing` polyfills. ScSS is the chosen style language.
- `tsconfig.spec.json` extends `./tsconfig.json`, declares `compilerOptions.types = ["jasmine"]`, includes `src/**/*.spec.ts` + `src/**/*.d.ts`.
- **There is no `karma.conf.js` at the admin-web root.** The Karma config is implicit (Angular CLI generates it on the fly from `angular.json`). Nothing to delete.
- **Spec inventory:** exactly one file, `src/app/app.component.spec.ts`. Uses `describe`, `beforeEach (async)`, `TestBed.configureTestingModule`, `it`, `expect`, `toBeTruthy`. NO `spyOn`, NO `jasmine.createSpy*`. Migration to Vitest is essentially zero-touch beyond confirming `RouterTestingModule` still imports correctly under Angular 21.
- `.github/workflows/admin-web-ci.yml` (from Story 1a.1) runs on Node 20, executes `npx ng test --watch=false --browsers=ChromeHeadless` and `npx ng build --configuration production`. `--browsers=ChromeHeadless` will be removed by this story; `--watch=false` stays.

### Latest tech info (2026-05-31)

- **Angular 21.x** is current stable. Verify via `npm view @angular/core dist-tags` before starting — drafting confirmed but state drift is possible.
- **Angular 21 TypeScript peer range** — verify at dev time (`npm view @angular/core@21 peerDependencies`). Expected `typescript ~5.7.x` or `~5.8.x`. The current `~5.4.2` pin is 3+ minors behind and MUST bump.
- **Angular 21 Node.js requirement** — Node ≥ 20 (LTS). CI already on Node 20. Local-dev users below Node 20 will see `npm install` peer warnings on `@angular/cli`.
- **Angular 21 zone.js peer** — verify; expected `zone.js ~0.15.x`. The current `~0.14.3` pin likely bumps automatically as part of `ng update @angular/core@21`.
- **`ng update` schematics that may run during the hops:**
  - 17→18: `inject()` typing tightening, optional standalone migration for any remaining NgModule-based components.
  - 18→19: optional control flow migration prompt (`*ngIf` → `@if`). **Decline** — out of scope.
  - 19→20: optional Signal-based forms migration prompt. **Decline** — out of scope.
  - 20→21: TBD; verify the Angular 21 release notes at dev time.
- **`@analogjs/vitest-angular`** — verify version range supports Angular 21. As of drafting, this is the canonical community adapter for Angular + Vitest. Alternative: native `vitest` + manual TestBed setup, but the adapter saves significant boilerplate.
- **Vitest config shape:** `defineConfig({ test: { globals: true, environment: 'jsdom', setupFiles: ['src/test-setup.ts'] } })`. The `setupFiles` entry imports `@analogjs/vitest-angular/setup-zone` to bootstrap `zone.js/testing` + `TestBed`.
- **Coverage:** Vitest's `v8` provider is the modern choice (was `c8`; renamed). `karma-coverage` outputs `lcov` by default — Vitest matches via `coverage.reporter = ['text', 'lcov', 'html']`. CI doesn't currently consume coverage artifacts (no Codecov/Coveralls wiring) — out of scope to add.

### Files to be modified / created

**UPDATE:**
- `admin-web/package.json` — bump all `@angular/*` to `^21.x`, bump `typescript` to Angular 21 peer minimum, bump `zone.js` to Angular 21 peer, remove all `karma*`, `jasmine-core`, `@types/jasmine`; add `vitest`, `@vitest/coverage-v8`, `@analogjs/vitest-angular` (or first-party equivalent), `jsdom`, `@types/node`. Add `test:watch` + `test:coverage` scripts.
- `admin-web/package-lock.json` — regenerated by `npm install` after each `ng update` hop. Commit each major's lock change separately for clean diffs.
- `admin-web/angular.json` — swap the `test` target builder from `@angular-devkit/build-angular:karma` to the Vitest builder (`@analogjs/vitest-angular:test` or first-party). Remove the `zone.js` + `zone.js/testing` polyfills from the test target (handled in `test-setup.ts` instead). Keep `tsConfig`, `inlineStyleLanguage`, `assets`, `styles`.
- `admin-web/tsconfig.spec.json` — `compilerOptions.types`: `["jasmine"]` → `["vitest/globals", "node"]`. Add `src/test-setup.ts` to `include` if needed.
- `admin-web/src/app/app.component.spec.ts` — likely zero or one-line change. Verify `RouterTestingModule` still resolves under Angular 21; if removed, swap to `provideRouter([])` in the testing provider list.
- `admin-web/README.md` — refresh test section, mention Vitest, document Node 20 + Angular 21 baseline.
- `.github/workflows/admin-web-ci.yml` — remove `--browsers=ChromeHeadless` from the test step. Keep everything else.

**NEW:**
- `admin-web/vitest.config.ts` — Vitest configuration (JSDOM env, globals, setup file, coverage provider).
- `admin-web/src/test-setup.ts` — Angular + zone.js/testing bootstrap import (one-liner imports the adapter's setup helper).

**DELETE:**
- None at the file level (no `karma.conf.js` exists today). The Karma + Jasmine packages disappear from `package.json` + `package-lock.json` via `npm uninstall`.

### What NOT to change

- `admin-web/src/main.ts`, `admin-web/src/app/app.component.ts`, `admin-web/src/app/app.component.html`, `admin-web/src/app/app.component.scss`, `admin-web/src/app/app.module.ts`, `admin-web/src/app/app-routing.module.ts`, `admin-web/src/styles.scss`, `admin-web/src/index.html`, `admin-web/src/environments/` — application code stays semantically identical. The `ng update` schematics may rewrite minor TS syntax (e.g., `inject()` typing) — accept those edits, do NOT manually expand them.
- `admin-web/tsconfig.json`, `admin-web/tsconfig.app.json` — only update if the upgrade schematics touch them. Do not preemptively reshape compiler options.
- `admin-web/dist/` — gitignored build output. Ignore.
- The `firebase: ^10.14.1` pin — DO NOT BUMP. Firebase SDK 10 is verified-compatible with Angular 17–21.
- The `rxjs: ~7.8.0` pin — leave unless an `ng update` hop requires a bump.
- The `build` and `serve` targets in `angular.json` — only the `test` target changes.
- `backend/`, `prototype/`, `client-app/`, `trainer-app/` — entirely off-limits.
- `.github/workflows/backend-ci.yml`, `.github/workflows/deploy-prototype.yml`, `.github/workflows/pact-verify.yml` — only `admin-web-ci.yml` changes.

### Risk register for the dev session

| Risk | Likelihood | Mitigation |
|---|---|---|
| `ng update` schematic fails mid-hop (e.g., 19→20) and leaves the tree in an inconsistent state | Med | Commit after every major. If a hop fails, `git reset --hard HEAD` returns to the last-known-good major. Re-attempt the failing hop after resolving the schematic's complaint. |
| Angular 21 drops `RouterTestingModule` in favor of `provideRouter([])` and `app.component.spec.ts` breaks | High | The single existing spec uses `RouterTestingModule`. If removed in Angular 21, swap to `imports: [RouterModule.forRoot([])]` or migrate to provider syntax. Verify at the `19→20` and `20→21` hops — that's where module-vs-provider testing utilities historically moved. |
| `@analogjs/vitest-angular` lags Angular 21 release and doesn't yet support 21 | Med | At dev time, check `npm view @analogjs/vitest-angular` for the latest version + its Angular peer range. If 21 is not yet supported, options: (a) wait for adapter release, (b) use the official `@angular/build:unit-test` builder if Angular 21 shipped it, (c) hand-roll Vitest + Angular compiler setup. Document the decision in `Dev Agent Record`. |
| TypeScript `~5.7+` bump surfaces latent type errors in existing code | Low (admin-web is tiny — only `AppComponent` + `AppModule`) | Fix only what the new TS version surfaces. Do NOT refactor unrelated code under cover of the upgrade. Each TS error gets its own commit comment. |
| Vitest's JSDOM environment behaves differently from Karma's real Chrome — e.g., missing browser APIs (`requestAnimationFrame`, `IntersectionObserver`) | Low (admin-web has no UI yet beyond the default scaffold) | If a spec relies on a browser API not in JSDOM, polyfill it in `src/test-setup.ts` via `vi.stubGlobal('requestAnimationFrame', ...)`. The current spec is trivial — no risk. |
| `npm test` script behavior changes (e.g., Vitest defaults to watch mode but `ng test` historically defaulted to single-run with `--watch=false`) | Med | Verify after wiring: `npm test` should default to single-run for CI compatibility. The adapter's docs specify the default; if it defaults to watch, override via `vitest run` in the npm script and add `test:watch` for the watch flow. |
| `admin-web-ci.yml` `--browsers=ChromeHeadless` flag is silently ignored by the Vitest builder, but the workflow stays green for the wrong reason | Low | Explicitly REMOVE the flag in the workflow update — don't rely on silent acceptance. Verified post-push by checking the workflow log shows zero Chrome-launcher activity. |
| `zone.js/testing` not loaded before TestBed initializes → cryptic `NG0908` or "Zone not patched" errors | Med | `src/test-setup.ts` MUST import `zone.js` + `zone.js/testing` (or the adapter's setup helper that does so) BEFORE any Angular import. Order matters. Document at the top of the file. |
| Production bundle size regresses past the 500 KB / 1 MB budget after Angular 21 upgrade | Low | Angular's per-major runtime size has trended DOWN since 16. If a regression appears, surface as a finding — do NOT silently relax the budget. Likely cause would be a tree-shaking miss in an `@angular/*` package, fixable via clean `npm ci`. |
| Firebase 10.14 SDK peer-warns or breaks under TypeScript ~5.7+ | Low | Firebase 10 ships its own bundled types; the TS bump should not affect it. If it does, document and proceed — Firebase SDK is not exercised yet (no auth wiring), so a TS warning is non-blocking until Story 1d.1. |
| `tslib` peer-dep range narrows under Angular 21 and the current `^2.3.0` no longer resolves | Low | `ng update` schematic usually bumps `tslib`. Accept the schematic's edit. |
| `--watch=false` flag rejected by Vitest builder | Med | If the Vitest builder uses `--run` instead of `--watch=false`, update the npm scripts AND the GH Actions workflow accordingly. The user-facing contract is "CI runs once and exits"; the flag name is implementation detail. |

### Previous story intelligence

From Story 1a.1 (GH Actions CI/CD pipelines, merged 2026-05-28, commit `a77c9e4`):

- `.github/workflows/admin-web-ci.yml` runs on `ubuntu-latest`, Node 20, with `npm ci` install. Test command is `npx ng test --watch=false --browsers=ChromeHeadless`. Build command is `npx ng build --configuration production`. Both steps will continue to work post-upgrade — the test command needs the `--browsers=ChromeHeadless` removal.
- No coverage upload, no Codecov / Coveralls wiring. Coverage is local-only via `npm run test:coverage` (added in this story).

From Story 1a.2 (Spring Boot 3.5 upgrade + `in.vis` package, merged 2026-05-29):

- Pattern of multi-major upgrade-with-commit-per-major was applied to the backend (Boot 3.3 → 3.5 in one hop because Boot 3.5 supports 3.3 baselines directly). Angular requires more granular hops than Spring Boot — four commits expected here, one per Angular major.
- The package-rename + grep-clean discipline from 1a.2 does not apply here — admin-web has no `gymculture` references to scrub.

Cross-story note for Story 1c.2 (Signals + services state architecture baseline):

- 1c.2 follows IMMEDIATELY after this story merges. 1c.2 introduces Signals usage, which Angular 17+ supports — Angular 21 obviously supports it. This story MUST land on a TS + Angular version that admits Signals (Angular 21 does) but MUST NOT actually introduce Signals.
- 1c.2 will also introduce a services state pattern (likely `@Injectable({providedIn: 'root'})` + `signal()` accessors). This story leaves the DI tree exactly as-is.

Cross-story note for Story 1d.1 (Firebase Auth integration):

- 1d.1 introduces Firebase Auth wiring + login screens in admin-web. It assumes Firebase SDK 10.14 (the pin we are NOT bumping). If Firebase SDK 11+ ships during the gap, 1d.1 owns the decision to upgrade — not this story.

### References

- Architecture §Step 5 "Admin-web stack" — Angular major + Vitest decision [`_bmad-output/planning-artifacts/architecture.md` admin-web section]
- PRD §Admin-web requirements [`prds/prd-Vis-2026-05-21/prd.md`]
- Epic spec Story 1c.1 ACs [`_bmad-output/planning-artifacts/epics.md` E1c section]
- Story 1a.1 (CI/CD pipelines, `admin-web-ci.yml`) [`stories/1a-1-gh-actions-ci-cd-pipelines.md`]
- Story 1a.2 (multi-step framework upgrade pattern) [`stories/1a-2-spring-boot-3-5-x-upgrade-in-vis-package-migration.md`]
- Angular Update Guide — interactive 17 → 21 path [`https://angular.dev/update-guide`]
- Angular 21 release notes (verify at dev time) [`https://blog.angular.dev/`]
- `@analogjs/vitest-angular` README [`https://github.com/analogjs/analog/tree/main/packages/vitest-angular`]
- Vitest configuration reference [`https://vitest.dev/config/`]
- Karma deprecation announcement [`https://github.com/karma-runner/karma/issues/3666`]
- Linear spec issue: GC-75 (E1c project). Per CLAUDE.md, a mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>`.

## Dev Agent Record

### Agent Model Used

_To be filled in by Dev Agent at execution time._

### Debug Log References

_To be filled in by Dev Agent at execution time. Expected entries: chosen Vitest adapter package + version, any spec API rewrites required for Angular 21 (e.g., `RouterTestingModule` → `provideRouter([])`), TypeScript bump resolution, any `ng update` schematic prompts that were declined as out-of-scope._

### Completion Notes List

_To be filled in by Dev Agent at execution time._

### File List

_To be filled in by Dev Agent at execution time._

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-31 | Story drafted (ready-for-dev). Sprint-status flipped: `1c-1-... : backlog → ready-for-dev`; `epic-1c: backlog → in-progress`. |
