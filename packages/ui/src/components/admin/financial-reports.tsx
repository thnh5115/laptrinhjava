"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Download } from "lucide-react"
import { mockTransactions, mockPayouts } from "../../../apps/web-portal-next/lib/mock-data"
import { useToast } from "../../hooks/use-toast"

export function FinancialReports() {
  const { toast } = useToast()

  const handleExport = (format: "csv" | "pdf") => {
    toast({
      title: "Export Started",
      description: `Financial report is being generated in ${format.toUpperCase()} format.`,
    })
  }

  const totalRevenue = mockTransactions.reduce((sum, t) => sum + t.totalPrice, 0)
  const platformFees = totalRevenue * 0.05
  const totalPayouts = mockPayouts.filter((p) => p.status === "completed").reduce((sum, p) => sum + p.amount, 0)

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Financial Summary</CardTitle>
          <CardDescription>Current period financial overview</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <span className="text-sm font-medium">Total Transaction Volume</span>
              <span className="text-lg font-bold">${totalRevenue.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center border-b pb-3">
              <span className="text-sm font-medium">Platform Fees Collected (5%)</span>
              <span className="text-lg font-bold text-emerald-600">${platformFees.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center border-b pb-3">
              <span className="text-sm font-medium">Total Payouts Completed</span>
              <span className="text-lg font-bold text-red-600">-${totalPayouts.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center pt-2">
              <span className="text-base font-bold">Net Platform Revenue</span>
              <span className="text-xl font-bold text-emerald-600">${(platformFees - totalPayouts).toFixed(2)}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Export Reports</CardTitle>
          <CardDescription>Download financial data in various formats</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div>
                <p className="font-medium">Transaction Report</p>
                <p className="text-xs text-muted-foreground">All transactions with details</p>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => handleExport("csv")}>
                  <Download className="h-4 w-4 mr-1" />
                  CSV
                </Button>
                <Button size="sm" variant="outline" onClick={() => handleExport("pdf")}>
                  <Download className="h-4 w-4 mr-1" />
                  PDF
                </Button>
              </div>
            </div>
            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div>
                <p className="font-medium">Payout Report</p>
                <p className="text-xs text-muted-foreground">All payout requests and statuses</p>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => handleExport("csv")}>
                  <Download className="h-4 w-4 mr-1" />
                  CSV
                </Button>
                <Button size="sm" variant="outline" onClick={() => handleExport("pdf")}>
                  <Download className="h-4 w-4 mr-1" />
                  PDF
                </Button>
              </div>
            </div>
            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div>
                <p className="font-medium">Revenue Summary</p>
                <p className="text-xs text-muted-foreground">Platform fees and net revenue</p>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => handleExport("csv")}>
                  <Download className="h-4 w-4 mr-1" />
                  CSV
                </Button>
                <Button size="sm" variant="outline" onClick={() => handleExport("pdf")}>
                  <Download className="h-4 w-4 mr-1" />
                  PDF
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
