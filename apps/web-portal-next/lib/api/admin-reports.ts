import axiosClient from "./axiosClient";

/**
 * Admin Reports API Client
 * Handles dashboard summary statistics and monthly chart data
 */

// === Type Definitions ===

/**
 * Summary statistics for dashboard overview
 * Matches ReportSummaryResponse from BE
 */
export interface ReportSummary {
  totalUsers: number;
  totalTransactions: number;
  totalRevenue: number;
  approvedTransactions: number;
  rejectedTransactions: number;
  pendingTransactions: number;
}

/**
 * Monthly chart data for time-series visualization
 * Matches ReportChartResponse from BE
 */
export interface MonthlyChartData {
  transactionsByMonth: Record<string, number>;  // "YYYY-MM" -> count
  revenueByMonth: Record<string, number>;       // "YYYY-MM" -> revenue
}

// === API Functions ===

/**
 * GET /api/admin/reports/summary
 * Fetch summary statistics for dashboard
 * 
 * Returns aggregated data:
 * - Total users
 * - Total transactions (all statuses)
 * - Total revenue (approved only)
 * - Approved/Rejected/Pending counts
 */
export async function getSummary(): Promise<ReportSummary> {
  const { data } = await axiosClient.get<ReportSummary>("/admin/reports/summary");
  return data;
}

/**
 * GET /api/admin/reports/monthly?year=YYYY
 * Fetch monthly report data for a specific year
 * 
 * Returns time-series data:
 * - Transaction count per month (all statuses)
 * - Revenue per month (approved only)
 * 
 * @param year Year to generate report for (defaults to current year)
 */
export async function getMonthly(year?: number): Promise<MonthlyChartData> {
  const currentYear = year ?? new Date().getFullYear();
  const { data } = await axiosClient.get<MonthlyChartData>("/admin/reports/monthly", {
    params: { year: currentYear },
  });
  return data;
}
