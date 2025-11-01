import apiClient from './base';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: 'BUYER' | 'OWNER' | 'CVA';
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email: string;
    fullName: string;
    role: string;
  };
}

export const authApi = {
  login: (credentials: LoginRequest) => 
    apiClient.post<AuthResponse>('/auth/login', credentials),
  
  register: (userData: RegisterRequest) => 
    apiClient.post<AuthResponse>('/auth/register', userData),
  
  logout: () => 
    apiClient.post('/auth/logout'),
  
  refreshToken: (refreshToken: string) => 
    apiClient.post<{ accessToken: string }>('/auth/refresh', { refreshToken }),
  
  getCurrentUser: () => 
    apiClient.get<AuthResponse['user']>('/auth/me'),
  
  // Token management helpers
  setToken: (token: string) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('access_token', token);
    }
  },
  
  getToken: () => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('access_token');
    }
    return null;
  },
  
  removeToken: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
    }
  },
};
