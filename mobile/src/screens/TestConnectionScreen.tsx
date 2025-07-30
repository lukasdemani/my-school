import config from '@/config';
import React, { useState } from 'react';
import { Alert, Button, StyleSheet, Text, View } from 'react-native';

export const TestConnectionScreen: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<string>('');

  const testConnection = async () => {
    setIsLoading(true);
    setConnectionStatus('Testando conexão...');

    try {
      // Fazer um teste simples de conexão
      const response = await fetch(`${config.API_BASE_URL}/health`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        setConnectionStatus('✅ Backend conectado com sucesso!');
        Alert.alert('Sucesso', 'Conexão com o backend estabelecida!');
      } else {
        setConnectionStatus(`❌ Erro de conexão: ${response.status}`);
        Alert.alert('Erro', `Falha na conexão: ${response.status}`);
      }
    } catch (error) {
      console.error('Erro de conexão:', error);
      setConnectionStatus(
        `❌ Erro: ${
          error instanceof Error ? error.message : 'Erro desconhecido'
        }`
      );
      Alert.alert('Erro', 'Falha na conexão com o backend');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Teste de Conexão Backend</Text>
      <Text style={styles.subtitle}>URL: {config.API_BASE_URL}</Text>

      <Button
        title={isLoading ? 'Testando...' : 'Testar Conexão'}
        onPress={testConnection}
        disabled={isLoading}
      />

      {connectionStatus ? (
        <Text style={styles.status}>{connectionStatus}</Text>
      ) : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 30,
  },
  status: {
    fontSize: 16,
    marginTop: 20,
    textAlign: 'center',
  },
});
