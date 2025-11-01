"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ShoppingCart, Briefcase, History, FileText } from "lucide-react"
import { TransactionHistory } from "@/components/buyer/transaction-history"

const navigation = [
  { name: "Dashboard", href: "/buyer/dashboard", icon: Home },
  { name: "Marketplace", href: "/buyer/marketplace", icon: ShoppingCart },
  { name: "My Portfolio", href: "/buyer/portfolio", icon: Briefcase },
  { name: "Transactions", href: "/buyer/transactions", icon: History },
  { name: "Certificates", href: "/buyer/certificates", icon: FileText },
]

export default function TransactionsPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Transaction History</h1>
          <p className="text-muted-foreground">View all your carbon credit purchases</p>
        </div>
        <TransactionHistory />
      </div>
    </DashboardLayout>
  )
}
