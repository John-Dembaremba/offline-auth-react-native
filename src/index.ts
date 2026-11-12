import OfflineAuthModule from './OfflineAuthModule';

export async function getPublicKey(): Promise<string> {
  return OfflineAuthModule.getPublicKey();
}
