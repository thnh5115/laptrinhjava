"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Input } from "../ui/input"
import { Badge } from "../ui/badge"
import { mockCredits, mockJourneys, mockUsers } from "../../../apps/web-portal-next/lib/mock-data"
import { Search, MapPin, Calendar, Zap, ShoppingCart } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "../ui/dialog"
import { Label } from "../ui/label"

export function MarketplaceBrowser() {
  const [searchQuery, setSearchQuery] = useState("")
  const [purchaseAmount, setPurchaseAmount] = useState("")
  const [isPurchasing, setIsPurchasing] = useState(false)

  const availableCredits = mockCredits.filter((c) => c.status === "available")

  const handlePurchase = async () => {
    setIsPurchasing(true)
    await new Promise((resolve) => setTimeout(resolve, 1500))
    setIsPurchasing(false)
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Search Credits</CardTitle>
          <CardDescription>Filter available carbon credits</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by location, date, or seller..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button variant="outline">Filter</Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {availableCredits.map((credit) => {
          const journey = mockJourneys.find((j) => j.id === credit.journeyId)
          const seller = mockUsers.find((u) => u.id === credit.ownerId)

          return (
            <Card key={credit.id} className="flex flex-col">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-lg">{credit.amount.toFixed(1)} tCO₂</CardTitle>
                    <CardDescription>Carbon Credits</CardDescription>
                  </div>
                  <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                    Verified
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="flex-1 space-y-4">
                {journey && (
                  <div className="space-y-2 text-sm">
                    <div className="flex items-start gap-2">
                      <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
                      <div>
                        <p className="font-medium">Route</p>
                        <p className="text-muted-foreground">
                          {journey.startLocation} → {journey.endLocation}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <span className="font-medium">Date: </span>
                        <span className="text-muted-foreground">{journey.date}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Zap className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <span className="font-medium">Distance: </span>
                        <span className="text-muted-foreground">{journey.distance} miles</span>
                      </div>
                    </div>
                  </div>
                )}

                <div className="pt-4 border-t">
                  <div className="flex items-baseline justify-between mb-3">
                    <span className="text-sm text-muted-foreground">Price per credit</span>
                    <span className="text-2xl font-bold text-emerald-600">${credit.pricePerCredit}</span>
                  </div>
                  <div className="flex items-baseline justify-between mb-4">
                    <span className="text-sm font-medium">Total Price</span>
                    <span className="text-lg font-bold">${(credit.amount * credit.pricePerCredit).toFixed(2)}</span>
                  </div>

                  <Dialog>
                    <DialogTrigger asChild>
                      <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                        <ShoppingCart className="mr-2 h-4 w-4" />
                        Purchase
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Purchase Carbon Credits</DialogTitle>
                        <DialogDescription>Complete your purchase of verified carbon credits</DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4 py-4">
                        <div className="p-4 bg-muted rounded-lg space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm">Available Credits</span>
                            <span className="font-medium">{credit.amount.toFixed(1)} tCO₂</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm">Price per Credit</span>
                            <span className="font-medium">${credit.pricePerCredit}</span>
                          </div>
                          {seller && (
                            <div className="flex justify-between">
                              <span className="text-sm">Seller</span>
                              <span className="font-medium">{seller.name}</span>
                            </div>
                          )}
                        </div>

                        <div className="space-y-2">
                          <Label htmlFor="amount">Amount to Purchase (tCO₂)</Label>
                          <Input
                            id="amount"
                            type="number"
                            step="0.1"
                            max={credit.amount}
                            value={purchaseAmount}
                            onChange={(e) => setPurchaseAmount(e.target.value)}
                            placeholder={credit.amount.toString()}
                          />
                        </div>

                        <div className="p-4 bg-emerald-50 dark:bg-emerald-950 rounded-lg border border-emerald-200 dark:border-emerald-800">
                          <div className="flex justify-between items-center">
                            <span className="font-medium">Total Cost</span>
                            <span className="text-2xl font-bold text-emerald-600">
                              $
                              {purchaseAmount
                                ? (Number.parseFloat(purchaseAmount) * credit.pricePerCredit).toFixed(2)
                                : (credit.amount * credit.pricePerCredit).toFixed(2)}
                            </span>
                          </div>
                        </div>
                      </div>
                      <DialogFooter>
                        <Button
                          onClick={handlePurchase}
                          disabled={isPurchasing}
                          className="w-full bg-emerald-600 hover:bg-emerald-700"
                        >
                          {isPurchasing ? "Processing..." : "Confirm Purchase"}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
