import axiosClient from "./axiosClient";

const OWNER_API_URL =
  process.env.NEXT_PUBLIC_OWNER_API_URL || "http://localhost:8182/api/owner";

// Helper request
const requestOwner = {
  post: async <T>(url: string, body: any) => {
    return axiosClient.post<T>(url, body, { baseURL: OWNER_API_URL });
  },
  get: async <T>(url: string, params?: any) => {
    return axiosClient.get<T>(url, { baseURL: OWNER_API_URL, params });
  },
};

// --- ĐỊNH NGHĨA KIỂU DỮ LIỆU TRẢ VỀ (Response) ---
export interface JourneyResponse {
  id: number;
  userId: number;
  journeyDate: string;
  startLocation: string;
  endLocation: string;
  distanceKm: number;
  energyUsedKwh: number;
  estimatedCredits: number;
  status: string;
  createdAt: string;
  message?: string;
}

export interface JourneyPayload {
  journeyDate: string; // Backend: journeyDate
  startLocation: string; // Backend: startLocation
  endLocation: string; // Backend: endLocation
  distanceKm: number; // Backend: distanceKm
  energyUsedKwh: number; // Backend: energyUsedKwh
  vehicleId?: string; // Backend: vehicleId
  notes?: string; // Backend: notes
}

// --- CÁC HÀM API ---

export async function submitJourney(payload: JourneyPayload) {
  const { data } = await requestOwner.post<JourneyResponse>(
    "/journeys",
    payload
  );
  return data;
}

export async function getMyJourneys() {
  // Backend trả về List<JourneyResponse> (Mảng), không phải Page
  const { data } = await requestOwner.get<JourneyResponse[]>("/journeys");
  return data;
}
export interface OwnerDashboardStats {
  walletId: number;
  userId: number;
  userEmail: string;
  balance: number; // Số dư hiện tại
  currency: string;
  status: string;
  lastUpdated: string;
  totalCreditsGenerated: number; // Tổng tín chỉ đã tạo
  totalEarnings: number; // Tổng tiền kiếm được
  totalWithdrawals: number;
  pendingWithdrawals: number;
}

// 2. Hàm gọi API lấy số liệu
export async function getOwnerDashboardStats() {
  // Gọi GET http://localhost:8182/api/owner/wallet/balance
  const { data } = await requestOwner.get<OwnerDashboardStats>(
    "/wallet/balance"
  );
  return data;
}
