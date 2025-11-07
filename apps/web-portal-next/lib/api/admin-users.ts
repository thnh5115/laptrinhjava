/**
 * Admin Users API Client
 * Endpoints for user management (view, filter, update status/role)
 */

import axiosClient from "./axiosClient";

// ================== TYPES ==================

export enum UserRole {
  ADMIN = "ADMIN",
  AUDITOR = "AUDITOR",
  BUYER = "BUYER",
  EV_OWNER = "EV_OWNER",
}

export enum UserStatus {
  ACTIVE = "ACTIVE",
  SUSPENDED = "SUSPENDED",
  BANNED = "BANNED",
}

export interface UserSummary {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;         // ISO 8601
  updatedAt?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserFilterParams {
  page?: number;
  size?: number;
  sort?: string;             // e.g., "createdAt,desc"
  keyword?: string;          // Search in email/fullName
  role?: UserRole | string;
  status?: UserStatus | string;
}

export interface UpdateUserStatusRequest {
  status: UserStatus;
}

export interface UpdateUserRoleRequest {
  role: UserRole;
}

// ================== API FUNCTIONS ==================

/**
 * GET /api/admin/users
 * List all users with filters and pagination
 */
export async function getUsers(params: UserFilterParams = {}) {
  const { data } = await axiosClient.get<PageResponse<UserSummary>>(
    "admin/users",
    { params }
  );
  return data;
}

/**
 * GET /api/admin/users/{id}
 * Get user details by ID
 */
export async function getUser(id: string | number) {
  const { data } = await axiosClient.get<UserSummary>(`admin/users/${id}`);
  return data;
}

/**
 * PUT /api/admin/users/{id}/status
 * Update user status (activate/suspend/ban)
 */
export async function updateUserStatus(id: string | number, status: UserStatus) {
  const { data } = await axiosClient.put<UserSummary>(
    `admin/users/${id}/status`,
    { status }
  );
  return data;
}

/**
 * PUT /api/admin/users/{id}/role
 * Update user role (admin/auditor/buyer/ev_owner)
 */
export async function updateUserRole(id: string | number, role: UserRole) {
  const { data } = await axiosClient.put<UserSummary>(
    `admin/users/${id}/role`,
    { role }
  );
  return data;
}
