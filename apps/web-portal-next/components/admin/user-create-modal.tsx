"use client"

import { useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { RoleSelector } from "@/components/auth/role-selector"
import { Switch } from "@/components/ui/switch"
import { adminCreateUserSchema, type AdminCreateUserInput, uiRoleToApiRole } from "@/lib/validators/user"
import { createAdminUser } from "@/lib/api/admin-users"

interface UserCreateModalProps {
  open: boolean
  onClose: () => void
  onCreated?: () => void
}

export function UserCreateModal({ open, onClose, onCreated }: UserCreateModalProps) {
  const { toast } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    reset,
    watch,
  } = useForm<AdminCreateUserInput>({
    resolver: zodResolver(adminCreateUserSchema),
    defaultValues: {
      fullName: "",
      email: "",
      password: "",
      role: "ev-owner",
      active: true,
    },
  })

  const selectedRole = watch("role")
  const isActive = watch("active")

  useEffect(() => {
    if (!open) {
      reset({
        fullName: "",
        email: "",
        password: "",
        role: "ev-owner",
        active: true,
      })
    }
  }, [open, reset])

  const onSubmit = async (data: AdminCreateUserInput) => {
    try {
      await createAdminUser({
        email: data.email,
        fullName: data.fullName,
        password: data.password,
        role: uiRoleToApiRole(data.role),
        active: data.active,
      })

      toast({
        title: "User created",
        description: `${data.email} has been added.`,
      })

      onClose()
      onCreated?.()
    } catch (err: any) {
      toast({
        title: "Failed to create user",
        description: err?.message || "Please check the input and try again",
        variant: "destructive",
      })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create New User</DialogTitle>
          <DialogDescription>Add a new user account to the platform</DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="fullName">Full Name</Label>
            <Input
              id="fullName"
              placeholder="Enter full name"
              {...register("fullName")}
              className={errors.fullName ? "border-red-500" : ""}
            />
            {errors.fullName && <p className="text-sm text-red-500">{errors.fullName.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email Address</Label>
            <Input
              id="email"
              type="email"
              placeholder="Enter email address"
              {...register("email")}
              className={errors.email ? "border-red-500" : ""}
            />
            {errors.email && <p className="text-sm text-red-500">{errors.email.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="Enter password"
              {...register("password")}
              className={errors.password ? "border-red-500" : ""}
            />
            {errors.password && <p className="text-sm text-red-500">{errors.password.message}</p>}
          </div>

          <div className="space-y-2">
            <Label>Select Role</Label>
            <RoleSelector
              selectedRole={selectedRole}
              onRoleChange={(role) => setValue("role", role, { shouldValidate: true })}
              showHidden
            />
            {errors.role && <p className="text-sm text-red-500">{errors.role.message}</p>}
          </div>

          <div className="flex items-center justify-between rounded-lg border p-3">
            <div>
              <p className="text-sm font-medium">Activate immediately</p>
              <p className="text-xs text-muted-foreground">If off, user will be created as suspended</p>
            </div>
            <Switch checked={isActive} onCheckedChange={(value) => setValue("active", value)} />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creating..." : "Create User"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
