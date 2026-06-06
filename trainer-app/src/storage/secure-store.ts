import { MMKV } from 'react-native-mmkv';

const STORAGE_ID = 'vis-trainer-secure';

export const secureStore = new MMKV({ id: STORAGE_ID });

const KEYS = {
  FIREBASE_ID_TOKEN: 'auth.firebase_id_token',
  FIREBASE_REFRESH_TOKEN: 'auth.firebase_refresh_token',
} as const;

export function setFirebaseIdToken(token: string): void {
  secureStore.set(KEYS.FIREBASE_ID_TOKEN, token);
}

export function getFirebaseIdToken(): string | undefined {
  return secureStore.getString(KEYS.FIREBASE_ID_TOKEN);
}

export function setFirebaseRefreshToken(token: string): void {
  secureStore.set(KEYS.FIREBASE_REFRESH_TOKEN, token);
}

export function getFirebaseRefreshToken(): string | undefined {
  return secureStore.getString(KEYS.FIREBASE_REFRESH_TOKEN);
}

export function clearAuthTokens(): void {
  secureStore.delete(KEYS.FIREBASE_ID_TOKEN);
  secureStore.delete(KEYS.FIREBASE_REFRESH_TOKEN);
}
