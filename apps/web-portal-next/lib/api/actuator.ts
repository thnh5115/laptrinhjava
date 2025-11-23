/**
 * Actuator API Client - Simplified version for Observability Page
 * 
 * Provides type-safe access to Spring Boot Actuator endpoints:
 * - /actuator/health (public)
 * - /actuator/metrics (ADMIN only - requires JWT)
 * - /actuator/loggers (ADMIN only - requires JWT)
 * 
 * IMPORTANT: Actuator endpoints are NOT under /api prefix
 * Base URL is derived from NEXT_PUBLIC_API_URL by removing '/api' suffix
 * 
 * Example:
 * - NEXT_PUBLIC_API_URL=http://localhost:8080/api
 * - Actuator base=http://localhost:8080
 */

import axiosClient from './axiosClient';

/**
 * Derive actuator base URL from API URL
 * Example: http://localhost:8080/api → http://localhost:8080
 */
const getActuatorBaseUrl = (): string => {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL;
  if (!apiUrl) {
    throw new Error(
      "[actuator api] Missing NEXT_PUBLIC_API_URL. Set it to the main backend base (e.g., http://localhost:8080/api or http://admin-backend:8080/api)."
    );
  }
  if (!apiUrl.endsWith("/api")) {
    throw new Error(
      `[actuator api] NEXT_PUBLIC_API_URL must end with '/api' to derive actuator base. Current value: ${apiUrl}`
    );
  }
  return apiUrl.replace(/\/api$/, '');
};

// ==================== Type Definitions ====================

export interface HealthStatus {
  status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';
  components?: Record<string, {
    status: string;
    details?: Record<string, any>;
  }>;
}

export interface MetricsListResponse {
  names: string[];
}

export interface MetricMeasurement {
  statistic: string; // VALUE, COUNT, TOTAL, MAX, etc.
  value: number;
}

export interface MetricDetailResponse {
  name: string;
  description?: string;
  baseUnit?: string;
  measurements: MetricMeasurement[];
  availableTags?: Array<{
    tag: string;
    values: string[];
  }>;
}

// ==================== API Functions ====================

/**
 * GET /actuator/health
 * Fetch application health status (PUBLIC endpoint - no JWT required)
 * 
 * @returns Health status with components (db, diskSpace, ping)
 * @throws Error if endpoint unreachable
 * 
 * @example
 * const health = await fetchHealth();
 * console.log(health.status); // "UP"
 */
export const fetchHealth = async (): Promise<HealthStatus> => {
  const baseUrl = getActuatorBaseUrl();
  const { data } = await axiosClient.get<HealthStatus>(`${baseUrl}/actuator/health`, {
    baseURL: baseUrl, // Override default baseURL for this request
  });
  return data;
};

/**
 * GET /actuator/metrics
 * Fetch list of available metric names (ADMIN only - requires JWT)
 * 
 * @returns Array of metric names
 * @throws Error if unauthorized (403) or endpoint unreachable
 * 
 * @example
 * const names = await fetchMetricsNames();
 * // ["jvm.memory.used", "system.cpu.usage", "http.server.requests", ...]
 */
export const fetchMetricsNames = async (): Promise<string[]> => {
  const baseUrl = getActuatorBaseUrl();
  const { data } = await axiosClient.get<MetricsListResponse>(`${baseUrl}/actuator/metrics`, {
    baseURL: baseUrl,
  });
  return data?.names ?? [];
};

/**
 * GET /actuator/metrics/{name}
 * Fetch specific metric value (ADMIN only - requires JWT)
 * 
 * @param name - Metric name (e.g., "jvm.memory.used", "system.cpu.usage")
 * @returns Metric value (first measurement) or 0 if not available
 * @throws Error if unauthorized (403) or metric not found
 * 
 * @example
 * const memUsed = await fetchMetricValue("jvm.memory.used");
 * console.log(`Memory: ${memUsed} bytes`);
 */
export const fetchMetricValue = async (name: string): Promise<number> => {
  const baseUrl = getActuatorBaseUrl();
  const { data } = await axiosClient.get<MetricDetailResponse>(
    `${baseUrl}/actuator/metrics/${encodeURIComponent(name)}`,
    { baseURL: baseUrl }
  );
  
  // Safe parsing: return first measurement value or 0
  const measurement = data?.measurements?.[0]?.value;
  return typeof measurement === 'number' && Number.isFinite(measurement) ? measurement : 0;
};

/**
 * GET /actuator/metrics/{name} - Full detail
 * Fetch complete metric information including all measurements and tags
 * 
 * @param name - Metric name
 * @returns Full metric detail object
 * @throws Error if unauthorized or metric not found
 * 
 * @example
 * const detail = await fetchMetricDetail("jvm.memory.used");
 * console.log(detail.measurements); // [{ statistic: "VALUE", value: 123456789 }]
 */
export const fetchMetricDetail = async (name: string): Promise<MetricDetailResponse> => {
  const baseUrl = getActuatorBaseUrl();
  const { data } = await axiosClient.get<MetricDetailResponse>(
    `${baseUrl}/actuator/metrics/${encodeURIComponent(name)}`,
    { baseURL: baseUrl }
  );
  return data;
};

/**
 * Helper: Fetch multiple metrics in parallel
 * 
 * @param names - Array of metric names to fetch
 * @returns Map of metric name → value
 * 
 * @example
 * const metrics = await fetchMultipleMetrics([
 *   "jvm.memory.used",
 *   "system.cpu.usage",
 *   "process.uptime"
 * ]);
 * console.log(metrics.get("jvm.memory.used")); // 123456789
 */
export const fetchMultipleMetrics = async (names: string[]): Promise<Map<string, number>> => {
  const results = await Promise.allSettled(
    names.map(async (name) => ({ name, value: await fetchMetricValue(name) }))
  );
  
  const metricsMap = new Map<string, number>();
  results.forEach((result) => {
    if (result.status === 'fulfilled') {
      metricsMap.set(result.value.name, result.value.value);
    }
  });
  
  return metricsMap;
};

/**
 * Helper: Format bytes to human-readable string
 * 
 * @example
 * formatBytes(1234567890) // "1.15 GB"
 */
export const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
};

/**
 * Helper: Format percentage
 * 
 * @example
 * formatPercentage(0.8765) // "87.65%"
 */
export const formatPercentage = (value: number): string => {
  return `${(value * 100).toFixed(2)}%`;
};
