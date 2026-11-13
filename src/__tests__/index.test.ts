import { beforeEach, describe, expect, it, jest } from '@jest/globals';

import OfflineAuthModule from '../OfflineAuthModule';
import { getPublicKey } from '../index';

jest.mock('../OfflineAuthModule', () => ({
  __esModule: true,
  default: {
    getPublicKey: jest.fn(),
  },
}));

const mockGetPublicKey = OfflineAuthModule.getPublicKey as jest.MockedFunction<
  typeof OfflineAuthModule.getPublicKey
>;

describe('getPublicKey', () => {
  beforeEach(() => {
    mockGetPublicKey.mockReset();
  });

  it('returns the public key from the native module', async () => {
    const publicKey = 'TEST_RSA_PUBLIC_KEY';

    mockGetPublicKey.mockResolvedValue(publicKey);

    await expect(getPublicKey()).resolves.toBe(publicKey);

    expect(mockGetPublicKey).toHaveBeenCalledTimes(1);
    expect(mockGetPublicKey).toHaveBeenCalledWith();
  });

  it('propagates native module errors', async () => {
    const error = new Error('Android Keystore unavailable');

    mockGetPublicKey.mockRejectedValue(error);

    await expect(getPublicKey()).rejects.toBe(error);

    expect(mockGetPublicKey).toHaveBeenCalledTimes(1);
  });

  it('calls the native module on every invocation', async () => {
    mockGetPublicKey.mockResolvedValue('TEST_RSA_PUBLIC_KEY');

    const firstKey = await getPublicKey();
    const secondKey = await getPublicKey();

    expect(firstKey).toBe(secondKey);
    expect(mockGetPublicKey).toHaveBeenCalledTimes(2);
  });
});
