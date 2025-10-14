"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { mockTransactions, mockUsers } from "@/lib/mock-data"
import { Download, CheckCircle2 } from "lucide-react"

export function TransactionHistory() {
  const userId = "2"
  const userTransactions = mockTransactions.filter((t) => t.buyerId === userId)

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>All Transactions</CardTitle>
          <Button variant="outline" size="sm">
            <Download className="mr-2 h-4 w-4" />
            Export CSV
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {userTransactions.map((transaction) => {
            const seller = mockUsers.find((u) => u.id === transaction.sellerId)
            return (
              <div key={transaction.id} className="flex items-center justify-between border rounded-lg p-4">
                <div className="flex-1 space-y-2">
                  <div className="flex items-center gap-2">
                    <p className="font-medium">Purchase of {transaction.amount.toFixed(1)} tCO₂</p>
                    <Badge
                      variant="default"
                      className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
                    >
                      <CheckCircle2 className="mr-1 h-3 w-3" />
                      {transaction.status}
                    </Badge>
                  </div>
                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>Transaction ID: {transaction.id}</span>
                    <span>Seller: {seller?.name || "Unknown"}</span>
                    <span>{new Date(transaction.timestamp).toLocaleString()}</span>
                  </div>
                  <div className="flex gap-4 text-sm">
                    <span>
                      <span className="text-muted-foreground">Price per credit:</span> ${transaction.pricePerCredit}
                    </span>
                    <span>
                      <span className="text-muted-foreground">Amount:</span> {transaction.amount.toFixed(1)} tCO₂
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold text-emerald-600">${transaction.totalPrice.toFixed(2)}</p>
                  <Button variant="outline" size="sm" className="mt-2 bg-transparent">
                    View Receipt
                  </Button>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
