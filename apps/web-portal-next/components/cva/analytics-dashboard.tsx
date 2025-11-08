"use client"

import type React from "react"
import { useEffect, useMemo, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, Clock, CheckCircle2, XCircle, BarChart3, AlertTriangle, Loader2 } from "lucide-react"
import {
  Line,
  LineChart,
  CartesianGrid,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Pie,
  PieChart,
  Cell,
  Legend,
} from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import {
  getAnalyticsOverview,
  listVerificationRequests,
  type AnalyticsOverviewResponse,
  type VerificationRequest,
} from "@/lib/api/cva"
import { Skeleton } from "@/components/ui/skeleton"

interface AnalyticsState {
  isLoading: boolean
  error: string | null
  overview: AnalyticsOverviewResponse | null
  items: VerificationRequest[]
}

export function AnalyticsDashboard() {
  const [{ isLoading, error, overview, items }, setState] = useState<AnalyticsState>({
    isLoading: true,
    error: null,
    overview: null,
    items: [],
  })

  useEffect(() => {
    let cancelled = false
    setState((prev: AnalyticsState) => ({ ...prev, isLoading: true, error: null }))

    Promise.all([getAnalyticsOverview(30), listVerificationRequests({ size: 500 })])
      .then(([overviewResponse, page]) => {
        if (!cancelled) {
          setState({ isLoading: false, error: null, overview: overviewResponse, items: page.content })
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setState({ isLoading: false, error: err.message ?? "Unable to load analytics", overview: null, items: [] })
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  const analytics = useMemo(() => deriveAnalytics(overview, items), [overview, items])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-3">
          <h1 className="text-3xl font-bold tracking-tight">Verification Analytics</h1>
          {isLoading && <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />}
        </div>
        <p className="text-muted-foreground">Insights generated from live verification activity</p>
        {error && (
          <p className="flex items-center gap-2 text-sm text-destructive">
            <AlertTriangle className="h-4 w-4" /> {error}
          </p>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          title="Total Reviews"
          icon={<BarChart3 className="h-4 w-4 text-emerald-600" />}
          value={isLoading ? undefined : analytics.totalReviewed}
          subtitle="Completed verifications"
        />
        <MetricCard
          title="Approval Rate"
          icon={<TrendingUp className="h-4 w-4 text-emerald-600" />}
          value={isLoading ? undefined : `${analytics.approvalRate.toFixed(1)}%`}
          subtitle="Completed decisions"
        />
        <MetricCard
          title="Avg Review Time"
          icon={<Clock className="h-4 w-4 text-emerald-600" />}
          value={isLoading ? undefined : `${analytics.avgReviewTime.toFixed(1)}h`}
          subtitle="Submission to decision"
        />
        <MetricCard
          title="Credits Issued"
          icon={<CheckCircle2 className="h-4 w-4 text-emerald-600" />}
          value={isLoading ? undefined : analytics.totalCredits.toFixed(2)}
          subtitle="tCO₂ approved"
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Review Activity</CardTitle>
            <CardDescription>Requests grouped by week</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                reviews: {
                  label: "Reviews",
                  color: "hsl(var(--chart-1))",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={analytics.weeklyActivity.length ? analytics.weeklyActivity : fallbackWeekly()}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="week" className="text-xs" />
                  <YAxis allowDecimals={false} className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Line
                    type="monotone"
                    dataKey="reviews"
                    stroke="var(--color-chart-1)"
                    strokeWidth={2}
                    dot={{ fill: "var(--color-chart-1)" }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Verification Status</CardTitle>
            <CardDescription>Distribution by decision</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                approved: {
                  label: "Approved",
                  color: "hsl(var(--chart-1))",
                },
                rejected: {
                  label: "Rejected",
                  color: "hsl(0, 84%, 60%)",
                },
                pending: {
                  label: "Pending",
                  color: "hsl(38, 92%, 50%)",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={analytics.statusBreakdown} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {analytics.statusBreakdown.map((entry: AnalyticsSummary["statusBreakdown"][number], index: number) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Performance Metrics</CardTitle>
          <CardDescription>Snapshot of current workload</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <MetricBlock icon={<CheckCircle2 className="h-8 w-8 text-emerald-600" />} label="Approved" value={analytics.approvedCount} />
            <MetricBlock icon={<XCircle className="h-8 w-8 text-red-600" />} label="Rejected" value={analytics.rejectedCount} />
            <MetricBlock icon={<Clock className="h-8 w-8 text-amber-600" />} label="Pending" value={analytics.pendingCount} />
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

interface AnalyticsSummary {
  approvedCount: number
  rejectedCount: number
  pendingCount: number
  totalReviewed: number
  approvalRate: number
  avgReviewTime: number
  totalCredits: number
  weeklyActivity: { week: string; reviews: number }[]
  statusBreakdown: { name: string; value: number; color: string }[]
}

function deriveAnalytics(
  overview: AnalyticsOverviewResponse | null,
  requests: VerificationRequest[]
): AnalyticsSummary {
  const approvedRequests = requests.filter((req) => req.status === "APPROVED")
  const rejectedRequests = requests.filter((req) => req.status === "REJECTED")
  const pendingRequests = requests.filter((req) => req.status === "PENDING")

  const approvedCount = overview?.approvedRequests ?? approvedRequests.length
  const rejectedCount = overview?.rejectedRequests ?? rejectedRequests.length
  const pendingCount = overview?.pendingRequests ?? pendingRequests.length
  const totalReviewed = overview ? overview.approvedRequests + overview.rejectedRequests : approvedCount + rejectedCount
  const approvalRate = overview?.approvalRate ?? (totalReviewed > 0 ? (approvedCount / totalReviewed) * 100 : 0)
  const totalCredits = overview ? Number(overview.totalCreditsIssued ?? 0) : totalCreditsFromRequests(approvedRequests)
  const avgReviewTime = computeAverageReviewTime([...approvedRequests, ...rejectedRequests])
  const weeklyActivity = overview?.recentTrend?.length
    ? groupTrendByWeek(overview.recentTrend)
    : groupRequestsByWeek(requests)
  const statusBreakdown = buildStatusBreakdown(approvedCount, rejectedCount, pendingCount)

  return {
    approvedCount,
    rejectedCount,
    pendingCount,
    totalReviewed,
    approvalRate,
    avgReviewTime,
    totalCredits,
    weeklyActivity,
    statusBreakdown,
  }
}

function getWeekBucket(date: Date) {
  const utc = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
  const day = utc.getUTCDay()
  const diff = (day + 6) % 7
  utc.setUTCDate(utc.getUTCDate() - diff)
  const key = utc.toISOString().slice(0, 10)
  const label = `Week of ${utc.toLocaleDateString(undefined, { month: "short", day: "numeric" })}`
  return { key, label, order: utc.getTime() }
}

function fallbackWeekly() {
  return [
    { week: "Week 1", reviews: 0 },
    { week: "Week 2", reviews: 0 },
    { week: "Week 3", reviews: 0 },
    { week: "Week 4", reviews: 0 },
  ]
}

function groupTrendByWeek(trend: AnalyticsOverviewResponse["recentTrend"]): { week: string; reviews: number }[] {
  const weeklyMap = new Map<string, { label: string; reviews: number; order: number }>()
  for (const entry of trend) {
    const date = new Date(entry.date)
    if (Number.isNaN(date.getTime())) continue
    const { key, label, order } = getWeekBucket(date)
    const reviews = entry.approvals + entry.rejections
    const existing = weeklyMap.get(key)
    if (existing) {
      existing.reviews += reviews
    } else {
      weeklyMap.set(key, { label, reviews, order })
    }
  }
  return Array.from(weeklyMap.values())
    .sort((a, b) => a.order - b.order)
    .slice(-6)
    .map(({ label, reviews }) => ({ week: label, reviews }))
}

function groupRequestsByWeek(requests: VerificationRequest[]): { week: string; reviews: number }[] {
  const weeklyMap = new Map<string, { label: string; reviews: number; order: number }>()
  for (const request of requests) {
    const created = new Date(request.createdAt)
    if (Number.isNaN(created.getTime())) continue
    const { key, label, order } = getWeekBucket(created)
    const existing = weeklyMap.get(key)
    if (existing) {
      existing.reviews += 1
    } else {
      weeklyMap.set(key, { label, reviews: 1, order })
    }
  }
  return Array.from(weeklyMap.values())
    .sort((a, b) => a.order - b.order)
    .slice(-6)
    .map(({ label, reviews }) => ({ week: label, reviews }))
}

function totalCreditsFromRequests(requests: VerificationRequest[]) {
  return requests.reduce((sum, request) => sum + (request.creditIssuance?.creditsRounded ?? 0), 0)
}

function computeAverageReviewTime(requests: VerificationRequest[]) {
  const durations: number[] = []
  for (const request of requests) {
    if (!request.verifiedAt) continue
    const created = new Date(request.createdAt)
    const verified = new Date(request.verifiedAt)
    if (Number.isNaN(created.getTime()) || Number.isNaN(verified.getTime())) continue
    const diffHours = (verified.getTime() - created.getTime()) / (1000 * 60 * 60)
    if (diffHours >= 0) {
      durations.push(diffHours)
    }
  }
  return durations.length ? durations.reduce((sum, value) => sum + value, 0) / durations.length : 0
}

function buildStatusBreakdown(approved: number, rejected: number, pending: number) {
  return [
    { name: "Approved", value: approved, color: "#10b981" },
    { name: "Rejected", value: rejected, color: "#ef4444" },
    { name: "Pending", value: pending, color: "#f59e0b" },
  ]
}

function MetricCard({
  title,
  icon,
  value,
  subtitle,
}: {
  title: string
  icon: React.ReactNode
  value?: string | number
  subtitle: string
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon}
      </CardHeader>
      <CardContent>
        {typeof value === "undefined" ? (
          <div className="space-y-1">
            <Skeleton className="h-6 w-24" />
            <p className="text-xs text-muted-foreground">{subtitle}</p>
          </div>
        ) : (
          <>
            <div className="text-2xl font-bold">{value}</div>
            <p className="text-xs text-muted-foreground">{subtitle}</p>
          </>
        )}
      </CardContent>
    </Card>
  )
}

function MetricBlock({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode
  label: string
  value: number
}) {
  return (
    <div className="flex items-center gap-4 rounded-lg border p-4">
      {icon}
      <div>
        <p className="text-sm text-muted-foreground">{label}</p>
        <p className="text-2xl font-bold">{value}</p>
      </div>
    </div>
  )
}
