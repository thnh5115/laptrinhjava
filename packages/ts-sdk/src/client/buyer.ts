import apiClient from './base';
import type {
  CreditListing,
  Transaction,
  Certificate,
  PaginatedResponse
} from '../models';

const BUYER_BASE =
  (typeof process !== 'undefined' && (process as any)?.env?.NEXT_PUBLIC_BUYER_URL) ||
  (typeof process !== 'undefined' && (process as any)?.env?.NEXT_PUBLIC_BUYER_URL) || // fallback for some bundlers
  'http://localhost:8082';

export const buyerApi = {
  // Marketplace
  marketplace: {
    // BE thực tế: GET /buyer/listings
    getListings: (page = 0, size = 20, filters?: any) =>
      apiClient.get<PaginatedResponse<CreditListing>>(
        `${BUYER_BASE}/buyer/listings`,
        { params: { page, size, ...filters } }
      ),

    // BE thực tế: GET /buyer/listings/{id}
    getListingById: (id: string | number) =>
      apiClient.get<CreditListing>(`${BUYER_BASE}/buyer/listings/${id}`),

    /**
     * @param listingId
     * @param quantity
     * @param opts { buyerId: number; amount?: number }
     */
    purchaseCredits: (
      listingId: string | number,
      quantity: number,
      opts?: { buyerId: number; amount?: number }
    ) => {
      const payload = {
        buyerId: opts?.buyerId,          // bắt buộc từ FE
        listingId: Number(listingId),
        quantity,
        amount: opts?.amount ?? undefined // nếu FE muốn gửi, không bắt buộc
      };
      return apiClient.post<Transaction>(`${BUYER_BASE}/api/buyer/transactions`, payload);
    },
  },

  portfolio: {
    getCredits: (page = 0, size = 20) =>
      apiClient.get<PaginatedResponse<any>>(`/buyer/portfolio/credits?page=${page}&size=${size}`),

    getStatistics: () => apiClient.get<any>('/buyer/portfolio/statistics'),

    getCO2Offset: () => apiClient.get<any>('/buyer/portfolio/co2-offset'),
  },

  certificates: {
    getAll: (page = 0, size = 20) =>
      apiClient.get<PaginatedResponse<Certificate>>(`/buyer/certificates?page=${page}&size=${size}`),

    getById: (id: string) => apiClient.get<Certificate>(`/buyer/certificates/${id}`),

    download: (id: string) =>
      apiClient.get(`/buyer/certificates/${id}/download`, {
        responseType: 'blob',
      }),
  },

  transactions: {

    getHistory: (page = 0, size = 20, status?: string) =>
      apiClient.get<PaginatedResponse<Transaction>>(
        `${BUYER_BASE}/api/buyer/transactions`,
        { params: { page, size, ...(status ? { status } : {}) } }
      ),
    getById: (id: string | number) =>
      apiClient.get<Transaction>(`${BUYER_BASE}/api/buyer/transactions/${id}`),
  },

  dashboard: {

    get: (buyerId: number) =>
      apiClient.get<any>(`${BUYER_BASE}/api/buyer/orders/dashboard/${buyerId}`),
  },
};
