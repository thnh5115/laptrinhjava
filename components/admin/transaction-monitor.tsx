"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { mockTransactions, mockUsers } from "@/lib/mock-data"
import { Download, CheckCircle2, Eye } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"

export function TransactionMonitor() {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>All Transactions ({mockTransactions.length})</CardTitle>
            <CardDescription>Complete transaction history</CardDescription>
          </div>
          <Button variant="outline" size="sm">
            <Download className="mr-2 h-4 w-4" />
            Export
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {mockTransactions.map((transaction) => {
            const buyer = mockUsers.find((u) => u.id === transaction.buyerId)
            const seller = mockUsers.find((u) => u.id === transaction.sellerId)

            return (
              <div key={transaction.id} className="flex items-center justify-between border rounded-lg p-4">
                <div className="flex-1 space-y-2">
                  <div className="flex items-center gap-2">
                    <p className="font-medium">Transaction #{transaction.id.toUpperCase()}</p>
                    <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                      <CheckCircle2 className="mr-1 h-3 w-3" />
                      {transaction.status}
                    </Badge>
                  </div>
                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>Buyer: {buyer?.name || "Unknown"}</span>
                    <span>Seller: {seller?.name || "Unknown"}</span>
                    <span>{new Date(transaction.timestamp).toLocaleDateString()}</span>
                  </div>
                  <div className="flex gap-4 text-sm">
                    <span>
                      <span className="text-muted-foreground">Amount:</span> {transaction.amount.toFixed(1)} tCO₂
                    </span>
                    <span>
                      <span className="text-muted-foreground">Price:</span> ${transaction.pricePerCredit}/tCO₂
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <p className="text-2xl font-bold text-emerald-600">${transaction.totalPrice.toFixed(2)}</p>
                    <p className="text-xs text-muted-foreground">Total value</p>
                  </div>

                  <Dialog>
                    <DialogTrigger asChild>
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4 mr-2" />
                        Details
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Transaction Details</DialogTitle>
                        <DialogDescription>Complete transaction information</DialogDescription>
                      </DialogHeader>
                      <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Transaction ID</p>
                            <p className="text-sm">{transaction.id.toUpperCase()}</p>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Status</p>
                            <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                              <CheckCircle2 className="mr-1 h-3 w-3" />
                              {transaction.status}
                            </Badge>
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Buyer</p>
                            <p className="text-sm">{buyer?.name || "Unknown"}</p>
                            <p className="text-xs text-muted-foreground">{buyer?.email}</p>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Seller</p>
                            <p className="text-sm">{seller?.name || "Unknown"}</p>
                            <p className="text-xs text-muted-foreground">{seller?.email}</p>
                          </div>
                        </div>
                        <div className="grid grid-cols-3 gap-4">
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Amount</p>
                            <p className="text-sm">{transaction.amount.toFixed(1)} tCO₂</p>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Price per Credit</p>
                            <p className="text-sm">${transaction.pricePerCredit}</p>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-muted-foreground">Total Price</p>
                            <p className="text-sm font-bold text-emerald-600">${transaction.totalPrice.toFixed(2)}</p>
                          </div>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground">Timestamp</p>
                          <p className="text-sm">{new Date(transaction.timestamp).toLocaleString()}</p>
                        </div>
                      </div>
                    </DialogContent>
                  </Dialog>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
