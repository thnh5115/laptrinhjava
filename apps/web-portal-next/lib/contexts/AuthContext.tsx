'use client';

/**
 * Authentication Context & Provider
 * Manages global auth state with localStorage
 * CRITICAL: Must save token BEFORE calling /me
 */

import React, { createContext, useContext, useState, useEffect, useCallback, useMemo, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import axiosClient from '@/lib/api/axiosClient';
import authService from '@/lib/auth/authService';

type AuthStatus = 'idle' | 'loading' | 'authenticated' | 'unauthenticated';

interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
}

interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
  user: User;
}

interface AuthContextType {
  user: User | null;
  accessToken: string | null;
  status: AuthStatus;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  fetchMe: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const fetchMe = useCallback(async () => {
    const token = authService.getToken();
    if (!token) {
      console.log('[AuthContext] No token found, setting unauthenticated');
      setStatus('unauthenticated');
      setUser(null);
      setAccessToken(null);
      return;
    }

    setStatus('loading');
    try {
      const res = await axiosClient.get('/auth/me');
      setUser(res.data);
      setAccessToken(token);
      setStatus('authenticated');
      setError(null);
      console.log('[AuthContext] User fetched:', res.data.email, 'role:', res.data.role);
    } catch (err: any) {
      const statusCode = err?.response?.status;
      console.error('[AuthContext] fetchMe failed:', statusCode, err?.response?.data || err.message);
      
      // Only clear tokens on 401 (unauthorized)
      if (statusCode === 401) {
        console.warn('[AuthContext] 401 Unauthorized - clearing tokens');
        authService.clearAuth();
        setUser(null);
        setAccessToken(null);
        setStatus('unauthenticated');
      } else {
        // Network error or 5xx - keep tokens, just set error
        setError('Failed to fetch user information. Please try again.');
        setStatus('unauthenticated');
      }
    }
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setStatus('loading');
    setError(null);
    
    try {
      console.log('[AuthContext] Attempting login for:', email);
      const res = await axiosClient.post<LoginResponse>('/auth/login', { email, password });
      
      const { accessToken, refreshToken, user: userData } = res.data;

      if (!accessToken || !refreshToken) {
        throw new Error('Invalid response from server: missing tokens');
      }

      console.log('[AuthContext] Login successful:', userData.email, 'role:', userData.role);

      // ✅ CRITICAL: Save tokens FIRST before calling /me or any other API
      authService.setAuthData(accessToken, refreshToken, userData);

      // Update state
      setAccessToken(accessToken);
      setUser(userData);
      setStatus('authenticated');
      setError(null);

      console.log('[AuthContext] Tokens saved to localStorage, redirecting...');
      
      // Redirect based on role
      if (userData.role === 'ADMIN') {
        router.push('/admin/dashboard');
      } else if (userData.role === 'BUYER') {
        router.push('/buyer/dashboard');
      } else if (userData.role === 'OWNER') {
        router.push('/owner/dashboard');
      } else if (userData.role === 'CVA') {
        router.push('/cva/dashboard');
      } else {
        router.push('/login');
        throw new Error('Unknown role: ' + userData.role);
      }
    } catch (err: any) {
      const statusCode = err?.response?.status;
      const errorData = err?.response?.data;
      const errorMessage = errorData?.message || err?.message || 'Login failed';
      
      console.error('[AuthContext] Login failed:', statusCode, errorMessage, errorData);
      
      // Set user-friendly error message based on status code
      if (statusCode === 401) {
        setError('Email hoặc mật khẩu không đúng');
      } else if (statusCode === 400) {
        setError(errorMessage);
      } else if (statusCode === 403) {
        setError('Tài khoản đã bị khóa hoặc vô hiệu hóa');
      } else if (!statusCode) {
        setError('Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.');
      } else {
        setError('Đăng nhập thất bại. Vui lòng thử lại sau.');
      }

      setUser(null);
      setAccessToken(null);
      setStatus('unauthenticated');
      
      throw err;
    }
  }, [router]);

  const logout = useCallback(async () => {
    console.log('[AuthContext] Logging out...');
    
    try {
      // Call backend logout to revoke refresh tokens
      await axiosClient.post('/auth/logout');
    } catch (err) {
      console.warn('[AuthContext] Logout API call failed:', err);
      // Ignore logout errors - still clear local state
    }

    // Clear tokens from localStorage
    authService.clearAuth();
    
    // Clear state
    setUser(null);
    setAccessToken(null);
    setStatus('unauthenticated');
    setError(null);

    console.log('[AuthContext] Logged out, redirecting to /login');
    router.push('/login');
  }, [router]);

  // Auto-check authentication on mount (run only ONCE)
  useEffect(() => {
    const token = authService.getToken();
    if (token) {
      console.log('[AuthContext] Token found on mount, fetching user...');
      setStatus('loading');
      axiosClient.get('/auth/me')
        .then((res) => {
          setUser(res.data);
          setAccessToken(token);
          setStatus('authenticated');
          setError(null);
          console.log('[AuthContext] Auto-login successful:', res.data.email, 'role:', res.data.role);
        })
        .catch((err: any) => {
          const statusCode = err?.response?.status;
          console.error('[AuthContext] Auto-login failed:', statusCode);
          
          if (statusCode === 401) {
            authService.clearAuth();
          }
          
          setUser(null);
          setAccessToken(null);
          setStatus('unauthenticated');
        });
    } else {
      console.log('[AuthContext] No token found on mount');
      setStatus('unauthenticated');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Run only once on mount

  const value = useMemo(
    () => ({ user, accessToken, status, error, login, logout, fetchMe, clearError }),
    [user, accessToken, status, error, login, logout, fetchMe, clearError]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to use Auth Context
 */
export function useAuth() {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
}

export default AuthContext;

