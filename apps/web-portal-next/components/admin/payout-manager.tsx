"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Check, X, Clock } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import axiosClient from "@/lib/api/axiosClient"

interface Payout {
  id: number
  userEmail: string
  amount: number
  status: string
  paymentMethod: string
  requestedAt: string
  processedAt?: string
}

export function PayoutManager() {
  const [payouts, setPayouts] = useState<Payout[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedPayout, setSelectedPayout] = useState<number | null>(null)
  const [action, setAction] = useState<"approve" | "reject" | null>(null)
  const [notes, setNotes] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchPayouts()
  }, [])

  // Tải danh sách yêu cầu rút tiền
  const fetchPayouts = async () => {
    try {
      const res = await axiosClient.get('/admin/payouts', {
        params: { page: 0, size: 50, sort: 'requestedAt,desc' }
      })
      setPayouts(res.data.content || [])
    } catch (err) {
      console.error('[Payout Manager] Failed to load payouts:', err)
      toast({ title: "Error", description: "Failed to load payout requests", variant: "destructive" })
    } finally {
      setLoading(false)
    }
  }

  // Duyệt/từ chối yêu cầu rút tiền
  const handleAction = async () => {
    if (!selectedPayout) return

    try {
      if (action === 'approve') {
        await axiosClient.post(`/admin/payouts/${selectedPayout}/approve`)
        toast({ title: "Payout Approved", description: "Payout request has been approved successfully." })
      } else {
        await axiosClient.post(`/admin/payouts/${selectedPayout}/reject`, { notes })
        toast({ title: "Payout Rejected", description: "Payout request has been rejected." })
      }
      fetchPayouts() // Refresh danh sách
    } catch (err) {
      console.error('[Payout Manager] Action failed:', err)
      toast({ title: "Error", description: "Failed to process payout request", variant: "destructive" })
    } finally {
      setSelectedPayout(null)
      setAction(null)
      setNotes("")
    }
  }

  const getStatusColor = (status: string) => {
    const normalized = status.toLowerCase()
    switch (normalized) {
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

  if (loading) {
    return <div className="p-8 text-center">Loading payout requests...</div>
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
              {payouts.map((payout) => (
                <TableRow key={payout.id}>
                  <TableCell>
                    <div>
                      <p className="font-medium">{payout.userEmail}</p>
                      <p className="text-xs text-muted-foreground">{payout.paymentMethod}</p>
                    </div>
                  </TableCell>
                  <TableCell className="font-medium">${payout.amount.toFixed(2)}</TableCell>
                  <TableCell>{new Date(payout.requestedAt).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Badge className={getStatusColor(payout.status)}>{payout.status}</Badge>
                  </TableCell>
                  <TableCell>
                    {payout.status.toLowerCase() === "pending" && (
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
                    {payout.status.toLowerCase() === "approved" && (
                      <Badge className="bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100">
                        <Clock className="h-3 w-3 mr-1" />
                        Processing
                      </Badge>
                    )}
                  </TableCell>
                </TableRow>
              ))}
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
