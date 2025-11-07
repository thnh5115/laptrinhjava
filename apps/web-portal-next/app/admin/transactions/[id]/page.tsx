"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  getTransaction,
  updateTransactionStatus,
  TransactionDetail,
  TransactionStatus,
} from "@/lib/api/admin-transactions"
import {
  ArrowLeft,
  Copy,
  CheckCircle2,
  RefreshCw,
  AlertCircle,
} from "lucide-react"
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

export default function TransactionDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { toast } = useToast()
  const id = params.id as string

  // State
  const [transaction, setTransaction] = useState<TransactionDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)
  const [selectedStatus, setSelectedStatus] = useState<TransactionStatus>("APPROVED")
  const [showConfirmDialog, setShowConfirmDialog] = useState(false)

  // Fetch transaction detail
  const fetchDetail = async () => {
    setLoading(true)
    try {
      const data = await getTransaction(id)
      setTransaction(data)
      // Set default selected status to current status or next logical status
      if (data.status === "PENDING") {
        setSelectedStatus("APPROVED")
      } else {
        setSelectedStatus(data.status as TransactionStatus)
      }
    } catch (error: any) {
      console.error("Failed to fetch transaction:", error)
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to load transaction details",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (id) {
      fetchDetail()
    }
  }, [id])

  // Copy to clipboard
  const handleCopy = (text: string, label: string) => {
    navigator.clipboard.writeText(text)
    toast({
      title: "Copied",
      description: `${label} copied to clipboard`,
    })
  }

  // Update status with confirmation
  const handleUpdateStatus = async () => {
    if (!transaction) return

    setUpdating(true)
    try {
      await updateTransactionStatus(transaction.id, selectedStatus)
      toast({
        title: "Success",
        description: `Transaction #${transaction.id} status updated to ${selectedStatus}`,
      })
      // Refresh detail
      await fetchDetail()
    } catch (error: any) {
      console.error("Failed to update status:", error)
      toast({
        title: "Update Failed",
        description: error?.response?.data?.message || "Failed to update transaction status",
        variant: "destructive",
      })
    } finally {
      setUpdating(false)
      setShowConfirmDialog(false)
    }
  }

  // Status badge color
  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-100"
      case "APPROVED":
        return "bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-100"
      case "REJECTED":
        return "bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-100"
      default:
        return "bg-gray-100 text-gray-700 dark:bg-gray-900 dark:text-gray-100"
    }
  }

  // Type badge color
  const getTypeColor = (type: string) => {
    switch (type) {
      case "CREDIT_PURCHASE":
        return "bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-100"
      case "CREDIT_SALE":
        return "bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-100"
      case "TRANSFER":
        return "bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-100"
      default:
        return "bg-gray-100 text-gray-700 dark:bg-gray-900 dark:text-gray-100"
    }
  }

  // Available status options based on current status
  const getAvailableStatusOptions = () => {
    if (!transaction) return []
    
    // Nếu đã APPROVED hoặc REJECTED, không cho đổi nữa (business rule)
    if (transaction.status === "APPROVED" || transaction.status === "REJECTED") {
      return []
    }
    
    // Nếu PENDING, cho phép APPROVED hoặc REJECTED
    return ["APPROVED", "REJECTED"] as TransactionStatus[]
  }

  const availableOptions = getAvailableStatusOptions()
  const canUpdateStatus = availableOptions.length > 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Transaction Detail</h1>
            <p className="text-muted-foreground">View and manage transaction</p>
          </div>
        </div>
        <Button variant="outline" size="sm" onClick={fetchDetail} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      {/* Loading Skeleton */}
      {loading ? (
        <div className="space-y-4">
          <Skeleton className="h-64 w-full" />
          <Skeleton className="h-48 w-full" />
        </div>
      ) : !transaction ? (
        /* Error State */
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <AlertCircle className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-lg font-medium mb-2">Transaction not found</p>
            <p className="text-muted-foreground mb-4">The transaction you're looking for doesn't exist</p>
            <Button onClick={() => router.push("/admin/transactions")}>Go to Transactions</Button>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Main Info Card */}
          <Card>
            <CardHeader>
              <div className="flex items-start justify-between">
                <div>
                  <CardTitle className="flex items-center gap-3">
                    Transaction #{transaction.id}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleCopy(transaction.id.toString(), "Transaction ID")}
                    >
                      <Copy className="h-4 w-4" />
                    </Button>
                  </CardTitle>
                  <CardDescription className="flex items-center gap-2 mt-2">
                    <span className="font-mono text-sm">{transaction.transactionCode}</span>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleCopy(transaction.transactionCode, "Transaction Code")}
                    >
                      <Copy className="h-3 w-3" />
                    </Button>
                  </CardDescription>
                </div>
                <div className="flex gap-2">
                  <Badge className={getStatusColor(transaction.status)}>{transaction.status}</Badge>
                  <Badge className={getTypeColor(transaction.type)}>
                    {transaction.type.replace(/_/g, " ")}
                  </Badge>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Buyer Info */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Buyer</p>
                  <p className="text-lg font-semibold">{transaction.buyerEmail}</p>
                </div>

                {/* Seller Info */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Seller</p>
                  <p className="text-lg font-semibold">{transaction.sellerEmail}</p>
                </div>

                {/* Amount */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Carbon Credit Amount</p>
                  <p className="text-2xl font-bold text-emerald-600">{transaction.amount.toFixed(2)} tCO2</p>
                </div>

                {/* Total Price */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Total Price</p>
                  <p className="text-2xl font-bold text-emerald-600">${transaction.totalPrice.toFixed(2)}</p>
                </div>

                {/* Created At */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Created At</p>
                  <p className="text-sm">{new Date(transaction.createdAt).toLocaleString()}</p>
                </div>

                {/* Updated At */}
                <div className="space-y-2">
                  <p className="text-sm font-medium text-muted-foreground">Last Updated</p>
                  <p className="text-sm">{new Date(transaction.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Status Update Card */}
          {canUpdateStatus && (
            <Card>
              <CardHeader>
                <CardTitle>Update Transaction Status</CardTitle>
                <CardDescription>Change the status of this transaction</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-end gap-4">
                  <div className="flex-1 space-y-2">
                    <label className="text-sm font-medium">New Status</label>
                    <Select
                      value={selectedStatus}
                      onValueChange={(v) => setSelectedStatus(v as TransactionStatus)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {availableOptions.map((status) => (
                          <SelectItem key={status} value={status}>
                            {status}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <Button
                    onClick={() => setShowConfirmDialog(true)}
                    disabled={updating || selectedStatus === transaction.status}
                  >
                    {updating ? (
                      <>
                        <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                        Updating...
                      </>
                    ) : (
                      <>
                        <CheckCircle2 className="mr-2 h-4 w-4" />
                        Update Status
                      </>
                    )}
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Info: Status cannot be changed */}
          {!canUpdateStatus && (
            <Card className="border-muted">
              <CardContent className="py-4">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <AlertCircle className="h-4 w-4" />
                  <p className="text-sm">
                    This transaction status is <strong>{transaction.status}</strong> and cannot be modified.
                  </p>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}

      {/* Confirmation Dialog */}
      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirm Status Update</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to change the transaction status from{" "}
              <strong>{transaction?.status}</strong> to <strong>{selectedStatus}</strong>?
              {selectedStatus === "REJECTED" && (
                <span className="block mt-2 text-red-600 font-medium">
                  Warning: Rejecting this transaction cannot be undone.
                </span>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleUpdateStatus}>Confirm</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
