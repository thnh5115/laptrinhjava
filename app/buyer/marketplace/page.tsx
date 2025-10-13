"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ShoppingCart, Briefcase, History, FileText } from "lucide-react"
import { MarketplaceBrowser } from "@/components/buyer/marketplace-browser"

const navigation = [
  { name: "Dashboard", href: "/buyer/dashboard", icon: Home },
  { name: "Marketplace", href: "/buyer/marketplace", icon: ShoppingCart },
  { name: "My Portfolio", href: "/buyer/portfolio", icon: Briefcase },
  { name: "Transactions", href: "/buyer/transactions", icon: History },
  { name: "Certificates", href: "/buyer/certificates", icon: FileText },
]

export default function MarketplacePage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Marketplace</h1>
          <p className="text-muted-foreground">Browse and purchase verified carbon credits from EV owners</p>
        </div>
        <MarketplaceBrowser />
      </div>
    </DashboardLayout>
  )
}
