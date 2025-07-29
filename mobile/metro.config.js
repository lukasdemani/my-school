const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

// Configuração mais restritiva para evitar EMFILE
config.watchFolders = [__dirname];

// Bloquear mais pastas para reduzir arquivos observados
config.resolver.blockList = [
  /node_modules\/.*\/node_modules\/react-native\/.*/,
  /node_modules\/react-native\/Libraries\/NewAppScreen\/.*/,
  /\.git\/.*/,
  /\.expo\/.*/,
  /node_modules\/.*\/\.git\/.*/,
  /node_modules\/@babel\/.*\/lib\/.*/,
];

// Plataformas específicas
config.resolver.platforms = ['ios', 'android', 'native'];

// Configurações de watcher mais conservadoras
config.watcher = {
  additionalExts: ['tsx', 'ts', 'js', 'jsx'],
  watchman: false,
  healthCheck: {
    enabled: false, // Desabilita health check para reduzir watches
  },
};

// Reduzir workers para diminuir carga
config.maxWorkers = 1;

// Adicionar transform para melhor performance
config.transformer = {
  ...config.transformer,
  minifierPath: 'metro-minify-terser',
  minifierConfig: {
    keep_fnames: true,
    mangle: {
      keep_fnames: true,
    },
  },
};

module.exports = config;
