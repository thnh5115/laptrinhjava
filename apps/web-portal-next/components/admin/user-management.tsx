"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Search, MoreVertical, UserCheck, UserX, Edit, Loader2, ChevronLeft, ChevronRight } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { getUsers, getUser, updateUserStatus, updateUserRole, type UserSummary, type UserFilterParams, UserRole, UserStatus } from "@/lib/api/admin-users"

export function UserManagement() {
  const { toast } = useToast()
  
  // State
  const [users, setUsers] = useState<UserSummary[]>([])
  const [loading, setLoading] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedUser, setSelectedUser] = useState<UserSummary | null>(null)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isUpdating, setIsUpdating] = useState(false)
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0)
  const [pageSize] = useState(20)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  
  // Filters
  const [roleFilter, setRoleFilter] = useState<string | undefined>(undefined)
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined)

  // Load users on mount and when filters/pagination change
  useEffect(() => {
    loadUsers()
  }, [currentPage, roleFilter, statusFilter])

  /**
   * Load users from API
   */
  const loadUsers = async () => {
    setLoading(true)
    try {
      const params: UserFilterParams = {
        page: currentPage,
        size: pageSize,
        sort: "createdAt,desc",
      }
      
      if (searchQuery.trim()) params.keyword = searchQuery.trim()
      if (roleFilter && roleFilter !== '__all__') params.role = roleFilter
      if (statusFilter && statusFilter !== '__all__') params.status = statusFilter

      const response = await getUsers(params)
      setUsers(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (error: any) {
      toast({
        title: "Failed to load users",
        description: error.message || "An error occurred while loading users",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  /**
   * Handle search
   */
  const handleSearch = () => {
    setCurrentPage(0) // Reset to first page
    loadUsers()
  }

  /**
   * Handle role update
   */
  const handleRoleUpdate = async (userId: number, newRole: UserRole) => {
    setIsUpdating(true)
    try {
      await updateUserRole(userId, newRole)
      toast({
        title: "Role updated",
        description: "User role has been updated successfully",
      })
      setIsEditDialogOpen(false)
      loadUsers() // Refresh list
    } catch (error: any) {
      toast({
        title: "Failed to update role",
        description: error.message || "An error occurred",
        variant: "destructive",
      })
    } finally {
      setIsUpdating(false)
    }
  }

  /**
   * Handle status update
   */
  const handleStatusUpdate = async (userId: number, newStatus: UserStatus) => {
    setIsUpdating(true)
    try {
      await updateUserStatus(userId, newStatus)
      toast({
        title: "Status updated",
        description: `User has been ${newStatus.toLowerCase()}`,
      })
      loadUsers() // Refresh list
    } catch (error: any) {
      toast({
        title: "Failed to update status",
        description: error.message || "An error occurred",
        variant: "destructive",
      })
    } finally {
      setIsUpdating(false)
    }
  }

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
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Search Users</CardTitle>
          <CardDescription>Find and manage platform users</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by name or email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                className="pl-10"
              />
            </div>
            <Select 
              value={roleFilter ?? '__all__'} 
              onValueChange={(v) => setRoleFilter(v === '__all__' ? undefined : v)}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by role" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">All Roles</SelectItem>
                <SelectItem value="ADMIN">Admin</SelectItem>
                <SelectItem value="AUDITOR">Auditor</SelectItem>
                <SelectItem value="BUYER">Buyer</SelectItem>
                <SelectItem value="EV_OWNER">EV Owner</SelectItem>
              </SelectContent>
            </Select>
            <Select 
              value={statusFilter ?? '__all__'} 
              onValueChange={(v) => setStatusFilter(v === '__all__' ? undefined : v)}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">All Status</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="SUSPENDED">Suspended</SelectItem>
                <SelectItem value="BANNED">Banned</SelectItem>
              </SelectContent>
            </Select>
            <Button onClick={handleSearch} disabled={loading}>
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : "Search"}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>All Users ({totalElements})</CardTitle>
              <CardDescription>
                Page {currentPage + 1} of {totalPages || 1}
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0 || loading}
              >
                <ChevronLeft className="h-4 w-4" />
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => p + 1)}
                disabled={currentPage >= totalPages - 1 || loading}
              >
                Next
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : users.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              No users found
            </div>
          ) : (
            <div className="space-y-4">
              {users.map((user) => (
                <div key={user.id} className="flex items-center justify-between border rounded-lg p-4">
                  <div className="flex items-center gap-4">
                    <Avatar>
                      <AvatarImage src="/placeholder.svg" alt={user.fullName} />
                      <AvatarFallback>{user.fullName.charAt(0)}</AvatarFallback>
                    </Avatar>
                    <div className="space-y-1">
                      <p className="font-medium">{user.fullName}</p>
                      <p className="text-sm text-muted-foreground">{user.email}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <Badge className={getRoleBadgeColor(user.role)}>{getRoleLabel(user.role)}</Badge>
                    <Badge className={getStatusBadgeColor(user.status)}>{user.status}</Badge>

                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" disabled={isUpdating}>
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => {
                            setSelectedUser(user)
                            setIsEditDialogOpen(true)
                          }}
                        >
                          <Edit className="mr-2 h-4 w-4" />
                          Edit Role
                        </DropdownMenuItem>
                        {user.status !== "ACTIVE" && (
                          <DropdownMenuItem onClick={() => handleStatusUpdate(user.id, UserStatus.ACTIVE)}>
                            <UserCheck className="mr-2 h-4 w-4" />
                            Activate
                          </DropdownMenuItem>
                        )}
                        {user.status === "ACTIVE" && (
                          <DropdownMenuItem onClick={() => handleStatusUpdate(user.id, UserStatus.SUSPENDED)}>
                            <UserX className="mr-2 h-4 w-4" />
                            Suspend
                          </DropdownMenuItem>
                        )}
                        {user.status !== "BANNED" && (
                          <DropdownMenuItem
                            className="text-destructive"
                            onClick={() => handleStatusUpdate(user.id, UserStatus.BANNED)}
                          >
                            <UserX className="mr-2 h-4 w-4" />
                            Ban User
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Edit Role Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit User Role</DialogTitle>
            <DialogDescription>Update user role and permissions</DialogDescription>
          </DialogHeader>
          {selectedUser && (
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label>Name</Label>
                <Input value={selectedUser.fullName} disabled />
              </div>
              <div className="space-y-2">
                <Label>Email</Label>
                <Input value={selectedUser.email} disabled />
              </div>
              <div className="space-y-2">
                <Label htmlFor="role">Role</Label>
                <Select
                  defaultValue={selectedUser.role}
                  onValueChange={(value) =>
                    setSelectedUser({ ...selectedUser, role: value as UserRole })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                    <SelectItem value="AUDITOR">Auditor</SelectItem>
                    <SelectItem value="BUYER">Buyer</SelectItem>
                    <SelectItem value="EV_OWNER">EV Owner</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)} disabled={isUpdating}>
              Cancel
            </Button>
            <Button
              onClick={() => selectedUser && handleRoleUpdate(selectedUser.id, selectedUser.role)}
              disabled={isUpdating}
              className="bg-emerald-600 hover:bg-emerald-700"
            >
              {isUpdating ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Updating...
                </>
              ) : (
                "Save Changes"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
