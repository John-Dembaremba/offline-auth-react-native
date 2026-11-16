import { NativeModule, requireNativeModule } from 'expo';

import type { CredentialSyncResult } from './OfflineAuth.types';

declare class OfflineAuthModule extends NativeModule<Record<string, never>> {
  getPublicKey(): Promise<string>;

  sync(snapshotJson: string): Promise<CredentialSyncResult>;

  authenticate(username: string, password: string): Promise<boolean>;
}

export default requireNativeModule<OfflineAuthModule>('OfflineAuth');
