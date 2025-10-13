"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { mockTransactions, mockUsers } from "@/lib/mock-data"
import { Leaf, TrendingUp, Award, Calendar } from "lucide-react"
import { Pie, PieChart, Cell, ResponsiveContainer, Legend } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"

export function PortfolioOverview() {
  const userId = "2"
  const userTransactions = mockTransactions.filter((t) => t.buyerId === userId)

  const totalCredits = userTransactions.reduce((sum, t) => sum + t.amount, 0)
  const totalSpent = userTransactions.reduce((sum, t) => sum + t.totalPrice, 0)

  const pieData = [
    { name: "Transportation", value: 45, color: "#10b981" },
    { name: "Energy", value: 30, color: "#14b8a6" },
    { name: "Industrial", value: 25, color: "#06b6d4" },
  ]

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Holdings</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCredits.toFixed(1)} tCO₂</div>
            <p className="text-xs text-muted-foreground">Carbon credits owned</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Portfolio Value</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalSpent.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Total investment</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Certificates</CardTitle>
            <Award className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{userTransactions.length}</div>
            <p className="text-xs text-muted-foreground">Verified purchases</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Portfolio Distribution</CardTitle>
            <CardDescription>Credits by category</CardDescription>
          </CardHeader>
          <CardContent>
            <ChartContainer
              config={{
                transportation: {
                  label: "Transportation",
                  color: "hsl(var(--chart-1))",
                },
                energy: {
                  label: "Energy",
                  color: "hsl(var(--chart-2))",
                },
                industrial: {
                  label: "Industrial",
                  color: "hsl(var(--chart-3))",
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

        <Card>
          <CardHeader>
            <CardTitle>Environmental Impact</CardTitle>
            <CardDescription>Your contribution to carbon neutrality</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-sm">CO₂ Offset</span>
                <span className="font-bold text-emerald-600">{totalCredits.toFixed(1)} tons</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm">Equivalent to</span>
                <span className="font-medium">{(totalCredits * 2.5).toFixed(0)} trees planted</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm">Or</span>
                <span className="font-medium">{(totalCredits * 112).toFixed(0)} miles not driven</span>
              </div>
            </div>
            <div className="pt-4 border-t">
              <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100 text-base px-4 py-2">
                <Award className="mr-2 h-4 w-4" />
                Carbon Neutral Certified
              </Badge>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Credit Holdings</CardTitle>
          <CardDescription>Detailed breakdown of your purchases</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {userTransactions.map((transaction) => {
              const seller = mockUsers.find((u) => u.id === transaction.sellerId)
              return (
                <div key={transaction.id} className="flex items-center justify-between border rounded-lg p-4">
                  <div className="space-y-1">
                    <p className="font-medium">{transaction.amount.toFixed(1)} tCO₂</p>
                    <p className="text-sm text-muted-foreground">From: {seller?.name || "Unknown Seller"}</p>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground">
                      <Calendar className="h-3 w-3" />
                      {new Date(transaction.timestamp).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold">${transaction.totalPrice.toFixed(2)}</p>
                    <p className="text-sm text-muted-foreground">${transaction.pricePerCredit}/tCO₂</p>
                    <Badge className="mt-1 bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                      Verified
                    </Badge>
                  </div>
                </div>
              )
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
