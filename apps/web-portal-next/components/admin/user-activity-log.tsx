"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Activity } from "lucide-react"
import { getAuditLogs, type AuditLogResponse } from "@/lib/api/admin-audit"
import { Skeleton } from "@/components/ui/skeleton"

interface UserActivityLogProps {
  userId: string
}

export function UserActivityLog({ userId }: UserActivityLogProps) {
  const [logs, setLogs] = useState<AuditLogResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isMounted = true
    const fetchLogs = async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await getAuditLogs({ page: 0, size: 10, username: undefined })
        if (isMounted) {
          // If filtering by userId is required, map username/email to userId; currently list latest logs
          setLogs(data.content || [])
        }
      } catch (err: any) {
        if (isMounted) {
          setError(err?.message || "Failed to load activity logs")
        }
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }
    fetchLogs()
    return () => {
      isMounted = false
    }
  }, [userId])

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Activity className="h-5 w-5" />
          Activity Log
        </CardTitle>
        <CardDescription>Recent platform activity</CardDescription>
      </CardHeader>
      <CardContent>
        {error && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        {loading ? (
          <div className="space-y-2">
            {[...Array(5)].map((_, idx) => (
              <Skeleton key={idx} className="h-6 w-full" />
            ))}
          </div>
        ) : logs.length === 0 ? (
          <div className="text-sm text-muted-foreground">No activity yet.</div>
        ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Action</TableHead>
              <TableHead>Endpoint</TableHead>
              <TableHead>Timestamp</TableHead>
              <TableHead>User</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {logs.map((log) => (
              <TableRow key={log.id}>
                <TableCell>
                  <Badge variant="outline">{log.action}</Badge>
                </TableCell>
                <TableCell className="text-sm">{log.endpoint}</TableCell>
                <TableCell>{new Date(log.createdAt).toLocaleString()}</TableCell>
                <TableCell className="text-sm">{log.username}</TableCell>
                <TableCell className="font-mono text-xs">{log.status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        )}
      </CardContent>
    </Card>
  )
}
