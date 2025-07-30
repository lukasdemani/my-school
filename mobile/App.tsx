import { registerRootComponent } from 'expo';
import { useEffect } from 'react';
import 'react-native-gesture-handler';
import 'react-native-get-random-values';
import 'react-native-url-polyfill/auto';
import { Provider } from 'react-redux';
import { AppNavigator } from './src/navigation/AppNavigator';
import { store } from './src/store';
import { initializeAuth } from './src/store/actions/authActions';
import { useAppDispatch } from './src/store/hooks';

function AppContent() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(initializeAuth());
  }, [dispatch]);

  return <AppNavigator />;
}

function App() {
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
}

registerRootComponent(App);
export default App;
