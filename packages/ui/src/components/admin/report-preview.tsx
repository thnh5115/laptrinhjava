"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Download, Eye } from "lucide-react"

interface ReportPreviewProps {
  reportType: string
  dateRange: { start: string; end: string }
  format: string
}

export function ReportPreview({ reportType, dateRange, format }: ReportPreviewProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Report Preview</CardTitle>
        <CardDescription>Preview your report before downloading</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="p-6 border rounded-lg bg-muted/50">
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="font-medium">Report Type:</span>
                <span className="capitalize">{reportType}</span>
              </div>
              <div className="flex justify-between">
                <span className="font-medium">Date Range:</span>
                <span>
                  {new Date(dateRange.start).toLocaleDateString()} - {new Date(dateRange.end).toLocaleDateString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="font-medium">Format:</span>
                <span className="uppercase">{format}</span>
              </div>
            </div>
          </div>

          <div className="flex gap-2">
            <Button variant="outline" className="flex-1 bg-transparent">
              <Eye className="h-4 w-4 mr-2" />
              Preview
            </Button>
            <Button className="flex-1">
              <Download className="h-4 w-4 mr-2" />
              Download
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
