"use client";

import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { Home, Upload, History, Wallet, ShoppingCart } from "lucide-react";
import { JourneyUploadForm } from "@/components/owner/journey-upload-form";

const navigation = [
  { name: "Dashboard", href: "/owner/dashboard", icon: Home }, // ĐÚNG (khớp với folder app/owner)
  { name: "Upload Journey", href: "/owner/upload", icon: Upload },
  { name: "Journey History", href: "/owner/history", icon: History },
  { name: "My Credits", href: "/owner/credits", icon: ShoppingCart },
  { name: "Wallet", href: "/owner/wallet", icon: Wallet },
];

export default function UploadJourneyPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div className="max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Upload Journey</h1>
          <p className="text-muted-foreground">
            Submit your EV journey details to generate carbon credits
          </p>
        </div>
        <JourneyUploadForm />
      </div>
    </DashboardLayout>
  );
}
