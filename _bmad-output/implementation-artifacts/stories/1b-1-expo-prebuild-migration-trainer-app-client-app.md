# Story 1b.1: Expo Prebuild Migration — trainer-app + client-app

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a mobile platform engineer,
I want both `trainer-app/` and `client-app/` migrated from hand-maintained vanilla React Native projects to the **Expo bare workflow** (SDK 52) via `npx expo prebuild --clean`, with `ios/` and `android/` directories regenerated from `app.json` + Expo config plugins,
so that every subsequent mobile story (WatermelonDB + op-sqlite, MMKV, theme tokens, Firebase Auth, EAS Build CI) can declare native modules through Expo plugins rather than hand-patching `AppDelegate`, `MainApplication`, `Podfile`, or Gradle config — and so that the two apps stay structurally identical and reproducible from a clean checkout.

## Acceptance Criteria

1. **Expo SDK 52 installed in both apps.** `trainer-app/package.json` and `client-app/package.json` both declare `"expo": "~52.0.0"` (or the latest SDK 52 patch) under `dependencies`. `expo-modules-core` is NOT installed separately — it ships transitively inside `expo` SDK ≥ 51. `grep -c '"expo"' trainer-app/package.json` returns ≥ `1`; same for `client-app/package.json`. No managed-workflow-only packages (`expo-router`, `expo-dev-client` optional, `expo-status-bar` allowed) are added.
2. **`app.json` migrated to Expo schema.** Both apps' `app.json` files have a top-level `expo` object with the required fields: `name`, `slug`, `version`, `orientation: "portrait"`, `icon`, `userInterfaceStyle: "automatic"`, `splash`, `ios.bundleIdentifier`, `android.package`, `jsEngine: "hermes"`, and an empty `plugins: []` array (the array is the seam future stories extend — `op-sqlite`, `expo-build-properties`, etc. drop in here, not in this story). `expo.ios.bundleIdentifier` and `expo.android.package` MUST match the values currently set in `ios/*/Info.plist` and `android/app/build.gradle` so existing Firebase config files keep validating.
3. **`npx expo prebuild --clean` regenerates `ios/` + `android/` reproducibly.** Running `npx expo prebuild --clean` in each app deletes and regenerates `ios/` and `android/` from `app.json`. The command exits 0. Re-running it on the same `app.json` produces a byte-stable diff (idempotent — the second run reports `Files already exist` / no changes). The regenerated `ios/AppDelegate.swift` (or `.mm` depending on RN version) and `android/app/src/main/java/.../MainApplication.kt` (or `.java`) contain Expo's bootstrap code without manual edits.
4. **Firebase config files restored post-prebuild.** Because `--clean` deletes the entire `ios/` and `android/` trees, the Firebase configuration files must be re-copied **and** the re-copy step must be documented + repeatable:
   - `trainer-app/ios/GoogleService-Info.plist` and `trainer-app/android/app/google-services.json` present after prebuild.
   - `client-app/ios/GoogleService-Info.plist` and `client-app/android/app/google-services.json` present after prebuild.
   - The re-copy procedure is documented in each app's README under a new "Native regeneration" section: prebuild step → copy step → verify step. (Permanent plugin-driven copy via `expo-build-properties` / file-asset plugin is out of scope here; Story 1d.1 owns it.)
   - Until the plugin-driven copy lands, the config files are committed into `ios/` and `android/app/` after prebuild so a fresh `npm install` checkout still builds.
5. **Both apps build and run on a clean checkout.** From a freshly-cloned working tree:
   - `cd trainer-app && npm install && npx react-native run-ios` builds and launches the iOS simulator without errors.
   - `cd trainer-app && npx react-native run-android` builds and launches the Android emulator without errors.
   - Same two commands succeed for `client-app/`.
   - Metro bundler starts clean — no `Unable to resolve module` errors, no warnings about missing peer deps, no red error screen on the simulator.
   - The default RN screen (the existing `App.tsx`) renders. **No new UI work** in this story.
