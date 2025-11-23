import axios, {
  AxiosError,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
} from "axios";
import authService from "@/lib/auth/authService";

const apiBaseUrl = process.env.NEXT_PUBLIC_API_URL;

if (!apiBaseUrl) {
  throw new Error(
    '[axiosClient] Missing NEXT_PUBLIC_API_URL. Set it to the correct backend base URL (e.g., http://localhost:8080/api or http://admin-backend:8080/api in Docker).'
  );
}

const axiosClient = axios.create({
  baseURL: apiBaseUrl,
  // Tokens are sent via Authorization header; keep cookies off unless explicitly enabled.
  withCredentials: false,
  timeout: 30000,
});

// Request interceptor - attach Authorization header (browser only)
axiosClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // CRITICAL: Only read localStorage in browser context
  // Server-side rendering cannot access localStorage
  if (typeof window !== "undefined") {
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
    const originalRequest = err.config as AxiosRequestConfig & {
      _retry?: boolean;
    };
    const status = err?.response?.status;
    const data: any = err?.response?.data;
    const method = originalRequest?.method?.toUpperCase();
    const url = originalRequest?.url;

    // Compact error logging
    // Distinguish pure network issues (no response) from HTTP errors
    const isNetworkError = !err.response;
    const resolvedBase = err.config?.baseURL || apiBaseUrl;
    const fullUrl = url?.startsWith("http") ? url : `${resolvedBase ?? ""}${url ?? ""}`;
    const errorMsg =
      data?.message ||
      data?.error ||
      (isNetworkError ? "Network error (connection/CORS failed)" : err.message) ||
      "Unknown error";
    console.error(
      `[API ERROR] base=${resolvedBase} fullUrl=${fullUrl} status=${status || "NET"} method=${method} msg=${errorMsg}`
    );

    // Don't retry login/register/refresh requests
    const isAuthEndpoint =
      url?.includes("/auth/login") ||
      url?.includes("/auth/register") ||
      url === "/auth/refresh";

    // Handle 401 Unauthorized - Clear tokens and redirect to login
    if (status === 401 && !isAuthEndpoint) {
      console.warn(
        "[axiosClient] 401 Unauthorized - clearing auth and redirecting to login"
      );
      authService.clearAuth();

      if (
        typeof window !== "undefined" &&
        !window.location.pathname.includes("/login")
      ) {
        window.location.href = "/login";
      }
      return Promise.reject(err);
    }

    // Handle 403 Forbidden
    if (status === 403) {
      if (typeof window !== "undefined" && (window as any).showToast) {
        (window as any).showToast({
          title: "Access Denied",
          description: "You do not have permission to perform this action",
          variant: "destructive",
        });
      }
    }

    // Handle 400 Bad Request - Attach server message to error
    if (status === 400) {
      err.message =
        data?.message || "Invalid request. Please check your input.";
    }

    // Handle 500 Internal Server Error
    if (status === 500) {
      console.error("[API ERROR] 500 Details:", {
        url,
        method,
        status,
        payload: originalRequest?.data,
        response: data,
      });
      err.message = "An unexpected error occurred. Please try again later.";
    }

    return Promise.reject(err);
  }
);

export default axiosClient;
