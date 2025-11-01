"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Users, BarChart3, Settings, Activity } from "lucide-react"
import { AdminDashboardOverview } from "@/components/admin/dashboard-overview"

const navigation = [
  { name: "Dashboard", href: "/admin/dashboard", icon: Home },
  { name: "User Management", href: "/admin/users", icon: Users },
  { name: "Platform Analytics", href: "/admin/analytics", icon: BarChart3 },
  { name: "Transactions", href: "/admin/transactions", icon: Activity },
  { name: "Settings", href: "/admin/settings", icon: Settings },
]

export default function AdminDashboardPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <AdminDashboardOverview />
    </DashboardLayout>
  )
}
