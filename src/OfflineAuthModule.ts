import { NativeModule, requireNativeModule } from 'expo';

import type { CredentialSyncResult } from './OfflineAuth.types';

declare class OfflineAuthModule extends NativeModule<Record<string, never>> {
  getPublicKey(): Promise<string>;

  sync(snapshotJson: string): Promise<CredentialSyncResult>;
}

export default requireNativeModule<OfflineAuthModule>('OfflineAuth');
