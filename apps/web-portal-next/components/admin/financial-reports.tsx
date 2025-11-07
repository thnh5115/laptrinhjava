"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Download, AlertCircle } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { useReportApi, ReportSummary } from "@/lib/hooks/useAdminApi"
import { Skeleton } from "@/components/ui/skeleton"
import { Alert, AlertDescription } from "@/components/ui/alert"

export function FinancialReports() {
  const { toast } = useToast()
  const { 
    getReportSummary, 
    exportTransactionsCSV, 
    exportTransactionsPDF,
    exportUsersCSV,
    exportUsersPDF,
    loading,
    error 
  } = useReportApi()

  const [summary, setSummary] = useState<ReportSummary | null>(null)
  const [exporting, setExporting] = useState<string | null>(null)

  useEffect(() => {
    loadSummary()
  }, [])

  const loadSummary = async () => {
    const data = await getReportSummary()
    if (data) {
      setSummary(data)
    }
  }

  const handleExport = async (type: 'transactions' | 'users', format: 'csv' | 'pdf') => {
    const exportKey = `${type}-${format}`
    setExporting(exportKey)
    
    try {
      let blob: Blob | null = null

      if (type === 'transactions' && format === 'csv') {
        blob = await exportTransactionsCSV()
      } else if (type === 'transactions' && format === 'pdf') {
        blob = await exportTransactionsPDF()
      } else if (type === 'users' && format === 'csv') {
        blob = await exportUsersCSV()
      } else if (type === 'users' && format === 'pdf') {
        blob = await exportUsersPDF()
      }

      if (blob) {
        // Create download link
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `${type}_report.${format}`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        toast({
          title: "Export Successful",
          description: `${type} report has been downloaded in ${format.toUpperCase()} format.`,
        })
      }
    } catch (err: any) {
      toast({
        title: "Export Failed",
        description: err?.response?.data?.message || "Failed to export report",
        variant: "destructive",
      })
    } finally {
      setExporting(null)
    }
  }

  // Calculate platform metrics from summary
  const platformFees = summary ? summary.totalRevenue * 0.05 : 0
  const netRevenue = platformFees // Assuming no payouts deducted yet

  if (loading && !summary) {
    return (
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-40" />
            <Skeleton className="h-4 w-64 mt-2" />
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[...Array(4)].map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-40" />
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {[...Array(3)].map((_, i) => (
                <Skeleton key={i} className="h-20 w-full" />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error && !summary) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    )
  }

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
              <span className="text-lg font-bold">${(summary?.totalRevenue ?? 0).toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center border-b pb-3">
              <span className="text-sm font-medium">Platform Fees Collected (5%)</span>
              <span className="text-lg font-bold text-emerald-600">${platformFees.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center border-b pb-3">
              <div>
                <span className="text-sm font-medium">Total Transactions</span>
                <div className="flex gap-4 mt-1 text-xs text-muted-foreground">
                  <span>✓ Approved: {summary?.approvedTransactions ?? 0}</span>
                  <span>⏳ Pending: {summary?.pendingTransactions ?? 0}</span>
                  <span>✗ Rejected: {summary?.rejectedTransactions ?? 0}</span>
                </div>
              </div>
              <span className="text-lg font-bold">{summary?.totalTransactions ?? 0}</span>
            </div>
            <div className="flex justify-between items-center pt-2">
              <span className="text-base font-bold">Net Platform Revenue</span>
              <span className="text-xl font-bold text-emerald-600">${netRevenue.toFixed(2)}</span>
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
                <Button 
                  size="sm" 
                  variant="outline" 
                  onClick={() => handleExport('transactions', 'csv')}
                  disabled={exporting === 'transactions-csv'}
                >
                  <Download className="h-4 w-4 mr-1" />
                  {exporting === 'transactions-csv' ? 'Exporting...' : 'CSV'}
                </Button>
                <Button 
                  size="sm" 
                  variant="outline" 
                  onClick={() => handleExport('transactions', 'pdf')}
                  disabled={exporting === 'transactions-pdf'}
                >
                  <Download className="h-4 w-4 mr-1" />
                  {exporting === 'transactions-pdf' ? 'Exporting...' : 'PDF'}
                </Button>
              </div>
            </div>
            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div>
                <p className="font-medium">User Report</p>
                <p className="text-xs text-muted-foreground">All users with activity details</p>
              </div>
              <div className="flex gap-2">
                <Button 
                  size="sm" 
                  variant="outline" 
                  onClick={() => handleExport('users', 'csv')}
                  disabled={exporting === 'users-csv'}
                >
                  <Download className="h-4 w-4 mr-1" />
                  {exporting === 'users-csv' ? 'Exporting...' : 'CSV'}
                </Button>
                <Button 
                  size="sm" 
                  variant="outline" 
                  onClick={() => handleExport('users', 'pdf')}
                  disabled={exporting === 'users-pdf'}
                >
                  <Download className="h-4 w-4 mr-1" />
                  {exporting === 'users-pdf' ? 'Exporting...' : 'PDF'}
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
