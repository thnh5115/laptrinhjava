"use client"

import { TransactionMonitor } from "@/components/admin/transaction-monitor"

export default function TransactionsPage() {
  return (
    <div>
      <div className="mb-6">
        <h1 className="text-3xl font-bold tracking-tight">Transaction Monitor</h1>
        <p className="text-muted-foreground">View and manage all platform transactions</p>
      </div>
      <TransactionMonitor />
    </div>
  )
}
