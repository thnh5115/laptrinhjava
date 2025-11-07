import axiosClient from "./axiosClient";

/**
 * Admin Transaction API Client
 * Handles all transaction management operations for Admin module
 */

// === Type Definitions ===

export type TransactionStatus = "PENDING" | "APPROVED" | "REJECTED";
export type TransactionType = "CREDIT_PURCHASE" | "CREDIT_SALE" | "TRANSFER";

export interface TransactionSummary {
  id: number;
  transactionCode: string;
  buyerEmail: string;
  sellerEmail: string;
  totalPrice: number;
  status: TransactionStatus;
  createdAt: string;
}

export interface TransactionDetail {
  id: number;
  transactionCode: string;
  buyerEmail: string;
  sellerEmail: string;
  amount: number;
  totalPrice: number;
  status: TransactionStatus;
  type: TransactionType;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TransactionQueryParams {
  page?: number;
  size?: number;
  sort?: string; // Combined sort format: "field,direction" (e.g., "createdAt,desc")
  keyword?: string;
  status?: TransactionStatus | "ALL";
  type?: TransactionType | "ALL";
}

export interface UpdateStatusRequest {
  status: TransactionStatus;
}

// === API Functions ===

/**
 * GET /api/admin/transactions
 * Fetch paginated transaction list with filters
 */
export async function listTransactions(
  params: TransactionQueryParams = {}
): Promise<PageResponse<TransactionSummary>> {
  // Clean params: remove "ALL" values before sending to API
  const cleanParams: Record<string, any> = {
    page: params.page ?? 0,
    size: params.size ?? 10,
    sort: params.sort ?? "createdAt,desc", // Combined sort format
  };

  if (params.keyword && params.keyword.trim()) {
    cleanParams.keyword = params.keyword.trim();
  }

  if (params.status && params.status !== "ALL") {
    cleanParams.status = params.status;
  }

  if (params.type && params.type !== "ALL") {
    cleanParams.type = params.type;
  }

  const { data} = await axiosClient.get<PageResponse<TransactionSummary>>(
    "/admin/transactions",
    { params: cleanParams }
  );

  return data;
}

/**
 * GET /api/admin/transactions/{id}
 * Fetch transaction detail by ID
 */
export async function getTransaction(id: number | string): Promise<TransactionDetail> {
  const { data } = await axiosClient.get<TransactionDetail>(`/admin/transactions/${id}`);
  return data;
}

/**
 * PUT /api/admin/transactions/{id}/status
 * Update transaction status (approve/reject)
 */
export async function updateTransactionStatus(
  id: number | string,
  status: TransactionStatus
): Promise<void> {
  await axiosClient.put(`/admin/transactions/${id}/status`, { status });
}
