/**
 * Admin Listings API Client
 * Endpoints for listing management (view, filter, approve/reject)
 */

import axiosClient from "./axiosClient";

// ================== TYPES ==================

export enum ListingStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
}

export interface ListingSummary {
  id: number;
  title: string;
  description: string;
  credits: number;           // tCO2
  pricePerCredit: number;    // USD
  totalPrice: number;        // credits * pricePerCredit
  status: ListingStatus;
  ownerEmail: string;
  ownerName: string;
  createdAt: string;         // ISO 8601
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ListingFilterParams {
  page?: number;
  size?: number;
  sort?: string;             // e.g., "createdAt,desc"
  keyword?: string;          // Search in title
  status?: ListingStatus | string;
  ownerEmail?: string;
}

export interface UpdateListingStatusRequest {
  status: ListingStatus;     // APPROVED or REJECTED
}

// ================== API FUNCTIONS ==================

/**
 * GET /api/admin/listings
 * List all listings with filters and pagination
 */
export async function getListings(params: ListingFilterParams = {}) {
  const { data } = await axiosClient.get<PageResponse<ListingSummary>>(
    "admin/listings",
    { params }
  );
  return data;
}

/**
 * GET /api/admin/listings/{id}
 * Get listing details by ID
 */
export async function getListing(id: string | number) {
  const { data } = await axiosClient.get<ListingSummary>(`admin/listings/${id}`);
  return data;
}

/**
 * PUT /api/admin/listings/{id}/status
 * Update listing status (approve/reject)
 */
export async function updateListingStatus(id: string | number, status: ListingStatus) {
  const { data } = await axiosClient.put<ListingSummary>(
    `admin/listings/${id}/status`,
    { status }
  );
  return data;
}
