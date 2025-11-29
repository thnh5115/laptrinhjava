import axiosClient from "./axiosClient";

// URL của Buyer Backend (Cổng 8081)
const BUYER_API_URL =
  process.env.NEXT_PUBLIC_BUYER_API_URL || "http://localhost:8081/api";

const requestBuyer = {
  get: async <T>(url: string) =>
    axiosClient.get<T>(url, { baseURL: BUYER_API_URL }),
  post: async <T>(url: string, body: any) =>
    axiosClient.post<T>(url, body, { baseURL: BUYER_API_URL }),
};

// --- Types ---
export interface Listing {
  id: number;
  sellerId: number;
  qty: number;
  pricePerUnit: number;
  status: string;
}

export interface Transaction {
  id: number;
  buyerId: number;
  listingId: number;
  qty: number;
  amount: number;
  status: string;
  createdAt: string;
}

export interface BuyerDashboardStats {
  totalOrders: number;
  pendingTransactions: number;
  completedTransactions: number;
  totalSpent: number;
}

export interface Invoice {
  id: number;
  trId: number;
  issueDate: string;
  filePath: string;
}

// --- API Functions ---

// 1. Lấy danh sách hàng (Marketplace)
export async function getListings() {
  const { data } = await requestBuyer.get<Listing[]>("/buyer/listings");
  return data;
}

// 2. Mua hàng
export async function purchaseCredit(payload: {
  buyerId: number;
  listingId: number;
  qty: number;
}) {
  const { data } = await requestBuyer.post("/buyer/transactions", payload);
  return data;
}

// 3. [MỚI] Lấy lịch sử giao dịch của tôi
export async function getMyTransactions(buyerId: number) {
  // Gọi API: GET /api/buyer/transactions?buyerId=...
  const { data } = await requestBuyer.get<Transaction[]>(
    `/buyer/transactions?buyerId=${buyerId}`
  );
  return data;
}

// 4. [MỚI] Lấy số liệu thống kê cho Dashboard
export async function getBuyerStats(buyerId: number) {
  // Gọi API: GET /api/buyer/transactions/dashboard/{buyerId}
  const { data } = await requestBuyer.get<BuyerDashboardStats>(
    `/buyer/transactions/dashboard/${buyerId}`
  );
  return data;
}
export async function getMyCertificates(buyerId: number) {
  // Gọi API: GET /api/buyer/invoices?buyerId=...
  const { data } = await requestBuyer.get<Invoice[]>(
    `/buyer/invoices?buyerId=${buyerId}`
  );
  return data;
}
export interface QuickBuyRequest {
  buyerId: number;
  qty: number;
}

export async function quickBuyCredits(data: QuickBuyRequest) {
  // Gọi API Backend mà chúng ta đã tạo ở TransactionController
  const response = await requestBuyer.post(
    "buyer/transactions/quick-buy",
    data
  );
  return response.data;
}
