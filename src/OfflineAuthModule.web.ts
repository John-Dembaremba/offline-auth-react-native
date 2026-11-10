import { registerWebModule, NativeModule } from 'expo';

// OfflineAuthModule is not available on the web platform.
class OfflineAuthModule extends NativeModule<Record<string, never>> {}

export default registerWebModule(OfflineAuthModule, 'OfflineAuthModule');
