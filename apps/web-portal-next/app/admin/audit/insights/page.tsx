"use client"

/**
 * Admin Audit Insights Page
 * Features: Summary KPI cards + Charts (requests by day, top endpoints)
 * Reuses: Card, recharts (BarChart, LineChart) from Day 8 Analytics
 */

import { useState, useEffect } from "react"
import { RefreshCw, Activity, Users, AlertTriangle } from "lucide-react"
import {
  getAuditSummary,
  getAuditCharts,
  type AuditSummaryResponse,
  type AuditChartResponse,
} from "@/lib/api/admin-audit"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"

export default function AuditInsightsPage() {
  const { toast } = useToast()

  // State
  const [summary, setSummary] = useState<AuditSummaryResponse | null>(null)
  const [charts, setCharts] = useState<AuditChartResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [days, setDays] = useState(7)

  // Fetch data
  const fetchData = async () => {
    try {
      setLoading(true)
      const [summaryData, chartsData] = await Promise.all([
        getAuditSummary(),
        getAuditCharts(days),
      ])
      setSummary(summaryData)
      setCharts(chartsData)
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to load audit insights",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [days])

  // Transform chart data
  const requestsByDayData = charts
    ? Object.entries(charts.requestsByDay)
        .map(([date, count]) => ({ date, requests: count }))
        .sort((a, b) => a.date.localeCompare(b.date))
    : []

  const topEndpointsData = charts
    ? Object.entries(charts.topEndpoints)
        .map(([endpoint, count]) => ({ 
          endpoint: endpoint.length > 40 ? endpoint.slice(0, 37) + "..." : endpoint,
          fullEndpoint: endpoint,
          count 
        }))
        .sort((a, b) => b.count - a.count)
        .slice(0, 10) // Top 10
    : []

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Audit Insights</h1>
          <p className="text-muted-foreground">
            Summary statistics and trends from audit logs
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            variant={days === 7 ? "default" : "outline"}
            size="sm"
            onClick={() => setDays(7)}
          >
            7 Days
          </Button>
          <Button
            variant={days === 14 ? "default" : "outline"}
            size="sm"
            onClick={() => setDays(14)}
          >
            14 Days
          </Button>
          <Button
            variant={days === 30 ? "default" : "outline"}
            size="sm"
            onClick={() => setDays(30)}
          >
            30 Days
          </Button>
          <Button onClick={fetchData} variant="outline" size="sm">
            <RefreshCw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {loading ? (
          <>
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </>
        ) : (
          <>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Total Logs</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {summary?.totalLogs.toLocaleString() || 0}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  All audit log entries
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Unique Users</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {summary?.totalUsers.toLocaleString() || 0}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  Active users logged
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Error Count</CardTitle>
                <AlertTriangle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-destructive">
                  {summary?.errorCount.toLocaleString() || 0}
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  HTTP 4xx/5xx errors
                </p>
              </CardContent>
            </Card>
          </>
        )}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Requests by Day - Line Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Requests by Day</CardTitle>
            <CardDescription>
              Daily request volume over the last {days} days
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-80 w-full" />
            ) : requestsByDayData.length === 0 ? (
              <div className="h-80 flex items-center justify-center text-muted-foreground">
                No data available for selected period
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <LineChart data={requestsByDayData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tick={{ fontSize: 12 }}
                    tickFormatter={(value) => {
                      const date = new Date(value)
                      return `${date.getMonth() + 1}/${date.getDate()}`
                    }}
                  />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip
                    labelFormatter={(value) => {
                      const date = new Date(value as string)
                      return date.toLocaleDateString()
                    }}
                  />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="requests"
                    stroke="#8884d8"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        {/* Top Endpoints - Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Top 10 Endpoints</CardTitle>
            <CardDescription>
              Most frequently accessed endpoints
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <Skeleton className="h-80 w-full" />
            ) : topEndpointsData.length === 0 ? (
              <div className="h-80 flex items-center justify-center text-muted-foreground">
                No endpoint data available
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={topEndpointsData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" tick={{ fontSize: 12 }} />
                  <YAxis
                    dataKey="endpoint"
                    type="category"
                    tick={{ fontSize: 10 }}
                    width={150}
                  />
                  <Tooltip
                    formatter={(value) => [value, "Requests"]}
                    labelFormatter={(value) => {
                      const item = topEndpointsData.find(d => d.endpoint === value)
                      return item?.fullEndpoint || value
                    }}
                  />
                  <Legend />
                  <Bar dataKey="count" fill="#82ca9d" name="Requests" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
