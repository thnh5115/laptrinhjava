"use client"

import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Home, ClipboardCheck, History, BarChart3, FileSearch } from "lucide-react"
import { CvaDashboardOverview } from "@/components/cva/dashboard-overview"

const navigation = [
  { name: "Dashboard", href: "/cva/dashboard", icon: Home },
  { name: "Pending Reviews", href: "/cva/reviews", icon: ClipboardCheck },
  { name: "Audit History", href: "/cva/history", icon: History },
  { name: "Analytics", href: "/cva/analytics", icon: BarChart3 },
  { name: "Audit Logs", href: "/cva/logs", icon: FileSearch },
]

export default function CvaDashboardPage() {
  return (
    <DashboardLayout navigation={navigation}>
      <CvaDashboardOverview />
    </DashboardLayout>
  )
}
