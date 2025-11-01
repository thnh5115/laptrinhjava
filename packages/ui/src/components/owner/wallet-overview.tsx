"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { mockTransactions, mockUsers } from "../../../apps/web-portal-next/lib/mock-data"
import { DollarSign, TrendingUp, ArrowUpRight, Download } from "lucide-react"

export function WalletOverview() {
  const userId = "1"
  const userTransactions = mockTransactions.filter((t) => t.sellerId === userId)

  const totalEarnings = userTransactions.reduce((sum, t) => sum + t.totalPrice, 0)
  const availableBalance = totalEarnings * 0.95 // Assuming 5% platform fee
  const pendingBalance = totalEarnings * 0.05

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Earnings</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${totalEarnings.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">All-time revenue</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Available Balance</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${availableBalance.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Ready to withdraw</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending</CardTitle>
            <ArrowUpRight className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${pendingBalance.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Processing</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Transaction History</CardTitle>
              <CardDescription>Your recent sales and earnings</CardDescription>
            </div>
            <Button variant="outline" size="sm">
              <Download className="mr-2 h-4 w-4" />
              Export
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {userTransactions.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">No transactions yet</p>
            ) : (
              userTransactions.map((transaction) => {
                const buyer = mockUsers.find((u) => u.id === transaction.buyerId)
                return (
                  <div key={transaction.id} className="flex items-center justify-between border-b pb-4 last:border-0">
                    <div className="space-y-1">
                      <p className="font-medium">Credit Sale</p>
                      <p className="text-sm text-muted-foreground">
                        {transaction.amount.toFixed(1)} tCO₂ @ ${transaction.pricePerCredit}/tCO₂
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Buyer: {buyer?.name || "Unknown"} • {new Date(transaction.timestamp).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-lg font-bold text-emerald-600">+${transaction.totalPrice.toFixed(2)}</p>
                      <p className="text-xs text-muted-foreground">Completed</p>
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
          <CardTitle>Withdraw Funds</CardTitle>
          <CardDescription>Transfer your earnings to your bank account</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-sm font-medium mb-1">Available to Withdraw</p>
              <p className="text-3xl font-bold text-emerald-600">${availableBalance.toFixed(2)}</p>
            </div>
            <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
              <Download className="mr-2 h-4 w-4" />
              Withdraw to Bank Account
            </Button>
            <p className="text-xs text-muted-foreground text-center">
              Withdrawals typically process within 2-3 business days
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
