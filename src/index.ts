import type { CredentialSyncResult, OfflineCredentialSnapshot } from './OfflineAuth.types';
import OfflineAuthModule from './OfflineAuthModule';

export type {
  CredentialSyncFailure,
  CredentialSyncResult,
  EncryptedEnvelope,
  OfflineCredentialSnapshot,
  SynchronizedCredential,
  CredentialFailure,
  LocalCredentialSyncFailureReason,
} from './OfflineAuth.types';

export async function getPublicKey(): Promise<string> {
  return OfflineAuthModule.getPublicKey();
}

export async function sync(snapshot: OfflineCredentialSnapshot): Promise<CredentialSyncResult> {
  return OfflineAuthModule.sync(JSON.stringify(snapshot));
}