6. **Existing Jest test passes; CI stays green.** `cd trainer-app && npm test` runs the existing `__tests__/App.test.tsx` and passes. Same for `client-app/`. No changes to `jest.config.js`, `babel.config.js` (unless Expo prebuild requires a `babel-preset-expo` swap — see Dev Notes), or `metro.config.js` beyond what `npx expo prebuild` writes. Any existing `mobile-ci.yml` GitHub Actions stub (placeholder from Story 1a.1) keeps passing — this story does NOT add real EAS Build CI; that lives in Story 1b.2.
7. **Scope guards verified by grep.** `grep -r "expo-router" trainer-app/ client-app/` returns empty (managed workflow not introduced). `grep -r "watermelondb\|op-sqlite\|mmkv" trainer-app/src/ client-app/src/` returns empty (those stories own those installs). No new dependency in either `package.json` outside the Expo SDK install (and `babel-preset-expo` if prebuild requires it).
8. **README updates land in both apps.** `trainer-app/README.md` and `client-app/README.md` each gain a `## Native regeneration (Expo prebuild)` section explaining: when to re-run prebuild, the `--clean` warning about Firebase config files, the manual re-copy step, and the future-state pointer to Story 1d.1's plugin-based copy. Existing run-instruction sections updated to mention that `ios/` and `android/` are now generated artifacts (still committed for now, but reproducible from `app.json`).

### Explicitly OUT OF SCOPE for this story (do NOT implement)

- ❌ **WatermelonDB + op-sqlite integration.** Story 1b.3 owns this — including the `op-sqlite` Expo config plugin entry in `app.json.expo.plugins`. This story ships `plugins: []` empty.
- ❌ **MMKV install + auth token storage.** Story 1b.4 owns this. No `react-native-mmkv` install here.
- ❌ **Light/dark theme token system + OS preference hooks.** Story 1b.5. No `tokens.ts`, no `useColorScheme()` plumbing.
- ❌ **EAS Build CI pipelines (`trainer-app-eas.yml` / `client-app-eas.yml`).** Story 1b.2. The mobile-CI workflow files stay as Story 1a.1's stub.
- ❌ **Firebase Auth SDK integration (Google / Apple / Phone OTP).** Epic E1d Story 1d.1. This story keeps the Firebase **config files** in place (so the existing baseline still builds) but does NOT install `@react-native-firebase/auth` or wire any auth UI.
- ❌ **Any feature code** — Equipment-Aware Logger, Set Row, Connection Orb, Recovery Chips, PR Moments — all live in Epics E4+. This story does not touch `src/` of either app.
- ❌ **Managed Expo workflow.** Story title is "bare". Do NOT add `expo-router`, do NOT migrate to `app/` directory routing, do NOT switch to Expo's managed build server. We stay on RN's bare workflow + Expo SDK for native module orchestration.
- ❌ **Upgrading React Native to a newer minor version** unless Expo SDK 52 hard-requires it. SDK 52 aligns with RN 0.76 — if the apps are currently on 0.73 / 0.74, do the smallest RN bump that SDK 52 demands and document it in the change log. No proactive bumps beyond that.
- ❌ **Reorganizing app source layout.** `src/` stays exactly as it is — no folder restructure, no path-alias additions, no `tsconfig` changes beyond what `npx expo prebuild` injects (which should be none — prebuild touches `ios/` + `android/` + `app.json`, not `src/` or `tsconfig.json`).
- ❌ **Removing the Gemfile or switching CocoaPods install flow.** Expo prebuild still uses CocoaPods on iOS. The existing `Gemfile` stays (or is regenerated identically) — no Bundler-strategy change.

## Tasks / Subtasks

- [ ] Pre-flight: snapshot existing native code so the hand-maintained `ios/` + `android/` is recoverable (AC: 3, 4)
  - [ ] `git status` clean before starting; create branch `feat/1b-1-expo-prebuild` from `main`.
  - [ ] `git stash` is not enough — `npx expo prebuild --clean` deletes the directories first. Commit a "pre-prebuild snapshot" commit on the branch with the current `ios/` + `android/` trees so the diff is recoverable: `git add trainer-app/ios trainer-app/android client-app/ios client-app/android && git commit -m "VIS-<mirror>: snapshot pre-prebuild native trees"`.
  - [ ] Audit both apps' current native code for non-default modifications. Run `git log --follow -- trainer-app/ios trainer-app/android` and `client-app/ios client-app/android` to find any past native edits (Firebase config aside). Document anything found in `Dev Agent Record → Debug Log References` for re-application post-prebuild.
  - [ ] Confirm Firebase config files are present and locate them: `trainer-app/ios/GoogleService-Info.plist`, `trainer-app/android/app/google-services.json`, plus same paths under `client-app/`. Copy each to a temporary location outside the working tree (e.g. `/tmp/vis-fb-configs/trainer/ios/...`) so they survive the `--clean`.
