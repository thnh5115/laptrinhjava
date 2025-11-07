/**
 * Authentication Service - Centralized Token Management
 * Uses localStorage for client-side token storage
 */

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

export const authService = {
  /**
   * Get access token from localStorage
   */
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },

  /**
   * Get refresh token from localStorage
   */
  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  /**
   * Get stored user data
   */
  getUser(): any | null {
    if (typeof window === 'undefined') return null;
    const userData = localStorage.getItem(USER_KEY);
    return userData ? JSON.parse(userData) : null;
  },

  /**
   * Save access token to localStorage
   */
  setToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  },

  /**
   * Save refresh token to localStorage
   */
  setRefreshToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  },

  /**
   * Save user data to localStorage
   */
  setUser(user: any): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },

  /**
   * Save complete auth data (tokens + user)
   */
  setAuthData(accessToken: string, refreshToken: string, user: any): void {
    this.setToken(accessToken);
    this.setRefreshToken(refreshToken);
    this.setUser(user);
  },

  /**
   * Clear all authentication data from localStorage
   */
  clearAuth(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null;
  },
};

export default authService;
