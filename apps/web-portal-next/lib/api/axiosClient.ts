import axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import authService from '@/lib/auth/authService';

const axiosClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  withCredentials: false,
  timeout: 30000,
});

// Request interceptor - attach Authorization header (browser only)
axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // CRITICAL: Only read localStorage in browser context
  // Server-side rendering cannot access localStorage
  if (typeof window !== 'undefined') {
    const token = authService.getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Response interceptor - handle errors with smart logging
axiosClient.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const originalRequest = err.config as AxiosRequestConfig & { _retry?: boolean };
    const status = err?.response?.status;
    const data: any = err?.response?.data;
    const method = originalRequest?.method?.toUpperCase();
    const url = originalRequest?.url;
    
    // Compact error logging
    const errorMsg = data?.message || data?.error || err.message || 'Unknown error';
    console.error(`[API ERROR] ${status || 'NET'} ${method} ${url} ${errorMsg}`);

    // Don't retry login/register/refresh requests
    const isAuthEndpoint = url?.includes('/auth/login') || 
                          url?.includes('/auth/register') ||
                          url === '/auth/refresh';
    
    // Handle 401 Unauthorized - Clear tokens and redirect to login
    if (status === 401 && !isAuthEndpoint) {
      console.warn('[axiosClient] 401 Unauthorized - clearing auth and redirecting to login');
      authService.clearAuth();
      
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
      return Promise.reject(err);
    }

    // Handle 403 Forbidden
    if (status === 403) {
      if (typeof window !== 'undefined' && (window as any).showToast) {
        (window as any).showToast({
          title: 'Access Denied',
          description: 'You do not have permission to perform this action',
          variant: 'destructive',
        });
      }
    }

    // Handle 400 Bad Request - Attach server message to error
    if (status === 400) {
      err.message = data?.message || 'Invalid request. Please check your input.';
    }

    // Handle 500 Internal Server Error
    if (status === 500) {
      console.error('[API ERROR] 500 Details:', {
        url,
        method,
        status,
        payload: originalRequest?.data,
        response: data,
      });
      err.message = 'An unexpected error occurred. Please try again later.';
    }

    return Promise.reject(err);
  }
);

export default axiosClient;