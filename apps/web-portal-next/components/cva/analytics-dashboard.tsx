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
import { listVerificationRequests, type VerificationRequest } from "@/lib/api/cva"

interface AnalyticsState {
  isLoading: boolean
  error: string | null
  items: VerificationRequest[]
}

export function AnalyticsDashboard() {
  const [{ isLoading, error, items }, setState] = useState<AnalyticsState>({
    isLoading: true,
    error: null,
    items: [],
  })

  useEffect(() => {
    let cancelled = false
    setState((prev) => ({ ...prev, isLoading: true, error: null }))

    listVerificationRequests({ size: 500 })
      .then((page) => {
        if (!cancelled) {
          setState({ isLoading: false, error: null, items: page.content })
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setState({ isLoading: false, error: err.message ?? "Unable to load analytics", items: [] })
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  const analytics = useMemo(() => deriveAnalytics(items), [items])

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
          value={analytics.totalReviewed}
          subtitle="Completed verifications"
        />
        <MetricCard
          title="Approval Rate"
          icon={<TrendingUp className="h-4 w-4 text-emerald-600" />}
          value={`${analytics.approvalRate.toFixed(1)}%`}
          subtitle="Completed decisions"
        />
        <MetricCard
          title="Avg Review Time"
          icon={<Clock className="h-4 w-4 text-emerald-600" />}
          value={`${analytics.avgReviewTime.toFixed(1)}h`}
          subtitle="Submission to decision"
        />
        <MetricCard
          title="Credits Issued"
          icon={<CheckCircle2 className="h-4 w-4 text-emerald-600" />}
          value={analytics.totalCredits.toFixed(2)}
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
                    {analytics.statusBreakdown.map((entry, index) => (
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

function deriveAnalytics(requests: VerificationRequest[]): AnalyticsSummary {
  const approved = requests.filter((req) => req.status === "APPROVED")
  const rejected = requests.filter((req) => req.status === "REJECTED")
  const pending = requests.filter((req) => req.status === "PENDING")

  const totalReviewed = approved.length + rejected.length
  const approvalRate = totalReviewed > 0 ? (approved.length / totalReviewed) * 100 : 0

  const reviewDurations: number[] = []
  for (const request of [...approved, ...rejected]) {
    if (!request.verifiedAt) continue
    const created = new Date(request.createdAt)
    const verified = new Date(request.verifiedAt)
    if (Number.isNaN(created.getTime()) || Number.isNaN(verified.getTime())) continue
    const diffHours = (verified.getTime() - created.getTime()) / (1000 * 60 * 60)
    if (diffHours >= 0) {
      reviewDurations.push(diffHours)
    }
  }
  const avgReviewTime = reviewDurations.length
    ? reviewDurations.reduce((sum, value) => sum + value, 0) / reviewDurations.length
    : 0

  const totalCredits = approved.reduce((sum, request) => sum + (request.creditIssuance?.creditsRounded ?? 0), 0)

  const weeklyMap = new Map<string, { week: string; reviews: number; order: number }>()
  for (const request of requests) {
    const created = new Date(request.createdAt)
    if (Number.isNaN(created.getTime())) continue
    const { key, label, order } = getWeekBucket(created)
    const existing = weeklyMap.get(key)
    if (existing) {
      existing.reviews += 1
    } else {
      weeklyMap.set(key, { week: label, reviews: 1, order })
    }
  }

  const weeklyActivity = Array.from(weeklyMap.values())
    .sort((a, b) => a.order - b.order)
    .slice(-6)
    .map(({ week, reviews }) => ({ week, reviews }))

  const statusBreakdown = [
    { name: "Approved", value: approved.length, color: "#10b981" },
    { name: "Rejected", value: rejected.length, color: "#ef4444" },
    { name: "Pending", value: pending.length, color: "#f59e0b" },
  ]

  return {
    approvedCount: approved.length,
    rejectedCount: rejected.length,
    pendingCount: pending.length,
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

function MetricCard({
  title,
  icon,
  value,
  subtitle,
}: {
  title: string
  icon: React.ReactNode
  value: string | number
  subtitle: string
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        <p className="text-xs text-muted-foreground">{subtitle}</p>
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
