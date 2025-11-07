"use client"

import { DisputeList } from "@/components/admin/dispute-list"

export default function AdminDisputesPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dispute Resolution</h1>
        <p className="text-muted-foreground">Manage transaction disputes and user complaints</p>
      </div>
      <DisputeList />
    </div>
  )
}
