"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Users, BarChart3, Settings, Activity, Wallet, AlertTriangle, FileText, List } from "lucide-react"
import { ReportGenerator } from "@/components/admin/report-generator"
import { ReportHistory } from "@/components/admin/report-history"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

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

export default function AdminReportsPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Report Generation</h1>
          <p className="text-muted-foreground">Generate and export platform reports</p>
        </div>

        <Tabs defaultValue="generate" className="space-y-4">
          <TabsList>
            <TabsTrigger value="generate">Generate Report</TabsTrigger>
            <TabsTrigger value="history">Report History</TabsTrigger>
          </TabsList>

          <TabsContent value="generate" className="space-y-4">
            <ReportGenerator />
          </TabsContent>

          <TabsContent value="history" className="space-y-4">
            <ReportHistory />
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  )
}
