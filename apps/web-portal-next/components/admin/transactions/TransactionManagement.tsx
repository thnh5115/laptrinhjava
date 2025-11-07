"use client"

import { useState, useEffect, useCallback } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  listTransactions,
  getTransaction,
  updateTransactionStatus,
  TransactionSummary,
  TransactionDetail,
  TransactionStatus,
  TransactionType,
  TransactionQueryParams,
} from "@/lib/api/admin-transactions"
import { Search, X, RefreshCw, CheckCircle, XCircle, Eye } from "lucide-react"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import { TransactionDetailModal } from "./TransactionDetailModal"

// Debounce hook
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value)
    }, delay)

    return () => {
      clearTimeout(handler)
    }
  }, [value, delay])

  return debouncedValue
}

export function TransactionManagement() {
  const { toast } = useToast()

  // Filter & pagination state
  const [keyword, setKeyword] = useState("")
  const [status, setStatus] = useState<TransactionStatus | "ALL">("ALL")
  const [type, setType] = useState<TransactionType | "ALL">("ALL")
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [sort, setSort] = useState("createdAt,desc")

  // Data state
  const [transactions, setTransactions] = useState<TransactionSummary[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  // Modal state
  const [selectedTransaction, setSelectedTransaction] = useState<TransactionDetail | null>(null)
  const [showDetailModal, setShowDetailModal] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)

  // Debounce keyword
  const debouncedKeyword = useDebounce(keyword, 400)

  // Fetch transactions
  const fetchTransactions = useCallback(async () => {
    setLoading(true)
    try {
      const params: TransactionQueryParams = {
        page,
        size,
        sort,
        keyword: debouncedKeyword,
        status,
        type,
      }

      const response = await listTransactions(params)
      setTransactions(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (error: any) {
      console.error("Failed to fetch transactions:", error)
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to load transactions",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }, [page, size, sort, debouncedKeyword, status, type, toast])

  useEffect(() => {
    fetchTransactions()
  }, [fetchTransactions])

  // Reset filters
  const handleReset = () => {
    setKeyword("")
    setStatus("ALL")
    setType("ALL")
    setPage(0)
    setSort("createdAt,desc")
  }

  // View transaction detail
  const handleViewDetail = async (id: number) => {
    setDetailLoading(true)
    setShowDetailModal(true)
    try {
      const detail = await getTransaction(id)
      setSelectedTransaction(detail)
    } catch (error: any) {
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to load transaction details",
        variant: "destructive",
      })
      setShowDetailModal(false)
    } finally {
      setDetailLoading(false)
    }
  }

  // Approve transaction
  const handleApprove = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation()
    if (!confirm("Are you sure you want to approve this transaction?")) return

    try {
      await updateTransactionStatus(id, "APPROVED")
      toast({
        title: "Success",
        description: "Transaction approved successfully",
      })
      fetchTransactions() // Refresh list
    } catch (error: any) {
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to approve transaction",
        variant: "destructive",
      })
    }
  }

  // Reject transaction
  const handleReject = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation()
    if (!confirm("Are you sure you want to reject this transaction?")) return

    try {
      await updateTransactionStatus(id, "REJECTED")
      toast({
        title: "Success",
        description: "Transaction rejected successfully",
      })
      fetchTransactions() // Refresh list
    } catch (error: any) {
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to reject transaction",
        variant: "destructive",
      })
    }
  }

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
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Transaction Management ({totalElements})</CardTitle>
              <CardDescription>Review and approve carbon credit transactions</CardDescription>
            </div>
            <Button variant="outline" size="sm" onClick={fetchTransactions} disabled={loading}>
              <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
              Refresh
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {/* Filters */}
          <div className="flex flex-col gap-4 mb-6">
            <div className="flex flex-wrap gap-3">
              {/* Search */}
              <div className="flex-1 min-w-[200px]">
                <div className="relative">
                  <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Search by code, buyer, seller..."
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    className="pl-8"
                  />
                  {keyword && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="absolute right-0 top-0 h-full px-3"
                      onClick={() => setKeyword("")}
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </div>

              {/* Status Filter */}
              <Select value={status} onValueChange={(v) => setStatus(v as TransactionStatus | "ALL")}>
                <SelectTrigger className="w-[140px]">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Status</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="APPROVED">Approved</SelectItem>
                  <SelectItem value="REJECTED">Rejected</SelectItem>
                </SelectContent>
              </Select>

              {/* Type Filter */}
              <Select value={type} onValueChange={(v) => setType(v as TransactionType | "ALL")}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Types</SelectItem>
                  <SelectItem value="CREDIT_PURCHASE">Purchase</SelectItem>
                  <SelectItem value="CREDIT_SALE">Sale</SelectItem>
                  <SelectItem value="TRANSFER">Transfer</SelectItem>
                </SelectContent>
              </Select>

              {/* Reset Button */}
              <Button variant="outline" onClick={handleReset}>
                Reset
              </Button>
            </div>
          </div>

          {/* Table */}
          {loading ? (
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : transactions.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <p>No transactions found</p>
            </div>
          ) : (
            <>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Code</TableHead>
                      <TableHead>Buyer</TableHead>
                      <TableHead>Seller</TableHead>
                      <TableHead className="text-right">Amount</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Created At</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transactions.map((transaction) => (
                      <TableRow key={transaction.id} className="cursor-pointer hover:bg-muted/50">
                        <TableCell className="font-mono text-sm">
                          {transaction.transactionCode}
                        </TableCell>
                        <TableCell>{transaction.buyerEmail}</TableCell>
                        <TableCell>{transaction.sellerEmail}</TableCell>
                        <TableCell className="text-right font-medium">
                          {formatCurrency(transaction.totalPrice)}
                        </TableCell>
                        <TableCell>
                          <span className="text-sm text-muted-foreground">
                            {transaction.transactionCode.startsWith("PURCHASE")
                              ? "Purchase"
                              : transaction.transactionCode.startsWith("SALE")
                              ? "Sale"
                              : "Transfer"}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Badge className={getStatusColor(transaction.status)} variant="secondary">
                            {transaction.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {formatDate(transaction.createdAt)}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex gap-2 justify-end">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleViewDetail(transaction.id)}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            {transaction.status === "PENDING" && (
                              <>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-green-600 hover:text-green-700 hover:bg-green-50"
                                  onClick={(e) => handleApprove(transaction.id, e)}
                                >
                                  <CheckCircle className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-red-600 hover:text-red-700 hover:bg-red-50"
                                  onClick={(e) => handleReject(transaction.id, e)}
                                >
                                  <XCircle className="h-4 w-4" />
                                </Button>
                              </>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="mt-4">
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setPage(Math.max(0, page - 1))}
                          className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                        />
                      </PaginationItem>

                      {[...Array(totalPages)].map((_, i) => (
                        <PaginationItem key={i}>
                          <PaginationLink
                            onClick={() => setPage(i)}
                            isActive={page === i}
                            className="cursor-pointer"
                          >
                            {i + 1}
                          </PaginationLink>
                        </PaginationItem>
                      ))}

                      <PaginationItem>
                        <PaginationNext
                          onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                          className={
                            page === totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"
                          }
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* Detail Modal */}
      <TransactionDetailModal
        transaction={selectedTransaction}
        loading={detailLoading}
        open={showDetailModal}
        onClose={() => {
          setShowDetailModal(false)
          setSelectedTransaction(null)
        }}
        onApprove={async (id: number) => {
          await handleApprove(id, {} as React.MouseEvent)
          setShowDetailModal(false)
        }}
        onReject={async (id: number) => {
          await handleReject(id, {} as React.MouseEvent)
          setShowDetailModal(false)
        }}
      />
    </>
  )
}
