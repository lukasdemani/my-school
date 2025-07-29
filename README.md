# 🇩🇪 German Learning App

Um aplicativo iOS completo para aprendizado de alemão, construído com React Native e backend em Scala.

## 📱 Funcionalidades

- **Vocabulário**: Aprenda novas palavras com flashcards inteligentes
- **Gramática**: Exercícios interativos de gramática alemã
- **Conversação**: Prática com IA usando LLM integration
- **Progresso**: Acompanhe seu desenvolvimento
- **Pronúncia**: Exercícios de speaking e listening

## 🏗️ Arquitetura

```
my-school/
├── mobile/          # React Native app (iOS)
├── backend/         # Scala backend (Play Framework)
├── shared/          # Modelos e tipos compartilhados
└── docs/            # Documentação
```

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
