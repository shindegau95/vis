# Trainer App — Vis

React Native 0.85 + Expo SDK 56 (bare workflow) — Personal Trainer app. Phase 0 ships only the auth shell:
`Login → (Pending | HomeShell)` driven by Firebase Auth + backend `GET /auth/me`.

Mirror of `client-app/` per spec — only the bundle ID (`in.vis.trainer`), the
`HomeShell` text, and the login subtitle differ.

## Local-only Firebase config (NEVER commit)

These come from the Firebase Console for the **Trainer App** entries
(`in.vis.trainer`). Both files are `.gitignore`d.

| File | Source | Drop into |
| -- | -- | -- |
| `google-services.json` | Firebase Console → Android app `in.vis.trainer` | `android/app/google-services.json` |
| `GoogleService-Info.plist` | Firebase Console → iOS app `in.vis.trainer` | `ios/VisTrainer/GoogleService-Info.plist` (also drag-drop into the Xcode project) |

Then update `src/config.ts` with the Firebase Console "Web SDK configuration → Web client ID"
value. (Same project as Client App, so the same Web client ID works.)

## First-run setup

```bash
npm install
cd ios && pod install && cd ..
```

## Run

```bash
npx react-native run-ios
npx react-native run-android   # with an emulator running
```

## Native regeneration (Expo prebuild)

`ios/` and `android/` are **generated artifacts** produced by `npx expo prebuild` from `app.json`. They are still committed today so a fresh checkout builds without the prebuild step, but they are reproducible from `app.json`.

Re-run prebuild after:
- editing `app.json` (`name`, `slug`, `bundleIdentifier`, `package`, `plugins`, etc.)
- adding an Expo config plugin (`op-sqlite`, `expo-build-properties`, `@react-native-firebase/app`, …)
- upgrading the Expo SDK

```bash
npx expo prebuild --clean
```

`--clean` deletes `ios/` and `android/` before regenerating. **Warning:** this also deletes:
- `ios/VisTrainer/GoogleService-Info.plist`
- `android/app/google-services.json`

Re-copy them from your secure store after every clean prebuild. Story 1d.1 introduces a config-plugin-driven copy step (likely via `@react-native-firebase/app`'s Expo plugin) so the manual copy goes away.

See `client-app/README.md` for the full auth-flow walkthrough — identical here.
