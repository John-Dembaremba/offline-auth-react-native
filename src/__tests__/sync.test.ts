import { beforeEach, describe, expect, it, jest } from '@jest/globals';

import type { CredentialSyncResult, OfflineCredentialSnapshot } from '../OfflineAuth.types';
import OfflineAuthModule from '../OfflineAuthModule';
import { sync } from '../index';

jest.mock('../OfflineAuthModule', () => ({
  __esModule: true,
  default: {
    sync: jest.fn(),
  },
}));

const mockSync = OfflineAuthModule.sync as jest.MockedFunction<typeof OfflineAuthModule.sync>;

const snapshot: OfflineCredentialSnapshot = {
  protocol_version: 1,
  credentials: [],
  failures: [
    {
      user_id: '2',
      username: 'bob',
      reason: 'unusable_password',
    },
  ],
};

const syncResult: CredentialSyncResult = {
  successfulUsernames: [],
  failures: [
    {
      username: 'bob',
      source: 'backend',
      reason: 'unusable_password',
      storedCredentialVersion: null,
      incomingCredentialVersion: null,
    },
  ],
};

describe('sync', () => {
  beforeEach(() => {
    mockSync.mockReset();
  });

  it('serializes the snapshot and delegates to native', async () => {
    mockSync.mockResolvedValue(syncResult);

    const result = await sync(snapshot);

    expect(result).toEqual(syncResult);
    expect(mockSync).toHaveBeenCalledTimes(1);
    expect(mockSync).toHaveBeenCalledWith(JSON.stringify(snapshot));
  });

  it('propagates native sync errors', async () => {
    const error = new Error('Unsupported protocol version');

    mockSync.mockRejectedValue(error);

    await expect(sync(snapshot)).rejects.toBe(error);
    expect(mockSync).toHaveBeenCalledTimes(1);
  });
});
