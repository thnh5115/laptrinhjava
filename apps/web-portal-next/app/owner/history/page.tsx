"use client";

import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { Home, Upload, History, Wallet, ShoppingCart,FileText } from "lucide-react";
import { JourneyHistoryTable } from "@/components/owner/journey-history-table";

const navigation = [
    { name: "Dashboard", href: "/owner/dashboard", icon: Home }, // ĐÚNG (khớp với folder app/owner)
    { name: "Upload Journey", href: "/owner/upload", icon: Upload },
    { name: "Journey History", href: "/owner/history", icon: History },
    { name: "My Credits", href: "/owner/credits", icon: ShoppingCart },
    { name: "Wallet", href: "/owner/wallet", icon: Wallet },
    {name: "Reports", href: "/owner/reports", icon: FileText,}
];

export default function JourneyHistoryPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Journey History</h1>
          <p className="text-muted-foreground">
            View all your submitted EV journeys and their verification status
          </p>
        </div>
        <JourneyHistoryTable />
      </div>
    </DashboardLayout>
  );
}
