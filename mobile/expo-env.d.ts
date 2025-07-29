/// <reference types="expo/types" />

declare module 'expo-constants' {
  export interface Constants {
    expoConfig?: {
      extra?: {
        apiUrl?: string;
      };
    };
  }
}

declare module '@expo/vector-icons' {
  export * from '@expo/vector-icons/build/createIconSet';
  export * from '@expo/vector-icons/build/vendor/react-native-vector-icons/lib/create-icon-set-from-fontello';
}
