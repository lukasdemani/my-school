# 🇩🇪 German Learning App

Um aplicativo iOS completo para aprendizado de alemão com arquitetura moderna e separação clara entre frontend e backend.

## 📱 Funcionalidades

- **Learning Journey Segmentada**: Progressão por níveis CEFR (A1-C2)
- **Teste de Proficiência**: Assessment adaptativo para determinar nível
- **Personalização por Interesses**: Conteúdo baseado em hobbies e preferências
- **Vocabulário Inteligente**: Flashcards com spaced repetition
- **Conversação com IA**: Prática conversacional usando OpenAI
- **Gamificação**: XP, badges e streaks para motivação

## 🏗️ Arquitetura

```
my-school/
├── mobile/          # 📱 Frontend React Native + Expo
│   ├── src/         # Código TypeScript
│   ├── assets/      # Recursos estáticos
│   └── package.json # Dependências mobile
├── backend/         # ⚙️  Backend Scala + Play Framework
│   ├── app/         # Código Scala com ZIO + Pekko
│   ├── conf/        # Configurações
│   └── build.sbt    # Dependências Scala
├── shared/          # 🔗 Tipos e modelos compartilhados
└── docs/            # 📚 Documentação
```

## 🚀 Como Executar

### 📱 **Frontend (Mobile)**
```bash
cd mobile
npm install
npm start
```

### ⚙️ **Backend (Scala)**
```bash
cd backend
sbt compile
sbt run
```

## 🛠️ **Stack Tecnológica**

### **Frontend (mobile/)**
- **React Native** 0.74.5 + **Expo** 51.0.28
- **TypeScript** 5.3.3 (strict mode)
- **Redux Toolkit** 2.2.7 + **React Query** 5.59.0
- **React Navigation** 6.1.18
- **Zod** 3.23.8 para validação

### **Backend (backend/)**
- **Scala** 3.3.1 + **Play Framework**
- **ZIO** para programação funcional
- **Apache Pekko** (Akka successor) para actors
- **PostgreSQL** com repositories custom
- **OpenAI API** para conversação

## 📁 **Separação de Responsabilidades**

### **Frontend (`mobile/`)** 
- Interface do usuário React Native
- State management com Redux
- Navegação e UX
- Cache local com AsyncStorage
- Tipos TypeScript para UI

### **Backend (`backend/`)** 
- APIs RESTful
- Lógica de negócio em Scala
- Integração com banco de dados
- Processamento de IA
- Autenticação JWT

### **Shared (`shared/`)**
- Modelos de dados compartilhados
- Tipos de API contracts
- Utilitários comum

---

**⚠️ Importante**: Cada parte do projeto tem seu próprio `package.json`/`build.sbt` e deve ser executada independentemente.

## 🚀 Tecnologias

### Frontend

- **React Native** - Framework móvel
- **TypeScript** - Tipagem estática
- **React Navigation** - Navegação
- **React Query** - Gerenciamento de estado
- **Expo** - Tooling e desenvolvimento

### Backend

- **Scala 3** - Linguagem principal
- **Play Framework** - Framework web
- **PostgreSQL** - Banco de dados
- **Docker** - Containerização
- **OpenAI API** - Integração com LLM

## 🏃‍♂️ Como executar

### Backend (Scala)

```bash
cd backend
sbt run
```

### Mobile (React Native)

```bash
cd mobile
npm install
npx expo start
```

## 📚 Aprendizado

Este projeto foi criado para aprender:

- Desenvolvimento em Scala
- Arquitetura de aplicações móveis
- Integração com LLMs
- Boas práticas de desenvolvimento

## 🎯 Roadmap

- [ ] Setup inicial do projeto
- [ ] Backend Scala com Play Framework
- [ ] App React Native básico
- [ ] Sistema de autenticação
- [ ] Módulo de vocabulário
- [ ] Integração com LLM
- [ ] Sistema de progresso
- [ ] Deploy e CI/CD
