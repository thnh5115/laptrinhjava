"use client"

import { PlatformAnalytics } from "@/components/admin/platform-analytics"

export default function AnalyticsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold tracking-tight">Platform Analytics</h1>
        <p className="text-muted-foreground">Comprehensive platform performance metrics</p>
      </div>
      <PlatformAnalytics />
    </div>
  )
}
