// Configuration for different environments
const config = {
  development: {
    // Temporarily use mock mode until backend is fixed
    API_BASE_URL: 'http://localhost:9000', // iOS simulator
    USE_MOCK_API: true, // Enable mock mode
    //API_BASE_URL: 'http://10.0.2.2:9000', // Android emulator
    // API_BASE_URL: 'http://192.168.1.XXX:9000', // Physical device (replace with your IP)
  },
  production: {
    API_BASE_URL: 'https://your-production-api.com',
    USE_MOCK_API: false,
  },
};

const environment = __DEV__ ? 'development' : 'production';

export default config[environment];
