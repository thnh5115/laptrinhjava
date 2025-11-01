"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "../ui/card"
import { Button } from "../ui/button"
import { Badge } from "../ui/badge"
import { Input } from "../ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../ui/table"
import { mockDisputes, mockUsers } from "../../../apps/web-portal-next/lib/mock-data"
import { Eye, Search } from "lucide-react"
import { DisputeDetail } from "./dispute-detail"

export function DisputeList() {
  const [selectedDispute, setSelectedDispute] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState("")

  const filteredDisputes = mockDisputes.filter((dispute) => {
    const reporter = mockUsers.find((u) => u.id === dispute.reportedBy)
    const reported = mockUsers.find((u) => u.id === dispute.reportedAgainst)
    return (
      reporter?.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      reported?.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      dispute.reason.toLowerCase().includes(searchTerm.toLowerCase())
    )
  })

  const getStatusColor = (status: string) => {
    switch (status) {
      case "open":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100"
      case "investigating":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100"
      case "resolved":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100"
      case "closed":
        return "bg-gray-100 text-gray-900 dark:bg-gray-900 dark:text-gray-100"
      default:
        return ""
    }
  }

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Active Disputes</CardTitle>
              <CardDescription>Review and resolve transaction disputes</CardDescription>
            </div>
            <div className="relative w-64">
              <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search disputes..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-8"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Dispute ID</TableHead>
                <TableHead>Reporter</TableHead>
                <TableHead>Reported Against</TableHead>
                <TableHead>Reason</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredDisputes.map((dispute) => {
                const reporter = mockUsers.find((u) => u.id === dispute.reportedBy)
                const reported = mockUsers.find((u) => u.id === dispute.reportedAgainst)
                return (
                  <TableRow key={dispute.id}>
                    <TableCell className="font-mono text-xs">{dispute.id}</TableCell>
                    <TableCell>
                      <div>
                        <p className="font-medium">{reporter?.name}</p>
                        <p className="text-xs text-muted-foreground">{reporter?.email}</p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div>
                        <p className="font-medium">{reported?.name}</p>
                        <p className="text-xs text-muted-foreground">{reported?.email}</p>
                      </div>
                    </TableCell>
                    <TableCell>{dispute.reason}</TableCell>
                    <TableCell>
                      <Badge className={getStatusColor(dispute.status)}>{dispute.status}</Badge>
                    </TableCell>
                    <TableCell>{new Date(dispute.createdAt).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Button size="sm" variant="outline" onClick={() => setSelectedDispute(dispute.id)}>
                        <Eye className="h-4 w-4 mr-1" />
                        View
                      </Button>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {selectedDispute && <DisputeDetail disputeId={selectedDispute} onClose={() => setSelectedDispute(null)} />}
    </>
  )
}
