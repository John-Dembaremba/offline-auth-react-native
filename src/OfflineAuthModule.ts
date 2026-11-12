import { NativeModule, requireNativeModule } from 'expo';

declare class OfflineAuthModule extends NativeModule<Record<string, never>> {
  getPublicKey(): Promise<string>;
}

export default requireNativeModule<OfflineAuthModule>('OfflineAuth');
