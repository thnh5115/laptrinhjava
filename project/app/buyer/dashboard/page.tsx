"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ShoppingCart, Briefcase, History, FileText } from "lucide-react"
import { BuyerDashboardOverview } from "@/components/buyer/dashboard-overview"

const navigation = [
  { name: "Dashboard", href: "/buyer/dashboard", icon: Home },
  { name: "Marketplace", href: "/buyer/marketplace", icon: ShoppingCart },
  { name: "My Portfolio", href: "/buyer/portfolio", icon: Briefcase },
  { name: "Transactions", href: "/buyer/transactions", icon: History },
  { name: "Certificates", href: "/buyer/certificates", icon: FileText },
]

export default function BuyerDashboardPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <BuyerDashboardOverview />
    </DashboardLayout>
  )
}
