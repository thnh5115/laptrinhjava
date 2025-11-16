"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetFooter,
} from "@/components/ui/sheet"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { Check, X, Loader2 } from "lucide-react"
import {
  getListing,
  updateListingStatus,
  ListingStatus,
  type ListingSummary,
} from "@/lib/api/admin-listings"

interface ListingActionsProps {
  listingId: number
  onClose: () => void
  onSuccess: () => void
}

export function ListingActions({ listingId, onClose, onSuccess }: ListingActionsProps) {
  const [listing, setListing] = useState<ListingSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    const fetchListing = async () => {
      setIsLoading(true)
      try {
        const data = await getListing(listingId)
        setListing(data)
      } catch (error: any) {
        toast({
          title: "Error",
          description: error.response?.data?.message || "Failed to load listing details",
          variant: "destructive",
        })
        onClose()
      } finally {
        setIsLoading(false)
      }
    }
    fetchListing()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [listingId])

  const handleAction = async (action: "approve" | "reject") => {
    if (!listing) return

    setIsUpdating(true)
    try {
      const status = action === "approve" ? ListingStatus.APPROVED : ListingStatus.REJECTED
      await updateListingStatus(listingId, status)

      toast({
        title: "Success",
        description: `Listing ${action}d successfully`,
      })

      onSuccess() // Refresh parent list
      onClose()
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || `Failed to ${action} listing`,
        variant: "destructive",
      })
    } finally {
      setIsUpdating(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      case "APPROVED":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "REJECTED":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      case "DELISTED":
        return "bg-slate-200 text-slate-900 dark:bg-slate-900 dark:text-slate-100"
      default:
        return ""
    }
  }

  return (
    <Sheet open={true} onOpenChange={onClose}>
      <SheetContent className="w-[500px]">
        <SheetHeader>
          <SheetTitle>Listing Details</SheetTitle>
          <SheetDescription>Review and approve/reject this listing</SheetDescription>
        </SheetHeader>

        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        )}

        {!isLoading && listing && (() => {
          const credits = Number(listing.quantity ?? 0)
          const pricePerCredit = Number(listing.price ?? 0)
          const totalPrice = pricePerCredit * credits
          const ownerName = listing.ownerFullName || listing.ownerEmail
          return (
            <div className="mt-6 space-y-6">
            {/* Status Badge */}
            <div>
              <label className="text-sm font-medium text-muted-foreground">Status</label>
              <div className="mt-1">
                <Badge className={getStatusColor(listing.status)}>{listing.status}</Badge>
              </div>
            </div>

            {/* Basic Info */}
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium text-muted-foreground">Title</label>
                <p className="text-sm font-medium mt-1">{listing.title}</p>
              </div>

              <div>
                <label className="text-sm font-medium text-muted-foreground">Description</label>
                <p className="text-sm mt-1 text-muted-foreground">{listing.description}</p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Credits</label>
                  <p className="text-sm font-medium mt-1">{credits} {listing.unit || "tCO2"}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Price/Credit</label>
                  <p className="text-sm font-medium mt-1">${pricePerCredit.toFixed(2)}</p>
                </div>
              </div>

              <div>
                <label className="text-sm font-medium text-muted-foreground">Total Price</label>
                <p className="text-lg font-bold mt-1">${totalPrice.toFixed(2)}</p>
              </div>

              <div>
                <label className="text-sm font-medium text-muted-foreground">Owner</label>
                <div className="mt-1">
                  <p className="text-sm font-medium">{ownerName}</p>
                  <p className="text-xs text-muted-foreground">{listing.ownerEmail}</p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Created</label>
                  <p className="text-sm mt-1">{new Date(listing.createdAt).toLocaleString()}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">Updated</label>
                  <p className="text-sm mt-1">{new Date(listing.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
            )
          })()}

        {!isLoading && listing && listing.status === "PENDING" && (
          <SheetFooter className="mt-6 pt-6 border-t">
            <div className="flex gap-2 w-full">
              <Button
                className="flex-1"
                variant="destructive"
                onClick={() => handleAction("reject")}
                disabled={isUpdating}
              >
                {isUpdating ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <X className="h-4 w-4 mr-2" />
                )}
                Reject
              </Button>
              <Button
                className="flex-1"
                onClick={() => handleAction("approve")}
                disabled={isUpdating}
              >
                {isUpdating ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Check className="h-4 w-4 mr-2" />
                )}
                Approve
              </Button>
            </div>
          </SheetFooter>
        )}

        {!isLoading && listing && listing.status !== "PENDING" && (
          <SheetFooter className="mt-6 pt-6 border-t">
            <Button variant="outline" onClick={onClose} className="w-full">
              Close
            </Button>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}