- [ ] Install Expo SDK 52 in `trainer-app/` (AC: 1, 2)
  - [ ] `cd trainer-app && npx expo install expo@~52.0.0` — `npx expo install` (not `npm install`) so Expo's dependency-version resolver pins the SDK-aligned version of React, RN, `react-native-reanimated`, etc.
  - [ ] Confirm `package.json` now has `"expo": "~52.0.0"` (or a `^52.x.y` patch). No `expo-modules-core` line — it ships inside `expo`.
  - [ ] Add a script: `"expo:prebuild": "expo prebuild --clean"` under `package.json.scripts`. Don't replace existing `ios` / `android` / `start` / `test` scripts.
  - [ ] If `npx expo install` complains about an RN minor mismatch with SDK 52 (SDK 52 wants RN 0.76), allow it to bump RN. Record the bump in the change log.
- [ ] Migrate `trainer-app/app.json` to Expo schema (AC: 2)
  - [ ] Before: vanilla RN `app.json` usually has just `{ "name": "trainer-app", "displayName": "trainer-app" }`. Keep these as a comment or migrate to the equivalent Expo fields.
  - [ ] After:
    ```json
    {
      "expo": {
        "name": "Vis Trainer",
        "slug": "vis-trainer",
        "version": "0.1.0",
        "orientation": "portrait",
        "icon": "./assets/icon.png",
        "userInterfaceStyle": "automatic",
        "splash": { "image": "./assets/splash.png", "resizeMode": "contain", "backgroundColor": "#F9F6F0" },
        "ios": { "bundleIdentifier": "in.vis.trainer", "supportsTablet": false },
        "android": { "package": "in.vis.trainer", "adaptiveIcon": { "foregroundImage": "./assets/adaptive-icon.png", "backgroundColor": "#F9F6F0" } },
        "jsEngine": "hermes",
        "plugins": []
      }
    }
    ```
  - [ ] **Critical:** `expo.ios.bundleIdentifier` and `expo.android.package` MUST match the existing values currently in `ios/trainerapp/Info.plist` (CFBundleIdentifier) and `android/app/build.gradle` (`applicationId`). If they differ, Firebase config files break (the bundle ID is baked into `GoogleService-Info.plist` + `google-services.json`). Verify before running prebuild.
  - [ ] Splash background color = `#F9F6F0` (warm ivory page bg per `CLAUDE.md` brand palette). **Never** `#FFFFFF`.
  - [ ] `userInterfaceStyle: "automatic"` enables OS-preference dark mode hooks — Story 1b.5 will rely on this.
  - [ ] Asset files `assets/icon.png`, `assets/splash.png`, `assets/adaptive-icon.png` — if they don't exist, create 1024x1024 placeholders (solid `var(--gc-bg)` background with the Vis orb at center). Real branded assets land in a later UX-polish story.
- [ ] Run `npx expo prebuild --clean` for `trainer-app/` (AC: 3)
  - [ ] `cd trainer-app && npx expo prebuild --clean`. Expect: "✔ Cleared native project directories" → "✔ Created native projects" → "✔ Installed CocoaPods" (or a hint to run `pod install` manually if CocoaPods is missing).
  - [ ] Verify `ios/` and `android/` now exist with Expo's bootstrap code:
    - `ios/<slug>/AppDelegate.swift` — should contain `import Expo` and `ExpoReactNativeFactoryDelegate` / `RCTAppDelegate` extension.
    - `android/app/src/main/java/in/vis/trainer/MainApplication.kt` — should contain `ReactNativeHostWrapper` and `ApplicationLifecycleDispatcher` calls.
  - [ ] Re-copy Firebase config files from `/tmp/vis-fb-configs/trainer/`:
    - `cp /tmp/vis-fb-configs/trainer/ios/GoogleService-Info.plist trainer-app/ios/`
    - `cp /tmp/vis-fb-configs/trainer/android/google-services.json trainer-app/android/app/`
  - [ ] `cd trainer-app/ios && pod install` (or `bundle exec pod install` if `Gemfile` was preserved) — confirm pods resolve, including `Expo` / `ExpoModulesCore`.
- [ ] Verify `trainer-app/` builds (AC: 5)
  - [ ] `cd trainer-app && npx react-native run-ios` — iOS simulator launches, default RN screen renders, no red-box errors.
  - [ ] `cd trainer-app && npx react-native run-android` — Android emulator launches (assume a configured AVD or skip with a documented `# REQUIRES_AVD` note if no AVD locally; CI will cover android in Story 1b.2).
  - [ ] `cd trainer-app && npm test` — `App.test.tsx` passes.
  - [ ] Metro bundler logs are clean — `grep` the bundler output for `error|Error|warn` and triage each finding.
