import { useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { getPublicKey } from 'offline-auth-react-native';

export default function App() {
  const [publicKey, setPublicKey] = useState<string | null>(null);
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

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Offline Auth</Text>

      <Text style={styles.subtitle}>Expo Modules API - Android Native Bridge Test</Text>

      <Pressable style={styles.button} onPress={handleGetPublicKey} disabled={loading}>
        <Text style={styles.buttonText}>Get Public Key</Text>
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

      {error && (
        <View style={styles.result}>
          <Text style={styles.error}>Error: {error}</Text>
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
