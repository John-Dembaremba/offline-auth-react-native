export interface EncryptedEnvelope {
  key_algorithm: string;
  content_algorithm: string;
  encrypted_key: string;
  nonce: string;
  ciphertext: string;
}

export interface SynchronizedCredential {
  user_id: string;
  username: string;
  envelope: EncryptedEnvelope;
}

export interface CredentialFailure {
  user_id: string;
  username: string;
  reason: string;
}

export interface OfflineCredentialSnapshot {
  protocol_version: number;
  credentials: SynchronizedCredential[];
  failures: CredentialFailure[];
}

export type LocalCredentialSyncFailureReason =
  | 'unsupported_key_algorithm'
  | 'unsupported_content_algorithm'
  | 'decryption_failed'
  | 'invalid_credential_payload'
  | 'credential_validation_failed'
  | 'unsupported_password_algorithm'
  | 'credential_version_rollback'
  | 'credential_processing_failed';

export interface CredentialSyncFailure {
  username: string;
  source: 'local' | 'backend';
  reason: LocalCredentialSyncFailureReason | string;
  storedCredentialVersion: number | null;
  incomingCredentialVersion: number | null;
}

export interface CredentialSyncResult {
  successfulUsernames: string[];
  failures: CredentialSyncFailure[];
}
