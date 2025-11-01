"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, Clock, CheckCircle2, XCircle, BarChart3 } from "lucide-react"
import { mockJourneys } from "@/lib/mock-data"
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

export function AnalyticsDashboard() {
  const verifiedJourneys = mockJourneys.filter((j) => j.status === "verified")
  const rejectedJourneys = mockJourneys.filter((j) => j.status === "rejected")
  const pendingJourneys = mockJourneys.filter((j) => j.status === "pending")

  const totalReviewed = verifiedJourneys.length + rejectedJourneys.length
  const approvalRate = totalReviewed > 0 ? (verifiedJourneys.length / totalReviewed) * 100 : 0
  const avgReviewTime = 2.3 // hours

  const totalCreditsVerified = verifiedJourneys.reduce((sum, j) => sum + j.creditsGenerated, 0)

  const lineData = [
    { week: "Week 1", reviews: 12 },
    { week: "Week 2", reviews: 18 },
    { week: "Week 3", reviews: 15 },
    { week: "Week 4", reviews: 22 },
  ]

  const pieData = [
    { name: "Verified", value: verifiedJourneys.length, color: "#10b981" },
    { name: "Rejected", value: rejectedJourneys.length, color: "#ef4444" },
    { name: "Pending", value: pendingJourneys.length, color: "#f59e0b" },
  ]

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Reviews</CardTitle>
            <BarChart3 className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalReviewed}</div>
            <p className="text-xs text-muted-foreground">Completed verifications</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Approval Rate</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{approvalRate.toFixed(1)}%</div>
            <p className="text-xs text-muted-foreground">Journey approval rate</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Review Time</CardTitle>
            <Clock className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{avgReviewTime}h</div>
            <p className="text-xs text-muted-foreground">Per submission</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Credits Verified</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCreditsVerified.toFixed(1)}</div>
            <p className="text-xs text-muted-foreground">tCO2 approved</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Review Activity</CardTitle>
            <CardDescription>Weekly verification trends</CardDescription>
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
                <LineChart data={lineData}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="week" className="text-xs" />
                  <YAxis className="text-xs" />
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
            <CardDescription>Distribution of journey statuses</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                verified: {
                  label: "Verified",
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
                  <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                    {pieData.map((entry, index) => (
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
          <CardDescription>Key verification statistics</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <div className="flex items-center gap-4 p-4 border rounded-lg">
              <CheckCircle2 className="h-8 w-8 text-emerald-600" />
              <div>
                <p className="text-sm text-muted-foreground">Verified</p>
                <p className="text-2xl font-bold">{verifiedJourneys.length}</p>
              </div>
            </div>
            <div className="flex items-center gap-4 p-4 border rounded-lg">
              <XCircle className="h-8 w-8 text-red-600" />
              <div>
                <p className="text-sm text-muted-foreground">Rejected</p>
                <p className="text-2xl font-bold">{rejectedJourneys.length}</p>
              </div>
            </div>
            <div className="flex items-center gap-4 p-4 border rounded-lg">
              <Clock className="h-8 w-8 text-amber-600" />
              <div>
                <p className="text-sm text-muted-foreground">Pending</p>
                <p className="text-2xl font-bold">{pendingJourneys.length}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
