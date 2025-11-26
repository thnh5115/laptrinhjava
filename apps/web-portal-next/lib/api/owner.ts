import axiosClient from "./axiosClient";

const OWNER_API_URL = (() => {
  const url = process.env.NEXT_PUBLIC_OWNER_API_URL;
  if (!url) {
    throw new Error(
      "[owner api] Missing NEXT_PUBLIC_OWNER_API_URL. Set it to Owner service base (e.g., http://localhost:8082/api/owner or http://owner-backend:8082/api/owner)."
    );
  }
  return url;
})();

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
export interface CreateListingPayload {
  amount: number;
  pricePerCredit: number;
}

export async function createListing(payload: CreateListingPayload) {
  // Gọi POST /api/owner/listings
  // (requestOwner đã có base URL là .../api/owner nên chỉ cần truyền "/listings")
  const { data } = await requestOwner.post("/listings", payload);
  return data;
}
export interface WalletBalance {
  balance: number;
  currency: string;
  totalEarnings: number;
  totalWithdrawals: number;
  pendingWithdrawals: number;
  status: string;
}

export interface WithdrawalRequest {
  amount: number;
  paymentMethod: string; // BANK_TRANSFER, PAYPAL, CRYPTO
  bankAccount: string;
  notes?: string;
}

export interface Payout {
  id: number;
  amount: number;
  status: string;
  requestedAt: string;
}

// 1. Lấy thông tin ví (Số dư)
export async function getWalletBalance() {
  const { data } = await requestOwner.get<WalletBalance>("/wallet/balance");
  return data;
}

// 2. Lấy lịch sử rút tiền
export async function getWithdrawals() {
  const { data } = await requestOwner.get<Payout[]>("/wallet/withdrawals");
  return data;
}

// 3. Gửi yêu cầu rút tiền
export async function requestWithdrawal(payload: WithdrawalRequest) {
  const { data } = await requestOwner.post("/wallet/withdraw", payload);
  return data;
}

// ========== REPORT TYPES ==========
export interface OwnerReportSummary {
    totalJourneys: number;
    verifiedJourneys: number;
    pendingJourneys: number;
    rejectedJourneys: number;
    totalCreditsGenerated: number;
    averageCreditsPerJourney: number;
    verificationRate: number;
    totalEarnings: number;
    totalWithdrawals: number;
    pendingWithdrawals: number;
    availableBalance: number;
}

export interface OwnerMonthlyReport {
    year: number;
    journeysByMonth: Record<string, number>;
    creditsByMonth: Record<string, number>;
    earningsByMonth: Record<string, number>;
}

// ========== REPORT API FUNCTIONS ==========

/**
 * Get comprehensive summary report
 */
export async function getOwnerReportSummary(): Promise<OwnerReportSummary> {
    const response = await requestOwner.get("/reports/summary");
    return response.data;
}

/**
 * Get monthly breakdown report
 */
export async function getOwnerMonthlyReport(
    year: number = new Date().getFullYear()
): Promise<OwnerMonthlyReport> {
    const response = await requestOwner.get("/reports/monthly", {
        params: { year },
    });
    return response.data;
}