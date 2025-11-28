"use client";

import { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Check, X, Clock, DollarSign, AlertCircle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import axiosClient from "@/lib/api/axiosClient";

interface Payout {
  id: number;
  userEmail: string;
  amount: number;
  status: string;
  paymentMethod: string;
  bankAccount?: string;
  requestedAt: string;
  processedAt?: string;
}

export function PayoutManager() {
  const [payouts, setPayouts] = useState<Payout[]>([]);
  const [loading, setLoading] = useState(true);

  // State quản lý hành động: null | approve | reject | complete
  const [selectedPayout, setSelectedPayout] = useState<number | null>(null);
  const [action, setAction] = useState<
    "approve" | "reject" | "complete" | null
  >(null);
  const [notes, setNotes] = useState("");

  const { toast } = useToast();

  useEffect(() => {
    fetchPayouts();
  }, []);

  // Tải danh sách yêu cầu rút tiền
  const fetchPayouts = async () => {
    try {
      setLoading(true);
      const res = await axiosClient.get("/admin/payouts", {
        params: { page: 0, size: 50, sort: "requestedAt,desc" },
      });
      setPayouts(res.data.content || []);
    } catch (err) {
      console.error("[Payout Manager] Failed to load payouts:", err);
      toast({
        title: "Error",
        description: "Failed to load payout requests",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  // Xử lý hành động (Duyệt / Từ chối / Hoàn tất)
  const handleAction = async () => {
    if (!selectedPayout || !action) return;

    try {
      if (action === "approve") {
        // 1. Duyệt đơn (Chuyển sang Approved)
        await axiosClient.post(`/admin/payouts/${selectedPayout}/approve`, {
          notes: notes || "Approved by admin",
        });
        toast({
          title: "Request Approved",
          description: "Request is now Approved. Please transfer money.",
        });
      } else if (action === "reject") {
        // 2. Từ chối đơn (Hoàn tiền về ví)
        await axiosClient.post(`/admin/payouts/${selectedPayout}/reject`, {
          notes,
        });
        toast({
          title: "Request Rejected",
          description: "Request rejected. Funds returned to user.",
        });
      } else if (action === "complete") {
        // 3. [MỚI] Xác nhận đã chuyển tiền (Chuyển sang Completed)
        // Lưu ý: Bạn cần đảm bảo Backend đã có API này (như hướng dẫn trước đó)
        await axiosClient.post(`/admin/payouts/${selectedPayout}/complete`, {
          notes: notes || "Payment completed",
        });
        toast({
          title: "Payment Completed",
          description: "Transaction marked as successful.",
        });
      }

      fetchPayouts(); // Refresh danh sách
    } catch (err: any) {
      console.error("[Payout Manager] Action failed:", err);
      toast({
        title: "Action Failed",
        description:
          err?.response?.data?.message || "Failed to process request",
        variant: "destructive",
      });
    } finally {
      // Reset form
      setSelectedPayout(null);
      setAction(null);
      setNotes("");
    }
  };

  const getStatusColor = (status: string) => {
    const normalized = status.toUpperCase();
    switch (normalized) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 hover:bg-yellow-100";
      case "APPROVED":
        return "bg-blue-100 text-blue-800 hover:bg-blue-100";
      case "COMPLETED":
        return "bg-emerald-100 text-emerald-800 hover:bg-emerald-100";
      case "REJECTED":
        return "bg-red-100 text-red-800 hover:bg-red-100";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  if (loading && payouts.length === 0) {
    return (
      <div className="p-8 text-center text-muted-foreground">
        Loading payout requests...
      </div>
    );
  }

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Payout Requests</CardTitle>
          <CardDescription>
            Manage withdrawal requests from EV Owners
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User Info</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payouts.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center h-24 text-muted-foreground"
                  >
                    No payout requests found.
                  </TableCell>
                </TableRow>
              ) : (
                payouts.map((payout) => (
                  <TableRow key={payout.id}>
                    <TableCell>
                      <div className="flex flex-col">
                        <span className="font-medium">{payout.userEmail}</span>
                        <span className="text-xs text-muted-foreground flex items-center gap-1">
                          {payout.paymentMethod} • {payout.bankAccount || "N/A"}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell className="font-semibold text-base">
                      ${payout.amount.toFixed(2)}
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {new Date(payout.requestedAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      <Badge
                        className={getStatusColor(payout.status)}
                        variant="outline"
                      >
                        {payout.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      {/* Case 1: PENDING -> Show Approve / Reject */}
                      {payout.status === "PENDING" && (
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-8 px-2 text-green-600 border-green-200 hover:bg-green-50 hover:text-green-700"
                            onClick={() => {
                              setSelectedPayout(payout.id);
                              setAction("approve");
                            }}
                          >
                            <Check className="h-3.5 w-3.5 mr-1" /> Approve
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-8 px-2 text-red-600 border-red-200 hover:bg-red-50 hover:text-red-700"
                            onClick={() => {
                              setSelectedPayout(payout.id);
                              setAction("reject");
                            }}
                          >
                            <X className="h-3.5 w-3.5 mr-1" /> Reject
                          </Button>
                        </div>
                      )}

                      {/* Case 2: APPROVED -> Show Mark Paid */}
                      {payout.status === "APPROVED" && (
                        <Button
                          size="sm"
                          className="h-8 bg-emerald-600 hover:bg-emerald-700 text-white"
                          onClick={() => {
                            setSelectedPayout(payout.id);
                            setAction("complete");
                          }}
                        >
                          <DollarSign className="h-3.5 w-3.5 mr-1" /> Mark Paid
                        </Button>
                      )}

                      {/* Case 3: COMPLETED / REJECTED -> Show Info Icon (Disabled) */}
                      {["COMPLETED", "REJECTED"].includes(payout.status) && (
                        <span className="text-xs text-muted-foreground italic">
                          Processed
                        </span>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Confirmation Dialog */}
      <Dialog
        open={!!selectedPayout}
        onOpenChange={(open) => !open && setSelectedPayout(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {action === "approve" && "Approve Request"}
              {action === "reject" && "Reject Request"}
              {action === "complete" && "Confirm Payment"}
            </DialogTitle>
            <DialogDescription>
              {action === "approve" &&
                "Are you sure you want to approve this request? The status will change to APPROVED (Processing)."}
              {action === "reject" &&
                "Are you sure you want to reject this request? The funds will be returned to the user's wallet."}
              {action === "complete" &&
                "Confirm that you have manually transferred the money to the user? This will mark the request as COMPLETED."}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            {action === "reject" && (
              <div className="space-y-2">
                <span className="text-sm font-medium">
                  Reason for rejection:
                </span>
                <Textarea
                  placeholder="e.g., Incorrect bank details..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>
            )}
            {action !== "reject" && (
              <div className="space-y-2">
                <span className="text-sm font-medium">
                  Admin Note (Optional):
                </span>
                <Textarea
                  placeholder="Add internal note..."
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                />
              </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedPayout(null)}>
              Cancel
            </Button>
            <Button
              variant={action === "reject" ? "destructive" : "default"}
              className={
                action === "complete" || action === "approve"
                  ? "bg-emerald-600 hover:bg-emerald-700"
                  : ""
              }
              onClick={handleAction}
            >
              {action === "approve" && "Approve"}
              {action === "reject" && "Reject"}
              {action === "complete" && "Confirm Paid"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
