import Ionicons from '@expo/vector-icons/Ionicons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Text, View } from 'react-native';

// Import real screens
import { AuthScreen } from '@/screens/auth/AuthScreen';
import { OnboardingScreen } from '@/screens/onboarding/OnboardingScreen';

// Placeholder screens (will create proper components next)
const DashboardScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Dashboard Screen</Text>
  </View>
);

const LearningScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Learning Screen</Text>
  </View>
);

const PracticeScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Practice Screen</Text>
  </View>
);

const ProgressScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Progress Screen</Text>
  </View>
);

const ProfileScreen = () => (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text>Profile Screen</Text>
  </View>
);

import type { MainTabParamList, RootStackParamList } from '@/types';

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();

const MainTabNavigator = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          switch (route.name) {
            case 'Dashboard':
              iconName = focused ? 'home' : 'home-outline';
              break;
            case 'Learning':
              iconName = focused ? 'book' : 'book-outline';
              break;
            case 'Practice':
              iconName = focused ? 'fitness' : 'fitness-outline';
              break;
            case 'Progress':
              iconName = focused ? 'stats-chart' : 'stats-chart-outline';
              break;
            case 'Profile':
              iconName = focused ? 'person' : 'person-outline';
              break;
            default:
              iconName = 'help-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#2196F3',
        tabBarInactiveTintColor: 'gray',
        headerShown: false,
      })}
    >
      <Tab.Screen
        name='Dashboard'
        component={DashboardScreen}
        options={{ title: 'Início' }}
      />
      <Tab.Screen
        name='Learning'
        component={LearningScreen}
        options={{ title: 'Aprender' }}
      />
      <Tab.Screen
        name='Practice'
        component={PracticeScreen}
        options={{ title: 'Praticar' }}
      />
      <Tab.Screen
        name='Progress'
        component={ProgressScreen}
        options={{ title: 'Progresso' }}
      />
      <Tab.Screen
        name='Profile'
        component={ProfileScreen}
        options={{ title: 'Perfil' }}
      />
    </Tab.Navigator>
  );
};

export const AppNavigator = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName='Onboarding'
        screenOptions={{
          headerShown: false,
        }}
      >
        <Stack.Screen name='Onboarding' component={OnboardingScreen} />
        <Stack.Screen name='Auth' component={AuthScreen} />
        <Stack.Screen name='MainTabs' component={MainTabNavigator} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};
