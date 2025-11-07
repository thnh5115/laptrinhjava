/**
 * Authentication Service
 * Handles login, logout, and user session management
 */

import { apiClient } from './client';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
}

export interface LogoutResponse {
  message: string;
  email: string;
}

/**
 * Authentication API Service
 */
export const authService = {
  /**
   * Login with email and password
   */
  async login(email: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', {
      email,
      password,
    });

    // Store token
    if (response.accessToken) {
      apiClient.setToken(response.accessToken);
    }

    return response;
  },

  /**
   * Get current authenticated user
   */
  async me(): Promise<User> {
    return await apiClient.get<User>('/auth/me');
  },

  /**
   * Logout current user
   */
  async logout(): Promise<LogoutResponse> {
    const response = await apiClient.post<LogoutResponse>('/auth/logout');

    // Clear token after logout
    apiClient.clearToken();

    return response;
  },

  /**
   * Refresh JWT token
   */
  async refresh(): Promise<LoginResponse> {
    return await apiClient.post<LoginResponse>('/auth/refresh');
  },

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    if (typeof window === 'undefined') return false;
    const token = localStorage.getItem('auth_token');
    return !!token;
  },

  /**
   * Get stored token
   */
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('auth_token');
  },

  /**
   * Clear authentication data
   */
  clearAuth() {
    apiClient.clearToken();
  },
};

export default authService;
