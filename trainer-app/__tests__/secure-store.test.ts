jest.mock('react-native-mmkv', () => {
  const map = new Map<string, string>();
  return {
    MMKV: jest.fn().mockImplementation(() => ({
      set: (k: string, v: string) => map.set(k, v),
      getString: (k: string) => map.get(k),
      delete: (k: string) => map.delete(k),
    })),
  };
});

import {
  setFirebaseIdToken,
  getFirebaseIdToken,
  setFirebaseRefreshToken,
  getFirebaseRefreshToken,
  clearAuthTokens,
} from '../src/storage/secure-store';

describe('secure-store (MMKV-backed)', () => {
  beforeEach(() => {
    clearAuthTokens();
  });

  it('round-trips a Firebase ID token', () => {
    setFirebaseIdToken('id-token-abc');
    expect(getFirebaseIdToken()).toBe('id-token-abc');
  });

  it('round-trips a Firebase refresh token', () => {
    setFirebaseRefreshToken('refresh-token-xyz');
    expect(getFirebaseRefreshToken()).toBe('refresh-token-xyz');
  });

  it('clearAuthTokens removes both tokens', () => {
    setFirebaseIdToken('id');
    setFirebaseRefreshToken('refresh');
    clearAuthTokens();
    expect(getFirebaseIdToken()).toBeUndefined();
    expect(getFirebaseRefreshToken()).toBeUndefined();
  });
});
