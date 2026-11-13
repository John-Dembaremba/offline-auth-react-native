import { useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { requireNativeModule } from 'expo';
import { getPublicKey, sync, type OfflineCredentialSnapshot } from 'offline-auth-react-native';

export default function App() {
  const [publicKey, setPublicKey] = useState<string | null>(null);
  const [syncResult, setSyncResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleGetPublicKey = async (): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      const key = await getPublicKey();
      setPublicKey(key);
    } catch (err: unknown) {
      setPublicKey(null);
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const handleInvalidSync = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    setSyncResult(null);

    try {
      const nativeModule = requireNativeModule('OfflineAuth') as {
        sync?: (snapshotJson: string) => Promise<unknown>;
        getPublicKey?: () => Promise<string>;
      };

      const diagnostics = [
        `JavaScript sync: ${typeof sync}`,
        `Native sync: ${typeof nativeModule.sync}`,
        `Native getPublicKey: ${typeof nativeModule.getPublicKey}`,
      ];

      console.log(diagnostics.join('\n'));

      if (typeof sync !== 'function' || typeof nativeModule.sync !== 'function') {
        setError(diagnostics.join('\n'));
        return;
      }

      const snapshot: OfflineCredentialSnapshot = {
        protocol_version: 999,
        credentials: [],
        failures: [],
      };

      const result = await sync(snapshot);
      setSyncResult(JSON.stringify(result, null, 2));
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      const stack = err instanceof Error ? err.stack : undefined;

      console.error('Invalid sync error:', err);
      setError([message, stack].filter(Boolean).join('\n'));
    } finally {
      setLoading(false);
    }
  };

  const handleValidSync = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    setSyncResult(null);

    const snapshot: OfflineCredentialSnapshot = {
      protocol_version: 1,
      credentials: [],
      failures: [
        {
          user_id: '2',
          username: 'sync_test_bob',
          reason: 'unusable_password',
        },
      ],
    };

    try {
      const result = await sync(snapshot);

      console.log('Valid sync result:', JSON.stringify(result, null, 2));

      setSyncResult(JSON.stringify(result, null, 2));
    } catch (err: unknown) {
      console.error('Valid sync error:', err);
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Offline Auth</Text>

      <Text style={styles.subtitle}>Expo Modules API - Android Native Bridge Test</Text>

      <Pressable style={styles.button} onPress={handleGetPublicKey} disabled={loading}>
        <Text style={styles.buttonText}>Get Public Key</Text>
      </Pressable>

      <Pressable style={styles.button} onPress={handleInvalidSync} disabled={loading}>
        <Text style={styles.buttonText}>Test Invalid Sync</Text>
      </Pressable>

      <Pressable style={styles.button} onPress={handleValidSync} disabled={loading}>
        <Text style={styles.buttonText}>Test Valid Sync</Text>
      </Pressable>

      {loading && <ActivityIndicator size="large" />}

      {publicKey && (
        <View style={styles.result}>
          <Text style={styles.label}>Public Key:</Text>
          <Text selectable style={styles.key}>
            {publicKey}
          </Text>
        </View>
      )}

      {syncResult && (
        <View style={styles.result}>
          <Text style={styles.label}>Synchronization Result:</Text>
          <Text selectable style={styles.key}>
            {syncResult}
          </Text>
        </View>
      )}

      {error && (
        <View style={styles.result}>
          <Text style={styles.label}>Native Error:</Text>
          <Text selectable style={styles.error}>
            {error}
          </Text>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
    gap: 20,
  },
  title: {
    fontSize: 30,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    textAlign: 'center',
  },
  button: {
    backgroundColor: '#2563eb',
    padding: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  result: {
    padding: 16,
    borderRadius: 10,
    backgroundColor: '#e5e7eb',
  },
  label: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  key: {
    fontSize: 12,
    color: '#111827',
  },
  error: {
    color: '#dc2626',
  },
});
