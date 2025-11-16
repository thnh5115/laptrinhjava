"use client"

import { PlatformSettings } from "@/components/admin/platform-settings"

export default function SettingsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold tracking-tight">Platform Settings</h1>
        <p className="text-muted-foreground">Configure system settings and preferences</p>
      </div>
      <PlatformSettings />
    </div>
  )
}
