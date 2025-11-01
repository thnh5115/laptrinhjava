"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { mockJourneys, mockUsers } from "@/lib/mock-data"
import { CheckCircle2, XCircle, MapPin, Calendar, Zap, User } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"

export function ReviewQueue() {
  const [selectedAction, setSelectedAction] = useState<"approve" | "reject" | null>(null)
  const [rejectionReason, setRejectionReason] = useState("")
  const [isProcessing, setIsProcessing] = useState(false)
  const [successMessage, setSuccessMessage] = useState("")

  const pendingJourneys = mockJourneys.filter((j) => j.status === "pending")

  const handleVerification = async (action: "approve" | "reject") => {
    setIsProcessing(true)
    await new Promise((resolve) => setTimeout(resolve, 1500))
    setIsProcessing(false)
    setSuccessMessage(action === "approve" ? "Journey approved successfully!" : "Journey rejected successfully!")
    setTimeout(() => {
      setSuccessMessage("")
      setSelectedAction(null)
      setRejectionReason("")
    }, 2000)
  }

  return (
    <div className="space-y-6">
      {successMessage && (
        <Alert className="border-emerald-600 bg-emerald-50 dark:bg-emerald-950">
          <CheckCircle2 className="h-4 w-4 text-emerald-600" />
          <AlertDescription className="text-emerald-900 dark:text-emerald-100">{successMessage}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Pending Submissions ({pendingJourneys.length})</CardTitle>
          <CardDescription>Review journey details and verify carbon credit calculations</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {pendingJourneys.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">No pending submissions</p>
            ) : (
              pendingJourneys.map((journey) => {
                const owner = mockUsers.find((u) => u.id === journey.userId)
                return (
                  <Card key={journey.id} className="border-2">
                    <CardContent className="pt-6">
                      <div className="space-y-4">
                        <div className="flex items-start justify-between">
                          <div className="space-y-1">
                            <h3 className="font-semibold text-lg">
                              {journey.startLocation} ? {journey.endLocation}
                            </h3>
                            <Badge
                              variant="secondary"
                              className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                            >
                              Pending Review
                            </Badge>
                          </div>
                          <div className="text-right">
                            <p className="text-sm text-muted-foreground">Credits Generated</p>
                            <p className="text-2xl font-bold text-emerald-600">
                              {journey.creditsGenerated.toFixed(1)} tCO2
                            </p>
                          </div>
                        </div>

                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 py-4 border-y">
                          <div className="flex items-center gap-2">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <div>
                              <p className="text-xs text-muted-foreground">Date</p>
                              <p className="text-sm font-medium">{journey.date}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="h-4 w-4 text-muted-foreground" />
                            <div>
                              <p className="text-xs text-muted-foreground">Distance</p>
                              <p className="text-sm font-medium">{journey.distance} miles</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <Zap className="h-4 w-4 text-muted-foreground" />
                            <div>
                              <p className="text-xs text-muted-foreground">Energy Used</p>
                              <p className="text-sm font-medium">{journey.energyUsed} kWh</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <User className="h-4 w-4 text-muted-foreground" />
                            <div>
                              <p className="text-xs text-muted-foreground">Owner</p>
                              <p className="text-sm font-medium">{owner?.name || "Unknown"}</p>
                            </div>
                          </div>
                        </div>

                        <div className="flex gap-2">
                          <Dialog>
                            <DialogTrigger asChild>
                              <Button
                                className="flex-1 bg-emerald-600 hover:bg-emerald-700"
                                onClick={() => setSelectedAction("approve")}
                              >
                                <CheckCircle2 className="mr-2 h-4 w-4" />
                                Approve
                              </Button>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Approve Journey</DialogTitle>
                                <DialogDescription>
                                  Confirm that this journey meets all verification criteria
                                </DialogDescription>
                              </DialogHeader>
                              <div className="space-y-4 py-4">
                                <div className="p-4 bg-muted rounded-lg space-y-2">
                                  <div className="flex justify-between">
                                    <span className="text-sm">Journey</span>
                                    <span className="font-medium">
                                      {journey.startLocation} ? {journey.endLocation}
                                    </span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm">Credits</span>
                                    <span className="font-medium text-emerald-600">
                                      {journey.creditsGenerated.toFixed(1)} tCO2
                                    </span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm">Owner</span>
                                    <span className="font-medium">{owner?.name || "Unknown"}</span>
                                  </div>
                                </div>
                                <Alert>
                                  <AlertDescription>
                                    By approving, you confirm that all documentation is valid and calculations are
                                    accurate.
                                  </AlertDescription>
                                </Alert>
                              </div>
                              <DialogFooter>
                                <Button
                                  onClick={() => handleVerification("approve")}
                                  disabled={isProcessing}
                                  className="w-full bg-emerald-600 hover:bg-emerald-700"
                                >
                                  {isProcessing ? "Processing..." : "Confirm Approval"}
                                </Button>
                              </DialogFooter>
                            </DialogContent>
                          </Dialog>

                          <Dialog>
                            <DialogTrigger asChild>
                              <Button
                                variant="destructive"
                                className="flex-1"
                                onClick={() => setSelectedAction("reject")}
                              >
                                <XCircle className="mr-2 h-4 w-4" />
                                Reject
                              </Button>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Reject Journey</DialogTitle>
                                <DialogDescription>Provide a reason for rejecting this submission</DialogDescription>
                              </DialogHeader>
                              <div className="space-y-4 py-4">
                                <div className="p-4 bg-muted rounded-lg space-y-2">
                                  <div className="flex justify-between">
                                    <span className="text-sm">Journey</span>
                                    <span className="font-medium">
                                      {journey.startLocation} ? {journey.endLocation}
                                    </span>
                                  </div>
                                  <div className="flex justify-between">
                                    <span className="text-sm">Owner</span>
                                    <span className="font-medium">{owner?.name || "Unknown"}</span>
                                  </div>
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="reason">Rejection Reason</Label>
                                  <Textarea
                                    id="reason"
                                    placeholder="Explain why this journey cannot be verified..."
                                    value={rejectionReason}
                                    onChange={(e) => setRejectionReason(e.target.value)}
                                    rows={4}
                                  />
                                </div>
                              </div>
                              <DialogFooter>
                                <Button
                                  variant="destructive"
                                  onClick={() => handleVerification("reject")}
                                  disabled={isProcessing || !rejectionReason}
                                  className="w-full"
                                >
                                  {isProcessing ? "Processing..." : "Confirm Rejection"}
                                </Button>
                              </DialogFooter>
                            </DialogContent>
                          </Dialog>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )
              })
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
