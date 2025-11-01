import apiClient from './base';
import type { 
  CreditListing,
  Transaction,
  WalletBalance,
  PaginatedResponse 
} from '../models';

export const ownerApi = {
  // Credits Management
  credits: {
    getAll: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<CreditListing>>(`/owner/credits?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<CreditListing>(`/owner/credits/${id}`),
    
    create: (creditData: Partial<CreditListing>) => 
      apiClient.post<CreditListing>('/owner/credits', creditData),
    
    update: (id: string, creditData: Partial<CreditListing>) => 
      apiClient.put<CreditListing>(`/owner/credits/${id}`, creditData),
    
    delete: (id: string) => 
      apiClient.delete(`/owner/credits/${id}`),
  },

  // Upload EV Data
  upload: {
    submitData: (formData: FormData) => 
      apiClient.post('/owner/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }),
    
    getUploadHistory: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<any>>(`/owner/upload/history?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<any>(`/owner/upload/${id}`),
  },

  // Wallet
  wallet: {
    getBalance: () => 
      apiClient.get<WalletBalance>('/owner/wallet/balance'),
    
    getTransactions: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<Transaction>>(`/owner/wallet/transactions?page=${page}&size=${size}`),
    
    withdraw: (amount: number, bankAccount: string) => 
      apiClient.post('/owner/wallet/withdraw', { amount, bankAccount }),
  },

  // History
  history: {
    getSales: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<Transaction>>(`/owner/history/sales?page=${page}&size=${size}`),
    
    getEarnings: (startDate: string, endDate: string) => 
      apiClient.get<any>(`/owner/history/earnings?startDate=${startDate}&endDate=${endDate}`),
  },

  // Dashboard
  dashboard: {
    getStats: () => 
      apiClient.get<any>('/owner/dashboard/stats'),
    
    getRecentActivity: () => 
      apiClient.get<any[]>('/owner/dashboard/activity'),
  },
};
