"use client"

import { TransactionDetail, TransactionStatus } from "@/lib/api/admin-transactions"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { CheckCircle, XCircle, Calendar, DollarSign, Hash, User, FileText } from "lucide-react"

interface TransactionDetailModalProps {
  transaction: TransactionDetail | null
  loading: boolean
  open: boolean
  onClose: () => void
  onApprove: (id: number) => Promise<void>
  onReject: (id: number) => Promise<void>
}

export function TransactionDetailModal({
  transaction,
  loading,
  open,
  onClose,
  onApprove,
  onReject,
}: TransactionDetailModalProps) {
  // Status badge color
  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400"
      case "APPROVED":
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
      case "REJECTED":
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-400"
    }
  }

  // Format currency
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  // Format date
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    })
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Transaction Details</DialogTitle>
          <DialogDescription>Complete information about this transaction</DialogDescription>
        </DialogHeader>

        {loading ? (
          <div className="space-y-4 py-4">
            <Skeleton className="h-6 w-3/4" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
          </div>
        ) : transaction ? (
          <div className="space-y-6 py-4">
            {/* Status */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Hash className="h-4 w-4 text-muted-foreground" />
                <span className="font-mono text-sm">{transaction.transactionCode}</span>
              </div>
              <Badge className={getStatusColor(transaction.status)} variant="secondary">
                {transaction.status}
              </Badge>
            </div>

            {/* Transaction Type */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <FileText className="h-4 w-4" />
                  <span>Transaction Type</span>
                </div>
                <p className="font-medium">{transaction.type}</p>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <DollarSign className="h-4 w-4" />
                  <span>Total Amount</span>
                </div>
                <p className="font-medium text-lg">{formatCurrency(transaction.totalPrice)}</p>
              </div>
            </div>

            {/* Buyer & Seller */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <User className="h-4 w-4" />
                  <span>Buyer</span>
                </div>
                <p className="font-medium">{transaction.buyerEmail}</p>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <User className="h-4 w-4" />
                  <span>Seller</span>
                </div>
                <p className="font-medium">{transaction.sellerEmail}</p>
              </div>
            </div>

            {/* Amount Details */}
            <div className="rounded-lg border p-4 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Credit Amount</span>
                <span className="font-medium">{transaction.amount} credits</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Price per Credit</span>
                <span className="font-medium">
                  {formatCurrency(transaction.totalPrice / transaction.amount)}
                </span>
              </div>
              <div className="border-t pt-2 flex justify-between font-medium">
                <span>Total Price</span>
                <span className="text-lg">{formatCurrency(transaction.totalPrice)}</span>
              </div>
            </div>

            {/* Timestamps */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>Created At</span>
                </div>
                <p className="text-sm">{formatDate(transaction.createdAt)}</p>
              </div>
              <div className="space-y-1">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>Last Updated</span>
                </div>
                <p className="text-sm">{formatDate(transaction.updatedAt)}</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="py-8 text-center text-muted-foreground">
            <p>No transaction data available</p>
          </div>
        )}

        <DialogFooter>
          {transaction && transaction.status === "PENDING" && (
            <div className="flex gap-2 w-full">
              <Button
                variant="outline"
                className="flex-1"
                onClick={() => {
                  if (confirm("Are you sure you want to reject this transaction?")) {
                    onReject(transaction.id)
                  }
                }}
              >
                <XCircle className="mr-2 h-4 w-4" />
                Reject
              </Button>
              <Button
                className="flex-1"
                onClick={() => {
                  if (confirm("Are you sure you want to approve this transaction?")) {
                    onApprove(transaction.id)
                  }
                }}
              >
                <CheckCircle className="mr-2 h-4 w-4" />
                Approve
              </Button>
            </div>
          )}
          <Button variant="secondary" onClick={onClose}>
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
