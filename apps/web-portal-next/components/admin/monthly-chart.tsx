"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import type { MonthlyChartData } from "@/lib/api/admin-reports"

interface MonthlyChartProps {
  data: MonthlyChartData;
  year: number;
  loading?: boolean;
}

/**
 * Monthly Chart Component
 * Visualizes transaction count and revenue trends across 12 months
 * Uses recharts LineChart with dual Y-axes
 */
export function MonthlyChart({ data, year, loading = false }: MonthlyChartProps) {
  // Transform BE data (Map<"YYYY-MM", number>) to recharts format
  // Ensure all 12 months are present (1..12)
  const chartData = Array.from({ length: 12 }, (_, i) => {
    const month = i + 1;
    const monthKey = `${year}-${month.toString().padStart(2, "0")}`; // "2025-01"
    
    return {
      month: new Date(year, i).toLocaleString("default", { month: "short" }), // "Jan", "Feb", ...
      transactions: data.transactionsByMonth[monthKey] ?? 0,
      revenue: data.revenueByMonth[monthKey] ?? 0,
    };
  });

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <div className="h-6 w-48 bg-muted animate-pulse rounded" />
          <div className="h-4 w-64 bg-muted animate-pulse rounded mt-2" />
        </CardHeader>
        <CardContent>
          <div className="h-80 bg-muted animate-pulse rounded" />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Monthly Performance - {year}</CardTitle>
        <CardDescription>Transaction volume and revenue trends throughout the year</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={350}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis 
              dataKey="month" 
              className="text-xs"
              tick={{ fill: "hsl(var(--foreground))" }}
            />
            <YAxis 
              yAxisId="left"
              className="text-xs"
              tick={{ fill: "hsl(var(--foreground))" }}
              label={{ value: "Transactions", angle: -90, position: "insideLeft", style: { fill: "hsl(var(--foreground))" } }}
            />
            <YAxis 
              yAxisId="right"
              orientation="right"
              className="text-xs"
              tick={{ fill: "hsl(var(--foreground))" }}
              label={{ value: "Revenue ($)", angle: 90, position: "insideRight", style: { fill: "hsl(var(--foreground))" } }}
            />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: "hsl(var(--background))",
                border: "1px solid hsl(var(--border))",
                borderRadius: "var(--radius)",
              }}
              formatter={(value: number, name: string) => {
                if (name === "revenue") {
                  return [`$${value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, "Revenue"];
                }
                return [value.toLocaleString(), "Transactions"];
              }}
            />
            <Legend />
            <Line 
              yAxisId="left"
              type="monotone" 
              dataKey="transactions" 
              stroke="hsl(var(--chart-1))" 
              strokeWidth={2}
              dot={{ fill: "hsl(var(--chart-1))", r: 4 }}
              activeDot={{ r: 6 }}
              name="Transactions"
            />
            <Line 
              yAxisId="right"
              type="monotone" 
              dataKey="revenue" 
              stroke="hsl(var(--chart-2))" 
              strokeWidth={2}
              dot={{ fill: "hsl(var(--chart-2))", r: 4 }}
              activeDot={{ r: 6 }}
              name="Revenue"
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
