"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Input } from "../ui/input"
import { Label } from "../ui/label"
import { Switch } from "../ui/switch"
import { Save } from "lucide-react"

export function PlatformSettings() {
  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>General Settings</CardTitle>
          <CardDescription>Configure basic platform parameters</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="platformName">Platform Name</Label>
            <Input id="platformName" defaultValue="CarbonCredit Marketplace" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="supportEmail">Support Email</Label>
            <Input id="supportEmail" type="email" defaultValue="support@carboncredit.com" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="platformFee">Platform Fee (%)</Label>
            <Input id="platformFee" type="number" step="0.1" defaultValue="5.0" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Verification Settings</CardTitle>
          <CardDescription>Configure carbon credit verification parameters</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="minCredits">Minimum Credits per Journey (tCO₂)</Label>
            <Input id="minCredits" type="number" step="0.1" defaultValue="1.0" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="maxCredits">Maximum Credits per Journey (tCO₂)</Label>
            <Input id="maxCredits" type="number" step="0.1" defaultValue="100.0" />
          </div>
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Auto-verification</Label>
              <p className="text-sm text-muted-foreground">Automatically verify journeys under threshold</p>
            </div>
            <Switch />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Marketplace Settings</CardTitle>
          <CardDescription>Configure marketplace behavior</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="minPrice">Minimum Price per Credit ($)</Label>
            <Input id="minPrice" type="number" step="0.01" defaultValue="10.00" />
          </div>
          <div className="space-y-2">
            <Label htmlFor="maxPrice">Maximum Price per Credit ($)</Label>
            <Input id="maxPrice" type="number" step="0.01" defaultValue="50.00" />
          </div>
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Enable Marketplace</Label>
              <p className="text-sm text-muted-foreground">Allow credit trading on the platform</p>
            </div>
            <Switch defaultChecked />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Notification Settings</CardTitle>
          <CardDescription>Configure system notifications</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Email Notifications</Label>
              <p className="text-sm text-muted-foreground">Send email updates to users</p>
            </div>
            <Switch defaultChecked />
          </div>
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Admin Alerts</Label>
              <p className="text-sm text-muted-foreground">Receive alerts for critical events</p>
            </div>
            <Switch defaultChecked />
          </div>
        </CardContent>
      </Card>

      <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
        <Save className="mr-2 h-4 w-4" />
        Save All Settings
      </Button>
    </div>
  )
}
