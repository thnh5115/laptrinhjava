"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ShoppingCart, Briefcase, History, FileText } from "lucide-react"
import { CertificateManager } from "@/components/buyer/certificate-manager"

const navigation = [
  { name: "Dashboard", href: "/buyer/dashboard", icon: Home },
  { name: "Marketplace", href: "/buyer/marketplace", icon: ShoppingCart },
  { name: "My Portfolio", href: "/buyer/portfolio", icon: Briefcase },
  { name: "Transactions", href: "/buyer/transactions", icon: History },
  { name: "Certificates", href: "/buyer/certificates", icon: FileText },
]

export default function CertificatesPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <div>
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight">Certificates</h1>
          <p className="text-muted-foreground">Download and manage your carbon offset certificates</p>
        </div>
        <CertificateManager />
      </div>
    </DashboardLayout>
  )
}
