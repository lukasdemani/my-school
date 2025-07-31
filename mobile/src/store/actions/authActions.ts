import { apiService } from '@/services/api';
import { TokenStorage } from '@/services/tokenStorage';
import {
  AuthResponse,
  SignInRequest,
  SignUpRequest,
  UserResponse,
} from '@/types/user';
import { createAsyncThunk } from '@reduxjs/toolkit';

// Async thunks for authentication
export const signUpUser = createAsyncThunk<
  AuthResponse,
  SignUpRequest,
  { rejectValue: string }
>('auth/signUp', async (userData, { rejectWithValue }) => {
  try {
    const response = await apiService.signUp(userData);

    // Set token for future requests
    apiService.setToken(response.accessToken);

    // Persist tokens
    await TokenStorage.setTokens(response.accessToken, response.refreshToken);
    await TokenStorage.setUserData(response.user);

    return response;
  } catch (error) {
    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});

export const signInUser = createAsyncThunk<
  AuthResponse,
  SignInRequest,
  { rejectValue: string }
>('auth/signIn', async (credentials, { rejectWithValue }) => {
  try {
    const response = await apiService.signIn(credentials);

    // Set token for future requests
    apiService.setToken(response.accessToken);

    // Persist tokens
    await TokenStorage.setTokens(response.accessToken, response.refreshToken);
    await TokenStorage.setUserData(response.user);

    return response;
  } catch (error) {
    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});

export const refreshUserToken = createAsyncThunk<
  { accessToken: string },
  string,
  { rejectValue: string }
>('auth/refreshToken', async (refreshToken, { rejectWithValue }) => {
  try {
    const response = await apiService.refreshToken(refreshToken);

    // Update token
    apiService.setToken(response.accessToken);

    return response;
  } catch (error) {
    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});

export const logoutUser = createAsyncThunk<
  void,
  void,
  {
    rejectValue: string;
    state: { auth: { refreshToken: string | null } };
  }
>('auth/logout', async (_, { rejectWithValue, getState }) => {
  try {
    const state = getState();
    const refreshToken = state.auth.refreshToken;

    if (refreshToken) {
      await apiService.logout(refreshToken);
    }

    // Clear token and stored data
    apiService.clearToken();
    await TokenStorage.clearTokens();
  } catch (error) {
    // Even if the backend call fails, we still want to clear local data
    apiService.clearToken();
    await TokenStorage.clearTokens();

    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});

export const getCurrentUser = createAsyncThunk<
  UserResponse,
  void,
  { rejectValue: string }
>('auth/getCurrentUser', async (_, { rejectWithValue }) => {
  try {
    return await apiService.getCurrentUser();
  } catch (error) {
    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});

// Initialize auth from stored tokens
export const initializeAuth = createAsyncThunk<
  { user: UserResponse; accessToken: string; refreshToken: string } | null,
  void,
  { rejectValue: string }
>('auth/initialize', async (_, { rejectWithValue }) => {
  try {
    const [accessToken, refreshToken, userData] = await Promise.all([
      TokenStorage.getAccessToken(),
      TokenStorage.getRefreshToken(),
      TokenStorage.getUserData(),
    ]);

    if (accessToken && refreshToken && userData) {
      // Set token for API service
      apiService.setToken(accessToken);

      return {
        user: userData,
        accessToken,
        refreshToken,
      };
    }

    return null;
  } catch (error) {
    const message =
      error instanceof Error ? error.message : 'Erro desconhecido';
    return rejectWithValue(message);
  }
});
