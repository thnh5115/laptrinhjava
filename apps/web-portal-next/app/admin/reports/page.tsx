"use client"

import { useState, useEffect } from "react"
import { ReportGenerator } from "@/components/admin/report-generator"
import { ReportHistory } from "@/components/admin/report-history"
import { SummaryCards } from "@/components/admin/summary-cards"
import { MonthlyChart } from "@/components/admin/monthly-chart"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useToast } from "@/hooks/use-toast"
import { getSummary, getMonthly, type ReportSummary, type MonthlyChartData } from "@/lib/api/admin-reports"
import { RefreshCw } from "lucide-react"
import { Button } from "@/components/ui/button"

export default function AdminReportsPage() {
  const { toast } = useToast()
  const [summary, setSummary] = useState<ReportSummary | null>(null)
  const [monthly, setMonthly] = useState<MonthlyChartData | null>(null)
  const [year, setYear] = useState(new Date().getFullYear())
  const [loading, setLoading] = useState(true)

  console.log("[Reports] Render - summary:", summary, "monthly:", monthly, "loading:", loading)

  // Fetch both summary and monthly data
  const fetchReports = async () => {
    setLoading(true)
    try {
      const [summaryData, monthlyData] = await Promise.all([
        getSummary(),
        getMonthly(year),
      ])
      
      setSummary(summaryData)
      setMonthly(monthlyData)
      
      console.log("[Reports] Summary fetched:", summaryData)
      console.log("[Reports] Monthly data fetched:", monthlyData)
    } catch (error: any) {
      console.error("[Reports] Failed to fetch data:", error)
      toast({
        title: "Error Loading Reports",
        description: error?.response?.data?.message || "Failed to load report data. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReports()
  }, [year])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Reports & Analytics</h1>
          <p className="text-muted-foreground">Platform performance metrics and report generation</p>
        </div>
        <Button variant="outline" size="sm" onClick={fetchReports} disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="generate">Generate Report</TabsTrigger>
          <TabsTrigger value="history">Report History</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-6">
          {/* Summary Cards */}
          {summary ? (
            <SummaryCards data={summary} loading={loading} />
          ) : (
            <SummaryCards 
              data={{
                totalUsers: 0,
                totalTransactions: 0,
                totalRevenue: 0,
                approvedTransactions: 0,
                rejectedTransactions: 0,
                pendingTransactions: 0,
              }}
              loading={loading}
            />
          )}

          {/* Monthly Chart */}
          {monthly ? (
            <MonthlyChart data={monthly} year={year} loading={loading} />
          ) : (
            <MonthlyChart 
              data={{ transactionsByMonth: {}, revenueByMonth: {} }}
              year={year}
              loading={loading}
            />
          )}
        </TabsContent>

        <TabsContent value="generate" className="space-y-4">
          <ReportGenerator />
        </TabsContent>

        <TabsContent value="history" className="space-y-4">
          <ReportHistory />
        </TabsContent>
      </Tabs>
    </div>
  )
}
