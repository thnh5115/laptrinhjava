"use client"

import { useState, useEffect, useCallback } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  listTransactions,
  TransactionSummary,
  TransactionStatus,
  TransactionType,
  TransactionQueryParams,
} from "@/lib/api/admin-transactions"
import { Search, X, ArrowUpDown, ArrowUp, ArrowDown, RefreshCw } from "lucide-react"
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"

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

export function TransactionMonitor() {
  const router = useRouter()
  const { toast } = useToast()

  // Filter & pagination state
  const [keyword, setKeyword] = useState("")
  const [status, setStatus] = useState<TransactionStatus | "ALL">("ALL")
  const [type, setType] = useState<TransactionType | "ALL">("ALL")
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [sortBy, setSortBy] = useState<"createdAt" | "totalPrice">("createdAt")
  const [direction, setDirection] = useState<"asc" | "desc">("desc")

  // Data state
  const [transactions, setTransactions] = useState<TransactionSummary[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  // Debounce keyword để tránh spam request
  const debouncedKeyword = useDebounce(keyword, 400)

  // Fetch transactions
  const fetchTransactions = useCallback(async () => {
    setLoading(true)
    try {
      const params: TransactionQueryParams = {
        page,
        size,
        sortBy,
        direction,
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
  }, [page, size, sortBy, direction, debouncedKeyword, status, type, toast])

  useEffect(() => {
    fetchTransactions()
  }, [fetchTransactions])

  // Reset filters
  const handleReset = () => {
    setKeyword("")
    setStatus("ALL")
    setType("ALL")
    setPage(0)
    setSortBy("createdAt")
    setDirection("desc")
  }

  // Toggle sort direction
  const handleSort = (field: "createdAt" | "totalPrice") => {
    if (sortBy === field) {
      setDirection(direction === "asc" ? "desc" : "asc")
    } else {
      setSortBy(field)
      setDirection("desc")
    }
  }

  // Row click navigation
  const handleRowClick = (id: number) => {
    router.push(`/admin/transactions/${id}`)
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

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between mb-4">
          <div>
            <CardTitle>All Transactions ({totalElements})</CardTitle>
            <CardDescription>View and manage carbon credit transactions</CardDescription>
          </div>
          <Button variant="outline" size="sm" onClick={fetchTransactions}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
        </div>

        {/* Filter Bar */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          {/* Search keyword */}
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by email or code..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="pl-8"
            />
          </div>

          {/* Status filter */}
          <Select value={status} onValueChange={(v) => setStatus(v as TransactionStatus | "ALL")}>
            <SelectTrigger>
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Status</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="APPROVED">Approved</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
            </SelectContent>
          </Select>

          {/* Type filter */}
          <Select value={type} onValueChange={(v) => setType(v as TransactionType | "ALL")}>
            <SelectTrigger>
              <SelectValue placeholder="Filter by type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Types</SelectItem>
              <SelectItem value="CREDIT_PURCHASE">Credit Purchase</SelectItem>
              <SelectItem value="CREDIT_SALE">Credit Sale</SelectItem>
              <SelectItem value="TRANSFER">Transfer</SelectItem>
            </SelectContent>
          </Select>

          {/* Reset button */}
          <Button variant="outline" onClick={handleReset}>
            <X className="mr-2 h-4 w-4" />
            Reset
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        {/* Loading state */}
        {loading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-24 w-full" />
            ))}
          </div>
        ) : transactions.length === 0 ? (
          /* Empty state */
          <div className="text-center py-12">
            <p className="text-muted-foreground mb-4">No transactions found</p>
            <Button variant="outline" onClick={handleReset}>
              Reset Filters
            </Button>
          </div>
        ) : (
          <>
            {/* Table Header */}
            <div className="mb-3 grid grid-cols-12 gap-2 px-4 py-2 bg-muted/50 rounded-md text-xs font-medium text-muted-foreground">
              <div className="col-span-2">ID / Code</div>
              <div className="col-span-2">Buyer</div>
              <div className="col-span-2">Seller</div>
              <div className="col-span-1">Status</div>
              <div
                className="col-span-2 flex items-center gap-1 cursor-pointer hover:text-foreground"
                onClick={() => handleSort("totalPrice")}
              >
                Total Price
                {sortBy === "totalPrice" &&
                  (direction === "desc" ? <ArrowDown className="h-3 w-3" /> : <ArrowUp className="h-3 w-3" />)}
              </div>
              <div
                className="col-span-3 flex items-center gap-1 cursor-pointer hover:text-foreground"
                onClick={() => handleSort("createdAt")}
              >
                Created At
                {sortBy === "createdAt" &&
                  (direction === "desc" ? <ArrowDown className="h-3 w-3" /> : <ArrowUp className="h-3 w-3" />)}
              </div>
            </div>

            {/* Transaction Rows */}
            <div className="space-y-2">
              {transactions.map((tx) => (
                <div
                  key={tx.id}
                  onClick={() => handleRowClick(tx.id)}
                  className="grid grid-cols-12 gap-2 items-center border rounded-lg px-4 py-3 hover:bg-accent/50 cursor-pointer transition-colors"
                >
                  <div className="col-span-2">
                    <p className="font-medium text-sm">#{tx.id}</p>
                    <p className="text-xs text-muted-foreground truncate">{tx.transactionCode}</p>
                  </div>
                  <div className="col-span-2 text-sm truncate">{tx.buyerEmail}</div>
                  <div className="col-span-2 text-sm truncate">{tx.sellerEmail}</div>
                  <div className="col-span-1">
                    <Badge className={getStatusColor(tx.status)}>{tx.status}</Badge>
                  </div>
                  <div className="col-span-2 font-semibold text-emerald-600">${tx.totalPrice.toFixed(2)}</div>
                  <div className="col-span-3 text-sm text-muted-foreground">
                    {new Date(tx.createdAt).toLocaleString()}
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                  Page {page + 1} of {totalPages} ({totalElements} total)
                </p>
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage(Math.max(0, page - 1))}
                        className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                      />
                    </PaginationItem>

                    {/* Page numbers */}
                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                      const pageNum = i
                      return (
                        <PaginationItem key={pageNum}>
                          <PaginationLink
                            onClick={() => setPage(pageNum)}
                            isActive={page === pageNum}
                            className="cursor-pointer"
                          >
                            {pageNum + 1}
                          </PaginationLink>
                        </PaginationItem>
                      )
                    })}

                    {totalPages > 5 && <PaginationEllipsis />}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                        className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
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
  )
}
