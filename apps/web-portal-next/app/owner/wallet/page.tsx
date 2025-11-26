"use client";

import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { Home, Upload, History, Wallet, ShoppingCart,FileText } from "lucide-react";
import { WalletOverview } from "@/components/owner/wallet-overview";

const navigation = [
    { name: "Dashboard", href: "/owner/dashboard", icon: Home }, // ĐÚNG (khớp với folder app/owner)
    { name: "Upload Journey", href: "/owner/upload", icon: Upload },
    { name: "Journey History", href: "/owner/history", icon: History },
    { name: "My Credits", href: "/owner/credits", icon: ShoppingCart },
    { name: "Wallet", href: "/owner/wallet", icon: Wallet },
    {name: "Reports", href: "/owner/reports", icon: FileText,}
];

export default function WalletPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
          <p className="text-muted-foreground">
            Track your earnings and transaction history
          </p>
        </div>
        <WalletOverview />
      </div>
    </DashboardLayout>
  );
}
