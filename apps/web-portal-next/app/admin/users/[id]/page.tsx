"use client"

import { use } from "react"
import { useRouter } from "next/navigation"
import { DashboardLayout } from "@/components/layout/dashboard-layout"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { ArrowLeft, Loader2, Mail, User as UserIcon, Shield, Activity } from "lucide-react"
import { Home, Users, BarChart3, Settings, Activity as ActivityIcon } from "lucide-react"
import { useUser } from "@/hooks/use-users"
import { UserRoleModal } from "@/components/admin/users/UserRoleModal"
import { UserStatusToggle } from "@/components/admin/users/UserStatusToggle"
import { useState } from "react"
import type { User } from "@repo/ts-sdk/models"

const navigation = [
  { name: "Dashboard", href: "/admin/dashboard", icon: Home },
  { name: "User Management", href: "/admin/users", icon: Users },
  { name: "Platform Analytics", href: "/admin/analytics", icon: BarChart3 },
  { name: "Transactions", href: "/admin/transactions", icon: ActivityIcon },
  { name: "Settings", href: "/admin/settings", icon: Settings },
]

interface PageProps {
  params: Promise<{ id: string }>
}

export default function UserDetailPage({ params }: PageProps) {
  const { id } = use(params)
  const router = useRouter()
  const { user, loading, error, refresh } = useUser(id)
  const [roleModalOpen, setRoleModalOpen] = useState(false)

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case "ADMIN":
        return "bg-purple-100 text-purple-900 dark:bg-purple-900 dark:text-purple-100"
      case "AUDITOR":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      case "BUYER":
        return "bg-teal-100 text-teal-900 dark:bg-teal-900 dark:text-teal-100"
      case "EV_OWNER":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      default:
        return ""
    }
  }

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "bg-green-100 text-green-900 dark:bg-green-900 dark:text-green-100"
      case "SUSPENDED":
        return "bg-yellow-100 text-yellow-900 dark:bg-yellow-900 dark:text-yellow-100"
      case "BANNED":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      default:
        return ""
    }
  }

  const getRoleLabel = (role: string) => {
    return role === "EV_OWNER" ? "EV Owner" : role.charAt(0) + role.slice(1).toLowerCase()
  }

  return (
    <DashboardLayout navigation={navigation}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => router.push("/admin/users")}
            >
              <ArrowLeft className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-3xl font-bold tracking-tight">User Details</h1>
              <p className="text-muted-foreground">View and manage user information</p>
            </div>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        )}

        {/* Error State */}
        {error && (
          <Card>
            <CardContent className="py-12 text-center">
              <p className="text-destructive">{error.message}</p>
              <Button onClick={refresh} variant="outline" className="mt-4">
                Try Again
              </Button>
            </CardContent>
          </Card>
        )}

        {/* User Details */}
        {!loading && !error && user && (
          <>
            {/* Profile Card */}
            <Card>
              <CardHeader>
                <CardTitle>Profile Information</CardTitle>
                <CardDescription>Personal details and account status</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="flex items-start gap-6">
                  <Avatar className="h-24 w-24">
                    <AvatarImage src="/placeholder.svg" alt={user.fullName} />
                    <AvatarFallback className="text-2xl">
                      {user.fullName?.charAt(0) || user.email.charAt(0)}
                    </AvatarFallback>
                  </Avatar>
                  
                  <div className="flex-1 space-y-4">
                    <div>
                      <h3 className="text-2xl font-semibold">{user.fullName || 'N/A'}</h3>
                      <p className="text-muted-foreground">{user.email}</p>
                    </div>

                    <div className="flex items-center gap-3">
                      <Badge className={getRoleBadgeColor(user.role)}>
                        {getRoleLabel(user.role)}
                      </Badge>
                      <Badge className={getStatusBadgeColor(user.status)}>
                        {user.status}
                      </Badge>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button
                        onClick={() => setRoleModalOpen(true)}
                        variant="outline"
                        size="sm"
                      >
                        <Shield className="mr-2 h-4 w-4" />
                        Change Role
                      </Button>
                      <UserStatusToggle user={user} onSuccess={refresh} />
                    </div>
                  </div>
                </div>

                <Separator />

                <div className="grid gap-6 md:grid-cols-2">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Mail className="h-4 w-4" />
                      Email
                    </div>
                    <p className="font-medium">{user.email}</p>
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <UserIcon className="h-4 w-4" />
                      Full Name
                    </div>
                    <p className="font-medium">{user.fullName || 'N/A'}</p>
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Shield className="h-4 w-4" />
                      Role
                    </div>
                    <p className="font-medium">{getRoleLabel(user.role)}</p>
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Activity className="h-4 w-4" />
                      Status
                    </div>
                    <p className="font-medium">{user.status}</p>
                  </div>

                  {user.createdAt && (
                    <div className="space-y-1">
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        Created At
                      </div>
                      <p className="font-medium">
                        {new Date(user.createdAt).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                        })}
                      </p>
                    </div>
                  )}

                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      User ID
                    </div>
                    <p className="font-medium font-mono text-sm">{user.id}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Activity History - Placeholder */}
            <Card>
              <CardHeader>
                <CardTitle>Recent Activity</CardTitle>
                <CardDescription>User activity and audit trail</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-center text-muted-foreground py-8">
                  Activity tracking coming soon...
                </p>
              </CardContent>
            </Card>
          </>
        )}

        {/* Role Modal */}
        {user && (
          <UserRoleModal
            user={user}
            open={roleModalOpen}
            onOpenChange={setRoleModalOpen}
            onSuccess={refresh}
          />
        )}
      </div>
    </DashboardLayout>
  )
}
