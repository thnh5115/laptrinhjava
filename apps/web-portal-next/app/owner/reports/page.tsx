"use client";

import { OwnerReportsView } from "@/components/owner/reports-view";
import { Home, Upload, History, Wallet, ShoppingCart, FileText } from "lucide-react";
import {DashboardLayout} from "@/components/layout/dashboard-layout.tsx";

const navigation = [
    { name: "Dashboard", href: "/owner/dashboard", icon: Home }, // ĐÚNG (khớp với folder app/owner)
    { name: "Upload Journey", href: "/owner/upload", icon: Upload },
    { name: "Journey History", href: "/owner/history", icon: History },
    { name: "My Credits", href: "/owner/credits", icon: ShoppingCart },
    { name: "Wallet", href: "/owner/wallet", icon: Wallet },
    {name: "Reports", href: "/owner/reports", icon: FileText,}
];


export default function OwnerReportsPage() {
    return (
        <DashboardLayout navigation={navigation}>
            <div className="max-w-3xl">
                <OwnerReportsView />
            </div>
        </DashboardLayout>
    );

}

