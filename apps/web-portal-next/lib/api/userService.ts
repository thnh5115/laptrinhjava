/**
 * User Service - Wraps admin user management API calls
 * Provides error handling, loading states, and toast notifications
 */

import { adminApi } from '@repo/ts-sdk/client';
import type { 
  User, 
  UserSearchParams,
  UserRoleUpdateRequest,
  UserStatusUpdateRequest,
  PaginatedResponse 
} from '@repo/ts-sdk/models';

export class UserService {
  /**
   * Search users with pagination and filters
   */
  async searchUsers(params: UserSearchParams = {}): Promise<PaginatedResponse<User>> {
    try {
      const response = await adminApi.users.search(params);
      return response.data;
    } catch (error: any) {
      console.error('Failed to search users:', error);
      throw this.handleError(error);
    }
  }

  /**
   * Get user by ID
   */
  async getUserById(id: number | string): Promise<User> {
    try {
      const response = await adminApi.users.getById(id);
      return response.data;
    } catch (error: any) {
      console.error(`Failed to get user ${id}:`, error);
      throw this.handleError(error);
    }
  }

  /**
   * Update user role
   */
  async updateUserRole(id: number | string, role: UserRoleUpdateRequest['role']): Promise<User> {
    try {
      const response = await adminApi.users.updateRole(id, { role });
      return response.data;
    } catch (error: any) {
      console.error(`Failed to update role for user ${id}:`, error);
      throw this.handleError(error);
    }
  }

  /**
   * Update user status
   */
  async updateUserStatus(id: number | string, status: UserStatusUpdateRequest['status']): Promise<User> {
    try {
      const response = await adminApi.users.updateStatus(id, { status });
      return response.data;
    } catch (error: any) {
      console.error(`Failed to update status for user ${id}:`, error);
      throw this.handleError(error);
    }
  }

  /**
   * Handle API errors and return user-friendly messages
   */
  private handleError(error: any): Error {
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 400:
          return new Error(data.message || 'Invalid request. Please check your input.');
        case 401:
          return new Error('Unauthorized. Please login again.');
        case 403:
          return new Error('You do not have permission to perform this action.');
        case 404:
          return new Error('User not found.');
        case 409:
          return new Error(data.message || 'Conflict occurred.');
        case 500:
          return new Error('Server error. Please try again later.');
        default:
          return new Error(data.message || 'An unexpected error occurred.');
      }
    }
    
    if (error.request) {
      return new Error('Network error. Please check your connection.');
    }
    
    return new Error(error.message || 'An unexpected error occurred.');
  }
}

// Export singleton instance
export const userService = new UserService();
export default userService;
