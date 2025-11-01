"use client"

import { Card, CardContent, CardHeader, CardTitle } from "../ui/card"
import { Badge } from "../ui/badge"
import { Button } from "../ui/button"
import { mockJourneys, mockUsers } from "../../../apps/web-portal-next/lib/mock-data"
import { CheckCircle2, XCircle, Eye } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "../ui/dialog"

export function AuditHistory() {
  const reviewedJourneys = mockJourneys.filter((j) => j.status !== "pending")

  return (
    <Card>
      <CardHeader>
        <CardTitle>Completed Reviews ({reviewedJourneys.length})</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {reviewedJourneys.map((journey) => {
            const owner = mockUsers.find((u) => u.id === journey.userId)
            const verifier = mockUsers.find((u) => u.id === journey.verifiedBy)

            return (
              <div key={journey.id} className="flex items-center justify-between border rounded-lg p-4">
                <div className="flex-1 space-y-2">
                  <div className="flex items-center gap-2">
                    <p className="font-medium">
                      {journey.startLocation} → {journey.endLocation}
                    </p>
                    <Badge
                      variant={journey.status === "verified" ? "default" : "destructive"}
                      className={
                        journey.status === "verified"
                          ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                          : ""
                      }
                    >
                      {journey.status === "verified" ? (
                        <CheckCircle2 className="mr-1 h-3 w-3" />
                      ) : (
                        <XCircle className="mr-1 h-3 w-3" />
                      )}
                      {journey.status}
                    </Badge>
                  </div>
                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>Owner: {owner?.name || "Unknown"}</span>
                    <span>Date: {journey.date}</span>
                    <span>Credits: {journey.creditsGenerated.toFixed(1)} tCO₂</span>
                    {journey.verifiedAt && <span>Reviewed: {new Date(journey.verifiedAt).toLocaleDateString()}</span>}
                  </div>
                  {verifier && <p className="text-xs text-muted-foreground">Verified by: {verifier.name}</p>}
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
                      <DialogTitle>Verification Details</DialogTitle>
                      <DialogDescription>Complete audit information</DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Status</p>
                          <Badge
                            variant={journey.status === "verified" ? "default" : "destructive"}
                            className={
                              journey.status === "verified"
                                ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                                : ""
                            }
                          >
                            {journey.status === "verified" ? (
                              <CheckCircle2 className="mr-1 h-3 w-3" />
                            ) : (
                              <XCircle className="mr-1 h-3 w-3" />
                            )}
                            {journey.status}
                          </Badge>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Journey Date</p>
                          <p className="text-sm">{journey.date}</p>
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
                          <p className="text-sm font-medium text-muted-foreground">Energy</p>
                          <p className="text-sm">{journey.energyUsed} kWh</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Credits</p>
                          <p className="text-sm font-medium text-emerald-600">
                            {journey.creditsGenerated.toFixed(1)} tCO₂
                          </p>
                        </div>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-muted-foreground">Owner</p>
                        <p className="text-sm">{owner?.name || "Unknown"}</p>
                      </div>
                      {verifier && (
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Verified By</p>
                          <p className="text-sm">{verifier.name}</p>
                        </div>
                      )}
                      {journey.verifiedAt && (
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Verification Date</p>
                          <p className="text-sm">{new Date(journey.verifiedAt).toLocaleString()}</p>
                        </div>
                      )}
                      {journey.rejectionReason && (
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Rejection Reason</p>
                          <p className="text-sm text-destructive">{journey.rejectionReason}</p>
                        </div>
                      )}
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
