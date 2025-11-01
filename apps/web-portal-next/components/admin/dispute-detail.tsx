"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { mockDisputes, mockUsers, mockTransactions } from "@/lib/mock-data"
import { useToast } from "@ui/hooks/use-toast"
import { Check, X, AlertCircle } from "lucide-react"

interface DisputeDetailProps {
  disputeId: string
  onClose: () => void
}

export function DisputeDetail({ disputeId, onClose }: DisputeDetailProps) {
  const [resolution, setResolution] = useState("")
  const { toast } = useToast()

  const dispute = mockDisputes.find((d) => d.id === disputeId)
  const reporter = mockUsers.find((u) => u.id === dispute?.reportedBy)
  const reported = mockUsers.find((u) => u.id === dispute?.reportedAgainst)
  const transaction = mockTransactions.find((t) => t.id === dispute?.transactionId)

  if (!dispute) return null

  const handleResolve = (action: "approve" | "refund" | "reject") => {
    toast({
      title: "Dispute Resolved",
      description: `Dispute has been ${action}ed successfully.`,
    })
    onClose()
  }

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Dispute Details</DialogTitle>
          <DialogDescription>Review and resolve this dispute</DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Dispute ID</label>
              <p className="font-mono text-sm">{dispute.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Status</label>
              <div className="mt-1">
                <Badge>{dispute.status}</Badge>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Reporter</label>
              <p className="font-medium">{reporter?.name}</p>
              <p className="text-xs text-muted-foreground">{reporter?.email}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Reported Against</label>
              <p className="font-medium">{reported?.name}</p>
              <p className="text-xs text-muted-foreground">{reported?.email}</p>
            </div>
          </div>

          <div>
            <label className="text-sm font-medium text-muted-foreground">Reason</label>
            <p className="mt-1">{dispute.reason}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-muted-foreground">Description</label>
            <p className="mt-1 text-sm">{dispute.description}</p>
          </div>

          {transaction && (
            <div className="p-4 border rounded-lg bg-muted/50">
              <p className="text-sm font-medium mb-2">Transaction Details</p>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div>
                  <span className="text-muted-foreground">Amount:</span> {transaction.amount} tCO2
                </div>
                <div>
                  <span className="text-muted-foreground">Price:</span> ${transaction.totalPrice}
                </div>
                <div>
                  <span className="text-muted-foreground">Date:</span>{" "}
                  {new Date(transaction.timestamp).toLocaleDateString()}
                </div>
                <div>
                  <span className="text-muted-foreground">Status:</span> {transaction.status}
                </div>
              </div>
            </div>
          )}

          <div>
            <label className="text-sm font-medium">Resolution Notes</label>
            <Textarea
              placeholder="Enter resolution details..."
              value={resolution}
              onChange={(e) => setResolution(e.target.value)}
              className="mt-1"
              rows={3}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="outline" onClick={() => handleResolve("reject")}>
            <X className="h-4 w-4 mr-1" />
            Reject Claim
          </Button>
          <Button variant="outline" onClick={() => handleResolve("refund")}>
            <AlertCircle className="h-4 w-4 mr-1" />
            Issue Refund
          </Button>
          <Button onClick={() => handleResolve("approve")}>
            <Check className="h-4 w-4 mr-1" />
            Approve & Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
