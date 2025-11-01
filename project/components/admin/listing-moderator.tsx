"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { mockListings, mockUsers } from "@/lib/mock-data"
import { Search, Flag, Check, X } from "lucide-react"
import { ListingActions } from "./listing-actions"

export function ListingModerator() {
  const [selectedListing, setSelectedListing] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")

  const filteredListings = mockListings.filter((listing) => {
    const seller = mockUsers.find((u) => u.id === listing.sellerId)
    const matchesSearch =
      seller?.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      listing.id.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || listing.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "flagged":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100"
      case "removed":
        return "bg-gray-100 text-gray-900 dark:bg-gray-900 dark:text-gray-100"
      case "sold":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      default:
        return ""
    }
  }

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Marketplace Listings</CardTitle>
              <CardDescription>Review and moderate carbon credit listings</CardDescription>
            </div>
            <div className="flex gap-2">
              <div className="relative w-64">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search listings..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-8"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-32">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All</SelectItem>
                  <SelectItem value="active">Active</SelectItem>
                  <SelectItem value="flagged">Flagged</SelectItem>
                  <SelectItem value="removed">Removed</SelectItem>
                  <SelectItem value="sold">Sold</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Listing ID</TableHead>
                <TableHead>Seller</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Price/Credit</TableHead>
                <TableHead>Total Value</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredListings.map((listing) => {
                const seller = mockUsers.find((u) => u.id === listing.sellerId)
                return (
                  <TableRow key={listing.id}>
                    <TableCell className="font-mono text-xs">{listing.id}</TableCell>
                    <TableCell>
                      <div>
                        <p className="font-medium">{seller?.name}</p>
                        <p className="text-xs text-muted-foreground">{seller?.email}</p>
                      </div>
                    </TableCell>
                    <TableCell>{listing.amount} tCO₂</TableCell>
                    <TableCell>${listing.pricePerCredit}</TableCell>
                    <TableCell className="font-medium">
                      ${(listing.amount * listing.pricePerCredit).toFixed(2)}
                    </TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(listing.status)}>{listing.status}</Badge>
                    </TableCell>
                    <TableCell>{new Date(listing.createdAt).toLocaleDateString()}</TableCell>
                    <TableCell>
                      {listing.status === "active" && (
                        <Button size="sm" variant="outline" onClick={() => setSelectedListing(listing.id)}>
                          <Flag className="h-4 w-4 mr-1" />
                          Flag
                        </Button>
                      )}
                      {listing.status === "flagged" && (
                        <div className="flex gap-1">
                          <Button size="sm" variant="outline" onClick={() => setSelectedListing(listing.id)}>
                            <Check className="h-4 w-4" />
                          </Button>
                          <Button size="sm" variant="outline" onClick={() => setSelectedListing(listing.id)}>
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {selectedListing && <ListingActions listingId={selectedListing} onClose={() => setSelectedListing(null)} />}
    </>
  )
}
