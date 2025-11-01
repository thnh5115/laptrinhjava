"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Leaf, TreePine, Car, Home } from "lucide-react"
import { mockJourneys } from "../../../apps/web-portal-next/lib/mock-data"

export function Co2ImpactDashboard() {
  const totalCredits = mockJourneys
    .filter((j) => j.status === "verified")
    .reduce((sum, j) => sum + j.creditsGenerated, 0)

  // Environmental equivalents
  const treesPlanted = Math.round(totalCredits * 45) // 1 tCO2 = ~45 trees
  const carsMilesOffset = Math.round(totalCredits * 2500) // 1 tCO2 = ~2500 car miles
  const homesEnergy = Math.round(totalCredits * 0.12) // 1 tCO2 = ~0.12 homes yearly energy

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Environmental Impact</h2>
        <p className="text-muted-foreground">Total CO₂ offset and environmental equivalents</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total CO₂ Offset</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalCredits.toFixed(1)} tCO₂</div>
            <p className="text-xs text-muted-foreground">Verified carbon credits</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Trees Equivalent</CardTitle>
            <TreePine className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{treesPlanted.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">Trees planted equivalent</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Car Miles Offset</CardTitle>
            <Car className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{carsMilesOffset.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">Miles of car emissions</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Home Energy</CardTitle>
            <Home className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{homesEnergy}</div>
            <p className="text-xs text-muted-foreground">Homes yearly energy</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Impact Breakdown</CardTitle>
          <CardDescription>Understanding your environmental contribution</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-start gap-3 p-4 border rounded-lg">
              <TreePine className="h-5 w-5 text-emerald-600 mt-0.5" />
              <div className="flex-1">
                <p className="font-medium">Forest Conservation</p>
                <p className="text-sm text-muted-foreground">
                  Equivalent to planting {treesPlanted.toLocaleString()} trees that grow for 10 years
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-4 border rounded-lg">
              <Car className="h-5 w-5 text-emerald-600 mt-0.5" />
              <div className="flex-1">
                <p className="font-medium">Transportation Impact</p>
                <p className="text-sm text-muted-foreground">
                  Offset emissions from {carsMilesOffset.toLocaleString()} miles of average car driving
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-4 border rounded-lg">
              <Home className="h-5 w-5 text-emerald-600 mt-0.5" />
              <div className="flex-1">
                <p className="font-medium">Energy Savings</p>
                <p className="text-sm text-muted-foreground">
                  Equivalent to powering {homesEnergy} homes for an entire year
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
