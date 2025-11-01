"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Users, BarChart3, Settings, Activity } from "lucide-react"
import { PlatformSettings } from "@/components/admin/platform-settings"

const navigation = [
  { name: "Dashboard", href: "/admin/dashboard", icon: Home },
  { name: "User Management", href: "/admin/users", icon: Users },
  { name: "Platform Analytics", href: "/admin/analytics", icon: BarChart3 },
  { name: "Transactions", href: "/admin/transactions", icon: Activity },
  { name: "Settings", href: "/admin/settings", icon: Settings },
]

export default function SettingsPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Platform Settings</h1>
          <p className="text-muted-foreground">Configure system settings and preferences</p>
        </div>
        <PlatformSettings />
      </div>
    </DashboardLayout>
  )
}
