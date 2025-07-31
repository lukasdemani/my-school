import config from '@/config';
import {
  AuthResponse,
  SignInRequest,
  SignUpRequest,
  UserResponse,
} from '@/types/user';

class ApiService {
  private baseUrl: string;
  private token: string | null = null;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  setToken(token: string) {
    this.token = token;
  }

  clearToken() {
    this.token = null;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;

    const config: RequestInit = {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(this.token && { Authorization: `Bearer ${this.token}` }),
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          errorData.message || `HTTP ${response.status}: ${response.statusText}`
        );
      }

      return await response.json();
    } catch (error) {
      console.error('API Request failed:', error);
      throw error;
    }
  }

  // Authentication endpoints
  async signUp(data: SignUpRequest): Promise<AuthResponse> {
    return this.request<AuthResponse>('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async signIn(data: SignInRequest): Promise<AuthResponse> {
    return this.request<AuthResponse>('/api/auth/signin', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async refreshToken(refreshToken: string): Promise<{ accessToken: string }> {
    return this.request<{ accessToken: string }>('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });
  }

  async logout(refreshToken: string): Promise<void> {
    return this.request<void>('/api/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });
  }

  // User endpoints
  async getCurrentUser(): Promise<UserResponse> {
    return this.request<UserResponse>('/api/user/profile');
  }

  async updateUserProfile(data: Partial<UserResponse>): Promise<UserResponse> {
    return this.request<UserResponse>('/api/user/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // Workspace endpoints
  async createWorkspace(data: {
    targetLanguage: string;
    languageLevel: string;
    name: string;
    description?: string;
  }) {
    return this.request('/api/workspaces', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getUserWorkspaces() {
    // Workspaces are returned with user profile
    const user = await this.getCurrentUser();
    return user.workspaces;
  }
}

export const apiService = new ApiService(config.API_BASE_URL);
