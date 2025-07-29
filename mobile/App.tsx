import { registerRootComponent } from 'expo';
import 'react-native-gesture-handler';
import { Provider } from 'react-redux';
import { AppNavigator } from './src/navigation/AppNavigator';
import { store } from './src/store';

function App() {
  return (
    <Provider store={store}>
      <AppNavigator />
    </Provider>
  );
}

registerRootComponent(App);
export default App;
