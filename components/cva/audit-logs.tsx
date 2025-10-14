"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { mockJourneys, mockUsers } from "@/lib/mock-data"
import { Download, CheckCircle2, XCircle, Clock } from "lucide-react"

export function AuditLogs() {
  const allJourneys = mockJourneys

  const getActionIcon = (status: string) => {
    switch (status) {
      case "verified":
        return <CheckCircle2 className="h-4 w-4 text-emerald-600" />
      case "rejected":
        return <XCircle className="h-4 w-4 text-red-600" />
      case "pending":
        return <Clock className="h-4 w-4 text-amber-600" />
      default:
        return null
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>System Activity Logs</CardTitle>
            <CardDescription>Complete audit trail of all verification activities</CardDescription>
          </div>
          <Button variant="outline" size="sm">
            <Download className="mr-2 h-4 w-4" />
            Export Logs
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {allJourneys.map((journey) => {
            const owner = mockUsers.find((u) => u.id === journey.userId)
            const verifier = mockUsers.find((u) => u.id === journey.verifiedBy)

            return (
              <div key={journey.id} className="flex items-start gap-4 border-b pb-3 last:border-0">
                <div className="mt-1">{getActionIcon(journey.status)}</div>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2">
                    <p className="text-sm font-medium">
                      Journey{" "}
                      {journey.status === "verified"
                        ? "Approved"
                        : journey.status === "rejected"
                          ? "Rejected"
                          : "Submitted"}
                    </p>
                    <Badge
                      variant={
                        journey.status === "verified"
                          ? "default"
                          : journey.status === "rejected"
                            ? "destructive"
                            : "secondary"
                      }
                      className={
                        journey.status === "verified"
                          ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                          : journey.status === "pending"
                            ? "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                            : ""
                      }
                    >
                      {journey.status}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {journey.startLocation} → {journey.endLocation} • {journey.creditsGenerated.toFixed(1)} tCO₂
                  </p>
                  <div className="flex gap-4 text-xs text-muted-foreground">
                    <span>Owner: {owner?.name || "Unknown"}</span>
                    {verifier && <span>Verifier: {verifier.name}</span>}
                    <span>Date: {journey.date}</span>
                    {journey.verifiedAt && <span>Reviewed: {new Date(journey.verifiedAt).toLocaleDateString()}</span>}
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
