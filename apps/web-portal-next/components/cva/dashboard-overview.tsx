"use client"

import type React from "react"
import { useEffect, useMemo, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Clock,
  CheckCircle2,
  XCircle,
  TrendingUp,
  AlertCircle,
  ClipboardCheck,
  BarChart3,
  FileSearch,
  RefreshCw,
  AlertTriangle,
} from "lucide-react"
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { listVerificationRequests, type VerificationRequest } from "@/lib/api/cva"

interface DashboardState {
  isLoading: boolean
  error: string | null
  items: VerificationRequest[]
}

export function CvaDashboardOverview() {
  const [{ isLoading, error, items }, setState] = useState<DashboardState>({
    isLoading: true,
    error: null,
    items: [],
  })

  useEffect(() => {
    let cancelled = false
    setState((prev) => ({ ...prev, isLoading: true, error: null }))

    listVerificationRequests({ size: 200 })
      .then((page) => {
        if (!cancelled) {
          setState({ isLoading: false, error: null, items: page.content })
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setState({ isLoading: false, error: err.message ?? "Failed to load requests", items: [] })
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  const { pending, approved, rejected, approvalRate, monthly } = useMemo(() => deriveStats(items), [items])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-3">
          <h1 className="text-3xl font-bold tracking-tight">CVA Dashboard</h1>
          {isLoading && <RefreshCw className="h-4 w-4 animate-spin text-muted-foreground" />}
        </div>
        <p className="text-muted-foreground">
          Carbon Verification Authority – monitor submissions and keep the ledger healthy
        </p>
        {error && (
          <p className="flex items-center gap-2 text-sm text-destructive">
            <AlertTriangle className="h-4 w-4" /> {error}
          </p>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          title="Pending Reviews"
          icon={<Clock className="h-4 w-4 text-amber-600" />}
          value={pending.length}
          subtitle="Awaiting verification"
        />
        <MetricCard
          title="Approved"
          icon={<CheckCircle2 className="h-4 w-4 text-emerald-600" />}
          value={approved.length}
          subtitle="Issued credits"
        />
        <MetricCard
          title="Rejected"
          icon={<XCircle className="h-4 w-4 text-red-600" />}
          value={rejected.length}
          subtitle="Returned to owners"
        />
        <MetricCard
          title="Approval Rate"
          icon={<TrendingUp className="h-4 w-4 text-emerald-600" />}
          value={`${approvalRate.toFixed(1)}%`}
          subtitle="For completed decisions"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Verification Activity</CardTitle>
          <CardDescription>Monthly view of approved vs. rejected requests</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer
            config={{
              approved: { label: "Approved", color: "hsl(var(--chart-1))" },
              rejected: { label: "Rejected", color: "hsl(0, 84%, 60%)" },
            }}
            className="h-[300px]"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthly}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="month" className="text-xs" />
                <YAxis allowDecimals={false} className="text-xs" />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Bar dataKey="approved" fill="var(--color-chart-1)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="rejected" fill="#f87171" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Urgent Reviews</CardTitle>
            <CardDescription>Newest submissions waiting for action</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {pending.length === 0 && !isLoading ? (
                <p className="text-sm text-muted-foreground text-center py-8">All caught up for now.</p>
              ) : (
                pending.slice(0, 3).map((request) => (
                  <div key={request.id} className="flex items-center justify-between border-b pb-3 last:border-0">
                    <div className="space-y-1">
                      <p className="text-sm font-medium leading-none">Trip #{request.tripId}</p>
                      <p className="text-xs text-muted-foreground">
                        {formatDate(request.createdAt)} • {request.distanceKm.toFixed(2)} km • {request.energyKwh.toFixed(2)} kWh
                      </p>
                    </div>
                    <Badge
                      variant="secondary"
                      className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                    >
                      <AlertCircle className="mr-1 h-3 w-3" />
                      Pending
                    </Badge>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Jump directly to the work queues</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <Button className="w-full justify-start bg-emerald-600 hover:bg-emerald-700" asChild>
              <a href="/cva/reviews">
                <ClipboardCheck className="mr-2 h-4 w-4" />
                Review Pending Submissions
              </a>
            </Button>
            <Button variant="outline" className="w-full justify-start bg-transparent" asChild>
              <a href="/cva/analytics">
                <BarChart3 className="mr-2 h-4 w-4" />
                View Analytics Report
              </a>
            </Button>
            <Button variant="outline" className="w-full justify-start bg-transparent" asChild>
              <a href="/cva/logs">
                <FileSearch className="mr-2 h-4 w-4" />
                Access Audit Logs
              </a>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

function deriveStats(requests: VerificationRequest[]) {
  const pending = requests.filter((req) => req.status === "PENDING").sort(sortByCreatedDesc)
  const approved = requests.filter((req) => req.status === "APPROVED")
  const rejected = requests.filter((req) => req.status === "REJECTED")

  const totalReviewed = approved.length + rejected.length
  const approvalRate = totalReviewed > 0 ? (approved.length / totalReviewed) * 100 : 0

  const monthlyMap = new Map<string, { approved: number; rejected: number; order: number; month: string }>()
  for (const request of requests) {
    const created = new Date(request.createdAt)
    if (Number.isNaN(created.getTime())) continue
    const monthLabel = created.toLocaleString("default", { month: "short", year: "numeric" })
    const key = `${created.getFullYear()}-${created.getMonth()}`
    const existing = monthlyMap.get(key)
    if (existing) {
      if (request.status === "APPROVED") {
        existing.approved += 1
      } else if (request.status === "REJECTED") {
        existing.rejected += 1
      }
    } else {
      monthlyMap.set(key, {
        month: monthLabel,
        approved: request.status === "APPROVED" ? 1 : 0,
        rejected: request.status === "REJECTED" ? 1 : 0,
        order: created.getFullYear() * 12 + created.getMonth(),
      })
    }
  }

  const monthly = Array.from(monthlyMap.entries())
    .sort((a, b) => a[1].order - b[1].order)
    .map(([_, value]) => ({ month: value.month, approved: value.approved, rejected: value.rejected }))

  return { pending, approved, rejected, approvalRate, monthly }
}

function sortByCreatedDesc(a: VerificationRequest, b: VerificationRequest) {
  return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
}

function formatDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

interface MetricCardProps {
  title: string
  icon: React.ReactNode
  value: number | string
  subtitle: string
}

function MetricCard({ title, icon, value, subtitle }: MetricCardProps) {
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
