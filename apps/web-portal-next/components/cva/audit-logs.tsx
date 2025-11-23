"use client";

import { useEffect, useMemo, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
  listVerificationRequests,
  type VerificationRequest,
} from "@/lib/api/cva";
import { useToast } from "@/hooks/use-toast";
import { Download, CheckCircle2, XCircle, Clock, Loader2 } from "lucide-react";

interface LogState {
  isLoading: boolean;
  error: string | null;
  items: VerificationRequest[];
  exporting: boolean;
}

interface AuditEvent {
  id: string;
  status: VerificationRequest["status"];
  ownerId: string;
  createdAt: string;
  verifiedAt?: string | null;
  summary: string;
  credits?: number | null;
}

export function AuditLogs() {
  const { toast } = useToast();
  const [state, setState] = useState<LogState>({
    isLoading: true,
    error: null,
    items: [],
    exporting: false,
  });

  useEffect(() => {
    let cancelled = false;
    setState((prev) => ({ ...prev, isLoading: true, error: null }));

    listVerificationRequests({ size: 500 })
      .then((page) => {
        if (!cancelled) {
          setState({
            isLoading: false,
            error: null,
            items: page.content,
            exporting: false,
          });
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setState({
            isLoading: false,
            error: err.message ?? "Failed to load audit logs",
            items: [],
            exporting: false,
          });
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const events = useMemo(() => buildEvents(state.items), [state.items]);

  const handleExport = () => {
    setState((prev) => ({ ...prev, exporting: true }));
    try {
      const payload = JSON.stringify(
        events.map((event) => ({
          ...event,
          createdAt: event.createdAt,
          verifiedAt: event.verifiedAt ?? undefined,
        })),
        null,
        2
      );
      const blob = new Blob([payload], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `cva-audit-log-${new Date()
        .toISOString()
        .slice(0, 10)}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      toast({
        title: "Export complete",
        description: "Audit log JSON downloaded.",
      });
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Unable to export logs";
      toast({
        variant: "destructive",
        title: "Export failed",
        description: message,
      });
    } finally {
      setState((prev) => ({ ...prev, exporting: false }));
    }
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <CardTitle>System Activity Logs</CardTitle>
            <CardDescription>
              End-to-end trace of verification events
            </CardDescription>
            {state.error && (
              <Alert variant="destructive" className="mt-3">
                <AlertDescription>{state.error}</AlertDescription>
              </Alert>
            )}
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={handleExport}
            disabled={state.exporting || events.length === 0}
          >
            {state.exporting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Exporting…
              </>
            ) : (
              <>
                <Download className="mr-2 h-4 w-4" /> Export Logs
              </>
            )}
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {state.isLoading ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Loading audit trail…
          </p>
        ) : events.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            No activity to display yet.
          </p>
        ) : (
          <div className="space-y-3">
            {events.map((event) => (
              <div
                key={event.id}
                className="flex items-start gap-4 border-b pb-3 last:border-0"
              >
                <StatusIcon status={event.status} />
                <div className="flex-1 space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-medium">{event.summary}</p>
                    <StatusBadge status={event.status} />
                  </div>
                  <p className="text-sm text-muted-foreground">
                    Owner {truncateId(event.ownerId)} • Created{" "}
                    {formatDateTime(event.createdAt)}
                    {event.verifiedAt &&
                      ` • Reviewed ${formatDateTime(event.verifiedAt)}`}
                    {typeof event.credits === "number" &&
                      ` • Credits ${event.credits.toFixed(2)}`}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function buildEvents(requests: VerificationRequest[]): AuditEvent[] {
  return requests
    .map((request) => {
      const status = request.status;
      const summary =
        status === "APPROVED"
          ? "Verification approved"
          : status === "REJECTED"
          ? "Verification rejected"
          : "Verification submitted";

      return {
        id: request.id,
        status,
        ownerId: request.ownerId,
        createdAt: request.createdAt,
        verifiedAt: request.verifiedAt,
        summary,
        credits: request.creditIssuance?.creditsRounded ?? null,
      };
    })
    .sort((a, b) => {
      const left = new Date(a.verifiedAt ?? a.createdAt).getTime();
      const right = new Date(b.verifiedAt ?? b.createdAt).getTime();
      return right - left;
    });
}

function StatusIcon({ status }: { status: VerificationRequest["status"] }) {
  switch (status) {
    case "APPROVED":
      return <CheckCircle2 className="mt-1 h-4 w-4 text-emerald-600" />;
    case "REJECTED":
      return <XCircle className="mt-1 h-4 w-4 text-red-600" />;
    default:
      return <Clock className="mt-1 h-4 w-4 text-amber-600" />;
  }
}

function StatusBadge({ status }: { status: VerificationRequest["status"] }) {
  if (status === "APPROVED") {
    return (
      <Badge
        className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
        variant="default"
      >
        Approved
      </Badge>
    );
  }
  if (status === "REJECTED") {
    return <Badge variant="destructive">Rejected</Badge>;
  }
  return (
    <Badge
      className="bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      variant="secondary"
    >
      Pending
    </Badge>
  );
}

function truncateId(value: string | number | undefined | null, length = 6) {
  if (!value) return "—";
  const str = value.toString(); // <--- QUAN TRỌNG: Ép kiểu sang chuỗi
  return str.length <= length * 2
    ? str
    : `${str.slice(0, length)}…${str.slice(-length)}`;
}
function formatDateTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
}
