import { NativeModule, requireNativeModule } from 'expo';

declare class OfflineAuthModule extends NativeModule<{}> {
  setValueAsync(value: string): Promise<void>;
}

export default requireNativeModule<OfflineAuthModule>('OfflineAuth');
