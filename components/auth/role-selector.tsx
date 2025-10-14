"use client"

import { Card } from "@/components/ui/card"
import { Leaf, ShoppingCart, Shield, Settings } from "lucide-react"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"

interface RoleSelectorProps {
  selectedRole: string
  onRoleChange: (role: string) => void
}

const roles = [
  {
    value: "ev-owner",
    label: "EV Owner",
    icon: Leaf,
    description: "Connect your EV, track CO₂ reduction, and sell credits.",
    color: "emerald",
  },
  {
    value: "buyer",
    label: "Buyer",
    icon: ShoppingCart,
    description: "Purchase verified carbon credits and get certification.",
    color: "blue",
  },
  {
    value: "cva",
    label: "CVA",
    icon: Shield,
    description: "Verify and approve emission data and credit issuance.",
    color: "purple",
  },
  {
    value: "admin",
    label: "Admin",
    icon: Settings,
    description: "Manage users, transactions, and platform analytics.",
    color: "orange",
    hidden: true, // Hidden by default
  },
]

export function RoleSelector({ selectedRole, onRoleChange }: RoleSelectorProps) {
  return (
    <TooltipProvider>
      <div className="grid grid-cols-2 gap-3">
        {roles
          .filter((role) => !role.hidden)
          .map((role) => {
            const Icon = role.icon
            const isSelected = selectedRole === role.value
            return (
              <Tooltip key={role.value}>
                <TooltipTrigger asChild>
                  <Card
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      isSelected
                        ? "border-emerald-600 bg-emerald-50 dark:bg-emerald-950/20 ring-2 ring-emerald-600"
                        : "border-border hover:border-emerald-300"
                    }`}
                    onClick={() => onRoleChange(role.value)}
                  >
                    <div className="p-4 flex flex-col items-center text-center gap-2">
                      <div
                        className={`p-3 rounded-lg ${
                          isSelected ? "bg-emerald-600 text-white" : "bg-muted text-muted-foreground"
                        }`}
                      >
                        <Icon className="w-5 h-5" />
                      </div>
                      <div>
                        <p className="font-medium text-sm">{role.label}</p>
                      </div>
                    </div>
                  </Card>
                </TooltipTrigger>
                <TooltipContent side="bottom" className="max-w-xs">
                  <p className="text-sm">{role.description}</p>
                </TooltipContent>
              </Tooltip>
            )
          })}
      </div>
    </TooltipProvider>
  )
}
