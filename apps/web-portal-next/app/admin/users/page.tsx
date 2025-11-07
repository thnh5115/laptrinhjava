"use client"

import { UserManagement } from "@/components/admin/user-management"

export default function UsersPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold tracking-tight">User Management</h1>
        <p className="text-muted-foreground">Manage platform users and permissions</p>
      </div>
      <UserManagement />
    </div>
  )
}
