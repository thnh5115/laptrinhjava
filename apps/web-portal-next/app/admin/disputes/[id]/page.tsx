"use client"

import { useParams, useRouter } from "next/navigation"
import { DisputeDetail } from "@/components/admin/dispute-detail"

/**
 * Dispute Detail Page
 * Reuses DisputeDetail dialog component for consistency
 * Pattern: Similar to transactions/[id] but using Dialog instead of full page
 */
export default function DisputeDetailPage() {
  const params = useParams()
  const router = useRouter()
  const id = parseInt(params.id as string, 10)

  const handleClose = () => {
    router.push("/admin/disputes")
  }

  const handleUpdated = () => {
    // Optionally refresh or navigate back
    router.push("/admin/disputes")
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dispute Detail</h1>
        <p className="text-muted-foreground">View and manage dispute information</p>
      </div>
      
      {/* Reuse DisputeDetail dialog component */}
      <DisputeDetail 
        disputeId={id} 
        onClose={handleClose}
        onUpdated={handleUpdated}
      />
    </div>
  )
}
