"use client"

import { useState } from "react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Loader2 } from "lucide-react"
import { useUpdateUserRole } from "@/hooks/use-users"
import { type UserSummary, UserRole } from "@/lib/api/admin-users"

interface UserRoleModalProps {
  user: UserSummary | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onSuccess?: () => void
}

const ROLES: Array<{ value: UserRole; label: string; description: string }> = [
  { value: UserRole.ADMIN, label: 'Admin', description: 'Full system access' },
  { value: UserRole.AUDITOR, label: 'Auditor', description: 'Verification and audit access' },
  { value: UserRole.BUYER, label: 'Buyer', description: 'Can purchase carbon credits' },
  { value: UserRole.EV_OWNER, label: 'EV Owner', description: 'Electric vehicle owner' },
]

export function UserRoleModal({ user, open, onOpenChange, onSuccess }: UserRoleModalProps) {
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null)
  const { updateRole, loading } = useUpdateUserRole()

  const handleSubmit = async () => {
    if (!user || !selectedRole) return

    const success = await updateRole(user.id, selectedRole)
    if (success) {
      onOpenChange(false)
      onSuccess?.()
    }
  }

  const handleOpenChange = (open: boolean) => {
    if (!loading) {
      onOpenChange(open)
      if (!open) {
        setSelectedRole(null)
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Change User Role</DialogTitle>
          <DialogDescription>
            Update the role for {user?.fullName || user?.email}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label>Current Role</Label>
            <div className="text-sm text-muted-foreground">
              {user?.role === UserRole.EV_OWNER ? 'EV Owner' : user?.role}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="role">New Role</Label>
            <Select
              value={selectedRole || user?.role}
              onValueChange={(value) => setSelectedRole(value as UserRole)}
              disabled={loading}
            >
              <SelectTrigger id="role">
                <SelectValue placeholder="Select a role" />
              </SelectTrigger>
              <SelectContent>
                {ROLES.map((role) => (
                  <SelectItem key={role.value} value={role.value}>
                    <div className="flex flex-col">
                      <span className="font-medium">{role.label}</span>
                      <span className="text-xs text-muted-foreground">{role.description}</span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {user?.role === UserRole.ADMIN && selectedRole && selectedRole !== UserRole.ADMIN && (
            <div className="rounded-md bg-yellow-50 dark:bg-yellow-900/20 p-3 text-sm text-yellow-800 dark:text-yellow-200">
              ⚠️ Warning: Removing ADMIN role may restrict this user's access.
            </div>
          )}
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading || !selectedRole || selectedRole === user?.role}
            className="bg-emerald-600 hover:bg-emerald-700"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Updating...
              </>
            ) : (
              'Update Role'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
