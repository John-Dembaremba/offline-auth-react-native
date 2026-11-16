import { beforeEach, describe, expect, it, jest } from '@jest/globals';

import OfflineAuthModule from '../OfflineAuthModule';
import { authenticate } from '../index';

jest.mock('../OfflineAuthModule', () => ({
  __esModule: true,
  default: {
    authenticate: jest.fn(),
  },
}));

const mockAuthenticate = OfflineAuthModule.authenticate as jest.MockedFunction<
  typeof OfflineAuthModule.authenticate
>;

describe('authenticate', () => {
  beforeEach(() => {
    mockAuthenticate.mockReset();
  });

  it('returns true when authentication succeeds', async () => {
    mockAuthenticate.mockResolvedValue(true);

    const result = await authenticate('alice', 'correct-password');

    expect(result).toBe(true);
    expect(mockAuthenticate).toHaveBeenCalledWith('alice', 'correct-password');
    expect(mockAuthenticate).toHaveBeenCalledTimes(1);
  });

  it('returns false when authentication fails', async () => {
    mockAuthenticate.mockResolvedValue(false);

    const result = await authenticate('alice', 'wrong-password');

    expect(result).toBe(false);
    expect(mockAuthenticate).toHaveBeenCalledWith('alice', 'wrong-password');
  });

  it('returns false for an unknown user', async () => {
    mockAuthenticate.mockResolvedValue(false);

    const result = await authenticate('unknown_user', 'test-password');

    expect(result).toBe(false);
  });

  it('propagates native authentication errors', async () => {
    const error = new Error('Native authentication failed');

    mockAuthenticate.mockRejectedValue(error);

    await expect(authenticate('alice', 'test-password')).rejects.toBe(error);
  });
});
