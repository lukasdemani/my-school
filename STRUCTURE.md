# 📁 Project Structure & Separation

Este documento garante a separação clara entre frontend e backend.

## 🎯 **Princípios de Separação**

### **✅ Raiz do Projeto (`/`)**

```
my-school/
├── .gitignore          # Configurações globais de git
├── README.md           # Documentação principal
├── package.json        # Apenas workspaces (sem dependências)
├── STRUCTURE.md        # Este arquivo
├── mobile/             # 📱 FRONTEND - React Native
├── backend/            # ⚙️  BACKEND - Scala + Play
└── shared/             # 🔗 Código compartilhado
```

### **📱 Frontend (`mobile/`)**

**O que DEVE estar aqui:**

- ✅ `package.json` com dependências React Native
- ✅ `node_modules/` do npm
- ✅ `tsconfig.json` para TypeScript
- ✅ `.eslintrc.js` para linting
- ✅ `metro.config.js` para bundling
- ✅ `app.json` para configuração Expo
- ✅ `.expo/` pasta de cache do Expo
- ✅ `src/` código TypeScript
- ✅ `assets/` recursos estáticos

**O que NÃO deve estar aqui:**

- ❌ Código Scala (.scala, .sbt)
- ❌ Configurações do Play Framework
- ❌ Dependências JVM

### **⚙️ Backend (`backend/`)**

**O que DEVE estar aqui:**

- ✅ `build.sbt` com dependências Scala
- ✅ `project/` configurações sbt
- ✅ `app/` código Scala
- ✅ `conf/` configurações Play Framework
- ✅ `target/` arquivos compilados

**O que NÃO deve estar aqui:**

- ❌ `package.json` ou `node_modules/`
- ❌ Código React Native (.tsx, .jsx)
- ❌ Configurações TypeScript/ESLint
- ❌ Assets do mobile

### **🔗 Shared (`shared/`)**

**O que DEVE estar aqui:**

- ✅ Tipos/interfaces compartilhados
- ✅ Constantes comuns
- ✅ Utilitários que ambos usam
- ✅ Esquemas de API contracts

## 🚀 **Como Executar Separadamente**

### **Frontend Only**

```bash
cd mobile/
npm install
npm start
```

### **Backend Only**

```bash
cd backend/
sbt compile
sbt run
```

### **Full Stack**

```bash
# Terminal 1 - Backend
cd backend && sbt run

# Terminal 2 - Frontend
cd mobile && npm start
```

## 🔒 **Validação da Separação**

### **✅ Checklist de Verificação**

#### **Na Raiz (`/`)**

- [ ] Sem `node_modules/`
- [ ] Sem `.expo/`
- [ ] Sem `tsconfig.json`
- [ ] Sem `.eslintrc.js`
- [ ] Apenas `package.json` com workspaces

#### **No Mobile (`mobile/`)**

- [ ] Tem `package.json` próprio
- [ ] Tem `node_modules/` próprio
- [ ] Sem arquivos `.scala` ou `.sbt`
- [ ] TypeScript config isolado

#### **No Backend (`backend/`)**

- [ ] Tem `build.sbt` próprio
- [ ] Sem `package.json` ou `node_modules/`
- [ ] Sem configurações React Native
- [ ] Scala/JVM isolado

## 🛠️ **Scripts de Manutenção**

### **Limpeza de Arquivos Misturados**

```bash
# Remove arquivos React Native da raiz
rm -rf .expo node_modules package-lock.json

# Remove arquivos Scala do mobile
cd mobile && rm -rf target *.sbt project/

# Remove arquivos React Native do backend
cd backend && rm -rf node_modules package.json .expo
```

---

**✅ Status**: Estrutura separada e organizada corretamente!
