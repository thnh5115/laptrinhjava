"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { getDispute, updateDisputeStatus, DisputeDetail as DisputeDetailType, DisputeStatus } from "@/lib/api/admin-disputes"
import { Check, X, AlertCircle, Copy } from "lucide-react"

interface DisputeDetailProps {
  disputeId: number
  onClose: () => void
  onUpdated?: () => void
}

export function DisputeDetail({ disputeId, onClose, onUpdated }: DisputeDetailProps) {
  const [dispute, setDispute] = useState<DisputeDetailType | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)
  const [adminNote, setAdminNote] = useState("")
  const [selectedStatus, setSelectedStatus] = useState<DisputeStatus>("RESOLVED")
  const { toast } = useToast()

  // Fetch dispute detail
  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true)
      try {
        const data = await getDispute(disputeId)
        setDispute(data)
        setAdminNote(data.adminNote || "")
        // Set initial status based on current status
        if (data.status === "OPEN") {
          setSelectedStatus("IN_REVIEW")
        } else {
          setSelectedStatus(data.status as DisputeStatus)
        }
      } catch (error: any) {
        console.error("Failed to fetch dispute:", error)
        toast({
          title: "Error",
          description: error?.response?.data?.message || "Failed to load dispute details",
          variant: "destructive",
        })
      } finally {
        setLoading(false)
      }
    }

    fetchDetail()
  }, [disputeId, toast])

  if (loading) {
    return (
      <Dialog open={true} onOpenChange={onClose}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Loading...</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-24 w-full" />
          </div>
        </DialogContent>
      </Dialog>
    )
  }

  if (!dispute) return null

  // Handle update status
  const handleUpdateStatus = async () => {
    setUpdating(true)
    try {
      await updateDisputeStatus(disputeId, {
        status: selectedStatus,
        adminNote: adminNote.trim() || undefined,
      })
      
      toast({
        title: "Success",
        description: `Dispute #${disputeId} status updated to ${selectedStatus} and recorded in Audit Log`,
      })
      
      onClose()
      onUpdated?.() // Trigger refresh in parent
    } catch (error: any) {
      console.error("Failed to update dispute:", error)
      toast({
        title: "Update Failed",
        description: error?.response?.data?.message || "Failed to update dispute status",
        variant: "destructive",
      })
    } finally {
      setUpdating(false)
    }
  }

  // Status badge color (reused pattern)
  const getStatusColor = (status: string) => {
    switch (status) {
      case "OPEN":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      case "IN_REVIEW":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      case "RESOLVED":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "REJECTED":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      default:
        return "bg-gray-100 text-gray-900 dark:bg-gray-900 dark:text-gray-100"
    }
  }

  // Copy to clipboard
  const handleCopy = (text: string, label: string) => {
    navigator.clipboard.writeText(text)
    toast({
      title: "Copied",
      description: `${label} copied to clipboard`,
    })
  }

  // Available status options based on current status
  const getAvailableStatusOptions = (): DisputeStatus[] => {
    // If already RESOLVED or REJECTED, cannot change
    if (dispute.status === "RESOLVED" || dispute.status === "REJECTED") {
      return []
    }
    
    // If OPEN or IN_REVIEW, allow all transitions
    return ["IN_REVIEW", "RESOLVED", "REJECTED"]
  }

  const availableOptions = getAvailableStatusOptions()
  const canUpdateStatus = availableOptions.length > 0

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-start justify-between">
            <div>
              <DialogTitle className="flex items-center gap-3">
                Dispute #{dispute.id}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleCopy(dispute.id.toString(), "Dispute ID")}
                >
                  <Copy className="h-4 w-4" />
                </Button>
              </DialogTitle>
              <DialogDescription className="flex items-center gap-2 mt-1">
                <span className="font-mono text-xs">{dispute.disputeCode}</span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleCopy(dispute.disputeCode, "Dispute Code")}
                >
                  <Copy className="h-3 w-3" />
                </Button>
              </DialogDescription>
            </div>
            <Badge className={getStatusColor(dispute.status)}>
              {dispute.status.replace(/_/g, " ")}
            </Badge>
          </div>
        </DialogHeader>

        <div className="space-y-4">
          {/* Basic Info */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Raised By</label>
              <p className="font-medium mt-1">{dispute.raisedBy}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Transaction ID</label>
              <p className="font-medium font-mono mt-1">#{dispute.transactionId}</p>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="text-sm font-medium text-muted-foreground">Description</label>
            <p className="mt-1 text-sm p-3 bg-muted/50 rounded-md">{dispute.description || "No description provided"}</p>
          </div>

          {/* Existing Admin Note (if any) */}
          {dispute.adminNote && (
            <div>
              <label className="text-sm font-medium text-muted-foreground">Previous Admin Note</label>
              <p className="mt-1 text-sm p-3 bg-muted/50 rounded-md">{dispute.adminNote}</p>
            </div>
          )}

          {/* Timestamps */}
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Created At</label>
              <p className="mt-1">{new Date(dispute.createdAt).toLocaleString()}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">Last Updated</label>
              <p className="mt-1">{new Date(dispute.updatedAt).toLocaleString()}</p>
            </div>
          </div>

          {/* Update Section (only if status allows) */}
          {canUpdateStatus && (
            <div className="border-t pt-4 mt-4">
              <h4 className="font-medium mb-3">Update Dispute Status</h4>
              
              <div className="space-y-3">
                {/* Status Select */}
                <div>
                  <label className="text-sm font-medium">New Status</label>
                  <Select
                    value={selectedStatus}
                    onValueChange={(v) => setSelectedStatus(v as DisputeStatus)}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {availableOptions.map((status) => (
                        <SelectItem key={status} value={status}>
                          {status.replace(/_/g, " ")}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* Admin Note */}
                <div>
                  <label className="text-sm font-medium">Admin Note (Optional)</label>
                  <Textarea
                    placeholder="Enter your resolution notes..."
                    value={adminNote}
                    onChange={(e) => setAdminNote(e.target.value)}
                    className="mt-1"
                    rows={3}
                    maxLength={1000}
                  />
                  <p className="text-xs text-muted-foreground mt-1">{adminNote.length}/1000 characters</p>
                </div>
              </div>
            </div>
          )}

          {/* Info: Status cannot be changed */}
          {!canUpdateStatus && (
            <div className="border-t pt-4 mt-4">
              <div className="flex items-center gap-2 text-muted-foreground p-3 bg-muted/30 rounded-md">
                <AlertCircle className="h-4 w-4" />
                <p className="text-sm">
                  This dispute status is <strong>{dispute.status}</strong> and cannot be modified.
                </p>
              </div>
            </div>
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={onClose}>
            Close
          </Button>
          {canUpdateStatus && (
            <Button
              onClick={handleUpdateStatus}
              disabled={updating || selectedStatus === dispute.status}
            >
              {updating ? (
                <>
                  <AlertCircle className="mr-2 h-4 w-4 animate-spin" />
                  Updating...
                </>
              ) : (
                <>
                  <Check className="mr-2 h-4 w-4" />
                  Update Status
                </>
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
