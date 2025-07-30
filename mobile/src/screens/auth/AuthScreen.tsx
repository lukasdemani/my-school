import { useAppDispatch, useAppSelector } from '@/hooks/redux';
import { signInUser, signUpUser } from '@/store/actions/authActions';
import { clearError } from '@/store/slices/authSlice';
import { useNavigation } from '@react-navigation/native';
import { useEffect, useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Platform,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';

export const AuthScreen = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [name, setName] = useState('');
  const [nativeLanguage, setNativeLanguage] = useState('pt');

  const navigation = useNavigation();
  const dispatch = useAppDispatch();
  const { isLoading, error, isAuthenticated } = useAppSelector(
    (state) => state.auth
  );

  // Navigate to main app when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigation.navigate('MainTabs' as never);
    }
  }, [isAuthenticated, navigation]);

  // Show error alerts
  useEffect(() => {
    if (error) {
      Alert.alert('Erro', error, [
        { text: 'OK', onPress: () => dispatch(clearError()) },
      ]);
    }
  }, [error, dispatch]);

  const handleSubmit = async () => {
    if (!email || !password) {
      Alert.alert('Erro', 'Por favor, preencha todos os campos obrigatórios');
      return;
    }

    if (!isLogin && password !== confirmPassword) {
      Alert.alert('Erro', 'As senhas não coincidem');
      return;
    }

    if (!isLogin && !name) {
      Alert.alert('Erro', 'Por favor, preencha seu nome');
      return;
    }

    try {
      if (isLogin) {
        await dispatch(signInUser({ email, password })).unwrap();
      } else {
        await dispatch(
          signUpUser({
            name,
            email,
            password,
            nativeLanguage,
          })
        ).unwrap();
      }
    } catch (error) {
      // Error will be handled by useEffect above
      console.error('Authentication error:', error);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle='dark-content' backgroundColor='#fff' />

      <View style={styles.header}>
        <Text style={styles.title}>
          {isLogin ? 'Bem-vindo de volta!' : 'Criar Nova Conta'}
        </Text>
        <Text style={styles.subtitle}>
          {isLogin
            ? 'Faça login com seu email e senha para continuar aprendendo alemão'
            : 'Preencha os dados abaixo para criar sua conta e começar a aprender alemão'}
        </Text>
      </View>

      <View style={styles.form}>
        {!isLogin && (
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Nome Completo *</Text>
            <Text style={styles.inputHelper}>
              Como você gostaria de ser chamado no app
            </Text>
            <TextInput
              style={styles.input}
              placeholder='Digite seu nome completo'
              value={name}
              onChangeText={setName}
              autoCapitalize='words'
            />
          </View>
        )}

        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>Email *</Text>
          <Text style={styles.inputHelper}>
            {isLogin
              ? 'Use o email cadastrado para fazer login'
              : 'Este será seu email de acesso ao app'}
          </Text>
          <TextInput
            style={styles.input}
            placeholder='exemplo@email.com'
            value={email}
            onChangeText={setEmail}
            keyboardType='email-address'
            autoCapitalize='none'
            autoCorrect={false}
          />
        </View>

        <View style={styles.inputContainer}>
          <Text style={styles.inputLabel}>Senha *</Text>
          <Text style={styles.inputHelper}>
            {isLogin
              ? 'Digite sua senha de acesso'
              : 'Mínimo 6 caracteres para maior segurança'}
          </Text>
          <TextInput
            style={styles.input}
            placeholder='Digite sua senha'
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            autoCapitalize='none'
          />
        </View>

        {!isLogin && (
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Confirmar Senha *</Text>
            <Text style={styles.inputHelper}>
              Digite a mesma senha novamente
            </Text>
            <TextInput
              style={styles.input}
              placeholder='Confirme sua senha'
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              secureTextEntry
              autoCapitalize='none'
            />
          </View>
        )}

        <View style={styles.requiredNote}>
          <Text style={styles.requiredNoteText}>
            * Todos os campos são obrigatórios
          </Text>
        </View>

        <TouchableOpacity
          style={[
            styles.submitButton,
            isLoading && styles.submitButtonDisabled,
          ]}
          onPress={handleSubmit}
          disabled={isLoading}
        >
          <Text style={styles.submitButtonText}>
            {isLoading
              ? 'Processando...'
              : isLogin
              ? 'Fazer Login'
              : 'Criar Minha Conta'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.switchButton}
          onPress={() => setIsLogin(!isLogin)}
        >
          <Text style={styles.switchButtonText}>
            {isLogin
              ? 'Ainda não tem conta? Cadastre-se aqui'
              : 'Já possui uma conta? Faça login aqui'}
          </Text>
        </TouchableOpacity>
      </View>

      <View style={styles.footer}>
        <Text style={styles.footerText}>
          Ao continuar, você concorda com nossos{'\n'}
          <Text style={styles.linkText}>Termos de Uso</Text> e{' '}
          <Text style={styles.linkText}>Política de Privacidade</Text>
        </Text>
      </View>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    paddingTop: 60,
    paddingHorizontal: 40,
    paddingBottom: 40,
    alignItems: 'center',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 10,
    color: '#333',
  },
  subtitle: {
    fontSize: 16,
    textAlign: 'center',
    color: '#666',
    lineHeight: 22,
  },
  form: {
    flex: 1,
    paddingHorizontal: 40,
  },
  inputContainer: {
    marginBottom: 20,
  },
  inputLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  inputHelper: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
    lineHeight: 18,
  },
  input: {
    borderWidth: 1,
    borderColor: '#E0E0E0',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 16,
    backgroundColor: '#FAFAFA',
  },
  requiredNote: {
    marginBottom: 20,
    alignItems: 'center',
  },
  requiredNoteText: {
    fontSize: 12,
    color: '#999',
    fontStyle: 'italic',
  },
  submitButton: {
    backgroundColor: '#2196F3',
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 20,
    marginBottom: 20,
  },
  submitButtonDisabled: {
    backgroundColor: '#B0BEC5',
  },
  submitButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
  switchButton: {
    alignItems: 'center',
    paddingVertical: 15,
  },
  switchButtonText: {
    color: '#2196F3',
    fontSize: 16,
    fontWeight: '500',
  },
  footer: {
    paddingHorizontal: 40,
    paddingBottom: 30,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 12,
    color: '#999',
    textAlign: 'center',
    lineHeight: 18,
  },
  linkText: {
    color: '#2196F3',
    fontWeight: '500',
  },
});
