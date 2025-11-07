/**
 * API client for Admin Audit Logs
 * Handles audit log retrieval, summary stats, and chart data
 */

import axiosClient from './axiosClient';

// TypeScript interfaces matching backend DTOs

/**
 * Audit log response from backend
 * Maps to AuditLogResponse.java
 */
export interface AuditLogResponse {
  id: number;
  username: string;
  method: string; // GET, POST, PUT, DELETE
  endpoint: string; // /api/admin/users, /api/transactions, etc.
  action: string; // VIEW_USERS, UPDATE_SETTING, etc.
  ip: string; // Client IP address
  status: number; // HTTP status code (200, 401, 500, etc.)
  createdAt: string; // ISO 8601 format from Instant
}

/**
 * Paginated response for audit logs
 * Matches PageResponse<AuditLogResponse> from backend
 */
export interface AuditLogsPageResponse {
  content: AuditLogResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Summary statistics for audit logs
 * Maps to AuditSummaryResponse.java
 */
export interface AuditSummaryResponse {
  totalLogs: number;
  totalUsers: number;
  errorCount: number; // Count of logs with status >= 400
}

/**
 * Chart data for audit visualizations
 * Maps to AuditChartResponse.java
 */
export interface AuditChartResponse {
  requestsByDay: Record<string, number>; // Map<String, Long> -> { "2025-11-01": 150, ... }
  topEndpoints: Record<string, number>; // Map<String, Long> -> { "/api/admin/users": 45, ... }
}

/**
 * Query parameters for filtering audit logs
 */
export interface AuditLogsQuery {
  page?: number;
  size?: number;
  sort?: string; // Combined sort format: "field,direction" (e.g., "createdAt,desc")
  keyword?: string; // Filter by endpoint (partial match)
  username?: string; // Filter by exact username
}

/**
 * GET /api/admin/audit
 * Fetch paginated audit logs with optional filters
 * 
 * @param params Query parameters (page, size, filters, sorting)
 * @returns Promise<AuditLogsPageResponse> Paginated audit logs
 * @throws Error if request fails or user unauthorized
 */
export const getAuditLogs = async (params: AuditLogsQuery = {}): Promise<AuditLogsPageResponse> => {
  try {
    const { data } = await axiosClient.get<AuditLogsPageResponse>('admin/audit', { params });
    console.log('[Audit] Fetched audit logs:', data.totalElements, 'total');
    return data;
  } catch (error: any) {
    console.error('[Audit] Error fetching logs:', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Failed to fetch audit logs');
  }
};

/**
 * GET /api/admin/audit/summary
 * Fetch summary statistics (total logs, users, errors)
 * 
 * @returns Promise<AuditSummaryResponse> Summary with counts
 * @throws Error if request fails or user unauthorized
 */
export const getAuditSummary = async (): Promise<AuditSummaryResponse> => {
  try {
    const { data } = await axiosClient.get<AuditSummaryResponse>('admin/audit/summary');
    console.log('[Audit] Fetched summary:', data);
    return data;
  } catch (error: any) {
    console.error('[Audit] Error fetching summary:', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Failed to fetch audit summary');
  }
};

/**
 * GET /api/admin/audit/charts?days=7
 * Fetch chart data for visualizations (time-series + top endpoints)
 * 
 * @param days Number of days to analyze (default: 7)
 * @returns Promise<AuditChartResponse> Chart data with requestsByDay and topEndpoints
 * @throws Error if request fails or user unauthorized
 */
export const getAuditCharts = async (days: number = 7): Promise<AuditChartResponse> => {
  try {
    const { data } = await axiosClient.get<AuditChartResponse>('admin/audit/charts', {
      params: { days },
    });
    console.log('[Audit] Fetched charts:', Object.keys(data.requestsByDay).length, 'days');
    return data;
  } catch (error: any) {
    console.error('[Audit] Error fetching charts:', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Failed to fetch audit charts');
  }
};
