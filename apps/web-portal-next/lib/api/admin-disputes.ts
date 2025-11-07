import axiosClient from "./axiosClient";

/**
 * Admin Dispute API Client
 * Handles all dispute management operations for Admin module
 * Pattern: Reused from admin-transactions.ts
 */

// === Type Definitions ===

export type DisputeStatus = "OPEN" | "IN_REVIEW" | "RESOLVED" | "REJECTED";

export interface DisputeSummary {
  id: number;
  disputeCode: string;
  raisedBy: string;
  status: DisputeStatus;
  transactionId: number;
  createdAt: string;
}

export interface DisputeDetail {
  id: number;
  disputeCode: string;
  raisedBy: string;
  description: string;
  adminNote: string | null;
  status: DisputeStatus;
  transactionId: number;
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

export interface DisputeQueryParams {
  page?: number;
  size?: number;
  sort?: string; // Combined sort format: "field,direction" (e.g., "createdAt,desc")
  keyword?: string;
  status?: DisputeStatus | "ALL";
}

export interface UpdateDisputeStatusRequest {
  status: DisputeStatus;
  adminNote?: string;
}

// === API Functions ===

/**
 * GET /api/admin/disputes
 * Fetch paginated dispute list with filters
 */
export async function listDisputes(
  params: DisputeQueryParams = {}
): Promise<PageResponse<DisputeSummary>> {
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

  const { data } = await axiosClient.get<PageResponse<DisputeSummary>>(
    "/admin/disputes",
    { params: cleanParams }
  );

  return data;
}

/**
 * GET /api/admin/disputes/{id}
 * Fetch dispute detail by ID
 */
export async function getDispute(id: number | string): Promise<DisputeDetail> {
  const { data } = await axiosClient.get<DisputeDetail>(`/admin/disputes/${id}`);
  return data;
}

/**
 * PUT /api/admin/disputes/{id}/status
 * Update dispute status and admin note
 */
export async function updateDisputeStatus(
  id: number | string,
  request: UpdateDisputeStatusRequest
): Promise<void> {
  await axiosClient.put(`/admin/disputes/${id}/status`, request);
}
