"use client"

import { Button } from "../ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog"
import { AlertTriangle } from "lucide-react"
import { useToast } from "../../hooks/use-toast"

interface UserDeleteModalProps {
  open: boolean
  userId: string
  userName: string
  onClose: () => void
}

export function UserDeleteModal({ open, userId, userName, onClose }: UserDeleteModalProps) {
  const { toast } = useToast()

  const handleDelete = () => {
    toast({
      title: "User Deleted",
      description: `${userName}'s account has been permanently deleted.`,
    })
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-red-600" />
            Delete User Account
          </DialogTitle>
          <DialogDescription>
            This action cannot be undone. This will permanently delete the user account and remove all associated data.
          </DialogDescription>
        </DialogHeader>

        <div className="p-4 border border-red-200 rounded-lg bg-red-50 dark:bg-red-950">
          <p className="text-sm font-medium text-red-900 dark:text-red-100">
            Are you sure you want to delete {userName}'s account?
          </p>
          <p className="text-xs text-red-700 dark:text-red-300 mt-1">User ID: {userId}</p>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="destructive" onClick={handleDelete}>
            Delete User
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
