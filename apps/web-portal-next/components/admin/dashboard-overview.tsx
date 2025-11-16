"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Users, Leaf, DollarSign, TrendingUp, Activity, AlertCircle, Loader2 } from "lucide-react"
import { Line, LineChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer, Bar, BarChart } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import { Badge } from "@/components/ui/badge"
import { getKpis, getTransactionTrends, getDisputeRatios, type SystemKpi, type TransactionTrend, type DisputeRatio } from "@/lib/api/admin-analytics"

export function AdminDashboardOverview() {
  // State
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [kpis, setKpis] = useState<SystemKpi | null>(null)
  const [trends, setTrends] = useState<TransactionTrend | null>(null)
  const [disputes, setDisputes] = useState<DisputeRatio | null>(null)

  // Fetch dashboard data on mount
  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true)
      setError(null)
      try {
        const currentYear = new Date().getFullYear()
        const [kpisData, trendsData, disputesData] = await Promise.all([
          getKpis(),
          getTransactionTrends(currentYear),
          getDisputeRatios(),
        ])
        setKpis(kpisData)
        setTrends(trendsData)
        setDisputes(disputesData)
      } catch (err: any) {
        console.error("Failed to load dashboard data:", err)
        setError(err.message || "Failed to load dashboard data")
      } finally {
        setLoading(false)
      }
    }

    fetchDashboardData()
  }, [])

  // Transform monthly data for charts
  const lineData = trends
    ? Object.entries(trends.monthlyTransactions)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([month, transactions]) => ({
          month: new Date(month + "-01").toLocaleDateString("en-US", { month: "short" }),
          transactions,
          revenue: trends.monthlyRevenue[month] || 0,
        }))
    : []

  const disputeData = disputes
    ? [
        { status: "Open", count: disputes.openCount },
        { status: "Resolved", count: disputes.resolvedCount },
        { status: "Rejected", count: disputes.rejectedCount },
      ]
    : []

  // Loading state
  if (loading) {
    return (
      <div className="flex items-center justify-center h-[600px]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-10 w-10 animate-spin text-emerald-600" />
          <p className="text-sm text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  // Error state
  if (error) {
    return (
      <div className="flex items-center justify-center h-[600px]">
        <div className="flex flex-col items-center gap-3 max-w-md text-center">
          <AlertCircle className="h-10 w-10 text-destructive" />
          <h3 className="text-lg font-semibold">Failed to Load Dashboard</h3>
          <p className="text-sm text-muted-foreground">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-emerald-600 text-white rounded-md hover:bg-emerald-700"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
        <p className="text-muted-foreground">Platform overview and system management</p>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Users</CardTitle>
            <Users className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalUsers ?? 0}</div>
            <p className="text-xs text-muted-foreground">Registered accounts</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Disputes</CardTitle>
            <AlertCircle className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalDisputes ?? 0}</div>
            <p className="text-xs text-muted-foreground">
              {kpis?.disputeRate ? `${kpis.disputeRate.toFixed(1)}% dispute rate` : "No disputes"}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Platform Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${kpis?.totalRevenue.toFixed(2) ?? "0.00"}</div>
            <p className="text-xs text-muted-foreground">Approved transactions only</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Transactions</CardTitle>
            <Activity className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalTransactions ?? 0}</div>
            <p className="text-xs text-muted-foreground">All statuses</p>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Transaction Trends</CardTitle>
            <CardDescription>Monthly transactions and revenue ({new Date().getFullYear()})</CardDescription>
          </CardHeader>
          <CardContent>
            {lineData.length > 0 ? (
              <ChartContainer
                config={{
                  transactions: {
                    label: "Transactions",
                    color: "hsl(var(--chart-1))",
                  },
                  revenue: {
                    label: "Revenue",
                    color: "hsl(var(--chart-2))",
                  },
                }}
                className="h-[300px]"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={lineData}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                    <XAxis dataKey="month" className="text-xs" />
                    <YAxis className="text-xs" />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <Line
                      type="monotone"
                      dataKey="transactions"
                      stroke="var(--color-chart-1)"
                      strokeWidth={2}
                      dot={{ fill: "var(--color-chart-1)" }}
                    />
                    <Line
                      type="monotone"
                      dataKey="revenue"
                      stroke="var(--color-chart-2)"
                      strokeWidth={2}
                      dot={{ fill: "var(--color-chart-2)" }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </ChartContainer>
            ) : (
              <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                No transaction data available
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Dispute Distribution</CardTitle>
            <CardDescription>Disputes by status</CardDescription>
          </CardHeader>
          <CardContent>
            {disputeData.length > 0 && disputes && disputes.total > 0 ? (
              <ChartContainer
                config={{
                  count: {
                    label: "Disputes",
                    color: "hsl(var(--chart-1))",
                  },
                }}
                className="h-[300px]"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={disputeData}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                    <XAxis dataKey="status" className="text-xs" />
                    <YAxis className="text-xs" />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <Bar dataKey="count" fill="var(--color-chart-1)" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </ChartContainer>
            ) : (
              <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                No dispute data available
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* System Info */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>System Summary</CardTitle>
            <CardDescription>Key platform statistics</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-start gap-3 p-3 border rounded-lg">
                <Users className="h-5 w-5 text-emerald-600 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium">Total Users</p>
                  <p className="text-xs text-muted-foreground">{kpis?.totalUsers ?? 0} registered accounts</p>
                </div>
                <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                  {kpis?.totalUsers ?? 0}
                </Badge>
              </div>
              <div className="flex items-start gap-3 p-3 border rounded-lg">
                <Activity className="h-5 w-5 text-blue-600 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium">Total Transactions</p>
                  <p className="text-xs text-muted-foreground">{kpis?.totalTransactions ?? 0} transactions processed</p>
                </div>
                <Badge className="bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100">
                  {kpis?.totalTransactions ?? 0}
                </Badge>
              </div>
              {kpis && kpis.totalDisputes > 0 && (
                <div className="flex items-start gap-3 p-3 border rounded-lg bg-amber-50 dark:bg-amber-950 border-amber-200 dark:border-amber-800">
                  <AlertCircle className="h-5 w-5 text-amber-600 mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm font-medium">Active Disputes</p>
                    <p className="text-xs text-muted-foreground">
                      {kpis.totalDisputes} disputes ({kpis.disputeRate.toFixed(1)}% rate)
                    </p>
                  </div>
                  <Badge
                    variant="secondary"
                    className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                  >
                    {kpis.totalDisputes}
                  </Badge>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Revenue Overview</CardTitle>
            <CardDescription>Platform financial summary</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b pb-3">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Total Revenue</p>
                  <p className="text-xs text-muted-foreground">From approved transactions</p>
                </div>
                <p className="text-lg font-bold">${kpis?.totalRevenue.toFixed(2) ?? "0.00"}</p>
              </div>
              <div className="flex items-center justify-between border-b pb-3">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Dispute Rate</p>
                  <p className="text-xs text-muted-foreground">Percentage of disputed transactions</p>
                </div>
                <p className="text-lg font-bold">{kpis?.disputeRate.toFixed(1) ?? "0.0"}%</p>
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Total Disputes</p>
                  <p className="text-xs text-muted-foreground">All dispute statuses</p>
                </div>
                <p className="text-lg font-bold">{kpis?.totalDisputes ?? 0}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
