"use client"

import { ListingModerator } from "@/components/admin/listing-moderator"

export default function AdminListingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Listing Oversight</h1>
        <p className="text-muted-foreground">Monitor and moderate marketplace listings</p>
      </div>
      <ListingModerator />
    </div>
  )
}
