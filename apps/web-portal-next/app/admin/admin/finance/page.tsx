"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Users, BarChart3, Settings, Activity, Wallet, AlertTriangle, FileText, List } from "lucide-react"
import { FinanceOverview } from "@/components/admin/finance-overview"
import { PayoutManager } from "@/components/admin/payout-manager"
import { FinancialReports } from "@/components/admin/financial-reports"
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

export default function AdminFinancePage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Finance Management</h1>
          <p className="text-muted-foreground">Platform wallet, payouts, and financial reports</p>
        </div>

        <Tabs defaultValue="overview" className="space-y-4">
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="payouts">Payouts</TabsTrigger>
            <TabsTrigger value="reports">Reports</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-4">
            <FinanceOverview />
          </TabsContent>

          <TabsContent value="payouts" className="space-y-4">
            <PayoutManager />
          </TabsContent>

          <TabsContent value="reports" className="space-y-4">
            <FinancialReports />
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  )
}
