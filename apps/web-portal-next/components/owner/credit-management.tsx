"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { mockCredits, mockJourneys } from "@/lib/mock-data"
import { DollarSign, Tag } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog"

export function CreditManagement() {
  const userId = "1"
  const userCredits = mockCredits.filter((c) => c.ownerId === userId)
  const [listingPrice, setListingPrice] = useState("25")
  const [isListing, setIsListing] = useState(false)

  const handleListCredit = async () => {
    setIsListing(true)
    await new Promise((resolve) => setTimeout(resolve, 1000))
    setIsListing(false)
  }

  const availableCredits = userCredits.filter((c) => c.status === "available")
  const soldCredits = userCredits.filter((c) => c.status === "sold")

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Available Credits</CardTitle>
            <Tag className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {availableCredits.reduce((sum, c) => sum + c.amount, 0).toFixed(1)} tCO2
            </div>
            <p className="text-xs text-muted-foreground">{availableCredits.length} listings</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Sold Credits</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {soldCredits.reduce((sum, c) => sum + c.amount, 0).toFixed(1)} tCO2
            </div>
            <p className="text-xs text-muted-foreground">
              ${soldCredits.reduce((sum, c) => sum + c.amount * c.pricePerCredit, 0).toFixed(2)} earned
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Price</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">$25.00</div>
            <p className="text-xs text-muted-foreground">Per tCO2</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Available Credits</CardTitle>
          <CardDescription>Credits currently listed for sale</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {availableCredits.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">No credits listed yet</p>
            ) : (
              availableCredits.map((credit) => {
                const journey = mockJourneys.find((j) => j.id === credit.journeyId)
                return (
                  <div key={credit.id} className="flex items-center justify-between border rounded-lg p-4">
                    <div className="space-y-1">
                      <p className="font-medium">{credit.amount.toFixed(1)} tCO2</p>
                      {journey && (
                        <p className="text-sm text-muted-foreground">
                          From: {journey.startLocation} ? {journey.endLocation}
                        </p>
                      )}
                      <p className="text-xs text-muted-foreground">
                        Listed {new Date(credit.listedAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-lg font-bold text-emerald-600">${credit.pricePerCredit}/tCO2</p>
                      <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                        Available
                      </Badge>
                    </div>
                  </div>
                )
              })
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>List New Credits</CardTitle>
          <CardDescription>Set a price and list your verified credits for sale</CardDescription>
        </CardHeader>
        <CardContent>
          <Dialog>
            <DialogTrigger asChild>
              <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                <Tag className="mr-2 h-4 w-4" />
                List Credits for Sale
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>List Credits</DialogTitle>
                <DialogDescription>Set your price per credit (tCO2) to list on the marketplace</DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="price">Price per Credit (USD)</Label>
                  <Input
                    id="price"
                    type="number"
                    step="0.01"
                    value={listingPrice}
                    onChange={(e) => setListingPrice(e.target.value)}
                    placeholder="25.00"
                  />
                  <p className="text-xs text-muted-foreground">Market average: $25.00 per tCO2</p>
                </div>
                <div className="p-4 bg-muted rounded-lg">
                  <p className="text-sm font-medium mb-2">Available to List:</p>
                  <p className="text-2xl font-bold text-emerald-600">15.2 tCO2</p>
                  <p className="text-xs text-muted-foreground mt-1">From verified journeys</p>
                </div>
              </div>
              <DialogFooter>
                <Button
                  onClick={handleListCredit}
                  disabled={isListing}
                  className="w-full bg-emerald-600 hover:bg-emerald-700"
                >
                  {isListing ? "Listing..." : "List Credits"}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>
    </div>
  )
}
