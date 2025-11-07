"use client"

/**
 * Admin Audit Logs Page
 * Features: Server-side pagination, filters (keyword/username), detail drawer
 * Reuses: Table, Input, Button, Badge, Drawer, Skeleton from shadcn/ui
 */

import { useState, useEffect, useCallback } from "react"
import { RefreshCw, Search, Eye, X } from "lucide-react"
import { format } from "date-fns"
import {
  getAuditLogs,
  type AuditLogResponse,
  type AuditLogsQuery,
} from "@/lib/api/admin-audit"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerFooter,
  DrawerHeader,
  DrawerTitle,
} from "@/components/ui/drawer"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"

export default function AuditLogsPage() {
  const { toast } = useToast()

  // State
  const [logs, setLogs] = useState<AuditLogResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Filters & Pagination
  const [query, setQuery] = useState<AuditLogsQuery>({
    page: 0,
    size: 10,
    sortBy: "createdAt",
    direction: "desc",
    keyword: "",
    username: "",
  })

  // Detail drawer
  const [selectedLog, setSelectedLog] = useState<AuditLogResponse | null>(null)
  const [drawerOpen, setDrawerOpen] = useState(false)

  // Fetch logs
  const fetchLogs = useCallback(async () => {
    try {
      setLoading(true)
      const data = await getAuditLogs(query)
      setLogs(data.content)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to load audit logs",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }, [query, toast])

  useEffect(() => {
    fetchLogs()
  }, [fetchLogs])

  // Handlers
  const handleSearch = () => {
    setQuery({ ...query, page: 0 }) // Reset to page 0 on new search
  }

  const handleReset = () => {
    setQuery({
      page: 0,
      size: 10,
      sortBy: "createdAt",
      direction: "desc",
      keyword: "",
      username: "",
    })
  }

  const handlePageChange = (newPage: number) => {
    setQuery({ ...query, page: newPage })
  }

  const handleViewDetail = (log: AuditLogResponse) => {
    setSelectedLog(log)
    setDrawerOpen(true)
  }

  // Utility: HTTP status badge variant
  const getStatusVariant = (status: number): "default" | "destructive" | "secondary" => {
    if (status >= 500) return "destructive"
    if (status >= 400) return "secondary"
    return "default"
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Audit Logs</h1>
          <p className="text-muted-foreground">
            View system activity logs with filtering and pagination
          </p>
        </div>
        <Button onClick={fetchLogs} variant="outline" size="sm">
          <RefreshCw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>
            Search by endpoint keyword or filter by username
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium mb-2 block">
                Keyword (Endpoint)
              </label>
              <Input
                placeholder="e.g., /api/admin/users"
                value={query.keyword || ""}
                onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Username</label>
              <Input
                placeholder="e.g., admin@gmail.com"
                value={query.username || ""}
                onChange={(e) => setQuery({ ...query, username: e.target.value })}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
            </div>
            <div className="flex items-end gap-2">
              <Button onClick={handleSearch} className="flex-1">
                <Search className="mr-2 h-4 w-4" />
                Search
              </Button>
              <Button onClick={handleReset} variant="outline" className="flex-1">
                <X className="mr-2 h-4 w-4" />
                Reset
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Results Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Audit Logs ({totalElements} total)
          </CardTitle>
          <CardDescription>
            Page {query.page! + 1} of {totalPages || 1}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : logs.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No audit logs found. Try adjusting your filters.
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Timestamp</TableHead>
                    <TableHead>User</TableHead>
                    <TableHead>Method</TableHead>
                    <TableHead>Endpoint</TableHead>
                    <TableHead>IP Address</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {logs.map((log) => (
                    <TableRow key={log.id}>
                      <TableCell className="font-mono text-sm">
                        {format(new Date(log.createdAt), "yyyy-MM-dd HH:mm:ss")}
                      </TableCell>
                      <TableCell className="font-medium">{log.username}</TableCell>
                      <TableCell>
                        <Badge variant="outline">{log.method}</Badge>
                      </TableCell>
                      <TableCell className="font-mono text-xs max-w-xs truncate">
                        {log.endpoint}
                      </TableCell>
                      <TableCell className="font-mono text-xs">{log.ip}</TableCell>
                      <TableCell>
                        <Badge variant={getStatusVariant(log.status)}>
                          {log.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleViewDetail(log)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* Pagination */}
              <div className="flex items-center justify-between mt-4">
                <div className="text-sm text-muted-foreground">
                  Showing {logs.length} of {totalElements} logs
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(query.page! - 1)}
                    disabled={query.page === 0}
                  >
                    Previous
                  </Button>
                  <div className="flex items-center gap-2 px-3 text-sm">
                    Page {query.page! + 1} of {totalPages || 1}
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handlePageChange(query.page! + 1)}
                    disabled={query.page! >= totalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Detail Drawer */}
      <Drawer open={drawerOpen} onOpenChange={setDrawerOpen}>
        <DrawerContent>
          <DrawerHeader>
            <DrawerTitle>Audit Log Detail</DrawerTitle>
            <DrawerDescription>
              {selectedLog && `ID: ${selectedLog.id} • ${format(new Date(selectedLog.createdAt), "PPpp")}`}
            </DrawerDescription>
          </DrawerHeader>
          {selectedLog && (
            <div className="px-4 py-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="text-sm font-medium text-muted-foreground">User</div>
                  <div className="font-medium">{selectedLog.username}</div>
                </div>
                <div>
                  <div className="text-sm font-medium text-muted-foreground">IP Address</div>
                  <div className="font-mono text-sm">{selectedLog.ip}</div>
                </div>
                <div>
                  <div className="text-sm font-medium text-muted-foreground">HTTP Method</div>
                  <Badge variant="outline">{selectedLog.method}</Badge>
                </div>
                <div>
                  <div className="text-sm font-medium text-muted-foreground">Status Code</div>
                  <Badge variant={getStatusVariant(selectedLog.status)}>
                    {selectedLog.status}
                  </Badge>
                </div>
              </div>
              <div>
                <div className="text-sm font-medium text-muted-foreground mb-1">Endpoint</div>
                <div className="font-mono text-sm bg-muted p-3 rounded-md break-all">
                  {selectedLog.endpoint}
                </div>
              </div>
              <div>
                <div className="text-sm font-medium text-muted-foreground mb-1">Action</div>
                <div className="font-medium">{selectedLog.action || "N/A"}</div>
              </div>
              <div>
                <div className="text-sm font-medium text-muted-foreground mb-1">Timestamp</div>
                <div className="font-mono text-sm">
                  {format(new Date(selectedLog.createdAt), "PPpp")} ({selectedLog.createdAt})
                </div>
              </div>
            </div>
          )}
          <DrawerFooter>
            <DrawerClose asChild>
              <Button variant="outline">Close</Button>
            </DrawerClose>
          </DrawerFooter>
        </DrawerContent>
      </Drawer>
    </div>
  )
}
