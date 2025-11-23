import axiosClient from "./axiosClient";

// --- 1. ĐỊNH NGHĨA INTERFACE (Bị thiếu ở code cũ) ---

export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  sort?: string;
}

export interface CreditIssuance {
  id: string;
  co2ReducedKg: number;
  creditsRaw: number;
  creditsRounded: number;
  idempotencyKey: string;
  correlationId?: string | null;
  createdAt: string;
}

export interface VerificationRequest {
  id: string;
  ownerId: string;
  tripId: string;
  distanceKm: number;
  energyKwh: number;
  checksum: string;
  status: VerificationStatus;
  createdAt: string;
  verifiedAt?: string | null;
  verifierId?: string | null;
  notes?: string | null;
  creditIssuance?: CreditIssuance | null;
}

export interface VerificationQuery {
  status?: VerificationStatus;
  ownerId?: string;
  createdFrom?: string;
  createdTo?: string;
  search?: string;
  page?: number;
  size?: number;
}

// --- 2. CẤU HÌNH CLIENT (Logic mới dùng Token hệ thống) ---

// Cấu hình URL riêng cho Microservice CVA (Cổng 8183)
// Lưu ý: Backend Java của bạn mapping là @RequestMapping("/api/cva/requests")
// Nên BASE_URL chỉ cần trỏ đến gốc /api/cva
const CVA_BASE_URL = process.env.NEXT_PUBLIC_CVA_API_URL || "http://localhost:8183/api/cva";

// Helper để override Base URL nhưng vẫn giữ Interceptor của axiosClient (để lấy Token)
const requestCva = {
  get: async <T>(url: string, params?: any) => {
    return axiosClient.get<T>(url, { 
      baseURL: CVA_BASE_URL, 
      params 
    });
  },
  
  put: async <T>(url: string, body: any) => {
    return axiosClient.put<T>(url, body, { 
      baseURL: CVA_BASE_URL 
    });
  },

  // Hàm download PDF trả về Blob
  getBlob: async (url: string) => {
    return axiosClient.get(url, { 
      baseURL: CVA_BASE_URL, 
      responseType: 'blob' 
    });
  }
};

// --- 3. CÁC HÀM API ---

export async function listVerificationRequests(query: VerificationQuery = {}): Promise<PageResponse<VerificationRequest>> {
  // Gọi vào http://localhost:8183/api/cva/requests
  const { data } = await requestCva.get<PageResponse<VerificationRequest>>('/requests', query);
  return data;
}

export async function getVerificationRequest(id: string): Promise<VerificationRequest> {
  const { data } = await requestCva.get<VerificationRequest>(`/requests/${id}`);
  return data;
}

interface ApprovePayload {
  verifierId: string;
  notes?: string;
  idempotencyKey?: string;
  correlationId?: string;
}

export async function approveVerificationRequest(id: string, payload: ApprovePayload): Promise<VerificationRequest> {
  const { data } = await requestCva.put<VerificationRequest>(`/requests/${id}/approve`, payload);
  return data;
}

interface RejectPayload {
  verifierId: string;
  reason: string;
  correlationId?: string;
}

export async function rejectVerificationRequest(id: string, payload: RejectPayload): Promise<VerificationRequest> {
  const { data } = await requestCva.put<VerificationRequest>(`/requests/${id}/reject`, payload);
  return data;
}

export async function downloadReportPdf(id: string): Promise<Blob> {
  const response = await requestCva.getBlob(`/reports/${id}?format=pdf`);
  return response.data;
}
