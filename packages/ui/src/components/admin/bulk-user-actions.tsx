"use client"
import { Button } from "../ui/button"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "../ui/dropdown-menu"
import { ChevronDown, Download, UserX, UserCheck } from "lucide-react"
import { useToast } from "../../hooks/use-toast"

interface BulkUserActionsProps {
  selectedUsers: string[]
  onClearSelection: () => void
}

export function BulkUserActions({ selectedUsers, onClearSelection }: BulkUserActionsProps) {
  const { toast } = useToast()

  const handleBulkAction = (action: string) => {
    toast({
      title: "Bulk Action Applied",
      description: `${action} applied to ${selectedUsers.length} user(s).`,
    })
    onClearSelection()
  }

  if (selectedUsers.length === 0) return null

  return (
    <div className="flex items-center gap-2 p-3 border rounded-lg bg-muted/50">
      <span className="text-sm font-medium">{selectedUsers.length} user(s) selected</span>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button size="sm" variant="outline">
            Bulk Actions
            <ChevronDown className="ml-2 h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem onClick={() => handleBulkAction("Suspend")}>
            <UserX className="mr-2 h-4 w-4" />
            Suspend Users
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => handleBulkAction("Activate")}>
            <UserCheck className="mr-2 h-4 w-4" />
            Activate Users
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => handleBulkAction("Export")}>
            <Download className="mr-2 h-4 w-4" />
            Export Data
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
      <Button size="sm" variant="ghost" onClick={onClearSelection}>
        Clear Selection
      </Button>
    </div>
  )
}
