"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { MapPin } from "lucide-react"

export function GeographicAnalytics() {
  const regions = [
    { name: "California", users: 45, transactions: 128, credits: 342.5 },
    { name: "New York", users: 32, transactions: 89, credits: 245.8 },
    { name: "Texas", users: 28, transactions: 76, credits: 198.3 },
    { name: "Florida", users: 21, transactions: 54, credits: 156.2 },
    { name: "Washington", users: 18, transactions: 48, credits: 132.7 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Geographic Distribution</h2>
        <p className="text-muted-foreground">User activity and transactions by region</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Top Regions</CardTitle>
          <CardDescription>Most active states by user count and transaction volume</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {regions.map((region, index) => (
              <div key={region.name} className="flex items-center gap-4">
                <div className="flex items-center justify-center w-8 h-8 rounded-full bg-emerald-100 dark:bg-emerald-900 text-emerald-900 dark:text-emerald-100 font-bold text-sm">
                  {index + 1}
                </div>
                <MapPin className="h-4 w-4 text-muted-foreground" />
                <div className="flex-1">
                  <p className="font-medium">{region.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {region.users} users • {region.transactions} transactions • {region.credits} tCO₂
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium">{region.credits} tCO₂</p>
                  <p className="text-xs text-muted-foreground">{region.transactions} txns</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Most Active Region</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">California</div>
            <p className="text-xs text-muted-foreground">45 users, 128 transactions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Fastest Growing</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">Texas</div>
            <p className="text-xs text-muted-foreground">+35% user growth this month</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Total Regions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12 States</div>
            <p className="text-xs text-muted-foreground">Active marketplace presence</p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
