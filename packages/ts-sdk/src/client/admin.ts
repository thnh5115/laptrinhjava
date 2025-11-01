import apiClient from './base';
import type { 
  User, 
  Transaction, 
  AnalyticsData,
  PaginatedResponse 
} from '../models';

export const adminApi = {
  // User Management
  users: {
    getAll: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<User>>(`/admin/users?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<User>(`/admin/users/${id}`),
    
    create: (userData: Partial<User>) => 
      apiClient.post<User>('/admin/users', userData),
    
    update: (id: string, userData: Partial<User>) => 
      apiClient.put<User>(`/admin/users/${id}`, userData),
    
    delete: (id: string) => 
      apiClient.delete(`/admin/users/${id}`),
    
    bulkDelete: (userIds: string[]) => 
      apiClient.post('/admin/users/bulk-delete', { userIds }),
  },

  // Transaction Monitoring
  transactions: {
    getAll: (page = 0, size = 20, status?: string) => 
      apiClient.get<PaginatedResponse<Transaction>>(`/admin/transactions?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
    
    getById: (id: string) => 
      apiClient.get<Transaction>(`/admin/transactions/${id}`),
    
    approve: (id: string) => 
      apiClient.post(`/admin/transactions/${id}/approve`),
    
    reject: (id: string, reason: string) => 
      apiClient.post(`/admin/transactions/${id}/reject`, { reason }),
  },

  // Analytics
  analytics: {
    getOverview: () => 
      apiClient.get<AnalyticsData>('/admin/analytics/overview'),
    
    getUserMetrics: (startDate: string, endDate: string) => 
      apiClient.get<any>(`/admin/analytics/users?startDate=${startDate}&endDate=${endDate}`),
    
    getTransactionMetrics: (startDate: string, endDate: string) => 
      apiClient.get<any>(`/admin/analytics/transactions?startDate=${startDate}&endDate=${endDate}`),
    
    getCO2Impact: () => 
      apiClient.get<any>('/admin/analytics/co2-impact'),
  },

  // Dispute Management
  disputes: {
    getAll: (page = 0, size = 20, status?: string) => 
      apiClient.get<PaginatedResponse<any>>(`/admin/disputes?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
    
    getById: (id: string) => 
      apiClient.get<any>(`/admin/disputes/${id}`),
    
    resolve: (id: string, resolution: string) => 
      apiClient.post(`/admin/disputes/${id}/resolve`, { resolution }),
  },

  // Finance
  finance: {
    getPayouts: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<any>>(`/admin/finance/payouts?page=${page}&size=${size}`),
    
    processPayout: (payoutId: string) => 
      apiClient.post(`/admin/finance/payouts/${payoutId}/process`),
    
    getFinancialReport: (year: number, month: number) => 
      apiClient.get<any>(`/admin/finance/reports?year=${year}&month=${month}`),
  },

  // Settings
  settings: {
    get: () => 
      apiClient.get<any>('/admin/settings'),
    
    update: (settings: any) => 
      apiClient.put('/admin/settings', settings),
  },
};
