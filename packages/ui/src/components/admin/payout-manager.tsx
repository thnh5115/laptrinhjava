"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Badge } from "../ui/badge"
import { Textarea } from "../ui/textarea"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog"
import { mockPayouts, mockUsers } from "../../../apps/web-portal-next/lib/mock-data"
import { Check, X, Clock } from "lucide-react"
import { useToast } from "../../hooks/use-toast"

export function PayoutManager() {
  const [selectedPayout, setSelectedPayout] = useState<string | null>(null)
  const [action, setAction] = useState<"approve" | "reject" | null>(null)
  const [notes, setNotes] = useState("")
  const { toast } = useToast()

  const handleAction = () => {
    toast({
      title: action === "approve" ? "Payout Approved" : "Payout Rejected",
      description: `Payout request has been ${action === "approve" ? "approved" : "rejected"} successfully.`,
    })
    setSelectedPayout(null)
    setAction(null)
    setNotes("")
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "pending":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      case "approved":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      case "completed":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "rejected":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      default:
        return ""
    }
  }

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Payout Requests</CardTitle>
          <CardDescription>Manage EV owner withdrawal requests</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Requested</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {mockPayouts.map((payout) => {
                const user = mockUsers.find((u) => u.id === payout.userId)
                return (
                  <TableRow key={payout.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{user?.name}</p>
                        <p className="text-xs text-muted-foreground">{user?.email}</p>
                      </div>
                    </TableCell>
                    <TableCell className="font-medium">${payout.amount.toFixed(2)}</TableCell>
                    <TableCell>{new Date(payout.requestedAt).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(payout.status)}>{payout.status}</Badge>
                    </TableCell>
                    <TableCell>
                      {payout.status === "pending" && (
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              setSelectedPayout(payout.id)
                              setAction("approve")
                            }}
                          >
                            <Check className="h-4 w-4 mr-1" />
                            Approve
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              setSelectedPayout(payout.id)
                              setAction("reject")
                            }}
                          >
                            <X className="h-4 w-4 mr-1" />
                            Reject
                          </Button>
                        </div>
                      )}
                      {payout.status === "approved" && (
                        <Badge className="bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100">
                          <Clock className="h-3 w-3 mr-1" />
                          Processing
                        </Badge>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={selectedPayout !== null} onOpenChange={() => setSelectedPayout(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{action === "approve" ? "Approve Payout" : "Reject Payout"}</DialogTitle>
            <DialogDescription>
              {action === "approve"
                ? "Confirm approval of this payout request. Funds will be transferred to the user's account."
                : "Provide a reason for rejecting this payout request."}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium">Notes</label>
              <Textarea
                placeholder={action === "approve" ? "Optional notes..." : "Reason for rejection..."}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="mt-1"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedPayout(null)}>
              Cancel
            </Button>
            <Button onClick={handleAction}>{action === "approve" ? "Approve" : "Reject"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
