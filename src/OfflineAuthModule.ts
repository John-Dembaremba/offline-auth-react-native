import { NativeModule, requireNativeModule } from 'expo';

declare class OfflineAuthModule extends NativeModule<Record<string, never>> {
  setValueAsync(value: string): Promise<void>;
}

export default requireNativeModule<OfflineAuthModule>('OfflineAuth');
