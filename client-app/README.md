# Client App — Vis

React Native 0.85 + Expo SDK 56 (bare workflow) — PT member-facing app. Phase 0 ships only the auth shell:
`Login → (Pending | HomeShell)` driven by Firebase Auth + backend `GET /auth/me`.

## Prerequisites

- Node 18+
- Xcode 16+ + CocoaPods (`brew install cocoapods`)
- Android Studio + an emulator (for Android builds)
- Backend running locally on `http://localhost:8080` (Android emulator hits `10.0.2.2:8080`
  automatically — see `src/config.ts`)

## Local-only Firebase config (NEVER commit)

These come from the Firebase Console for the **Client App** entries (`in.vis.client`).
Both files are `.gitignore`d.

| File | Source | Drop into |
| -- | -- | -- |
| `google-services.json` | Firebase Console → Project settings → Android app `in.vis.client` | `android/app/google-services.json` |
| `GoogleService-Info.plist` | Firebase Console → iOS app `in.vis.client` | `ios/VisClient/GoogleService-Info.plist` (also drag-drop into the Xcode project so it's in the resources bundle) |

Then update `src/config.ts` with the **Web client ID** from
`Firebase Console → Project settings → General → Web SDK configuration → Web client ID`.
This is required for `@react-native-google-signin/google-signin` to mint a Firebase ID token.

## First-run setup

```bash
npm install                 # installs JS deps (already run by scaffold)
cd ios && pod install && cd ..   # iOS native modules
```

Add the Firebase Gradle plugin in `android/build.gradle` and apply it in
`android/app/build.gradle` per `@react-native-firebase/auth` install docs (the CLI's auto-link
covers the rest).

## Run

```bash
# iOS
npx react-native run-ios

# Android (with an emulator running)
npx react-native run-android
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
- `ios/VisClient/GoogleService-Info.plist`
- `android/app/google-services.json`

Re-copy them from your secure store after every clean prebuild. Story 1d.1 introduces a config-plugin-driven copy step (likely via `@react-native-firebase/app`'s Expo plugin) so the manual copy goes away.

## Auth flow (Phase 0)

1. `App.tsx` mounts `AppNavigator`.
2. `AppNavigator.useEffect` subscribes to `auth().onAuthStateChanged`.
3. While the listener has not fired → `ActivityIndicator` splash.
4. Listener fires:
   - No Firebase user → `LoginScreen` (Continue with Google).
   - Firebase user present → `fetchMe()` against `/auth/me`:
     - 200 → `HomeShell` (renders user JSON).
     - 404 → `PendingScreen` (account not yet activated by an admin).
     - Anything else → falls back to `PendingScreen` for now (graceful degradation).
5. `signOut()` (in `PendingScreen` / `HomeShell`) clears Firebase + Google session, dropping
   back to step 4.

`apiService.ts` injects `Authorization: Bearer <firebase-id-token>` on every request via an
Axios interceptor that calls `getIdToken()` at request time.