- [ ] Repeat the full sequence for `client-app/` (AC: 1, 2, 3, 4, 5, 6)
  - [ ] Same `npx expo install expo@~52.0.0`.
  - [ ] `client-app/app.json` Expo schema with `expo.name = "Vis Client"`, `expo.slug = "vis-client"`, `expo.ios.bundleIdentifier = "in.vis.client"`, `expo.android.package = "in.vis.client"`. Bundle ID / package must match the current `ios/` + `android/` values — verify before prebuild.
  - [ ] `npx expo prebuild --clean` in `client-app/`.
  - [ ] Re-copy `client-app/ios/GoogleService-Info.plist` and `client-app/android/app/google-services.json` from the temp snapshot.
  - [ ] `npx react-native run-ios` + `run-android` + `npm test` all pass.
- [ ] Update both READMEs (AC: 8)
  - [ ] In `trainer-app/README.md` and `client-app/README.md`, add a `## Native regeneration (Expo prebuild)` section after the existing run-instructions section. Cover:
    - When to re-run: after editing `app.json`, after adding an Expo config plugin (op-sqlite, expo-build-properties, etc.), after upgrading the Expo SDK.
    - The command: `npx expo prebuild --clean`.
    - The Firebase config warning: `--clean` deletes `ios/` and `android/`, including `GoogleService-Info.plist` and `google-services.json`. Re-copy them from your secure store after every clean prebuild.
    - Future-state pointer: Story 1d.1 introduces a config-plugin-driven copy step (or `expo-build-properties` extra-files mechanism) so the manual copy goes away.
  - [ ] Update the existing run-instructions section to note that `ios/` and `android/` are generated artifacts — still committed today (so a fresh checkout builds), but reproducible from `app.json`.
- [ ] Commit strategy (AC: 3, 4, 5, 6)
  - [ ] **Decision: one bundled commit per app**, in this order, on the `feat/1b-1-expo-prebuild` branch:
    1. `VIS-<mirror>: trainer-app — snapshot pre-prebuild native trees` (the safety snapshot from pre-flight).
    2. `VIS-<mirror>: trainer-app — Expo SDK 52 install + app.json migration`.
    3. `VIS-<mirror>: trainer-app — regenerated ios/ + android/ via expo prebuild`.
    4. `VIS-<mirror>: trainer-app — restored Firebase config files`.
    5. `VIS-<mirror>: client-app — Expo SDK 52 install + app.json migration`.
    6. `VIS-<mirror>: client-app — regenerated ios/ + android/ via expo prebuild`.
    7. `VIS-<mirror>: client-app — restored Firebase config files`.
    8. `VIS-<mirror>: READMEs — Native regeneration sections`.
  - [ ] Rationale: bisectable. If a Firebase build later fails, `git bisect` lands on commit 4 or 7 immediately. A single mega-commit hides which step broke.
  - [ ] Alternative considered: one commit per app. Rejected — too coarse for bisect; keep the per-step granularity above.
- [ ] Scope guard verification (AC: 7)
  - [ ] `grep -r "expo-router" trainer-app/ client-app/` → empty.
  - [ ] `grep -r "watermelondb\|op-sqlite\|mmkv" trainer-app/src/ client-app/src/` → empty.
  - [ ] `grep -c '"expo"' trainer-app/package.json` ≥ 1; same for `client-app/package.json`.
  - [ ] `diff <(jq '.expo.plugins' trainer-app/app.json) <(jq '.expo.plugins' client-app/app.json)` — both apps' `plugins` arrays are identical empty arrays.
- [ ] Update sprint status + Linear mirror to In Review (mirror created at dev-story start in project `E1b — Mobile Infrastructure` per CLAUDE.md convention; spec issue is GC-70; commit prefix `VIS-<mirror-number>`).

## Dev Notes

### Scope discipline — what this story IS and IS NOT

**IS:**
- Install `expo` SDK 52 in both `trainer-app/` and `client-app/`.
- Migrate `app.json` in both apps to the Expo schema (with `expo.name`, `expo.slug`, `expo.ios.bundleIdentifier`, `expo.android.package`, `expo.plugins: []`, etc.).
- Run `npx expo prebuild --clean` to regenerate `ios/` and `android/` from `app.json` in both apps.
- Re-copy Firebase config files (`GoogleService-Info.plist`, `google-services.json`) after every clean prebuild.
- Verify the apps build via `npx react-native run-ios` + `run-android` and that the existing Jest test passes.
- Document the native regeneration workflow in both READMEs.

**IS NOT:**
- WatermelonDB / op-sqlite install (1b.3).
- MMKV / secure storage (1b.4).
- Theme tokens / dark mode plumbing (1b.5).
- EAS Build CI (1b.2).
- Firebase Auth SDK install (1d.1).
- Any feature code, any `src/` reorganization, any UI work.

