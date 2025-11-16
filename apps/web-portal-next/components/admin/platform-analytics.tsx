"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, Users, Leaf, DollarSign, Activity, AlertTriangle, RefreshCw } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"
import {
  getKpis,
  getTransactionTrends,
  getDisputeRatios,
  type SystemKpi,
  type TransactionTrend,
  type DisputeRatio,
} from "@/lib/api/admin-analytics"
import {
  Area,
  AreaChart,
  CartesianGrid,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Pie,
  PieChart,
  Cell,
  Legend,
  Tooltip,
} from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"

export function PlatformAnalytics() {
  const { toast } = useToast()
  const [kpis, setKpis] = useState<SystemKpi | null>(null)
  const [trends, setTrends] = useState<TransactionTrend | null>(null)
  const [disputes, setDisputes] = useState<DisputeRatio | null>(null)
  const [year, setYear] = useState(new Date().getFullYear())
  const [loading, setLoading] = useState(true)

  // Fetch all analytics data
  const fetchAnalytics = async () => {
    setLoading(true)
    try {
      const [kpiData, trendData, disputeData] = await Promise.all([
        getKpis(),
        getTransactionTrends(year),
        getDisputeRatios(),
      ])

      setKpis(kpiData)
      setTrends(trendData)
      setDisputes(disputeData)

      console.log("[Analytics] KPIs:", kpiData)
      console.log("[Analytics] Trends:", trendData)
      console.log("[Analytics] Disputes:", disputeData)
    } catch (error: any) {
      console.error("[Analytics] Failed to fetch data:", error)
      toast({
        title: "Error Loading Analytics",
        description: error?.response?.data?.message || "Failed to load analytics data. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAnalytics()
  }, [year])

  // Transform transaction trends for charts
  const monthlyData = trends
    ? Array.from({ length: 12 }, (_, i) => {
        const month = i + 1
        const monthKey = `${year}-${month.toString().padStart(2, "0")}`
        return {
          month: new Date(year, i).toLocaleString("default", { month: "short" }),
          transactions: trends.monthlyTransactions[monthKey] ?? 0,
          revenue: trends.monthlyRevenue[monthKey] ?? 0,
        }
      })
    : []

  // Transform dispute data for pie chart
  const disputeChartData = disputes
    ? [
        { name: "Open", value: disputes.openCount, color: "#ef4444" },
        { name: "Resolved", value: disputes.resolvedCount, color: "#10b981" },
        { name: "Rejected", value: disputes.rejectedCount, color: "#6b7280" },
      ].filter(item => item.value > 0)
    : []

  if (loading) {
    return (
      <div className="space-y-6">
        {/* KPI Cards Skeleton */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-4 rounded" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-32 mb-2" />
                <Skeleton className="h-3 w-20" />
              </CardContent>
            </Card>
          ))}
        </div>
        {/* Charts Skeleton */}
        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
              <Skeleton className="h-4 w-64 mt-2" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-[300px] w-full" />
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
              <Skeleton className="h-4 w-64 mt-2" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-[300px] w-full" />
            </CardContent>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header with Refresh Button */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Dashboard KPIs</h2>
          <p className="text-muted-foreground">Real-time platform performance metrics</p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchAnalytics} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Users</CardTitle>
            <Users className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalUsers.toLocaleString() ?? 0}</div>
            <p className="text-xs text-muted-foreground">Registered in system</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Transactions</CardTitle>
            <Activity className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalTransactions.toLocaleString() ?? 0}</div>
            <p className="text-xs text-muted-foreground">All statuses</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${kpis?.totalRevenue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Approved transactions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Disputes</CardTitle>
            <AlertTriangle className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{kpis?.totalDisputes.toLocaleString() ?? 0}</div>
            <p className="text-xs text-muted-foreground">
              {kpis?.disputeRate.toFixed(1) ?? "0.0"}% dispute rate
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Charts Row 1: Transaction Trends & Dispute Breakdown */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Transaction Trends Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Transaction Trends - {year}</CardTitle>
            <CardDescription>Monthly transaction volume and revenue</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                transactions: {
                  label: "Transactions",
                  color: "hsl(var(--chart-1))",
                },
                revenue: {
                  label: "Revenue ($)",
                  color: "hsl(var(--chart-2))",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={monthlyData}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="month" className="text-xs" tick={{ fill: "hsl(var(--foreground))" }} />
                  <YAxis yAxisId="left" className="text-xs" tick={{ fill: "hsl(var(--foreground))" }} />
                  <YAxis yAxisId="right" orientation="right" className="text-xs" tick={{ fill: "hsl(var(--foreground))" }} />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Area
                    yAxisId="left"
                    type="monotone"
                    dataKey="transactions"
                    stroke="var(--color-chart-1)"
                    fill="var(--color-chart-1)"
                    fillOpacity={0.2}
                  />
                  <Area
                    yAxisId="right"
                    type="monotone"
                    dataKey="revenue"
                    stroke="var(--color-chart-2)"
                    fill="var(--color-chart-2)"
                    fillOpacity={0.2}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>

        {/* Dispute Ratio Pie Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Dispute Status Breakdown</CardTitle>
            <CardDescription>Distribution of disputes by status</CardDescription>
          </CardHeader>
          <CardContent>
            {disputeChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={disputeChartData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, value }) => `${name}: ${value}`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {disputeChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                No dispute data available
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Additional Metrics Row */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Transaction Value</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${kpis && kpis.totalTransactions > 0
                ? (kpis.totalRevenue / kpis.totalTransactions).toFixed(2)
                : "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Per transaction</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Open Disputes</CardTitle>
            <AlertTriangle className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{disputes?.openCount.toLocaleString() ?? 0}</div>
            <p className="text-xs text-muted-foreground">Requires attention</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Resolved Disputes</CardTitle>
            <Activity className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{disputes?.resolvedCount.toLocaleString() ?? 0}</div>
            <p className="text-xs text-muted-foreground">Successfully closed</p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
