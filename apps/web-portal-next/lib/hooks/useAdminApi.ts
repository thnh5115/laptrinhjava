/**
 * Admin API Hooks
 * Centralized API calls for admin dashboard
 * Prevents code duplication across components
 */

import { useState, useCallback } from 'react';
import axiosClient from '@/lib/api/axiosClient';

// ============================================
// Types (matching backend DTOs)
// ============================================

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  sort: string;
}

interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface UserSummary {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
  createdAt: string;
}

export interface UserOverview extends UserSummary {
  phoneNumber?: string;
  lastLoginAt?: string;
  listingCount: number;
  transactionCount: number;
  disputeCount: number;
  journeyCount: number;
  walletBalance: number;
  totalCreditsGenerated: number;
  totalEarnings: number;
  totalSpending: number;
  payoutRequestCount: number;
  pendingPayoutAmount: number;
  approvedPayoutAmount: number;
}

export interface TransactionSummary {
  id: number;
  buyerEmail: string;
  sellerEmail: string;
  type: string;
  status: string;
  totalPrice: number;
  quantity: number;
  unitPrice: number;
  createdAt: string;
}

export interface DisputeSummary {
  id: number;
  transactionId: number;
  claimantEmail: string;
  respondentEmail: string;
  reason: string;
  status: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface ListingSummary {
  id: number;
  carbonCreditId: number;
  title: string;
  description: string;
  ownerEmail: string;
  ownerFullName: string;
  price: number;
  quantity: number;
  unit: string;
  listingType: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  approvedBy?: number;
  approvedByEmail?: string;
  approvedAt?: string;
  rejectReason?: string;
}

export interface PayoutSummary {
  id: number;
  userEmail: string;
  amount: number;
  status: string;
  paymentMethod: string;
  requestedAt: string;
  processedAt?: string;
}

export interface JourneySummary {
  id: number;
  title: string;
  description: string;
  ownerEmail: string;
  status: string;
  creditsGenerated: number;
  verifiedAt?: string;
  createdAt: string;
}

export interface ReportSummary {
  totalUsers: number;
  totalTransactions: number;
  totalRevenue: number;
  approvedTransactions: number;
  rejectedTransactions: number;
  pendingTransactions: number;
}

export interface ReportHistory {
  id: number;
  type: string;
  generatedBy: number;
  generatedByName: string;
  generatedByEmail: string;
  generatedAt: string;
  startDate?: string;
  endDate?: string;
  dateRange: string;
  format: string;
  filePath?: string;
}

export interface KPIResponse {
  totalUsers: number;
  activeUsers: number;
  totalTransactions: number;
  totalRevenue: number;
  completedTransactions: number;
  pendingDisputes: number;
  totalDisputes: number;
  resolvedDisputes: number;
}

export interface AuditLog {
  id: number;
  action: string;
  method: string;
  endpoint: string;
  userEmail: string;
  ipAddress: string;
  userAgent: string;
  timestamp: string;
  statusCode: number;
  responseTime: number;
}

// ============================================
// Generic hook for API calls with loading/error
// ============================================

export function useAdminApi() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRequest = useCallback(async <T,>(
    request: () => Promise<T>
  ): Promise<T | null> => {
    setLoading(true);
    setError(null);
    try {
      const result = await request();
      return result;
    } catch (err: any) {
      const errorMsg = err?.response?.data?.message || err.message || 'Unknown error';
      setError(errorMsg);
      console.error('[useAdminApi] Error:', errorMsg);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return { loading, error, handleRequest, setError };
}

// ============================================
// User Management API
// ============================================

export function useUserApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getUsers = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    role?: string;
    status?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<UserSummary>>('/admin/users', { params });
      return res.data;
    });
  }, [handleRequest]);

  const getUserOverview = useCallback(async (userId: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<UserOverview>(`/admin/users/${userId}/overview`);
      return res.data;
    });
  }, [handleRequest]);

  const updateUserStatus = useCallback(async (userId: number, status: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.put(`/admin/users/${userId}/status`, { status });
      return res.data;
    });
  }, [handleRequest]);

  const updateUserRole = useCallback(async (userId: number, role: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.put(`/admin/users/${userId}/role`, { role });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getUsers, getUserOverview, updateUserStatus, updateUserRole };
}

// ============================================
// Transaction Management API
// ============================================

export function useTransactionApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getTransactions = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    status?: string;
    type?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<TransactionSummary>>('/admin/transactions', { params });
      return res.data;
    });
  }, [handleRequest]);

  const getTransactionDetail = useCallback(async (id: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.get(`/admin/transactions/${id}`);
      return res.data;
    });
  }, [handleRequest]);

  const updateTransactionStatus = useCallback(async (id: number, status: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.put(`/admin/transactions/${id}/status`, { status });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getTransactions, getTransactionDetail, updateTransactionStatus };
}

// ============================================
// Dispute Management API
// ============================================

