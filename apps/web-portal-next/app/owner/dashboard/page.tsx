"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Upload, History, Wallet, ShoppingCart } from "lucide-react"
import { OwnerDashboardOverview } from "@/components/owner/dashboard-overview"

const navigation = [
  { name: "Dashboard", href: "/ev-owner/dashboard", icon: Home },
  { name: "Upload Journey", href: "/ev-owner/upload", icon: Upload },
  { name: "Journey History", href: "/ev-owner/history", icon: History },
  { name: "My Credits", href: "/ev-owner/credits", icon: ShoppingCart },
  { name: "Wallet", href: "/ev-owner/wallet", icon: Wallet },
]

export default function EvOwnerDashboardPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <OwnerDashboardOverview />
    </DashboardLayout>
  )
}
