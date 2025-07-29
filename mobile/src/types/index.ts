// Re-export all types for easy importing
export * from './assessment';
export * from './content';
export * from './learning';
export * from './user';

// API Response types
export interface ApiResponse<T = any> {
  data: T;
  message?: string;
  status: 'success' | 'error';
  errors?: string[];
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
}

// Navigation types
export type RootStackParamList = {
  Onboarding: undefined;
  Auth: undefined;
  MainTabs: undefined;
};

export type MainTabParamList = {
  Dashboard: undefined;
  Learning: undefined;
  Practice: undefined;
  Progress: undefined;
  Profile: undefined;
};

// Error types
export interface AppError {
  code: string;
  message: string;
  details?: any;
  timestamp: string;
}

// Storage types
export interface StorageKeys {
  AUTH_TOKEN: string;
  USER_PROFILE: string;
  STUDY_PROGRESS: string;
  OFFLINE_DATA: string;
  PREFERENCES: string;
}

// App State types
export interface AppState {
  isLoading: boolean;
  isAuthenticated: boolean;
  isOnboarded: boolean;
  currentUser: any;
  error: AppError | null;
  networkStatus: 'online' | 'offline';
}
