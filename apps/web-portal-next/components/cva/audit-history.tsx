"use client"

import type React from "react"
import { useCallback, useEffect, useMemo, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { useToast } from "@/hooks/use-toast"
import { downloadReportPdf, listVerificationRequests, type VerificationRequest } from "@/lib/api/cva"
import { CheckCircle2, Download, FileText, Loader2, XCircle } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"

interface HistoryState {
  isLoading: boolean
  error: string | null
  items: VerificationRequest[]
  downloadingId: string | null
}

export function AuditHistory() {
  const { toast } = useToast()
  const [state, setState] = useState<HistoryState>({
    isLoading: true,
    error: null,
    items: [],
    downloadingId: null,
  })

  const loadHistory = useCallback(() => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }))
    listVerificationRequests({ size: 200 })
      .then((page) => {
        setState((prev) => ({ ...prev, isLoading: false, items: page.content }))
      })
      .catch((err) => {
        setState((prev) => ({ ...prev, isLoading: false, error: err.message ?? "Failed to load audit history" }))
      })
  }, [])

  useEffect(() => {
    loadHistory()
  }, [loadHistory])

  const reviewedRequests = useMemo(
    () =>
      state.items
        .filter((request) => request.status !== "PENDING")
        .sort((a, b) => {
          const left = new Date(a.verifiedAt ?? a.createdAt).getTime()
          const right = new Date(b.verifiedAt ?? b.createdAt).getTime()
          return right - left
        }),
    [state.items]
  )

  const handleDownloadPdf = async (request: VerificationRequest) => {
    setState((prev) => ({ ...prev, downloadingId: request.id }))
    try {
      const blob = await downloadReportPdf(request.id)
      const url = URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = `cva-report-${request.id}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      toast({ title: "Report ready", description: "Carbon audit PDF downloaded." })
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unable to download report"
      toast({ variant: "destructive", title: "Download failed", description: message })
    } finally {
      setState((prev) => ({ ...prev, downloadingId: null }))
    }
  }

  return (
    <Card>
      <CardHeader className="flex items-center justify-between gap-4">
        <div>
          <CardTitle>Completed Reviews ({reviewedRequests.length})</CardTitle>
          {state.error && (
            <Alert variant="destructive" className="mt-3">
              <AlertDescription>{state.error}</AlertDescription>
            </Alert>
          )}
        </div>
        <Button variant="outline" size="sm" onClick={loadHistory} disabled={state.isLoading}>
          {state.isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Refreshing…
            </>
          ) : (
            "Refresh"
          )}
        </Button>
      </CardHeader>
      <CardContent>
        {state.isLoading ? (
          <p className="py-8 text-center text-sm text-muted-foreground">Loading completed reviews…</p>
        ) : reviewedRequests.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">No completed reviews yet.</p>
        ) : (
          <div className="space-y-4">
            {reviewedRequests.map((request) => (
              <div key={request.id} className="flex flex-col gap-4 rounded-lg border p-4 md:flex-row md:items-center md:justify-between">
                <div className="space-y-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium">Trip {request.tripId}</p>
                    <StatusBadge status={request.status} />
                  </div>
                  <div className="flex flex-wrap gap-x-6 gap-y-2 text-sm text-muted-foreground">
                    <span>Owner {truncateId(request.ownerId)}</span>
                    <span>Submitted {formatDateTime(request.createdAt)}</span>
                    {request.verifiedAt && <span>Reviewed {formatDateTime(request.verifiedAt)}</span>}
                    {request.creditIssuance && (
                      <span>
                        Credits {Number(request.creditIssuance.creditsRounded).toFixed(2)} (CO₂ {Number(request.creditIssuance.co2ReducedKg).toFixed(2)} kg)
                      </span>
                    )}
                  </div>
                  {request.notes && (
                    <p className="text-xs text-destructive">Reason: {request.notes}</p>
                  )}
                </div>

                <div className="flex flex-col gap-2 md:flex-row">
                  <Dialog>
                    <DialogTrigger asChild>
                      <Button variant="outline" size="sm" className="justify-start">
                        <FileText className="mr-2 h-4 w-4" /> Details
                      </Button>
                    </DialogTrigger>
                    <DialogContent className="max-w-xl">
                      <DialogHeader>
                        <DialogTitle>Verification summary</DialogTitle>
                        <DialogDescription>Carbon audit metadata for request {truncateId(request.id)}</DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        <Section title="Status">
                          <StatusBadge status={request.status} detailed />
                          {request.verifierId && (
                            <p className="text-sm text-muted-foreground">Verifier {truncateId(request.verifierId)}</p>
                          )}
                        </Section>
                        <Section title="Submission">
                          <DetailRow label="Trip ID">{request.tripId}</DetailRow>
                          <DetailRow label="Owner">{request.ownerId}</DetailRow>
                          <DetailRow label="Checksum">{request.checksum}</DetailRow>
                          <DetailRow label="Created">{formatDateTime(request.createdAt)}</DetailRow>
                          {request.verifiedAt && <DetailRow label="Reviewed">{formatDateTime(request.verifiedAt)}</DetailRow>}
                        </Section>
                        <Section title="Metrics">
                          <DetailRow label="Distance (km)">{request.distanceKm.toFixed(2)}</DetailRow>
                          <DetailRow label="Energy (kWh)">{request.energyKwh.toFixed(2)}</DetailRow>
                        </Section>
                        {request.creditIssuance && (
                          <Section title="Credit issuance">
                            <DetailRow label="Issuance ID">{request.creditIssuance.id}</DetailRow>
                            <DetailRow label="Issued at">{formatDateTime(request.creditIssuance.createdAt)}</DetailRow>
                            <DetailRow label="Credits rounded">{Number(request.creditIssuance.creditsRounded).toFixed(2)}</DetailRow>
                            <DetailRow label="Credits raw">{Number(request.creditIssuance.creditsRaw).toFixed(4)}</DetailRow>
                            <DetailRow label="CO₂ reduced (kg)">{Number(request.creditIssuance.co2ReducedKg).toFixed(3)}</DetailRow>
                            <DetailRow label="Idempotency key">{request.creditIssuance.idempotencyKey}</DetailRow>
                            {request.creditIssuance.correlationId && (
                              <DetailRow label="Correlation ID">{request.creditIssuance.correlationId}</DetailRow>
                            )}
                          </Section>
                        )}
                        {request.notes && (
                          <Section title="Notes">
                            <p className="text-sm text-destructive">{request.notes}</p>
                          </Section>
                        )}
                      </div>
                    </DialogContent>
                  </Dialog>
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => handleDownloadPdf(request)}
                    disabled={state.downloadingId === request.id}
                  >
                    {state.downloadingId === request.id ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Preparing…
                      </>
                    ) : (
                      <>
                        <Download className="mr-2 h-4 w-4" /> PDF Report
                      </>
                    )}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

function StatusBadge({ status, detailed }: { status: VerificationRequest["status"]; detailed?: boolean }) {
  const isApproved = status === "APPROVED"
  const variant = isApproved ? "default" : "destructive"
  const icon = isApproved ? <CheckCircle2 className="mr-1 h-3 w-3" /> : <XCircle className="mr-1 h-3 w-3" />
  const label = isApproved ? "Approved" : "Rejected"

  return (
    <Badge
      variant={variant}
      className={
        isApproved
          ? "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
          : "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      }
    >
      {icon}
      {detailed ? label : status.toLowerCase()}
    </Badge>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <p className="text-sm font-semibold text-foreground">{title}</p>
      <div className="space-y-1 text-sm text-muted-foreground">{children}</div>
    </div>
  )
}

function DetailRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs uppercase tracking-wide text-muted-foreground/80">{label}</span>
      <span className="font-medium text-foreground">{children}</span>
    </div>
  )
}

function truncateId(value: string | undefined | null, length = 6) {
  if (!value) return "—"
  return value.length <= length * 2 ? value : `${value.slice(0, length)}…${value.slice(-length)}`
}

function formatDateTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}
