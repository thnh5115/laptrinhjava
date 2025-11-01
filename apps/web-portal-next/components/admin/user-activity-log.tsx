"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { mockUserActivities, mockUsers } from "@/lib/mock-data"
import { Activity } from "lucide-react"

interface UserActivityLogProps {
  userId: string
}

export function UserActivityLog({ userId }: UserActivityLogProps) {
  const userActivities = mockUserActivities.filter((a) => a.userId === userId)
  const user = mockUsers.find((u) => u.id === userId)

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Activity className="h-5 w-5" />
          Activity Log
        </CardTitle>
        <CardDescription>Recent activity for {user?.name}</CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Action</TableHead>
              <TableHead>Details</TableHead>
              <TableHead>Timestamp</TableHead>
              <TableHead>IP Address</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {userActivities.map((activity) => (
              <TableRow key={activity.id}>
                <TableCell>
                  <Badge variant="outline">{activity.action}</Badge>
                </TableCell>
                <TableCell className="text-sm">{activity.details}</TableCell>
                <TableCell>{new Date(activity.timestamp).toLocaleString()}</TableCell>
                <TableCell className="font-mono text-xs">{activity.ipAddress}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  )
}
