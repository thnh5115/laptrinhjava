"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { ArrowRight } from "lucide-react"

export function ConversionFunnel() {
  const funnelStages = [
    { stage: "Visitors", count: 1250, percentage: 100 },
    { stage: "Sign Ups", count: 425, percentage: 34 },
    { stage: "Profile Complete", count: 312, percentage: 25 },
    { stage: "First Journey/Purchase", count: 198, percentage: 16 },
    { stage: "Active Users", count: 145, percentage: 12 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Conversion Funnel</h2>
        <p className="text-muted-foreground">User journey from visitor to active user</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>User Conversion Flow</CardTitle>
          <CardDescription>Track how users progress through the platform</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {funnelStages.map((stage, index) => (
              <div key={stage.stage}>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-3">
                    <div className="flex items-center justify-center w-8 h-8 rounded-full bg-emerald-100 dark:bg-emerald-900 text-emerald-900 dark:text-emerald-100 font-bold text-sm">
                      {index + 1}
                    </div>
                    <span className="font-medium">{stage.stage}</span>
                  </div>
                  <div className="text-right">
                    <p className="font-bold">{stage.count}</p>
                    <p className="text-xs text-muted-foreground">{stage.percentage}%</p>
                  </div>
                </div>
                <div className="w-full bg-muted rounded-full h-3">
                  <div
                    className="bg-emerald-600 h-3 rounded-full transition-all"
                    style={{ width: `${stage.percentage}%` }}
                  />
                </div>
                {index < funnelStages.length - 1 && (
                  <div className="flex justify-center my-2">
                    <ArrowRight className="h-4 w-4 text-muted-foreground" />
                  </div>
                )}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Overall Conversion</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">11.6%</div>
            <p className="text-xs text-muted-foreground">Visitor to active user</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Sign Up Rate</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">34%</div>
            <p className="text-xs text-muted-foreground">Visitors who register</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Activation Rate</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">46.6%</div>
            <p className="text-xs text-muted-foreground">Sign ups who complete first action</p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
