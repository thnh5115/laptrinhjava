"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Users, Leaf, DollarSign, TrendingUp, Activity, AlertCircle } from "lucide-react"
import { mockUsers, mockJourneys, mockTransactions } from "../../../apps/web-portal-next/lib/mock-data"
import { Line, LineChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer, Bar, BarChart } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "../ui/chart"
import { Badge } from "../ui/badge"

export function AdminDashboardOverview() {
  const totalUsers = mockUsers.length
  const totalCredits = mockJourneys
    .filter((j) => j.status === "verified")
    .reduce((sum, j) => sum + j.creditsGenerated, 0)
  const totalTransactions = mockTransactions.length
  const totalRevenue = mockTransactions.reduce((sum, t) => sum + t.totalPrice, 0)

  const usersByRole = {
    "ev-owner": mockUsers.filter((u) => u.role === "ev-owner").length,
    buyer: mockUsers.filter((u) => u.role === "buyer").length,
    cva: mockUsers.filter((u) => u.role === "cva").length,
    admin: mockUsers.filter((u) => u.role === "admin").length,
  }

  const lineData = [
    { month: "Jan", users: 45, transactions: 32 },
    { month: "Feb", users: 52, transactions: 41 },
    { month: "Mar", users: 61, transactions: 38 },
    { month: "Apr", users: 73, transactions: 52 },
    { month: "May", users: 85, transactions: 61 },
    { month: "Jun", users: 98, transactions: 73 },
  ]

  const barData = [
    { category: "EV Owners", count: usersByRole["ev-owner"] },
    { category: "Buyers", count: usersByRole.buyer },
    { category: "CVAs", count: usersByRole.cva },
    { category: "Admins", count: usersByRole.admin },
  ]

  const pendingReviews = mockJourneys.filter((j) => j.status === "pending").length

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Admin Dashboard</h1>
        <p className="text-muted-foreground">Platform overview and system management</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Users</CardTitle>
            <Users className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalUsers}</div>
            <p className="text-xs text-muted-foreground">Registered accounts</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Credits</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCredits.toFixed(1)} tCO₂</div>
            <p className="text-xs text-muted-foreground">Verified credits</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Platform Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalRevenue.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Total transactions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Transactions</CardTitle>
            <Activity className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalTransactions}</div>
            <p className="text-xs text-muted-foreground">Completed trades</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Platform Growth</CardTitle>
            <CardDescription>User registrations and transaction volume</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                users: {
                  label: "Users",
                  color: "hsl(var(--chart-1))",
                },
                transactions: {
                  label: "Transactions",
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
                    dataKey="users"
                    stroke="var(--color-chart-1)"
                    strokeWidth={2}
                    dot={{ fill: "var(--color-chart-1)" }}
                  />
                  <Line
                    type="monotone"
                    dataKey="transactions"
                    stroke="var(--color-chart-2)"
                    strokeWidth={2}
                    dot={{ fill: "var(--color-chart-2)" }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>User Distribution</CardTitle>
            <CardDescription>Users by role type</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                count: {
                  label: "Users",
                  color: "hsl(var(--chart-1))",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis dataKey="category" className="text-xs" />
                  <YAxis className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Bar dataKey="count" fill="var(--color-chart-1)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </ChartContainer>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>System Alerts</CardTitle>
            <CardDescription>Important notifications requiring attention</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {pendingReviews > 0 && (
                <div className="flex items-start gap-3 p-3 border rounded-lg bg-amber-50 dark:bg-amber-950 border-amber-200 dark:border-amber-800">
                  <AlertCircle className="h-5 w-5 text-amber-600 mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm font-medium">Pending Verifications</p>
                    <p className="text-xs text-muted-foreground">
                      {pendingReviews} journey submissions awaiting CVA review
                    </p>
                  </div>
                  <Badge
                    variant="secondary"
                    className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                  >
                    {pendingReviews}
                  </Badge>
                </div>
              )}
              <div className="flex items-start gap-3 p-3 border rounded-lg">
                <TrendingUp className="h-5 w-5 text-emerald-600 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium">Platform Growth</p>
                  <p className="text-xs text-muted-foreground">User registrations up 23% this month</p>
                </div>
                <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                  +23%
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Latest platform events</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b pb-3">
                <div className="space-y-1">
                  <p className="text-sm font-medium">New user registration</p>
                  <p className="text-xs text-muted-foreground">EV Owner account created</p>
                </div>
                <p className="text-xs text-muted-foreground">2h ago</p>
              </div>
              <div className="flex items-center justify-between border-b pb-3">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Credit transaction</p>
                  <p className="text-xs text-muted-foreground">18.0 tCO₂ sold for $432</p>
                </div>
                <p className="text-xs text-muted-foreground">5h ago</p>
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Journey verified</p>
                  <p className="text-xs text-muted-foreground">CVA approved 15.2 tCO₂</p>
                </div>
                <p className="text-xs text-muted-foreground">8h ago</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
