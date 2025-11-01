import type { User } from "../models"

export type UserRole = User['role'];

export function getCurrentUser(): User | null {
  if (typeof window === "undefined") return null
  const userStr = localStorage.getItem("currentUser")
  return userStr ? JSON.parse(userStr) : null
}

export function requireAuth(allowedRoles?: UserRole[]): User | null {
  const user = getCurrentUser()
  if (!user) return null
  if (allowedRoles && !allowedRoles.includes(user.role)) return null
  return user
}