If you reach for `@nozbe/watermelondb`, `react-native-mmkv`, `@react-native-firebase/auth`, an `eas.json` file, or start touching `src/components/` — **stop**, that's outside this story.

### Why Expo SDK 52 (not 53, not 51)

- **SDK 52** released 2024-11-12, aligned with React Native **0.76**, React **18.3.1**. It's the current stable / LTS choice as of 2026-05-31.
- SDK 53 (the bleeding edge as of mid-2026) bumps to RN 0.77+ and introduces breaking changes to the new architecture (Fabric / TurboModules become default-on, not opt-in). The downstream stories in Epic E1b (op-sqlite, MMKV) and Epic E4+ (animations, Skia) haven't been validated against SDK 53 — pin to 52 for stability now, defer the 53 bump to a dedicated infra story after Epic E1b ships.
- SDK 51 is one release behind; it pre-dates the `expo-modules-core` bundling-into-expo convenience and would require an extra `expo-modules-core` install line. Not worth the savings.
- Source: Expo's release blog (`https://expo.dev/changelog/2024/11-12-sdk-52`) lists RN 0.76 as the supported pair and SDK 52 as the recommended floor for new bare-workflow projects.

### Why "bare" workflow, not "managed"

- The Vis apps need full native control: WatermelonDB requires a custom JSI adapter (op-sqlite), MMKV needs a JSI bridge, Firebase Auth phone-OTP needs `RNFBAuth` native methods, and EAS Build (1b.2) still works against bare workflow projects.
- Managed workflow constrains us to Expo's supported module set + Expo Go runtime. WatermelonDB is not in Expo Go. Choosing managed = dead end.
- Bare + Expo SDK gives us: prebuild orchestration (so `ios/` + `android/` are reproducible from `app.json`), Expo modules ecosystem (`expo-secure-store`, `expo-notifications`, etc.), and EAS Build access — without the Expo Go sandbox limitations.
- Story title literally says "bare workflow". Do not drift.

### `app.json` before/after sketch

**Before (vanilla RN, both apps look the same):**

```json
{
  "name": "trainer-app",
  "displayName": "trainer-app"
}
```

**After (Expo bare schema, trainer-app):**

```json
{
  "expo": {
    "name": "Vis Trainer",
    "slug": "vis-trainer",
    "version": "0.1.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "automatic",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#F9F6F0"
    },
    "ios": {
      "bundleIdentifier": "in.vis.trainer",
      "supportsTablet": false
    },
    "android": {
      "package": "in.vis.trainer",
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#F9F6F0"
      }
    },
    "jsEngine": "hermes",
    "plugins": []
  }
}
```

The `client-app/app.json` is identical structurally — only `name`, `slug`, `bundleIdentifier`, `package` differ (`Vis Client`, `vis-client`, `in.vis.client`).

### The "prebuild blows away `ios/` + `android/`" risk

`npx expo prebuild --clean` is destructive. It deletes the entire `ios/` and `android/` trees before regenerating them. Anything not derivable from `app.json` is lost. In this story the only things at risk are:

1. **Firebase config files** — `GoogleService-Info.plist` (iOS) and `google-services.json` (Android). These are gitignored per `CLAUDE.md`. They must be re-copied from a secure store after every clean prebuild.
2. **Custom native patches** — none expected at this story (the apps are fresh from `npx react-native init`). If the pre-flight audit surfaces any past edits to `AppDelegate`, `MainApplication`, `Info.plist`, `build.gradle`, or `Podfile`, document them in the Debug Log and decide whether to (a) re-apply manually post-prebuild and accept the next prebuild will require the same re-apply, or (b) defer the edit to a future story that introduces it via an Expo config plugin.

Future stories WILL declare native module config via Expo plugins in `app.json.expo.plugins`. Once the plugin-driven Firebase config copy lands (Story 1d.1 or a dedicated infra plugin), the manual `cp` step disappears. For now, document the manual copy + commit the config files into the regenerated tree.

### Implications for future stories under E1b

