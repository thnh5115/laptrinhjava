"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Users, BarChart3, Settings, Activity, Wallet, AlertTriangle, FileText, List } from "lucide-react"
import { DisputeList } from "@/components/admin/dispute-list"

const navigation = [
  { name: "Dashboard", href: "/admin/dashboard", icon: Home },
  { name: "User Management", href: "/admin/users", icon: Users },
  { name: "Platform Analytics", href: "/admin/analytics", icon: BarChart3 },
  { name: "Transactions", href: "/admin/transactions", icon: Activity },
  { name: "Finance", href: "/admin/finance", icon: Wallet },
  { name: "Disputes", href: "/admin/disputes", icon: AlertTriangle },
  { name: "Listings", href: "/admin/listings", icon: List },
  { name: "Reports", href: "/admin/reports", icon: FileText },
  { name: "Settings", href: "/admin/settings", icon: Settings },
]

export default function AdminDisputesPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Dispute Resolution</h1>
          <p className="text-muted-foreground">Manage transaction disputes and user complaints</p>
        </div>
        <DisputeList />
      </div>
    </DashboardLayout>
  )
}
