"use client";

import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { Home, Upload, History, Wallet, ShoppingCart,FileText } from "lucide-react";
import { OwnerDashboardOverview } from "@/components/owner/dashboard-overview";

const navigation = [
  { name: "Dashboard", href: "/owner/dashboard", icon: Home }, // ĐÚNG (khớp với folder app/owner)
  { name: "Upload Journey", href: "/owner/upload", icon: Upload },
  { name: "Journey History", href: "/owner/history", icon: History },
  { name: "My Credits", href: "/owner/credits", icon: ShoppingCart },
  { name: "Wallet", href: "/owner/wallet", icon: Wallet },
  {name: "Reports", href: "/owner/reports", icon: FileText,}
];

export default function EvOwnerDashboardPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <OwnerDashboardOverview />
    </DashboardLayout>
  );
}
