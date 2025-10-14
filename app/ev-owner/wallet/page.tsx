"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, Upload, History, Wallet, ShoppingCart } from "lucide-react"
import { WalletOverview } from "@/components/ev-owner/wallet-overview"

const navigation = [
  { name: "Dashboard", href: "/ev-owner/dashboard", icon: Home },
  { name: "Upload Journey", href: "/ev-owner/upload", icon: Upload },
  { name: "Journey History", href: "/ev-owner/history", icon: History },
  { name: "My Credits", href: "/ev-owner/credits", icon: ShoppingCart },
  { name: "Wallet", href: "/ev-owner/wallet", icon: Wallet },
]

export default function WalletPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
          <p className="text-muted-foreground">Track your earnings and transaction history</p>
        </div>
        <WalletOverview />
      </div>
    </DashboardLayout>
  )
}
