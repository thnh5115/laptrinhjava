"use client"

import { FinanceOverview } from "@/components/admin/finance-overview"
import { PayoutManager } from "@/components/admin/payout-manager"
import { FinancialReports } from "@/components/admin/financial-reports"
import { TransactionManagement } from "@/components/admin/transactions/TransactionManagement"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

export default function AdminFinancePage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Finance Management</h1>
        <p className="text-muted-foreground">Platform wallet, transactions, payouts, and financial reports</p>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="transactions">Transactions</TabsTrigger>
          <TabsTrigger value="payouts">Payouts</TabsTrigger>
          <TabsTrigger value="reports">Reports</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <FinanceOverview />
        </TabsContent>

        <TabsContent value="transactions" className="space-y-4">
          <TransactionManagement />
        </TabsContent>

        <TabsContent value="payouts" className="space-y-4">
          <PayoutManager />
        </TabsContent>

        <TabsContent value="reports" className="space-y-4">
          <FinancialReports />
        </TabsContent>
      </Tabs>
    </div>
  )
}
