/**
 * API client for Spring Boot Actuator Observability
 * Provides access to /actuator endpoints (health, metrics, loggers)
 * 
 * IMPORTANT: Actuator endpoints are NOT under /api prefix
 * - /actuator/health: Public (no auth required)
 * - /actuator/metrics/**, /actuator/loggers/**: Requires ADMIN role + JWT token
 */

import axiosClient from './axiosClient';

/**
 * NO dedicated actuatorClient needed - reuse axiosClient
 * We'll manually construct /actuator paths and override baseURL when needed
 */

// TypeScript interfaces for Actuator responses

/**
 * Health status from /actuator/health
 */
export interface HealthStatus {
  status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';
  components?: Record<string, {
    status: string;
    details?: Record<string, any>;
  }>;
  details?: Record<string, any>;
}

/**
 * Metrics list from /actuator/metrics
 */
export interface MetricsList {
  names: string[]; // ["jvm.memory.used", "system.cpu.usage", "process.uptime", ...]
}

/**
 * Specific metric from /actuator/metrics/{name}
 */
export interface MetricDetail {
  name: string;
  description?: string;
  baseUnit?: string;
  measurements: Array<{
    statistic: string; // VALUE, COUNT, TOTAL, MAX, etc.
    value: number;
  }>;
  availableTags?: Array<{
    tag: string;
    values: string[];
  }>;
}

/**
 * Loggers configuration from /actuator/loggers
 */
export interface LoggersResponse {
  levels: string[]; // ["OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"]
  loggers: Record<string, {
    configuredLevel?: string | null;
    effectiveLevel: string;
  }>;
}

/**
 * GET /actuator/health
 * Fetch application health status (PUBLIC - no auth required)
 * 
 * @returns Promise<HealthStatus> Health with status and components (db, diskSpace, ping)
 * @throws Error if actuator endpoint unreachable
 */
export const getHealth = async (): Promise<HealthStatus> => {
  try {
    // Use full URL for /actuator/health (not under /api)
    const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace('/api', '') || 'http://localhost:8080';
    const { data } = await axiosClient.get<HealthStatus>(`${baseUrl}/actuator/health`, {
      // Override baseURL for this request
      baseURL: baseUrl,
    });
    console.log('[Observability] Health status:', data.status);
    return data;
  } catch (error: any) {
    console.error('[Observability] Error fetching health:', error.message);
    throw new Error('Failed to fetch health status');
  }
};

/**
 * GET /actuator/metrics
 * Fetch list of available metrics (REQUIRES ADMIN + JWT)
 * 
 * @returns Promise<MetricsList> Array of metric names
 * @throws Error if actuator endpoint unreachable or unauthorized
 */
export const getMetrics = async (): Promise<MetricsList> => {
  try {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace('/api', '') || 'http://localhost:8080';
    const { data } = await axiosClient.get<MetricsList>(`${baseUrl}/actuator/metrics`, {
      baseURL: baseUrl,
    });
    console.log('[Observability] Available metrics:', data.names?.length || 0);
    return data;
  } catch (error: any) {
    console.error('[Observability] Error fetching metrics:', error.message);
    throw new Error('Failed to fetch metrics list');
  }
};

/**
 * GET /actuator/metrics/{name}
 * Fetch specific metric details (REQUIRES ADMIN + JWT)
 * 
 * @param name Metric name (e.g., "jvm.memory.used", "system.cpu.usage")
 * @returns Promise<MetricDetail> Metric with measurements
 * @throws Error if metric not found or actuator unreachable
 */
export const getMetric = async (name: string): Promise<MetricDetail> => {
  try {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace('/api', '') || 'http://localhost:8080';
    const { data } = await axiosClient.get<MetricDetail>(
      `${baseUrl}/actuator/metrics/${encodeURIComponent(name)}`,
      { baseURL: baseUrl }
    );
    const value = data.measurements?.[0]?.value ?? 0;
    console.log('[Observability] Metric:', name, '=', value);
    return data;
  } catch (error: any) {
    console.error('[Observability] Error fetching metric:', name, error.message);
    throw new Error(`Failed to fetch metric: ${name}`);
  }
};

/**
 * GET /actuator/loggers
 * Fetch all logger configurations (REQUIRES ADMIN + JWT, read-only)
 * 
 * @returns Promise<LoggersResponse> Loggers with configured/effective levels
 * @throws Error if actuator endpoint unreachable or unauthorized
 */
export const getLoggers = async (): Promise<LoggersResponse> => {
  try {
    const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace('/api', '') || 'http://localhost:8080';
    const { data } = await axiosClient.get<LoggersResponse>(`${baseUrl}/actuator/loggers`, {
      baseURL: baseUrl,
    });
    console.log('[Observability] Loggers count:', Object.keys(data.loggers || {}).length);
    return data;
  } catch (error: any) {
    console.error('[Observability] Error fetching loggers:', error.message);
    throw new Error('Failed to fetch loggers');
  }
};

/**
 * Utility: Fetch multiple metrics safely (returns null if not found)
 * Safe parsing: data?.measurements?.[0]?.value ?? null
 * 
 * @param metricNames Array of metric names to fetch
 * @returns Promise<Record<string, number | null>> Map of metric name -> value (null if error)
 */
export const getMetricsSafely = async (metricNames: string[]): Promise<Record<string, number | null>> => {
  const results: Record<string, number | null> = {};
  
  await Promise.allSettled(
    metricNames.map(async (name) => {
      try {
        const metric = await getMetric(name);
        // Safe parsing with optional chaining
        results[name] = metric?.measurements?.[0]?.value ?? null;
      } catch {
        results[name] = null;
      }
    })
  );
  
  return results;
};
