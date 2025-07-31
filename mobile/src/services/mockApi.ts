import {
  AuthResponse,
  SignInRequest,
  SignUpRequest,
  UserResponse,
} from '@/types/user';

class MockApiService {
  private mockUser: UserResponse = {
    id: '1',
    name: 'Test User',
    email: 'test@example.com',
    nativeLanguage: 'Portuguese',
    bio: 'Learning German',
    interests: 'Technology, Languages', // String format as per type definition
    workspaces: [],
  };

  private mockTokens = {
    accessToken: 'mock-access-token-12345',
    refreshToken: 'mock-refresh-token-67890',
  };

  async signUp(userData: SignUpRequest): Promise<AuthResponse> {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 1000));

    console.log('Mock SignUp:', userData);

    return {
      user: {
        ...this.mockUser,
        name: userData.name,
        email: userData.email,
      },
      ...this.mockTokens,
    };
  }

  async signIn(credentials: SignInRequest): Promise<AuthResponse> {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 1000));

    console.log('Mock SignIn:', credentials);

    // Simulate authentication check
    if (
      credentials.email === 'test@test.com' &&
      credentials.password === '123'
    ) {
      return {
        user: this.mockUser,
        ...this.mockTokens,
      };
    }

    throw new Error('Invalid credentials');
  }

  async refreshToken(refreshToken: string): Promise<{ accessToken: string }> {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 500));

    console.log('Mock RefreshToken:', refreshToken);

    return {
      accessToken: 'mock-new-access-token-' + Date.now(),
    };
  }

  async logout(refreshToken: string): Promise<void> {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 500));

    console.log('Mock Logout with refreshToken:', refreshToken);
  }

  async getCurrentUser(): Promise<UserResponse> {
    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 500));

    console.log('Mock GetCurrentUser');

    return this.mockUser;
  }
}

export const mockApiService = new MockApiService();
