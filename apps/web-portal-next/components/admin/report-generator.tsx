"use client"

import { useState } from "react"
import { format as formatDate } from "date-fns"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import { CalendarIcon, Download, Loader2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { useReportApi } from "@/lib/hooks/useAdminApi"

type ReportType = "transactions" | "users"
type ExportFormat = "csv" | "pdf"

export function ReportGenerator() {
  const [reportType, setReportType] = useState<ReportType | "">("")
  const [format, setFormat] = useState<ExportFormat | "">("")
  const [dateFrom, setDateFrom] = useState<Date>()
  const [dateTo, setDateTo] = useState<Date>()
  const [generating, setGenerating] = useState(false)
  
  const { toast } = useToast()
  const { 
    exportTransactionsCSV,
    exportTransactionsPDF,
    exportUsersCSV,
    exportUsersPDF
  } = useReportApi()

  const handleGenerate = async () => {
    if (!reportType || !format) {
      toast({
        title: "Missing Information",
        description: "Please select report type and format.",
        variant: "destructive",
      })
      return
    }

    setGenerating(true)

    try {
      // Prepare date filters (convert to ISO string for backend)
      const filters: any = {}
      if (dateFrom) {
        filters.from = dateFrom.toISOString()
      }
      if (dateTo) {
        // Set to end of day
        const endDate = new Date(dateTo)
        endDate.setHours(23, 59, 59, 999)
        filters.to = endDate.toISOString()
      }

      let blob: Blob | null = null
      let filename = ""

      // Call appropriate API based on report type and format
      if (reportType === "transactions" && format === "csv") {
        blob = await exportTransactionsCSV(filters)
        filename = "transactions_report.csv"
      } else if (reportType === "transactions" && format === "pdf") {
        blob = await exportTransactionsPDF(filters)
        filename = "transactions_report.pdf"
      } else if (reportType === "users" && format === "csv") {
        blob = await exportUsersCSV(filters)
        filename = "users_report.csv"
      } else if (reportType === "users" && format === "pdf") {
        blob = await exportUsersPDF(filters)
        filename = "users_report.pdf"
      }

      if (blob) {
        // Create download link
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        toast({
          title: "Report Generated",
          description: `Your ${reportType} report has been downloaded successfully.`,
        })

        // Reset form
        setReportType("")
        setFormat("")
        setDateFrom(undefined)
        setDateTo(undefined)
      }
    } catch (error: any) {
      console.error("Failed to generate report:", error)
      toast({
        title: "Generation Failed",
        description: error?.response?.data?.message || "Failed to generate report. Please try again.",
        variant: "destructive",
      })
    } finally {
      setGenerating(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Generate New Report</CardTitle>
        <CardDescription>Select report type, date range (optional), and export format</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label>Report Type</Label>
              <Select value={reportType} onValueChange={(v) => setReportType(v as ReportType)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select report type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="transactions">Transaction Report</SelectItem>
                  <SelectItem value="users">User Report</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Export Format</Label>
              <Select value={format} onValueChange={(v) => setFormat(v as ExportFormat)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select format" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="csv">CSV</SelectItem>
                  <SelectItem value="pdf">PDF</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label>From Date (Optional)</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn("w-full justify-start text-left font-normal", !dateFrom && "text-muted-foreground")}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {dateFrom ? formatDate(dateFrom, "PPP") : "All dates"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar mode="single" selected={dateFrom} onSelect={setDateFrom} initialFocus />
                </PopoverContent>
              </Popover>
            </div>

            <div className="space-y-2">
              <Label>To Date (Optional)</Label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    className={cn("w-full justify-start text-left font-normal", !dateTo && "text-muted-foreground")}
                  >
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {dateTo ? formatDate(dateTo, "PPP") : "All dates"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar mode="single" selected={dateTo} onSelect={setDateTo} initialFocus />
                </PopoverContent>
              </Popover>
            </div>
          </div>

          <Button onClick={handleGenerate} className="w-full" disabled={generating}>
            {generating ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Download className="mr-2 h-4 w-4" />
                Generate Report
              </>
            )}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
