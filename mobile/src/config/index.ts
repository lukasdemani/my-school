// Configuration for different environments
const config = {
  development: {
    // Backend is now working - using real API
    API_BASE_URL: 'http://localhost:9000/api', // iOS simulator
    USE_MOCK_API: false, // Disable mock mode - using real backend
    //API_BASE_URL: 'http://10.0.2.2:9000/api', // Android emulator
    // API_BASE_URL: 'http://192.168.1.XXX:9000/api', // Physical device (replace with your IP)
  },
  production: {
    API_BASE_URL: 'https://your-production-api.com',
    USE_MOCK_API: false,
  },
};

const environment = __DEV__ ? 'development' : 'production';

export default config[environment];
