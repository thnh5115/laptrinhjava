import apiClient from './base';
import type { 
  CreditListing, 
  Transaction, 
  Certificate,
  PaginatedResponse 
} from '../models';

export const buyerApi = {
  // Marketplace
  marketplace: {
    getListings: (page = 0, size = 20, filters?: any) => 
      apiClient.get<PaginatedResponse<CreditListing>>('/buyer/marketplace/listings', {
        params: { page, size, ...filters }
      }),
    
    getListingById: (id: string) => 
      apiClient.get<CreditListing>(`/buyer/marketplace/listings/${id}`),
    
    purchaseCredits: (listingId: string, quantity: number) => 
      apiClient.post<Transaction>('/buyer/marketplace/purchase', {
        listingId,
        quantity
      }),
  },

  // Portfolio
  portfolio: {
    getCredits: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<any>>(`/buyer/portfolio/credits?page=${page}&size=${size}`),
    
    getStatistics: () => 
      apiClient.get<any>('/buyer/portfolio/statistics'),
    
    getCO2Offset: () => 
      apiClient.get<any>('/buyer/portfolio/co2-offset'),
  },

  // Certificates
  certificates: {
    getAll: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<Certificate>>(`/buyer/certificates?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<Certificate>(`/buyer/certificates/${id}`),
    
    download: (id: string) => 
      apiClient.get(`/buyer/certificates/${id}/download`, {
        responseType: 'blob'
      }),
  },

  // Transactions
  transactions: {
    getHistory: (page = 0, size = 20) => 
      apiClient.get<PaginatedResponse<Transaction>>(`/buyer/transactions?page=${page}&size=${size}`),
    
    getById: (id: string) => 
      apiClient.get<Transaction>(`/buyer/transactions/${id}`),
  },
};
