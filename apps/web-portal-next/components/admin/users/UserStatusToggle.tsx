"use client"

import { useState } from "react"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Button } from "@/components/ui/button"
import { Loader2, UserCheck, UserX, Ban } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useUpdateUserStatus } from "@/hooks/use-users"
import { type UserSummary, UserStatus } from "@/lib/api/admin-users"

interface UserStatusToggleProps {
  user: UserSummary
  onSuccess?: () => void
  trigger?: React.ReactNode
}

export function UserStatusToggle({ user, onSuccess, trigger }: UserStatusToggleProps) {
  const [confirmAction, setConfirmAction] = useState<{
    status: UserStatus
    title: string
    description: string
  } | null>(null)
  const { updateStatus, loading } = useUpdateUserStatus()

  const handleStatusChange = async (newStatus: UserStatus) => {
    if (!confirmAction) return

    const success = await updateStatus(user.id, newStatus)
    if (success) {
      setConfirmAction(null)
      onSuccess?.()
    }
  }

  const getStatusAction = (status: UserStatus) => {
    switch (status) {
      case UserStatus.ACTIVE:
        return {
          status,
          title: 'Activate User',
          description: `Are you sure you want to activate ${user.fullName || user.email}? They will be able to access the system.`,
        }
      case UserStatus.SUSPENDED:
        return {
          status,
          title: 'Suspend User',
          description: `Are you sure you want to suspend ${user.fullName || user.email}? They will not be able to login until reactivated.`,
        }
      case UserStatus.BANNED:
        return {
          status,
          title: 'Ban User',
          description: `Are you sure you want to permanently ban ${user.fullName || user.email}? This action is severe and should be used carefully.`,
        }
    }
  }

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          {trigger || (
            <Button variant="outline" size="sm" disabled={loading}>
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Change Status'}
            </Button>
          )}
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>Change Status</DropdownMenuLabel>
          <DropdownMenuSeparator />
          
          {user.status !== UserStatus.ACTIVE && (
            <DropdownMenuItem
              onClick={() => setConfirmAction(getStatusAction(UserStatus.ACTIVE))}
              disabled={loading}
            >
              <UserCheck className="mr-2 h-4 w-4 text-green-600" />
              Activate
            </DropdownMenuItem>
          )}
          
          {user.status === UserStatus.ACTIVE && (
            <DropdownMenuItem
              onClick={() => setConfirmAction(getStatusAction(UserStatus.SUSPENDED))}
              disabled={loading}
            >
              <UserX className="mr-2 h-4 w-4 text-yellow-600" />
              Suspend
            </DropdownMenuItem>
          )}
          
          {user.status !== UserStatus.BANNED && (
            <DropdownMenuItem
              onClick={() => setConfirmAction(getStatusAction(UserStatus.BANNED))}
              disabled={loading}
              className="text-destructive focus:text-destructive"
            >
              <Ban className="mr-2 h-4 w-4" />
              Ban User
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>

      <AlertDialog open={!!confirmAction} onOpenChange={(open) => !open && setConfirmAction(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{confirmAction?.title}</AlertDialogTitle>
            <AlertDialogDescription>{confirmAction?.description}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={loading}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => confirmAction && handleStatusChange(confirmAction.status)}
              disabled={loading}
              className={
                confirmAction?.status === UserStatus.BANNED
                  ? 'bg-destructive hover:bg-destructive/90'
                  : 'bg-emerald-600 hover:bg-emerald-700'
              }
            >
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Updating...
                </>
              ) : (
                'Confirm'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
