import { NativeModule } from 'expo';

class OfflineAuthWebModule extends NativeModule<Record<string, never>> {
  async getPublicKey(): Promise<string> {
    throw new Error('offline-auth-react-native is only supported on Android.');
  }
}

export default new OfflineAuthWebModule();
