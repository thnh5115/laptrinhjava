import apiClient from './base';
import type { 
  VerificationRequest,
  AnalyticsData,
  PaginatedResponse 
} from '../models';

export const cvaApi = {
  // Reviews
  reviews: {
    getPending: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<VerificationRequest>>(`/cva/reviews/pending?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<VerificationRequest>(`/cva/reviews/${id}`),
    
    approve: (id: string, comments?: string) => 
      apiClient.post(`/cva/reviews/${id}/approve`, { comments }),
    
    reject: (id: string, reason: string) => 
      apiClient.post(`/cva/reviews/${id}/reject`, { reason }),
    
    requestMoreInfo: (id: string, message: string) => 
      apiClient.post(`/cva/reviews/${id}/request-info`, { message }),
  },

  // Analytics
  analytics: {
    getOverview: () => 
      apiClient.get<AnalyticsData>('/cva/analytics/overview'),
    
    getReviewMetrics: (startDate: string, endDate: string) => 
      apiClient.get<any>(`/cva/analytics/reviews?startDate=${startDate}&endDate=${endDate}`),
    
    getPerformance: () => 
      apiClient.get<any>('/cva/analytics/performance'),
  },

  // History
  history: {
    getReviews: (page = 0, size = 20, status?: string) => 
      apiClient.get<PaginatedResponse<VerificationRequest>>(`/cva/history?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
    
    exportReport: (startDate: string, endDate: string) => 
      apiClient.get('/cva/history/export', {
        params: { startDate, endDate },
        responseType: 'blob'
      }),
  },

  // Logs
  logs: {
    getActivity: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<any>>(`/cva/logs?page=${page}&size=${size}`),
  },
};
