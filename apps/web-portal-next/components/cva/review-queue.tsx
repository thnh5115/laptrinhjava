"use client";

import type React from "react";
import { useCallback, useEffect, useState } from "react";
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
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useToast } from "@/hooks/use-toast";
// 1. IMPORT AUTH CONTEXT
import { useAuth } from "@/lib/contexts/AuthContext";
import {
  approveVerificationRequest,
  listVerificationRequests,
  rejectVerificationRequest,
  type VerificationRequest,
} from "@/lib/api/cva";
import {
  Calendar,
  Hash,
  MapPinned,
  Zap,
  CheckCircle2,
  XCircle,
  Info,
  AlertTriangle,
} from "lucide-react";

interface QueueState {
  isLoading: boolean;
  error: string | null;
  items: VerificationRequest[];
  processingId: number | null; // ID bây giờ là số
}

type DialogMode = "approve" | "reject";

export function ReviewQueue() {
  const { toast } = useToast();
  // 2. LẤY USER TỪ CONTEXT CHUẨN CỦA HỆ THỐNG
  const { user } = useAuth();

  const [state, setState] = useState<QueueState>({
    isLoading: true,
    error: null,
    items: [],
    processingId: null,
  });
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<DialogMode>("approve");
  const [activeRequest, setActiveRequest] =
    useState<VerificationRequest | null>(null);
  const [rejectionReason, setRejectionReason] = useState("");

  const loadPending = useCallback(() => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    listVerificationRequests({ status: "PENDING", size: 100 })
      .then((page) => {
        setState({
          isLoading: false,
          error: null,
          items: page.content,
          processingId: null,
        });
      })
      .catch((err) => {
        setState((prev) => ({
          ...prev,
          isLoading: false,
          error: err.message ?? "Failed to load pending requests",
        }));
      });
  }, []);

  useEffect(() => {
    loadPending();
  }, [loadPending]);

  const pendingCount = state.items.length;

  // Helper tạo UUID ngẫu nhiên cho idempotencyKey
  const generateUUID = () => {
    if (typeof crypto !== "undefined" && crypto.randomUUID) {
      return crypto.randomUUID();
    }
    return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, (c) =>
      (
        +c ^
        (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (+c / 4)))
      ).toString(16)
    );
  };

  const openDialog = (request: VerificationRequest, mode: DialogMode) => {
    setActiveRequest(request);
    setDialogMode(mode);
    setRejectionReason("");
    setDialogOpen(true);
  };

  const closeDialog = () => {
    setDialogOpen(false);
    setActiveRequest(null);
    setRejectionReason("");
  };

  const handleConfirm = async () => {
    if (!activeRequest) return;

    // 3. KIỂM TRA USER & CHUYỂN ĐỔI ID SANG SỐ
    if (!user || !user.id) {
      toast({
        variant: "destructive",
        title: "Missing identity",
        description: "Please sign in as a CVA officer first.",
      });
      return;
    }

    // QUAN TRỌNG: Backend CVA bây giờ yêu cầu Long (số), không phải UUID
    const verifierId = Number(user.id);

    if (isNaN(verifierId)) {
      console.error("Invalid verifier ID:", user.id);
      toast({
        variant: "destructive",
        title: "Invalid ID",
        description: "Your User ID is not a valid number.",
      });
      return;
    }

    // Tạo key chống trùng lặp
    const idempotencyKey = generateUUID();
    const correlationId = generateUUID();

    if (dialogMode === "reject" && !rejectionReason.trim()) {
      toast({
        variant: "destructive",
        title: "Reason required",
        description: "Provide a short rejection reason to continue.",
      });
      return;
    }

    setState((prev) => ({ ...prev, processingId: Number(activeRequest.id) }));

    try {
      // Lưu ý: activeRequest.id bây giờ là số (Long)
      if (dialogMode === "approve") {
        await approveVerificationRequest(activeRequest.id.toString(), {
          verifierId: verifierId.toString(), // API client convert sang string nhưng backend sẽ parse lại
          idempotencyKey,
          correlationId,
          notes: "Approved via Web Portal",
        });

        toast({
          title: "Request approved",
          description: `Issued credits for trip ${activeRequest.tripId}`,
        });
      } else {
        await rejectVerificationRequest(activeRequest.id.toString(), {
          verifierId: verifierId.toString(),
          reason: rejectionReason.trim(),
          correlationId,
        });

        toast({
          title: "Request rejected",
          variant: "destructive",
          description: "The EV owner will be notified.",
        });
      }
      closeDialog();
      loadPending();
    } catch (err: any) {
      const message =
        err?.response?.data?.message ||
        err.message ||
        "Unable to process request";
      toast({
        variant: "destructive",
        title: "Action failed",
        description: message,
      });
    } finally {
      setState((prev) => ({ ...prev, processingId: null }));
    }
  };

  const dialogTitle =
    dialogMode === "approve"
      ? "Approve verification request"
      : "Reject verification request";
  const dialogDescription =
    dialogMode === "approve"
      ? "Confirm this submission meets all validation and audit requirements."
      : "Provide a concise reason that can be shared with the EV owner.";
  const dialogActionLabel =
    dialogMode === "approve" ? "Confirm approval" : "Confirm rejection";

  return (
    <div className="space-y-6">
      {state.error && (
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>{state.error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Pending Submissions ({pendingCount})</CardTitle>
          <CardDescription>
            Review telemetry and approve or reject carbon credit issuance.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {state.isLoading ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Loading pending requests…
            </p>
          ) : pendingCount === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              No pending submissions right now.
            </p>
          ) : (
            <div className="space-y-4">
              {state.items.map((request) => (
                <Card key={request.id} className="border-2">
                  <CardContent className="space-y-4 pt-6">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div className="space-y-1">
                        <h3 className="text-lg font-semibold">
                          Trip {request.tripId}
                        </h3>
                        <Badge
                          variant="secondary"
                          className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
                        >
                          Pending review
                        </Badge>
                      </div>
                      <div className="text-right text-sm text-muted-foreground">
                        <p className="font-medium text-foreground">Owner ID</p>
                        <p>{truncateId(request.ownerId.toString())}</p>
                      </div>
                    </div>

                    <div className="grid gap-4 md:grid-cols-4">
                      <InfoRow
                        icon={
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                        }
                        label="Submitted"
                      >
                        {formatDateTime(request.createdAt)}
                      </InfoRow>
                      <InfoRow
                        icon={
                          <MapPinned className="h-4 w-4 text-muted-foreground" />
                        }
                        label="Trip checksum"
                      >
                        {truncateHash(request.checksum)}
                      </InfoRow>
                      <InfoRow
                        icon={<Zap className="h-4 w-4 text-muted-foreground" />}
                        label="Energy (kWh)"
                      >
                        {request.energyKwh.toFixed(2)}
                      </InfoRow>
                      <InfoRow
                        icon={
                          <Hash className="h-4 w-4 text-muted-foreground" />
                        }
                        label="Distance (km)"
                      >
                        {request.distanceKm.toFixed(2)}
                      </InfoRow>
                    </div>

                    <div className="grid gap-2 md:grid-cols-2">
                      <Button
                        className="bg-emerald-600 hover:bg-emerald-700"
                        disabled={state.processingId === Number(request.id)}
                        onClick={() => openDialog(request, "approve")}
                      >
                        <CheckCircle2 className="mr-2 h-4 w-4" />
                        Approve
                      </Button>
                      <Button
                        variant="destructive"
                        disabled={state.processingId === Number(request.id)}
                        onClick={() => openDialog(request, "reject")}
                      >
                        <XCircle className="mr-2 h-4 w-4" />
                        Reject
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{dialogTitle}</DialogTitle>
            <DialogDescription>{dialogDescription}</DialogDescription>
          </DialogHeader>

          {activeRequest && (
            <div className="space-y-4 py-4">
              <div className="space-y-1 text-sm">
                <p className="font-medium text-muted-foreground">
                  Trip reference
                </p>
                <p className="font-semibold">{activeRequest.tripId}</p>
                <p className="text-muted-foreground">
                  Owner {truncateId(activeRequest.ownerId.toString())} •
                  submitted {formatDateTime(activeRequest.createdAt)}
                </p>
              </div>

              {dialogMode === "approve" ? (
                <Alert>
                  <Info className="h-4 w-4" />
                  <AlertDescription>
                    Approval will post the calculated credit issuance to the EV
                    owner wallet and emit an audit event.
                  </AlertDescription>
                </Alert>
              ) : (
                <div className="space-y-2">
                  <Label htmlFor="rejection-reason">Rejection reason</Label>
                  <Textarea
                    id="rejection-reason"
                    placeholder="Explain why this submission cannot be approved"
                    value={rejectionReason}
                    onChange={(event) => setRejectionReason(event.target.value)}
                    rows={4}
                  />
                </div>
              )}
            </div>
          )}

          <DialogFooter>
            <Button
              className={
                dialogMode === "approve"
                  ? "bg-emerald-600 hover:bg-emerald-700"
                  : ""
              }
              variant={dialogMode === "approve" ? "default" : "destructive"}
              onClick={handleConfirm}
              disabled={
                state.processingId ===
                (activeRequest ? Number(activeRequest.id) : null)
              }
            >
              {state.processingId ===
              (activeRequest ? Number(activeRequest.id) : null)
                ? "Processing…"
                : dialogActionLabel}
            </Button>
            <Button
              variant="outline"
              onClick={closeDialog}
              disabled={!!state.processingId}
            >
              Cancel
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

interface InfoRowProps {
  icon: React.ReactNode;
  label: string;
  children: React.ReactNode;
}

function InfoRow({ icon, label, children }: InfoRowProps) {
  return (
    <div className="flex items-center gap-2 rounded-lg border bg-muted/40 p-3 text-sm">
      {icon}
      <div className="space-y-1">
        <p className="text-xs uppercase tracking-wide text-muted-foreground">
          {label}
        </p>
        <p className="font-medium text-foreground">{children}</p>
      </div>
    </div>
  );
}

function truncateId(value: string, length = 6) {
  if (!value) return "";
  return value.length <= length * 2
    ? value
    : `${value.slice(0, length)}…${value.slice(-length)}`;
}

function truncateHash(value: string, length = 8) {
  if (!value) return "";
  return value.length <= length * 2
    ? value
    : `${value.slice(0, length)}…${value.slice(-length)}`;
}

function formatDateTime(raw: string) {
  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    return raw;
  }
  return date.toLocaleString();
}
