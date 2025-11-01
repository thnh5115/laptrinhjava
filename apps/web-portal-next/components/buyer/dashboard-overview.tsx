"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { TrendingUp, Leaf, DollarSign, ShoppingCart, Award } from "lucide-react"
import { mockTransactions, mockCredits } from "@/lib/mock-data"
import { Line, LineChart, CartesianGrid, XAxis, YAxis, ResponsiveContainer } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"
import { Button } from "@/components/ui/button"

export function BuyerDashboardOverview() {
  const userId = "2"
  const userTransactions = mockTransactions.filter((t) => t.buyerId === userId)

  const totalCredits = userTransactions.reduce((sum, t) => sum + t.amount, 0)
  const totalSpent = userTransactions.reduce((sum, t) => sum + t.totalPrice, 0)
  const avgPrice = totalCredits > 0 ? totalSpent / totalCredits : 0

  const chartData = [
    { month: "Jan", credits: 12.5 },
    { month: "Feb", credits: 30.8 },
    { month: "Mar", credits: 45.2 },
    { month: "Apr", credits: 52.7 },
    { month: "May", credits: 68.3 },
    { month: "Jun", credits: 80.8 },
  ]

  const availableListings = mockCredits.filter((c) => c.status === "available")

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back! Here's your carbon credit portfolio overview.</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Credits Owned</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCredits.toFixed(1)} tCO2</div>
            <p className="text-xs text-muted-foreground">Carbon offset achieved</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Investment</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalSpent.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Lifetime spending</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Price</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${avgPrice.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Per tCO2</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Impact Score</CardTitle>
            <Award className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">A+</div>
            <p className="text-xs text-muted-foreground">Environmental rating</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Portfolio Growth</CardTitle>
          <CardDescription>Your carbon credit accumulation over time</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer
            config={{
              credits: {
                label: "Credits (tCO2)",
                color: "hsl(var(--chart-1))",
              },
            }}
            className="h-[300px]"
          >
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="month" className="text-xs" />
                <YAxis className="text-xs" />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Line
                  type="monotone"
                  dataKey="credits"
                  stroke="var(--color-chart-1)"
                  strokeWidth={2}
                  dot={{ fill: "var(--color-chart-1)" }}
                />
              </LineChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Purchases</CardTitle>
            <CardDescription>Your latest transactions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {userTransactions.slice(0, 3).map((transaction) => (
                <div key={transaction.id} className="flex items-center justify-between border-b pb-3 last:border-0">
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">{transaction.amount.toFixed(1)} tCO2</p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(transaction.timestamp).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium">${transaction.totalPrice.toFixed(2)}</p>
                    <p className="text-xs text-emerald-600">Completed</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Marketplace Highlights</CardTitle>
            <CardDescription>Available credits</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium">Available Listings</p>
                <p className="text-2xl font-bold">{availableListings.length}</p>
              </div>
              <ShoppingCart className="h-8 w-8 text-emerald-600" />
            </div>
            <Button className="w-full bg-emerald-600 hover:bg-emerald-700" asChild>
              <a href="/buyer/marketplace">Browse Marketplace</a>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
