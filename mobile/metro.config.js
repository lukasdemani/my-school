// Configuração Metro básica para Expo SDK 53
const { getDefaultConfig } = require('@expo/metro-config');

const config = getDefaultConfig(__dirname, {
  // Configurações explícitas para evitar problemas
  isCSSEnabled: true,
});

// Não usar serializers customizados que podem causar problemas
delete config.serializer;

module.exports = config;