export function useDisputeApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getDisputes = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    status?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<DisputeSummary>>('/admin/disputes', { params });
      return res.data;
    });
  }, [handleRequest]);

  const getDisputeDetail = useCallback(async (id: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.get(`/admin/disputes/${id}`);
      return res.data;
    });
  }, [handleRequest]);

  const updateDisputeStatus = useCallback(async (id: number, status: string, resolution?: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.put(`/admin/disputes/${id}/status`, { status, resolution });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getDisputes, getDisputeDetail, updateDisputeStatus };
}

// ============================================
// Listing Management API
// ============================================

export function useListingApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getListings = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    status?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<ListingSummary>>('/admin/listings', { params });
      return res.data;
    });
  }, [handleRequest]);

  const approveListing = useCallback(async (id: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.post(`/admin/listings/${id}/approve`);
      return res.data;
    });
  }, [handleRequest]);

  const rejectListing = useCallback(async (id: number, reason: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.post(`/admin/listings/${id}/reject`, { reason });
      return res.data;
    });
  }, [handleRequest]);

  const delistListing = useCallback(async (id: number, reason: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.post(`/admin/listings/${id}/delist`, { reason });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getListings, approveListing, rejectListing, delistListing };
}

// ============================================
// Payout Management API
// ============================================

export function usePayoutApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getPayouts = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    status?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<PayoutSummary>>('/admin/payouts', { params });
      return res.data;
    });
  }, [handleRequest]);

  const approvePayout = useCallback(async (id: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.post(`/admin/payouts/${id}/approve`);
      return res.data;
    });
  }, [handleRequest]);

  const rejectPayout = useCallback(async (id: number, notes?: string) => {
    return handleRequest(async () => {
      const res = await axiosClient.post(`/admin/payouts/${id}/reject`, { notes });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getPayouts, approvePayout, rejectPayout };
}

// ============================================
// Journey & Credits API
// ============================================

export function useJourneyApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getJourneys = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
    status?: string;
    keyword?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<JourneySummary>>('/admin/journeys', { params });
      return res.data;
    });
  }, [handleRequest]);

  const getJourneyStatistics = useCallback(async () => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/journeys/statistics');
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getJourneys, getJourneyStatistics };
}

// ============================================
// Analytics API
// ============================================

export function useAnalyticsApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getKPIs = useCallback(async () => {
    return handleRequest(async () => {
      const res = await axiosClient.get<KPIResponse>('/admin/analytics/kpis');
      return res.data;
    });
  }, [handleRequest]);

  const getTransactionAnalytics = useCallback(async (year: number) => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/analytics/transactions', { params: { year } });
      return res.data;
    });
  }, [handleRequest]);

  const getDisputeAnalytics = useCallback(async () => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/analytics/disputes');
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getKPIs, getTransactionAnalytics, getDisputeAnalytics };
}

// ============================================
// Report & Audit API
// ============================================

export function useReportApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getReportSummary = useCallback(async () => {
    return handleRequest(async () => {
      const res = await axiosClient.get<ReportSummary>('/admin/reports/summary');
      return res.data;
    });
  }, [handleRequest]);

  const getReportHistory = useCallback(async (params: {
    page?: number;
    size?: number;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<SpringPage<ReportHistory>>('/admin/reports/history', { params });
      const data = res.data;
      return {
        content: data.content,
        page: data.number,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        first: data.first,
        last: data.last,
        sort: '',
      } as PageResponse<ReportHistory>;
    });
  }, [handleRequest]);

  const exportTransactionsCSV = useCallback(async (filters: any = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/reports/transactions.csv', {
        params: filters,
        responseType: 'blob',
      });
      return res.data;
    });
  }, [handleRequest]);

  const exportTransactionsPDF = useCallback(async (filters: any = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/reports/transactions.pdf', {
        params: filters,
        responseType: 'blob',
      });
      return res.data;
    });
  }, [handleRequest]);

  const exportUsersCSV = useCallback(async (filters: any = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/reports/users.csv', {
        params: filters,
        responseType: 'blob',
      });
      return res.data;
    });
  }, [handleRequest]);

  const exportUsersPDF = useCallback(async (filters: any = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get('/admin/reports/users.pdf', {
        params: filters,
        responseType: 'blob',
      });
      return res.data;
    });
  }, [handleRequest]);

  return { 
    loading, 
    error, 
    setError, 
    getReportSummary,
    getReportHistory, 
    exportTransactionsCSV, 
    exportTransactionsPDF,
    exportUsersCSV,
    exportUsersPDF
  };
}

export function useAuditApi() {
  const { loading, error, handleRequest, setError } = useAdminApi();

  const getAuditLogs = useCallback(async (params: {
    page?: number;
    size?: number;
    sort?: string;
  } = {}) => {
    return handleRequest(async () => {
      const res = await axiosClient.get<PageResponse<AuditLog>>('/admin/audit', { params });
      return res.data;
    });
  }, [handleRequest]);

  return { loading, error, setError, getAuditLogs };
}
