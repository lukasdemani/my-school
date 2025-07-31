import { logoutUser } from '@/store/actions/authActions';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import Ionicons from '@expo/vector-icons/Ionicons';
import { useNavigation } from '@react-navigation/native';
import {
  Alert,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

export const ProfileScreen = () => {
  const dispatch = useAppDispatch();
  const navigation = useNavigation();
  const { user, isLoading } = useAppSelector((state) => state.auth);

  const handleLogout = () => {
    Alert.alert('Logout', 'Tem certeza que deseja sair da sua conta?', [
      {
        text: 'Cancelar',
        style: 'cancel',
      },
      {
        text: 'Sair',
        style: 'destructive',
        onPress: async () => {
          try {
            await dispatch(logoutUser()).unwrap();
            // Navigation will be handled by App.tsx when auth state changes
          } catch (error) {
            Alert.alert(
              'Erro',
              'Não foi possível fazer logout. Tente novamente.'
            );
          }
        },
      },
    ]);
  };

  if (!user) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Carregando perfil...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.profileImageContainer}>
            <Ionicons name='person-circle' size={80} color='#2196F3' />
          </View>
          <Text style={styles.userName}>{user.name}</Text>
          <Text style={styles.userEmail}>{user.email}</Text>
          {user.nativeLanguage && (
            <Text style={styles.nativeLanguage}>
              Idioma nativo:{' '}
              {user.nativeLanguage === 'pt' ? 'Português' : user.nativeLanguage}
            </Text>
          )}
        </View>

        {/* User Bio */}
        {user.bio && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Sobre</Text>
            <Text style={styles.bioText}>{user.bio}</Text>
          </View>
        )}

        {/* Interests */}
        {user.interests && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Interesses</Text>
            <Text style={styles.interestsText}>{user.interests}</Text>
          </View>
        )}

        {/* Workspaces */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Workspaces</Text>
          {user.workspaces && user.workspaces.length > 0 ? (
            user.workspaces.map((workspace) => (
              <View key={workspace.id} style={styles.workspaceCard}>
                <Text style={styles.workspaceName}>{workspace.name}</Text>
                <Text style={styles.workspaceDetails}>
                  {workspace.targetLanguage} - {workspace.languageLevel}
                </Text>
                {workspace.description && (
                  <Text style={styles.workspaceDescription}>
                    {workspace.description}
                  </Text>
                )}
                {workspace.progress && (
                  <View style={styles.progressInfo}>
                    <Text style={styles.progressText}>
                      📚 {workspace.progress.totalLessonsCompleted} lições
                      completadas
                    </Text>
                    <Text style={styles.progressText}>
                      ⭐ {workspace.progress.totalPoints} pontos
                    </Text>
                    <Text style={styles.progressText}>
                      🔥 {workspace.progress.currentStreak} dias consecutivos
                    </Text>
                  </View>
                )}
              </View>
            ))
          ) : (
            <Text style={styles.emptyText}>Nenhum workspace criado ainda</Text>
          )}
        </View>

        {/* Settings Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Configurações</Text>

          <TouchableOpacity style={styles.settingItem}>
            <Ionicons name='person-outline' size={24} color='#666' />
            <Text style={styles.settingText}>Editar Perfil</Text>
            <Ionicons name='chevron-forward' size={20} color='#ccc' />
          </TouchableOpacity>

          <TouchableOpacity style={styles.settingItem}>
            <Ionicons name='notifications-outline' size={24} color='#666' />
            <Text style={styles.settingText}>Notificações</Text>
            <Ionicons name='chevron-forward' size={20} color='#ccc' />
          </TouchableOpacity>

          <TouchableOpacity style={styles.settingItem}>
            <Ionicons name='language-outline' size={24} color='#666' />
            <Text style={styles.settingText}>Idioma do App</Text>
            <Ionicons name='chevron-forward' size={20} color='#ccc' />
          </TouchableOpacity>

          <TouchableOpacity style={styles.settingItem}>
            <Ionicons name='help-circle-outline' size={24} color='#666' />
            <Text style={styles.settingText}>Ajuda e Suporte</Text>
            <Ionicons name='chevron-forward' size={20} color='#ccc' />
          </TouchableOpacity>

          <TouchableOpacity style={styles.settingItem}>
            <Ionicons name='shield-outline' size={24} color='#666' />
            <Text style={styles.settingText}>Privacidade</Text>
            <Ionicons name='chevron-forward' size={20} color='#ccc' />
          </TouchableOpacity>
        </View>

        {/* Logout Button */}
        <View style={styles.logoutSection}>
          <TouchableOpacity
            style={styles.logoutButton}
            onPress={handleLogout}
            disabled={isLoading}
          >
            <Ionicons name='log-out-outline' size={24} color='#FF4444' />
            <Text style={styles.logoutText}>
              {isLoading ? 'Saindo...' : 'Sair da Conta'}
            </Text>
          </TouchableOpacity>
        </View>

        {/* App Info */}
        <View style={styles.appInfo}>
          <Text style={styles.appVersion}>German Learning App v1.0.0</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  scrollView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: '#666',
  },
  header: {
    backgroundColor: '#fff',
    alignItems: 'center',
    paddingVertical: 30,
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  profileImageContainer: {
    marginBottom: 15,
  },
  userName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  userEmail: {
    fontSize: 16,
    color: '#666',
    marginBottom: 5,
  },
  nativeLanguage: {
    fontSize: 14,
    color: '#888',
    fontStyle: 'italic',
  },
  section: {
    backgroundColor: '#fff',
    marginBottom: 20,
    paddingVertical: 20,
    paddingHorizontal: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 15,
  },
  bioText: {
    fontSize: 16,
    color: '#666',
    lineHeight: 24,
  },
  interestsText: {
    fontSize: 16,
    color: '#666',
    lineHeight: 24,
  },
  workspaceCard: {
    backgroundColor: '#f8f9fa',
    borderRadius: 12,
    padding: 15,
    marginBottom: 10,
    borderLeftWidth: 4,
    borderLeftColor: '#2196F3',
  },
  workspaceName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  workspaceDetails: {
    fontSize: 14,
    color: '#666',
    marginBottom: 5,
  },
  workspaceDescription: {
    fontSize: 14,
    color: '#888',
    marginBottom: 10,
    fontStyle: 'italic',
  },
  progressInfo: {
    marginTop: 10,
  },
  progressText: {
    fontSize: 12,
    color: '#666',
    marginBottom: 2,
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
    textAlign: 'center',
    fontStyle: 'italic',
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  settingText: {
    flex: 1,
    fontSize: 16,
    color: '#333',
    marginLeft: 15,
  },
  logoutSection: {
    marginBottom: 20,
  },
  logoutButton: {
    backgroundColor: '#fff',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 15,
    marginHorizontal: 20,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#FF4444',
  },
  logoutText: {
    fontSize: 16,
    color: '#FF4444',
    fontWeight: '600',
    marginLeft: 10,
  },
  appInfo: {
    alignItems: 'center',
    paddingVertical: 20,
    paddingBottom: 40,
  },
  appVersion: {
    fontSize: 12,
    color: '#999',
  },
});
