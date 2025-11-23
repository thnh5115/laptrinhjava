"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Wallet, TrendingUp, TrendingDown, DollarSign, AlertCircle } from "lucide-react"
import { getKpis, getTransactionTrends, SystemKpi, TransactionTrend } from "@/lib/api/admin-analytics"
import { getPayoutStatistics, PayoutStatisticsResponse } from "@/lib/api/admin-payouts"
import { Area, AreaChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import { Alert, AlertDescription } from "@/components/ui/alert"

export function FinanceOverview() {
  const [kpis, setKpis] = useState<SystemKpi | null>(null)
  const [trends, setTrends] = useState<TransactionTrend | null>(null)
  const [payoutStats, setPayoutStats] = useState<PayoutStatisticsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true)
      setError(null)
      try {
        const currentYear = new Date().getFullYear()
        const [kpisData, trendsData, payoutData] = await Promise.all([
          getKpis(),
          getTransactionTrends(currentYear),
          getPayoutStatistics(),
        ])
        setKpis(kpisData)
        setTrends(trendsData)
        setPayoutStats(payoutData)
      } catch (err: any) {
        console.error("Failed to fetch finance overview:", err)
        setError(err?.response?.data?.message || "Failed to load finance data")
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  // Transform trends data for chart
  const revenueData = trends
    ? Object.keys(trends.monthlyRevenue)
        .sort()
        .map((month) => {
          const monthName = new Date(month + "-01").toLocaleDateString("en-US", { month: "short" })
          return {
            month: monthName,
            revenue: trends.monthlyRevenue[month] || 0,
            fees: (trends.monthlyRevenue[month] || 0) * 0.05, // 5% platform fee
          }
        })
    : []

  // Calculate platform metrics
  const platformFees = kpis ? kpis.totalRevenue * 0.05 : 0
  const pendingPayouts = payoutStats?.pendingAmount ?? 0
  const platformBalance = platformFees - pendingPayouts

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <Card key={i}>
              <CardHeader className="space-y-0 pb-2">
                <Skeleton className="h-4 w-32" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-24 mb-2" />
                <Skeleton className="h-3 w-20" />
              </CardContent>
            </Card>
          ))}
        </div>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-40" />
            <Skeleton className="h-4 w-64 mt-2" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-[300px] w-full" />
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    )
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Platform Balance</CardTitle>
            <Wallet className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${platformBalance.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Available funds</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${(kpis?.totalRevenue ?? 0).toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">{kpis?.totalTransactions ?? 0} transactions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Platform Fees</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${platformFees.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">5% commission</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending Payouts</CardTitle>
            <TrendingDown className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${pendingPayouts.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Awaiting approval</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Revenue Trends</CardTitle>
          <CardDescription>Monthly revenue and platform fees collected</CardDescription>
        </CardHeader>
        <CardContent>
          {revenueData.length === 0 ? (
            <div className="h-[300px] flex items-center justify-center text-muted-foreground">
              <p>No revenue data available</p>
            </div>
          ) : (
            <ChartContainer
              config={{
                revenue: {
                  label: "Revenue",
                  color: "hsl(var(--chart-1))",
                },
                fees: {
                  label: "Fees",
                  color: "hsl(var(--chart-2))",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={revenueData}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="month" className="text-xs" />
                  <YAxis className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Area
                    type="monotone"
                    dataKey="revenue"
                    stroke="var(--color-revenue)"
                    fill="var(--color-revenue)"
                    fillOpacity={0.2}
                  />
                  <Area
                    type="monotone"
                    dataKey="fees"
                    stroke="var(--color-fees)"
                    fill="var(--color-fees)"
                    fillOpacity={0.2}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </ChartContainer>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
