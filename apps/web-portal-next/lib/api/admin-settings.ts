/**
 * API client for Admin Settings Management
 * Handles CRUD operations for platform settings
 */

import axiosClient from './axiosClient';

// TypeScript interfaces matching backend DTOs

/**
 * Setting response from backend
 * Maps to SettingResponse.java
 */
export interface SettingResponse {
  id: number;
  keyName: string;
  value: string;
  description: string;
  updatedAt: string; // ISO 8601 format from LocalDateTime
}

/**
 * Request payload for updating a setting
 * Maps to UpdateSettingRequest.java
 */
export interface UpdateSettingRequest {
  value: string; // @NotBlank validation on backend
}

/**
 * Fetches all platform settings
 * @returns Promise<SettingResponse[]> List of all settings
 * @throws Error if request fails or user unauthorized
 */
export const getSettings = async (): Promise<SettingResponse[]> => {
  try {
    const response = await axiosClient.get<SettingResponse[]>('/admin/settings');
    console.log('[Settings] Fetched settings:', response.data);
    return response.data;
  } catch (error: any) {
    console.error('[Settings] Error fetching settings:', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Failed to fetch settings');
  }
};

/**
 * Updates a setting value
 * @param id Setting ID to update
 * @param data Update payload with new value
 * @returns Promise<SettingResponse> Updated setting
 * @throws Error if request fails or validation fails
 */
export const updateSetting = async (
  id: number,
  data: UpdateSettingRequest
): Promise<SettingResponse> => {
  try {
    const response = await axiosClient.put<SettingResponse>(
      `/admin/settings/${id}`,
      data
    );
    console.log('[Settings] Updated setting:', response.data);
    return response.data;
  } catch (error: any) {
    console.error('[Settings] Error updating setting:', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Failed to update setting');
  }
};
