"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { mockJourneys } from "@/lib/mock-data"
import { Eye, CheckCircle2, Clock, XCircle } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"

export function JourneyHistoryTable() {
  const userId = "1"
  const userJourneys = mockJourneys.filter((j) => j.userId === userId)

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "verified":
        return <CheckCircle2 className="h-4 w-4" />
      case "pending":
        return <Clock className="h-4 w-4" />
      case "rejected":
        return <XCircle className="h-4 w-4" />
      default:
        return null
    }
  }

  const getStatusVariant = (status: string): "default" | "secondary" | "destructive" => {
    switch (status) {
      case "verified":
        return "default"
      case "pending":
        return "secondary"
      case "rejected":
        return "destructive"
      default:
        return "secondary"
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>All Journeys</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {userJourneys.map((journey) => (
            <div key={journey.id} className="flex items-center justify-between border rounded-lg p-4">
              <div className="flex-1 space-y-1">
                <div className="flex items-center gap-2">
                  <p className="font-medium">
                    {journey.startLocation} → {journey.endLocation}
                  </p>
                  <Badge
                    variant={getStatusVariant(journey.status)}
                    className={
                      journey.status === "verified"
                        ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                        : journey.status === "pending"
                          ? "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                          : ""
                    }
                  >
                    {getStatusIcon(journey.status)}
                    <span className="ml-1 capitalize">{journey.status}</span>
                  </Badge>
                </div>
                <div className="flex gap-4 text-sm text-muted-foreground">
                  <span>{journey.date}</span>
                  <span>{journey.distance} miles</span>
                  <span>{journey.energyUsed} kWh</span>
                  <span className="font-medium text-emerald-600">{journey.creditsGenerated.toFixed(1)} tCO₂</span>
                </div>
              </div>

              <Dialog>
                <DialogTrigger asChild>
                  <Button variant="outline" size="sm">
                    <Eye className="h-4 w-4 mr-2" />
                    Details
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Journey Details</DialogTitle>
                    <DialogDescription>Complete information about this journey</DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Date</p>
                        <p className="text-sm">{journey.date}</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Status</p>
                        <Badge
                          variant={getStatusVariant(journey.status)}
                          className={
                            journey.status === "verified"
                              ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                              : journey.status === "pending"
                                ? "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                                : ""
                          }
                        >
                          {getStatusIcon(journey.status)}
                          <span className="ml-1 capitalize">{journey.status}</span>
                        </Badge>
                      </div>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Route</p>
                      <p className="text-sm">
                        {journey.startLocation} → {journey.endLocation}
                      </p>
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Distance</p>
                        <p className="text-sm">{journey.distance} miles</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Energy Used</p>
                        <p className="text-sm">{journey.energyUsed} kWh</p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Credits</p>
                        <p className="text-sm font-medium text-emerald-600">
                          {journey.creditsGenerated.toFixed(1)} tCO₂
                        </p>
                      </div>
                    </div>
                    {journey.status === "verified" && journey.verifiedAt && (
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Verified At</p>
                        <p className="text-sm">{new Date(journey.verifiedAt).toLocaleString()}</p>
                      </div>
                    )}
                    {journey.status === "rejected" && journey.rejectionReason && (
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Rejection Reason</p>
                        <p className="text-sm text-destructive">{journey.rejectionReason}</p>
                      </div>
                    )}
                  </div>
                </DialogContent>
              </Dialog>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
