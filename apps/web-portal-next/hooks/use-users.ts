/**
 * Custom hooks for User Management
 * Provides data fetching and mutations with loading/error states
 */

import { useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { useToast } from '@/hooks/use-toast'
import { getUsers, getUser, updateUserStatus, updateUserRole, type UserSummary, type UserFilterParams, UserRole, UserStatus } from '@/lib/api/admin-users'

/**
 * Hook for fetching paginated user list
 * 
 * @example
 * const { users, loading, error, totalPages, refresh } = useUserList({
 *   page: 0,
 *   size: 20,
 *   keyword: 'john',
 *   role: 'BUYER'
 * })
 */
export function useUserList(params: UserFilterParams = {}) {
  const [users, setUsers] = useState<UserSummary[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await getUsers(params)
      setUsers(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (err: any) {
      setError(err)
      setUsers([])
    } finally {
      setLoading(false)
    }
  }, [JSON.stringify(params)])

  useEffect(() => {
    fetchUsers()
  }, [fetchUsers])

  return {
    users,
    loading,
    error,
    totalPages,
    totalElements,
    refresh: fetchUsers,
  }
}

/**
 * Hook for fetching single user by ID
 * 
 * @example
 * const { user, loading, error, refresh } = useUser(userId)
 */
export function useUser(id: number | string | null) {
  const [user, setUser] = useState<UserSummary | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  const fetchUser = useCallback(async () => {
    if (!id) return

    setLoading(true)
    setError(null)
    try {
      const data = await getUser(id)
      setUser(data)
    } catch (err: any) {
      setError(err)
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    fetchUser()
  }, [fetchUser])

  return {
    user,
    loading,
    error,
    refresh: fetchUser,
  }
}

/**
 * Hook for updating user role
 * 
 * @example
 * const { updateRole, loading } = useUpdateUserRole()
 * await updateRole(userId, 'ADMIN')
 */
export function useUpdateUserRole() {
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()
  const router = useRouter()

  const updateRole = async (id: number | string, role: UserRole) => {
    setLoading(true)
    try {
      await updateUserRole(id, role)
      
      toast({
        title: 'Role updated',
        description: `User role has been changed to ${role}`,
      })
      
      router.refresh()
      return true
    } catch (error: any) {
      toast({
        title: 'Failed to update role',
        description: error.message || 'An error occurred while updating the role',
        variant: 'destructive',
      })
      return false
    } finally {
      setLoading(false)
    }
  }

  return { updateRole, loading }
}

/**
 * Hook for updating user status
 * 
 * @example
 * const { updateStatus, loading } = useUpdateUserStatus()
 * await updateStatus(userId, 'SUSPENDED')
 */
export function useUpdateUserStatus() {
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()
  const router = useRouter()

  const updateStatus = async (id: number | string, status: UserStatus) => {
    setLoading(true)
    try {
      await updateUserStatus(id, status)
      
      toast({
        title: 'Status updated',
        description: `User has been ${status.toLowerCase()}`,
      })
      
      router.refresh()
      return true
    } catch (error: any) {
      toast({
        title: 'Failed to update status',
        description: error.message || 'An error occurred while updating the status',
        variant: 'destructive',
      })
      return false
    } finally {
      setLoading(false)
    }
  }

  return { updateStatus, loading }
}
