"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { TrendingUp, Leaf, DollarSign, Clock, Upload, ShoppingCart } from "lucide-react"
import { mockJourneys, mockCredits } from "../../../apps/web-portal-next/lib/mock-data"
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "../ui/chart"

export function OwnerDashboardOverview() {
  const userId = "1"
  const userJourneys = mockJourneys.filter((j) => j.userId === userId)
  const userCredits = mockCredits.filter((c) => c.ownerId === userId)

  const totalCredits = userJourneys
    .filter((j) => j.status === "verified")
    .reduce((sum, j) => sum + j.creditsGenerated, 0)

  const totalEarnings = userCredits
    .filter((c) => c.status === "sold")
    .reduce((sum, c) => sum + c.amount * c.pricePerCredit, 0)

  const pendingJourneys = userJourneys.filter((j) => j.status === "pending").length

  const chartData = [
    { month: "Jan", credits: 45.2 },
    { month: "Feb", credits: 52.8 },
    { month: "Mar", credits: 38.5 },
    { month: "Apr", credits: 61.3 },
    { month: "May", credits: 48.9 },
    { month: "Jun", credits: 55.7 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back! Here's your carbon credit overview.</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Credits Generated</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCredits.toFixed(1)} tCO₂</div>
            <p className="text-xs text-muted-foreground">From verified journeys</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Earnings</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalEarnings.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">From sold credits</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending Verification</CardTitle>
            <Clock className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingJourneys}</div>
            <p className="text-xs text-muted-foreground">Journeys awaiting review</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Price</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">$25.00</div>
            <p className="text-xs text-muted-foreground">Per credit (tCO₂)</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Credits Generated Over Time</CardTitle>
          <CardDescription>Monthly carbon credit generation from your EV journeys</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer
            config={{
              credits: {
                label: "Credits (tCO₂)",
                color: "hsl(var(--chart-1))",
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
                <Bar dataKey="credits" fill="var(--color-charts-1)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Journeys</CardTitle>
            <CardDescription>Your latest EV trips</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {userJourneys.slice(0, 3).map((journey) => (
                <div key={journey.id} className="flex items-center justify-between border-b pb-3 last:border-0">
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {journey.startLocation} → {journey.endLocation}
                    </p>
                    <p className="text-xs text-muted-foreground">{journey.date}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium">{journey.creditsGenerated.toFixed(1)} tCO₂</p>
                    <p
                      className={`text-xs ${
                        journey.status === "verified"
                          ? "text-emerald-600"
                          : journey.status === "pending"
                            ? "text-amber-600"
                            : "text-red-600"
                      }`}
                    >
                      {journey.status}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common tasks</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <a
              href="/ev-owner/upload"
              className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
            >
              <div className="flex items-center gap-3">
                <Upload className="h-5 w-5 text-emerald-600" />
                <span className="font-medium">Upload New Journey</span>
              </div>
            </a>
            <a
              href="/ev-owner/credits"
              className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
            >
              <div className="flex items-center gap-3">
                <ShoppingCart className="h-5 w-5 text-emerald-600" />
                <span className="font-medium">List Credits for Sale</span>
              </div>
            </a>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