- **1b.2 EAS Build CI** — EAS reads `app.json` and `eas.json`. SDK 52 + bare workflow + the `expo.plugins` seam from this story is exactly what EAS Build needs. Story 1b.2 adds `eas.json` and the GitHub Actions workflows.
- **1b.3 WatermelonDB + op-sqlite** — `op-sqlite` ships an Expo config plugin. Story 1b.3 adds `["@op-engineering/op-sqlite", { ...options }]` to the `expo.plugins` array. After re-running `npx expo prebuild --clean`, the native projects pick up op-sqlite's `Podfile` and Gradle additions automatically.
- **1b.4 MMKV** — `react-native-mmkv` v3+ uses JSI; minimal native config. Story 1b.4 adds the install + a usage utility under `src/storage/`. No `app.json` plugin entry expected.
- **1b.5 Theme tokens** — Pure JS / TypeScript work. No native impact. Relies on `userInterfaceStyle: "automatic"` from this story so `useColorScheme()` returns the OS preference.
- **1d.1 Firebase Auth** — Adds `@react-native-firebase/auth` + Google/Apple/Phone-OTP. This is the story where the Firebase config-file copy becomes plugin-driven (likely via `@react-native-firebase/app`'s Expo plugin). Once that lands, the README warning about manual re-copy is replaced with "plugin handles it".

### Current state (verified 2026-05-31)

- Both `trainer-app/` and `client-app/` exist, created via `npx react-native init` originally (Epic E1b had not started).
- Each contains: `App.tsx`, `index.js`, `babel.config.js`, `metro.config.js`, `jest.config.js`, `package.json`, `Gemfile`, `__tests__/App.test.tsx`, `src/`, `ios/`, `android/`, `app.json` (vanilla RN style, two fields only).
- No prior Expo dependency in either `package.json`.
- Backend (Epic E1a) finished 2026-05-31 — unrelated to this story.
- `mobile-ci.yml` GitHub Actions workflow is a stub from Story 1a.1 — runs `npm install` + `npm test` only. Not affected by this story (the test still passes post-Expo install).
- Firebase config files (`GoogleService-Info.plist`, `google-services.json`) are present in both apps' current `ios/` + `android/app/` directories. **Confirm before running `--clean`** — if they're missing locally, source them from the Firebase console / secure store first.

### Latest tech info (2026-05-31)

- **Expo SDK 52** is current stable. Install via `npx expo install expo@~52.0.0`. The SDK aligns with React Native 0.76, React 18.3.1, Hermes default.
- **`expo-modules-core`** is no longer a separate install (since SDK 51). It ships transitively inside `expo`. Do not add it explicitly — duplicate install can cause Hermes bytecode mismatch.
- **`expo prebuild`** is the canonical command for the bare workflow. The `--clean` flag deletes existing `ios/` + `android/` before regen. Without `--clean`, prebuild attempts a 3-way merge that often produces partial / inconsistent results.
- **`AppDelegate.swift` (modern Expo) vs. `AppDelegate.mm` (older RN)** — SDK 52's prebuild templates emit Swift on iOS. The old `.m` / `.mm` Objective-C files are not regenerated. This is fine — Expo's templates handle the Swift bridge.
- **`MainApplication.kt` vs. `MainApplication.java`** — SDK 52 emits Kotlin. Same reasoning — Expo's Kotlin templates handle the bridge.
- **`babel.config.js`** — SDK 52 prebuild may suggest swapping `module.exports = { presets: ['module:@react-native/babel-preset'] }` to `presets: ['babel-preset-expo']`. Allow it if prebuild requires; otherwise leave alone. The Expo preset is a superset of the RN preset.
- **`metro.config.js`** — Expo provides `getDefaultConfig` from `expo/metro-config`. If prebuild rewrites `metro.config.js`, accept the rewrite. The Expo Metro config still supports `react-native-svg-transformer` and other transformers added in future stories.
- **Firebase config via plugin** — `@react-native-firebase/app` ships an Expo plugin (`expo.plugins: ["@react-native-firebase/app"]`). This is the long-term answer to the "Firebase config blown away by prebuild" problem. Story 1d.1 will add it. For THIS story, manual copy is the documented workflow.

### Files to be modified / created

**UPDATE (trainer-app):**
- `trainer-app/package.json` — add `expo: ~52.0.0` dependency + `expo:prebuild` npm script. RN version may bump to 0.76 if Expo's resolver requires it.
- `trainer-app/app.json` — full rewrite to Expo schema (`expo.name`, `expo.slug`, `expo.version`, `expo.orientation`, `expo.icon`, `expo.userInterfaceStyle`, `expo.splash`, `expo.ios.bundleIdentifier`, `expo.android.package`, `expo.jsEngine`, `expo.plugins: []`).
- `trainer-app/babel.config.js` — only if `npx expo prebuild` swaps to `babel-preset-expo`. If untouched by prebuild, no change.
- `trainer-app/metro.config.js` — only if prebuild rewrites to use `expo/metro-config`. If untouched, no change.
- `trainer-app/README.md` — new `## Native regeneration (Expo prebuild)` section + run-instructions clarification.

**UPDATE (client-app):** — same five files, same shape, with `Vis Client` / `vis-client` / `in.vis.client` values.

**REGENERATED by `npx expo prebuild --clean` (do not hand-edit, but commit so fresh checkouts build):**
- `trainer-app/ios/**` — entire tree, including `AppDelegate.swift`, `Info.plist`, `Podfile`, `Podfile.lock`, `<slug>.xcodeproj/`, `<slug>.xcworkspace/`, etc.
- `trainer-app/android/**` — entire tree, including `app/build.gradle`, `app/src/main/java/in/vis/trainer/MainApplication.kt`, `app/src/main/AndroidManifest.xml`, `settings.gradle`, etc.
- Same for `client-app/ios/**` and `client-app/android/**`.

**NEW (committed manually after prebuild, until Story 1d.1 makes them plugin-driven):**
- `trainer-app/ios/GoogleService-Info.plist` — re-copied from secure store / pre-prebuild snapshot.
- `trainer-app/android/app/google-services.json` — re-copied from secure store / pre-prebuild snapshot.
- `client-app/ios/GoogleService-Info.plist` — same.
- `client-app/android/app/google-services.json` — same.

**NEW (asset placeholders if missing — solid `#F9F6F0` + Vis orb):**
- `trainer-app/assets/icon.png`, `trainer-app/assets/splash.png`, `trainer-app/assets/adaptive-icon.png`.
- `client-app/assets/icon.png`, `client-app/assets/splash.png`, `client-app/assets/adaptive-icon.png`.

### What NOT to change

- `App.tsx` (both apps) — untouched. The default RN screen renders post-prebuild.
- `src/` (both apps) — untouched. No folder restructure, no path-alias additions.
- `__tests__/App.test.tsx` (both apps) — untouched. Must continue passing.
- `jest.config.js`, `tsconfig.json` (both apps) — untouched unless prebuild explicitly rewrites them (it shouldn't).
- Backend (`backend/`) — entirely unrelated.
- `admin-web/` — entirely unrelated (Angular, Epic E1c territory).
- `prototype/` — entirely unrelated (web prototype for design exploration).
- `_bmad/`, `_bmad-output/` — unrelated to mobile code.

### Risk register for the dev session

| Risk | Likelihood | Mitigation |
|---|---|---|
| `npx expo prebuild --clean` deletes Firebase config files (`GoogleService-Info.plist`, `google-services.json`) and the dev forgets to re-copy → Firebase init crashes on app launch | **High** | Pre-flight task explicitly copies Firebase configs to `/tmp/vis-fb-configs/` BEFORE running `--clean`. Post-prebuild task re-copies them and verifies presence. README documents the warning permanently. |
| `expo.ios.bundleIdentifier` / `expo.android.package` in `app.json` doesn't match the existing Firebase config's bundle ID → Firebase rejects the app at runtime | High | Pre-flight verifies current bundle ID in `ios/<app>/Info.plist` (`CFBundleIdentifier`) and `android/app/build.gradle` (`applicationId`), then `app.json` is written with the exact same values. Do not invent new bundle IDs in this story. |
| Expo SDK 52 install bumps RN minor version (0.73 → 0.76), breaking some transitive dep that the existing `__tests__/App.test.tsx` relies on | Med | Run `npm test` AFTER `npx expo install` but BEFORE prebuild. If the test breaks at the install step, the cause is the RN bump, not prebuild — easier to debug. If it breaks after prebuild, separate concern. |
| Prebuild rewrites `babel.config.js` / `metro.config.js` to use `babel-preset-expo` / `expo/metro-config`, breaking custom transformer config added in some future story | Low (this story has no custom transformers) → Med (future) | Accept the rewrite for this story. Future stories that add transformers (Skia, SVG) must additively extend the Expo Metro config, not replace it. Document in Story 1b.3's Dev Notes when it lands. |
| Both apps' `slug` collides on Expo's identity service (if EAS Build is later wired with auto-managed credentials) | Low | `vis-trainer` vs. `vis-client` — distinct slugs. Story 1b.2 verifies EAS picks them up as separate projects. |
| Hand-maintained Podfile customizations (e.g., a manual `pod 'Firebase/...'` line) are deleted by `--clean` | Low (fresh init has none) | Pre-flight audit explicitly checks `Podfile` git history for non-default edits. If any found, document and decide per-edit. |
| `pod install` fails on M-series Mac without `bundle exec` + Ruby version pin from `Gemfile` | Med | Run `bundle install && bundle exec pod install` (CocoaPods 1.15+ via Bundler). If `Gemfile` was regenerated by prebuild, verify the Ruby version pin is preserved. |
| `npx expo prebuild --clean` re-run after a future `app.json` edit overwrites the Firebase configs again | High (chronic) | This story commits the configs back into `ios/` and `android/app/` post-prebuild + documents in README. Story 1d.1's `@react-native-firebase/app` plugin solves it permanently. |
| Splash screen / icon assets missing → prebuild fails or produces broken native splash | Low | Pre-flight task creates 1024x1024 placeholder PNGs (warm ivory bg + Vis orb) if `assets/` is empty. Real branded assets land in a later UX-polish story. |
| Hermes engine flag (`jsEngine: "hermes"`) doesn't match the existing native Hermes-vs-JSC config → bytecode incompatibility on first build | Low | `npx react-native init` defaults to Hermes since RN 0.70+. Set `jsEngine: "hermes"` in `app.json` to match. If the existing app was on JSC (verify via `ios/Podfile`'s `hermes_enabled: true` line), align `app.json` to match the current setting and defer the JSC→Hermes swap to a separate story. |

### Reference outcomes — what a clean prebuild looks like

After `npx expo prebuild --clean` succeeds, the new `ios/` should contain (approximate):

```
trainer-app/ios/
  GoogleService-Info.plist          ← re-copied manually, not generated
  Podfile
  Podfile.properties.json
  vis-trainer.xcodeproj/
  vis-trainer.xcworkspace/
  vis-trainer/
    AppDelegate.swift               ← Expo-templated, contains `import Expo`
    Info.plist                      ← bundleIdentifier = in.vis.trainer
    Supporting/
    ...
```

And `android/`:

```
trainer-app/android/
  build.gradle
  gradle.properties
  settings.gradle
  app/
    build.gradle                    ← applicationId = in.vis.trainer
    google-services.json            ← re-copied manually, not generated
    src/main/AndroidManifest.xml    ← package = in.vis.trainer
    src/main/java/in/vis/trainer/
      MainActivity.kt
      MainApplication.kt            ← Expo-templated, calls ApplicationLifecycleDispatcher
```

If anything in this layout is missing post-prebuild, do not patch by hand — re-run prebuild and inspect the logs.

### Previous story intelligence

This is the first story in Epic E1b. There is no prior E1b story to inherit from. Lessons from Epic E1a that apply here:

From **Story 1a.1 (GH Actions CI/CD)** — the `mobile-ci.yml` workflow is a placeholder that just runs `npm install` + `npm test`. It does NOT run prebuild, does NOT build native, does NOT upload artifacts. This story keeps that workflow as-is. Story 1b.2 replaces it with real EAS Build pipelines.

From **Story 1a.3 (Redis + Testcontainers)** — the pattern of "set up infrastructure scaffolding now so call sites can rely on it later" applies. Future E1b stories drop entries into `app.json.expo.plugins[]`; this story establishes the empty seam.

From **Story 1a.6 (Observability)** — the discipline of "register the scaffold, defer the call sites" applies. The empty `plugins: []` array is the moral equivalent of the unregistered counter — present, available, populated by downstream stories.

### References

- Epic E1b spec issue: Linear `GC-70` (Epic project: `E1b — Mobile Infrastructure`). Per CLAUDE.md, a dev mirror tracking issue is created in the same project at dev-story start, moved In Progress → In Review → Done as work progresses. Commit prefix is `VIS-<mirror-number>`.
- Architecture §Mobile Infrastructure decisions (Expo bare workflow rationale) [`_bmad-output/planning-artifacts/architecture.md`]
- PRD §Mobile-App Foundations [`prds/prd-Vis-2026-05-21/prd.md`]
- Epic spec Story 1b.1 ACs [`_bmad-output/planning-artifacts/epics.md`]
- Expo SDK 52 release notes [`https://expo.dev/changelog/2024/11-12-sdk-52`]
- Expo prebuild reference [`https://docs.expo.dev/workflow/prebuild/`]
- Expo bare workflow guide [`https://docs.expo.dev/bare/overview/`]
- Expo config plugins reference [`https://docs.expo.dev/config-plugins/introduction/`]
- React Native 0.76 release notes [`https://reactnative.dev/blog/2024/10/23/release-0.76-new-architecture`]
- `@react-native-firebase/app` Expo plugin (for future Story 1d.1) [`https://rnfirebase.io/#expo`]
- CLAUDE.md brand palette — splash background `#F9F6F0` (warm ivory, never pure white).

## Dev Agent Record

### Agent Model Used

_To be filled by dev session._

### Debug Log References

_To be filled by dev session._

### Completion Notes List

_To be filled by dev session._

### File List

_To be filled by dev session._

### Change Log

| Date       | Change |
|------------|--------|
| 2026-05-31 | Initial story draft (ready-for-dev). |
