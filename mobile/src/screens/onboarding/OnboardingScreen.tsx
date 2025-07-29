import { useNavigation } from '@react-navigation/native';
import { useState } from 'react';
import {
  Dimensions,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

const { width, height } = Dimensions.get('window');

const onboardingData = [
  {
    title: '🇩🇪 Bem-vindo ao German Learning',
    subtitle: 'Aprenda alemão de forma inteligente e personalizada',
    description:
      'Descubra uma nova maneira de aprender alemão adaptada aos seus interesses e nível de proficiência.',
  },
  {
    title: '🎯 Aprendizado Personalizado',
    subtitle: 'Conteúdo baseado nos seus interesses',
    description:
      'Escolha temas como tecnologia, esportes, cultura e receba lições personalizadas.',
  },
  {
    title: '📊 Teste de Proficiência CEFR',
    subtitle: 'Descubra seu nível atual',
    description:
      'Faça um teste adapativo e descubra se você está no nível A1, A2, B1, B2, C1 ou C2.',
  },
  {
    title: '🚀 Pronto para começar?',
    subtitle: 'Sua jornada de aprendizado espera por você',
    description:
      'Vamos começar criando seu perfil personalizado e descobrindo seus interesses.',
  },
];

export const OnboardingScreen = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const navigation = useNavigation();

  const handleNext = () => {
    if (currentIndex < onboardingData.length - 1) {
      setCurrentIndex(currentIndex + 1);
    } else {
      // Navigate to Auth screen
      navigation.navigate('Auth' as never);
    }
  };

  const handleSkip = () => {
    navigation.navigate('Auth' as never);
  };

  const currentData = onboardingData[currentIndex];
  const isLastSlide = currentIndex === onboardingData.length - 1;

  return (
    <View style={styles.container}>
      <StatusBar barStyle='dark-content' backgroundColor='#fff' />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={handleSkip} style={styles.skipButton}>
          <Text style={styles.skipText}>Pular</Text>
        </TouchableOpacity>
      </View>

      {/* Content */}
      <View style={styles.content}>
        <Text style={styles.title}>{currentData.title}</Text>
        <Text style={styles.subtitle}>{currentData.subtitle}</Text>
        <Text style={styles.description}>{currentData.description}</Text>
      </View>

      {/* Pagination Dots */}
      <View style={styles.pagination}>
        {onboardingData.map((_, index) => (
          <View
            key={index}
            style={[
              styles.dot,
              index === currentIndex ? styles.activeDot : styles.inactiveDot,
            ]}
          />
        ))}
      </View>

      {/* Footer */}
      <View style={styles.footer}>
        <TouchableOpacity style={styles.nextButton} onPress={handleNext}>
          <Text style={styles.nextButtonText}>
            {isLastSlide ? 'Começar' : 'Próximo'}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 20,
  },
  skipButton: {
    padding: 10,
  },
  skipText: {
    fontSize: 16,
    color: '#666',
    fontWeight: '500',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    lineHeight: 40,
  },
  subtitle: {
    fontSize: 20,
    fontWeight: '600',
    textAlign: 'center',
    marginBottom: 15,
    color: '#333',
    lineHeight: 28,
  },
  description: {
    fontSize: 16,
    textAlign: 'center',
    color: '#666',
    lineHeight: 24,
    marginBottom: 40,
  },
  pagination: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 40,
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginHorizontal: 5,
  },
  activeDot: {
    backgroundColor: '#2196F3',
  },
  inactiveDot: {
    backgroundColor: '#E0E0E0',
  },
  footer: {
    paddingHorizontal: 40,
    paddingBottom: 50,
  },
  nextButton: {
    backgroundColor: '#2196F3',
    paddingVertical: 15,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  nextButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
});
