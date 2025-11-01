"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { mockListings, mockUsers } from "@/lib/mock-data"
import { useToast } from "@/hooks/use-toast"
import { Flag, Check, X } from "lucide-react"

interface ListingActionsProps {
  listingId: string
  onClose: () => void
}

export function ListingActions({ listingId, onClose }: ListingActionsProps) {
  const [reason, setReason] = useState("")
  const { toast } = useToast()

  const listing = mockListings.find((l) => l.id === listingId)
  const seller = mockUsers.find((u) => u.id === listing?.sellerId)

  if (!listing) return null

  const handleAction = (action: "flag" | "approve" | "remove") => {
    toast({
      title: `Listing ${action === "flag" ? "Flagged" : action === "approve" ? "Approved" : "Removed"}`,
      description: `The listing has been ${action}ed successfully.`,
    })
    onClose()
  }

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Moderate Listing</DialogTitle>
          <DialogDescription>Review and take action on this listing</DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="p-4 border rounded-lg bg-muted/50">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span className="text-muted-foreground">Seller:</span> {seller?.name}
              </div>
              <div>
                <span className="text-muted-foreground">Amount:</span> {listing.amount} tCO₂
              </div>
              <div>
                <span className="text-muted-foreground">Price:</span> ${listing.pricePerCredit}/credit
              </div>
              <div>
                <span className="text-muted-foreground">Total:</span> $
                {(listing.amount * listing.pricePerCredit).toFixed(2)}
              </div>
            </div>
          </div>

          {listing.status === "flagged" && listing.flaggedReason && (
            <div className="p-3 border border-red-200 rounded-lg bg-red-50 dark:bg-red-950">
              <p className="text-sm font-medium text-red-900 dark:text-red-100">Flagged Reason:</p>
              <p className="text-sm text-red-700 dark:text-red-300 mt-1">{listing.flaggedReason}</p>
            </div>
          )}

          <div>
            <label className="text-sm font-medium">
              {listing.status === "active" ? "Reason for Flagging" : "Action Notes"}
            </label>
            <Textarea
              placeholder="Enter reason or notes..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="mt-1"
              rows={3}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          {listing.status === "active" && (
            <Button variant="destructive" onClick={() => handleAction("flag")}>
              <Flag className="h-4 w-4 mr-1" />
              Flag Listing
            </Button>
          )}
          {listing.status === "flagged" && (
            <>
              <Button variant="outline" onClick={() => handleAction("remove")}>
                <X className="h-4 w-4 mr-1" />
                Remove
              </Button>
              <Button onClick={() => handleAction("approve")}>
                <Check className="h-4 w-4 mr-1" />
                Approve
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
