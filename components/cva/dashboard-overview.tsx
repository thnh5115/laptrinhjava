"use client"

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
} from "lucide-react"
import { mockJourneys } from "@/lib/mock-data"
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"

export function CvaDashboardOverview() {
  const pendingJourneys = mockJourneys.filter((j) => j.status === "pending")
  const verifiedJourneys = mockJourneys.filter((j) => j.status === "verified")
  const rejectedJourneys = mockJourneys.filter((j) => j.status === "rejected")

  const totalReviewed = verifiedJourneys.length + rejectedJourneys.length
  const approvalRate = totalReviewed > 0 ? (verifiedJourneys.length / totalReviewed) * 100 : 0

  const chartData = [
    { month: "Jan", verified: 45, rejected: 5 },
    { month: "Feb", verified: 52, rejected: 8 },
    { month: "Mar", verified: 48, rejected: 4 },
    { month: "Apr", verified: 61, rejected: 7 },
    { month: "May", verified: 55, rejected: 6 },
    { month: "Jun", verified: 58, rejected: 5 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">CVA Dashboard</h1>
        <p className="text-muted-foreground">Carbon Verification Authority - Review and verify journey submissions</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending Reviews</CardTitle>
            <Clock className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingJourneys.length}</div>
            <p className="text-xs text-muted-foreground">Awaiting verification</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Verified</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{verifiedJourneys.length}</div>
            <p className="text-xs text-muted-foreground">Approved journeys</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Rejected</CardTitle>
            <XCircle className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{rejectedJourneys.length}</div>
            <p className="text-xs text-muted-foreground">Declined submissions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Approval Rate</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{approvalRate.toFixed(1)}%</div>
            <p className="text-xs text-muted-foreground">Overall accuracy</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Verification Activity</CardTitle>
          <CardDescription>Monthly verification statistics</CardDescription>
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
                color: "hsl(142, 76%, 36%)",
              },
            }}
            className="h-[300px]"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="month" className="text-xs" />
                <YAxis className="text-xs" />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Bar dataKey="verified" fill="var(--color-chart-1)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="rejected" fill="#ef4444" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Urgent Reviews</CardTitle>
            <CardDescription>Submissions requiring immediate attention</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {pendingJourneys.slice(0, 3).map((journey) => (
                <div key={journey.id} className="flex items-center justify-between border-b pb-3 last:border-0">
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {journey.startLocation} → {journey.endLocation}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {journey.date} • {journey.distance} miles
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge
                      variant="secondary"
                      className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                    >
                      <AlertCircle className="mr-1 h-3 w-3" />
                      Pending
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common verification tasks</CardDescription>
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
