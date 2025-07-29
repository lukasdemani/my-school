# 🇩🇪 German Learning App - Backend

Backend em Scala usando ZIO e Pekko (Apache Pekko) para o aplicativo de aprendizado de alemão.

## 🏗️ Arquitetura

### Tecnologias Principais

- **Scala 3.3.1** - Linguagem principal
- **Play Framework** - Framework web
- **ZIO** - Programação funcional e efeitos
- **Apache Pekko** - Sistema de atores (sucessor do Akka)
- **PostgreSQL** - Banco de dados
- **JWT** - Autenticação

### Estrutura do Projeto

```
backend/
├── app/
│   ├── actors/              # Pekko actors para sessões e conversas
│   ├── controllers/         # Controladores REST
│   ├── models/              # Modelos de dados
│   ├── repositories/        # Acesso ao banco usando ZIO
│   └── services/            # Lógica de negócio com ZIO
├── conf/
│   ├── application.conf     # Configurações
│   ├── routes              # Rotas da API
│   └── evolutions/         # Migrações do banco
└── project/                # Configuração SBT
```

## 🎯 Funcionalidades

### Sistema de Atores (Pekko)

- **LearningSessionActor** - Gerencia sessões de prática
- **ConversationActor** - Gerencia conversas com IA

### Serviços ZIO

- **AuthService** - Autenticação JWT com ZIO
- **VocabularyService** - CRUD de vocabulário
- **VocabularyStreamService** - Processamento com Pekko Streams
- **OpenAIService** - Integração com LLM

### APIs REST

```
# Autenticação
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh

# Vocabulário
GET  /api/vocabulary
POST /api/vocabulary
GET  /api/vocabulary/:id

# Prática
POST /api/practice/vocabulary
POST /api/practice/grammar
POST /api/practice/conversation

# IA
POST /api/ai/chat
POST /api/ai/translate
POST /api/ai/explain
```

## 🚀 Como executar

### Pré-requisitos

- Java 11+
- SBT 1.9+
- PostgreSQL
- Docker (opcional)

### Setup do Banco

```sql
CREATE DATABASE german_learning;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE german_learning TO postgres;
```

### Executar o Backend

```bash
cd backend
sbt run
```

O servidor estará disponível em `http://localhost:9000`

### Configuração

Edite `conf/application.conf`:

```hocon
# Database
slick.dbs.default.db.url="jdbc:postgresql://localhost:5432/german_learning"
slick.dbs.default.db.user="seu_usuario"
slick.dbs.default.db.password="sua_senha"

# JWT
jwt.secret="sua-chave-secreta"

# OpenAI
openai.api.key="sua-chave-openai"
```

## 🧪 Testes

```bash
sbt test
```

## 📝 Exemplos de Uso

### Registrar Usuário

```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "João",
    "lastName": "Silva"
  }'
```

### Iniciar Sessão de Vocabulário

```bash
curl -X POST http://localhost:9000/api/practice/vocabulary \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "vocabularyIds": [1, 2, 3, 4, 5],
    "practiceType": "flashcard"
  }'
```

### Chat com IA

```bash
curl -X POST http://localhost:9000/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Wie geht es dir heute?"
  }'
```

## 🏄‍♂️ Conceitos Aprendidos

### ZIO

- Programação funcional
- Gerenciamento de efeitos
- Tratamento de erros
- Execução assíncrona

### Pekko (Akka)

- Sistema de atores
- Processamento de streams
- Gerenciamento de estado
- Tolerância a falhas

### Scala

- Tipagem forte
- Pattern matching
- Case classes
- Imutabilidade
