import axiosClient from "./axiosClient";

/**
 * Admin Analytics API Client
 * Handles dashboard KPIs, transaction trends, and dispute statistics
 */

// === Type Definitions ===

/**
 * System-wide KPI metrics for dashboard widgets
 * Matches SystemKpiResponse from BE
 */
export interface SystemKpi {
  totalUsers: number;
  totalTransactions: number;
  totalDisputes: number;
  totalRevenue: number;
  disputeRate: number;
}

/**
 * Transaction trend data for line charts
 * Matches TransactionTrendResponse from BE
 */
export interface TransactionTrend {
  monthlyTransactions: Record<string, number>;  // "YYYY-MM" -> count
  monthlyRevenue: Record<string, number>;       // "YYYY-MM" -> revenue
}

/**
 * Dispute ratio statistics for pie charts
 * Matches DisputeRatioResponse from BE
 */
export interface DisputeRatio {
  openCount: number;
  resolvedCount: number;
  rejectedCount: number;
  total: number;
}

// === API Functions ===

/**
 * GET /api/admin/analytics/kpis
 * Fetch system-wide KPI metrics
 * 
 * Returns:
 * - Total users
 * - Total transactions
 * - Total disputes
 * - Total revenue (approved transactions only)
 * - Dispute rate (%)
 */
export async function getKpis(): Promise<SystemKpi> {
  const { data } = await axiosClient.get<SystemKpi>("/admin/analytics/kpis");
  return data;
}

/**
 * GET /api/admin/analytics/transactions?year=YYYY
 * Fetch transaction trends for a specific year
 * 
 * Returns monthly breakdown:
 * - Transaction count per month
 * - Revenue per month (approved only)
 * 
 * @param year Year to analyze (defaults to current year)
 */
export async function getTransactionTrends(year?: number): Promise<TransactionTrend> {
  const currentYear = year ?? new Date().getFullYear();
  const { data } = await axiosClient.get<TransactionTrend>("/admin/analytics/transactions", {
    params: { year: currentYear },
  });
  return data;
}

/**
 * GET /api/admin/analytics/disputes
 * Fetch dispute ratio statistics
 * 
 * Returns breakdown by status:
 * - OPEN count
 * - RESOLVED count
 * - REJECTED count
 * - Total count
 */
export async function getDisputeRatios(): Promise<DisputeRatio> {
  const { data } = await axiosClient.get<DisputeRatio>("/admin/analytics/disputes");
  return data;
}
