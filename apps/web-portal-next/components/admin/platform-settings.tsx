"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { RefreshCw, Pencil } from "lucide-react"
import { getSettings, updateSetting, type SettingResponse, type UpdateSettingRequest } from "@/lib/api/admin-settings"
import { useToast } from "@/hooks/use-toast"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

export function PlatformSettings() {
  const [settings, setSettings] = useState<SettingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [selectedSetting, setSelectedSetting] = useState<SettingResponse | null>(null)
  const [newValue, setNewValue] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { toast } = useToast()

  // Fetch settings on mount
  const fetchSettings = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getSettings()
      setSettings(data)
    } catch (err: any) {
      setError(err.message || "Failed to load settings")
      toast({
        title: "Error",
        description: err.message || "Failed to load settings",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSettings()
  }, [])

  // Open edit dialog
  const handleEdit = (setting: SettingResponse) => {
    setSelectedSetting(setting)
    setNewValue(setting.value)
    setEditDialogOpen(true)
  }

  // Submit update
  const handleSubmit = async () => {
    if (!selectedSetting) return

    // Validate value
    if (!newValue.trim()) {
      toast({
        title: "Validation Error",
        description: "Value cannot be empty",
        variant: "destructive",
      })
      return
    }

    // Validate numeric fields (rate, fee, percent, price, etc.)
    const numericKeys = ["rate", "fee", "percent", "price", "min", "max", "duration"]
    const isNumericField = numericKeys.some(key => 
      selectedSetting.keyName.toLowerCase().includes(key)
    )

    if (isNumericField) {
      const numValue = parseFloat(newValue)
      if (isNaN(numValue) || numValue < 0) {
        toast({
          title: "Validation Error",
          description: "Numeric value must be greater than or equal to 0",
          variant: "destructive",
        })
        return
      }
    }

    try {
      setIsSubmitting(true)
      const payload: UpdateSettingRequest = { value: newValue }
      await updateSetting(selectedSetting.id, payload)
      
      toast({
        title: "Success",
        description: `Setting "${selectedSetting.keyName}" updated successfully`,
      })

      // Refresh settings and close dialog
      await fetchSettings()
      setEditDialogOpen(false)
      setSelectedSetting(null)
      setNewValue("")
    } catch (err: any) {
      toast({
        title: "Error",
        description: err.message || "Failed to update setting",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  // Format date
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Platform Settings</CardTitle>
              <CardDescription>
                Manage system-wide configuration and parameters
              </CardDescription>
            </div>
            <Button
              onClick={fetchSettings}
              variant="outline"
              size="sm"
              disabled={loading}
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
              Refresh
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="space-y-2">
              {[...Array(6)].map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-8 text-destructive">
              <p>{error}</p>
              <Button onClick={fetchSettings} variant="outline" className="mt-4">
                Retry
              </Button>
            </div>
          ) : settings.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No settings found
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Key</TableHead>
                  <TableHead>Value</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Updated At</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {settings.map((setting) => (
                  <TableRow key={setting.id}>
                    <TableCell className="font-mono text-sm">
                      {setting.keyName}
                    </TableCell>
                    <TableCell className="font-medium">
                      {setting.value}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {setting.description}
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {formatDate(setting.updatedAt)}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        onClick={() => handleEdit(setting)}
                        variant="ghost"
                        size="sm"
                      >
                        <Pencil className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Setting</DialogTitle>
            <DialogDescription>
              Update the value for {selectedSetting?.keyName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="keyName">Key</Label>
              <Input
                id="keyName"
                value={selectedSetting?.keyName || ""}
                disabled
                className="font-mono"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Input
                id="description"
                value={selectedSetting?.description || ""}
                disabled
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="value">Value *</Label>
              <Input
                id="value"
                value={newValue}
                onChange={(e) => setNewValue(e.target.value)}
                placeholder="Enter new value"
                disabled={isSubmitting}
              />
              <p className="text-xs text-muted-foreground">
                {selectedSetting?.keyName.toLowerCase().includes("rate") ||
                selectedSetting?.keyName.toLowerCase().includes("fee") ||
                selectedSetting?.keyName.toLowerCase().includes("percent") ||
                selectedSetting?.keyName.toLowerCase().includes("price") ||
                selectedSetting?.keyName.toLowerCase().includes("min") ||
                selectedSetting?.keyName.toLowerCase().includes("max")
                  ? "Must be a number ≥ 0"
                  : "Required field"}
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setEditDialogOpen(false)
                setSelectedSetting(null)
                setNewValue("")
              }}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : "Save Changes"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
