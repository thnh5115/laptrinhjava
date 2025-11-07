"use client"

import { useState, useEffect, useCallback } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  listDisputes,
  DisputeSummary,
  DisputeStatus,
  DisputeQueryParams,
} from "@/lib/api/admin-disputes"
import { Search, X, ArrowUp, ArrowDown, RefreshCw } from "lucide-react"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"

// Debounce hook (reused from TransactionMonitor pattern)
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

export function DisputeList() {
  const router = useRouter()
  const { toast } = useToast()

  // Filter & pagination state (pattern from TransactionMonitor)
  const [keyword, setKeyword] = useState("")
  const [status, setStatus] = useState<DisputeStatus | "ALL">("ALL")
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [sortBy, setSortBy] = useState<"createdAt">("createdAt")
  const [direction, setDirection] = useState<"asc" | "desc">("desc")

  // Data state
  const [disputes, setDisputes] = useState<DisputeSummary[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  // Debounce keyword to avoid spam requests
  const debouncedKeyword = useDebounce(keyword, 400)

  // Fetch disputes (pattern from TransactionMonitor)
  const fetchDisputes = useCallback(async () => {
    setLoading(true)
    try {
      const params: DisputeQueryParams = {
        page,
        size,
        sortBy,
        direction,
        keyword: debouncedKeyword,
        status,
      }

      const response = await listDisputes(params)
      setDisputes(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (error: any) {
      console.error("Failed to fetch disputes:", error)
      toast({
        title: "Error",
        description: error?.response?.data?.message || "Failed to load disputes",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }, [page, size, sortBy, direction, debouncedKeyword, status, toast])

  useEffect(() => {
    fetchDisputes()
  }, [fetchDisputes])

  // Reset filters
  const handleReset = () => {
    setKeyword("")
    setStatus("ALL")
    setPage(0)
    setSortBy("createdAt")
    setDirection("desc")
  }

  // Toggle sort direction
  const handleSort = (field: "createdAt") => {
    if (sortBy === field) {
      setDirection(direction === "asc" ? "desc" : "asc")
    } else {
      setSortBy(field)
      setDirection("desc")
    }
  }

  // Row click navigation
  const handleRowClick = (id: number) => {
    router.push(`/admin/disputes/${id}`)
  }

  // Status badge color (reused pattern)
  const getStatusColor = (status: string) => {
    switch (status) {
      case "OPEN":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      case "IN_REVIEW":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      case "RESOLVED":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "REJECTED":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      default:
        return "bg-gray-100 text-gray-900 dark:bg-gray-900 dark:text-gray-100"
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between mb-4">
          <div>
            <CardTitle>Disputes Management ({totalElements})</CardTitle>
            <CardDescription>Review and resolve transaction disputes</CardDescription>
          </div>
          <Button variant="outline" size="sm" onClick={fetchDisputes}>
            <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </Button>
        </div>

        {/* Filter Bar - reused from TransactionMonitor pattern */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {/* Search keyword */}
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by description or email..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="pl-8"
            />
          </div>

          {/* Status filter */}
          <Select value={status} onValueChange={(v) => setStatus(v as DisputeStatus | "ALL")}>
            <SelectTrigger>
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Status</SelectItem>
              <SelectItem value="OPEN">Open</SelectItem>
              <SelectItem value="IN_REVIEW">In Review</SelectItem>
              <SelectItem value="RESOLVED">Resolved</SelectItem>
              <SelectItem value="REJECTED">Rejected</SelectItem>
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
              <Skeleton key={i} className="h-20 w-full" />
            ))}
          </div>
        ) : disputes.length === 0 ? (
          /* Empty state */
          <div className="text-center py-12">
            <p className="text-muted-foreground mb-4">No disputes found</p>
            <Button variant="outline" onClick={handleReset}>
              Reset Filters
            </Button>
          </div>
        ) : (
          <>
            {/* Table Header */}
            <div className="mb-3 grid grid-cols-12 gap-2 px-4 py-2 bg-muted/50 rounded-md text-xs font-medium text-muted-foreground">
              <div className="col-span-2">ID / Code</div>
              <div className="col-span-3">Raised By</div>
              <div className="col-span-2">Transaction ID</div>
              <div className="col-span-2">Status</div>
              <div
                className="col-span-3 flex items-center gap-1 cursor-pointer hover:text-foreground"
                onClick={() => handleSort("createdAt")}
              >
                Created At
                {sortBy === "createdAt" &&
                  (direction === "desc" ? <ArrowDown className="h-3 w-3" /> : <ArrowUp className="h-3 w-3" />)}
              </div>
            </div>

            {/* Dispute Rows */}
            <div className="space-y-2">
              {disputes.map((dispute) => (
                <div
                  key={dispute.id}
                  onClick={() => handleRowClick(dispute.id)}
                  className="grid grid-cols-12 gap-2 items-center border rounded-lg px-4 py-3 hover:bg-accent/50 cursor-pointer transition-colors"
                >
                  <div className="col-span-2">
                    <p className="font-medium text-sm">#{dispute.id}</p>
                    <p className="text-xs text-muted-foreground truncate">{dispute.disputeCode}</p>
                  </div>
                  <div className="col-span-3 text-sm truncate">{dispute.raisedBy}</div>
                  <div className="col-span-2 text-sm font-mono">#{dispute.transactionId}</div>
                  <div className="col-span-2">
                    <Badge className={getStatusColor(dispute.status)}>{dispute.status.replace(/_/g, " ")}</Badge>
                  </div>
                  <div className="col-span-3 text-sm text-muted-foreground">
                    {new Date(dispute.createdAt).toLocaleString()}
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
