"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ClipboardCheck, History, BarChart3, FileSearch } from "lucide-react"
import { AnalyticsDashboard } from "@/components/cva/analytics-dashboard"

const navigation = [
  { name: "Dashboard", href: "/cva/dashboard", icon: Home },
  { name: "Pending Reviews", href: "/cva/reviews", icon: ClipboardCheck },
  { name: "Audit History", href: "/cva/history", icon: History },
  { name: "Analytics", href: "/cva/analytics", icon: BarChart3 },
  { name: "Audit Logs", href: "/cva/logs", icon: FileSearch },
]

export default function AnalyticsPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Analytics</h1>
          <p className="text-muted-foreground">Verification performance and platform insights</p>
        </div>
        <AnalyticsDashboard />
      </div>
    </DashboardLayout>
  )
}
