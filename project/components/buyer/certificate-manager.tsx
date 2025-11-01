"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { mockTransactions, mockUsers } from "@/lib/mock-data"
import { Download, FileText, Award } from "lucide-react"

export function CertificateManager() {
  const userId = "2"
  const userTransactions = mockTransactions.filter((t) => t.buyerId === userId)

  const handleDownload = (transactionId: string) => {
    console.log("[v0] Downloading certificate for transaction:", transactionId)
    // Simulate certificate download
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Carbon Offset Certificates</CardTitle>
          <CardDescription>Official certificates for your carbon credit purchases</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {userTransactions.map((transaction) => {
              const seller = mockUsers.find((u) => u.id === transaction.sellerId)
              return (
                <div key={transaction.id} className="flex items-center justify-between border rounded-lg p-4">
                  <div className="flex items-start gap-4">
                    <div className="p-3 bg-emerald-100 dark:bg-emerald-900 rounded-lg">
                      <Award className="h-6 w-6 text-emerald-600" />
                    </div>
                    <div className="space-y-1">
                      <p className="font-medium">Carbon Offset Certificate</p>
                      <p className="text-sm text-muted-foreground">
                        {transaction.amount.toFixed(1)} tCO₂ • Certificate #{transaction.id.toUpperCase()}
                      </p>
                      <div className="flex gap-2 text-xs text-muted-foreground">
                        <span>Issued: {new Date(transaction.timestamp).toLocaleDateString()}</span>
                        <span>•</span>
                        <span>Seller: {seller?.name || "Unknown"}</span>
                      </div>
                      <Badge className="bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100">
                        <FileText className="mr-1 h-3 w-3" />
                        Verified
                      </Badge>
                    </div>
                  </div>
                  <Button variant="outline" onClick={() => handleDownload(transaction.id)}>
                    <Download className="mr-2 h-4 w-4" />
                    Download PDF
                  </Button>
                </div>
              )
            })}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Bulk Actions</CardTitle>
          <CardDescription>Download all certificates at once</CardDescription>
        </CardHeader>
        <CardContent>
          <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
            <Download className="mr-2 h-4 w-4" />
            Download All Certificates (ZIP)
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
