"use client"

/**
 * Admin Observability Page
 * Features: Spring Boot Actuator health status monitoring
 * Reuses: Card, Table, Badge, Skeleton from shadcn/ui
 */

import { useState, useEffect } from "react"
import { RefreshCw, CheckCircle2, XCircle, AlertCircle } from "lucide-react"
import { fetchHealth, type HealthStatus } from "@/lib/api/actuator"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { useToast } from "@/hooks/use-toast"

export default function ObservabilityPage() {
  const { toast } = useToast()

  // State
  const [health, setHealth] = useState<HealthStatus | null>(null)
  const [loading, setLoading] = useState(true)

  // Fetch data
  const fetchData = async () => {
    try {
      setLoading(true)

      // Fetch health (public endpoint)
      const healthData = await fetchHealth()
      setHealth(healthData)
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to load health status",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Get health status icon
  const getHealthIcon = (status: string) => {
    switch (status) {
      case "UP":
        return <CheckCircle2 className="h-5 w-5 text-green-500" />
      case "DOWN":
        return <XCircle className="h-5 w-5 text-red-500" />
      default:
        return <AlertCircle className="h-5 w-5 text-yellow-500" />
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">System Observability</h1>
          <p className="text-muted-foreground">
            Monitor application health and component statuses
          </p>
        </div>
        <Button onClick={fetchData} variant="outline" size="sm" disabled={loading}>
          <RefreshCw className={`mr-2 h-4 w-4 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      {/* Health Status Card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Overall Health Status
            {health && getHealthIcon(health.status)}
          </CardTitle>
          <CardDescription>
            Application health and component statuses
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <Skeleton className="h-24 w-full" />
          ) : health ? (
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border rounded-lg">
                <div>
                  <div className="font-medium">Application Status</div>
                  <div className="text-sm text-muted-foreground">
                    Overall system health
                  </div>
                </div>
                <Badge
                  variant={health.status === "UP" ? "default" : "destructive"}
                  className="text-lg px-4 py-2"
                >
                  {health.status}
                </Badge>
              </div>

              {health.components && (
                <div>
                  <h3 className="font-medium mb-3">Components</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {Object.entries(health.components).map(([name, component]) => (
                      <div
                        key={name}
                        className="flex items-center justify-between p-3 border rounded-lg"
                      >
                        <div className="flex items-center gap-2">
                          {getHealthIcon(component.status)}
                          <span className="font-medium capitalize">{name}</span>
                        </div>
                        <Badge variant="outline">{component.status}</Badge>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground">
              Failed to load health status
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
