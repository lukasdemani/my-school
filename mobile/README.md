# 📱 German Learning App - Mobile

Frontend React Native do aplicativo de aprendizado de alemão com foco em performance e boas práticas.

## 🏗️ Arquitetura

### Tecnologias Principais

- **React Native** + **Expo** - Framework mobile
- **TypeScript** - Tipagem estática
- **Redux Toolkit** - Gerenciamento de estado
- **React Query** - Cache e sincronização de dados
- **React Navigation** - Navegação
- **Zod** - Validação de schemas

### Estrutura do Projeto

```
mobile/
├── src/
│   ├── components/         # Componentes reutilizáveis
│   ├── screens/           # Telas da aplicação
│   ├── navigation/        # Configuração de navegação
│   ├── store/            # Redux store e slices
│   ├── services/         # APIs e serviços externos
│   ├── hooks/            # Custom hooks
│   ├── utils/            # Utilitários e helpers
│   ├── constants/        # Constantes da aplicação
│   └── types/            # Definições de tipos TypeScript
├── assets/               # Recursos estáticos
└── config/              # Configurações
```

## 🎯 Funcionalidades Principais

### 📚 **Learning Journey Segmentada**

- **Topics Structure** - Hierarquia de tópicos por nível CEFR
- **Prerequisites System** - Desbloqueio baseado em progresso
- **Adaptive Learning** - Conteúdo personalizado por interesses
- **Progress Tracking** - Acompanhamento detalhado

### 🎭 **Sistema de Proficiência**

- **CEFR Assessment** - Testes de nivelamento (A1-C2)
- **Skill Breakdown** - Análise por habilidade
- **Adaptive Testing** - Questões que se adaptam à performance
- **Certification Prep** - Preparação para exames oficiais

### 👤 **Personalização Avançada**

- **Interest-based Content** - Conteúdo baseado em hobbies
- **Learning Style** - Adaptação ao estilo de aprendizado
- **Dynamic Difficulty** - Ajuste automático de dificuldade
- **Cultural Context** - Contexto cultural alemão

### 🎮 **Gamificação**

- **XP System** - Pontos de experiência
- **Achievement Badges** - Sistema de conquistas
- **Study Streaks** - Sequência de estudos
- **Leaderboards** - Ranking social (opcional)

## 🚀 Como Executar

### Pré-requisitos

- Node.js 18+
- Expo CLI
- iOS Simulator / Android Emulator

### Instalação

```bash
cd mobile
npm install
```

### Desenvolvimento

```bash
# iOS
npm run ios

# Android
npm run android

# Web (para desenvolvimento)
npm run web
```

### Scripts Disponíveis

```bash
npm run start        # Inicia o servidor Expo
npm run lint         # Executa linting
npm run type-check   # Verificação de tipos
npm run test         # Executa testes
```

## 📊 Performance Optimizations

### **State Management**

- Redux Toolkit para estado global eficiente
- React Query para cache inteligente de APIs
- Lazy loading de componentes
- Memoização com useMemo/useCallback

### **Rendering Optimizations**

- FlatList para listas grandes
- Image lazy loading
- Component code splitting
- Virtual scrolling

### **Network Optimizations**

- Request deduplication
- Background sync
- Offline-first approach
- Optimistic updates

### **Bundle Optimizations**

- Tree shaking automático
- Asset optimization
- Code splitting por rota
- Bundle analysis

## 🎨 Design System

### **Tema e Cores**

```typescript
// Baseado no nível CEFR
A1: '#4CAF50',  // Verde (Beginner)
A2: '#2196F3',  // Azul (Elementary)
B1: '#FF9800',  // Laranja (Intermediate)
B2: '#F44336',  // Vermelho (Upper-Intermediate)
C1: '#9C27B0', // Roxo (Advanced)
C2: '#607D8B'  // Cinza (Proficient)
```

### **Componentes Base**

- Design System consistente
- Componentes acessíveis
- Responsive design
- Dark/Light theme

## 📱 Navegação

### **Stack Principal**

```
🏠 Dashboard
├── Daily Goals
├── Progress Overview
└── Quick Actions

📚 Learning Path
├── Topic Categories
├── Current Lesson
└── Recommendations

🎯 Practice
├── Flashcards
├── Conversations
└── Mini Games

📊 Progress
├── Skill Analysis
├── Achievements
└── Study Stats

👤 Profile
├── Interests
├── Settings
└── Preferences
```

## 🔧 Configuração de Desenvolvimento

### **TypeScript Paths**

```typescript
"@/*": ["src/*"]
"@/components/*": ["src/components/*"]
"@/screens/*": ["src/screens/*"]
// etc...
```

### **ESLint + Prettier**

- Formatação automática
- Regras TypeScript
- React hooks rules
- Performance linting

## 📈 Roadmap

### **Commit 1** ✅ - Configuração inicial e tipos

### **Commit 2** - Configuração de navegação e estado

### **Commit 3** - Componentes base do design system

### **Commit 4** - Telas principais (Dashboard, Learning)

### **Commit 5** - Sistema de autenticação

### **Commit 6** - Integração com APIs do backend

### **Commit 7** - Sistema de proficiência

### **Commit 8** - Personalização e interesses

### **Commit 9** - Sistema de prática e exercícios

### **Commit 10** - Otimizações de performance

O app está sendo construído com foco em **performance**, **UX**, e **maintainability**!
