/**
 * Centralized Admin Navigation Configuration
 * Single source of truth for admin sidebar menu
 * Used by /app/admin/layout.tsx to render consistent navigation across all admin pages
 */

import { Home, Users, BarChart3, Settings, Activity, Wallet, AlertTriangle, FileText, List, Shield, TrendingUp, Eye } from "lucide-react"
import type { LucideIcon } from "lucide-react"

export interface NavigationItem {
  name: string
  href: string
  icon: LucideIcon
}

/**
 * Admin navigation items - complete menu
 * All admin pages will see this same sidebar
 */
export const adminNavigation: NavigationItem[] = [
  { name: "Dashboard", href: "/admin/dashboard", icon: Home },
  { name: "User Management", href: "/admin/users", icon: Users },
  { name: "Platform Analytics", href: "/admin/analytics", icon: BarChart3 },
  { name: "Transactions", href: "/admin/transactions", icon: Activity },
  { name: "Finance", href: "/admin/finance", icon: Wallet },
  { name: "Disputes", href: "/admin/disputes", icon: AlertTriangle },
  { name: "Listings", href: "/admin/listings", icon: List },
  { name: "Reports", href: "/admin/reports", icon: FileText },
  { name: "Audit Logs", href: "/admin/audit", icon: Shield },
  { name: "Audit Insights", href: "/admin/audit/insights", icon: TrendingUp },
  { name: "Observability", href: "/admin/observability", icon: Eye },
  { name: "Settings", href: "/admin/settings", icon: Settings },
]
