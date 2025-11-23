"use client";

import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  TrendingUp,
  Leaf,
  DollarSign,
  Clock,
  Upload,
  ShoppingCart,
  Loader2,
} from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  XAxis,
  YAxis,
  ResponsiveContainer,
} from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Button } from "@/components/ui/button";
import Link from "next/link";

// Import API thật
import {
  getMyJourneys,
  getOwnerDashboardStats,
  type JourneyResponse,
  type OwnerDashboardStats,
} from "@/lib/api/owner";

export function OwnerDashboardOverview() {
  const [stats, setStats] = useState<OwnerDashboardStats | null>(null);
  const [journeys, setJourneys] = useState<JourneyResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        // Gọi song song cả 2 API để lấy dữ liệu
        const [statsData, journeysData] = await Promise.all([
          getOwnerDashboardStats(),
          getMyJourneys(),
        ]);

        setStats(statsData);
        // Backend trả về mảng hoặc object page, xử lý an toàn:
        setJourneys(
          Array.isArray(journeysData)
            ? journeysData
            : (journeysData as any).content || []
        );
      } catch (error) {
        console.error("Failed to load dashboard data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Tính toán dữ liệu cho biểu đồ từ danh sách hành trình thực tế
  const chartData = (() => {
    const monthlyData: Record<string, number> = {};
    journeys.forEach((j) => {
      if (j.status === "APPROVED" || j.status === "VERIFIED") {
        // Chỉ tính cái đã duyệt
        const date = new Date(j.journeyDate);
        const monthKey = date.toLocaleString("default", { month: "short" }); // "Jan", "Feb"
        monthlyData[monthKey] =
          (monthlyData[monthKey] || 0) + (j.estimatedCredits || 0);
      }
    });

    // Chuyển về dạng mảng cho Recharts
    return Object.entries(monthlyData).map(([month, credits]) => ({
      month,
      credits,
    }));
  })();

  const pendingCount = journeys.filter((j) => j.status === "PENDING").length;

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome back! Here's your carbon credit overview.
        </p>
      </div>

      {/* KPI CARDS - DỮ LIỆU THẬT TỪ API */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Credits Generated
            </CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.totalCreditsGenerated?.toFixed(2) || "0.00"} tCO2
            </div>
            <p className="text-xs text-muted-foreground">
              From verified journeys
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Wallet Balance
            </CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${stats?.balance?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">
              Available to withdraw
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Pending Verification
            </CardTitle>
            <Clock className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingCount}</div>
            <p className="text-xs text-muted-foreground">
              Journeys awaiting review
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Earnings
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${stats?.totalEarnings?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Lifetime earnings</p>
          </CardContent>
        </Card>
      </div>

      {/* BIỂU ĐỒ - DỮ LIỆU THẬT TỪ JOURNEYS */}
      <Card>
        <CardHeader>
          <CardTitle>Credits Generated Over Time</CardTitle>
          <CardDescription>
            Monthly carbon credit generation from your verified journeys
          </CardDescription>
        </CardHeader>
        <CardContent>
          {chartData.length > 0 ? (
            <ChartContainer
              config={{
                credits: {
                  label: "Credits (tCO2)",
                  color: "hsl(var(--chart-1))",
                },
              }}
              className="h-[300px]"
            >
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid
                    strokeDasharray="3 3"
                    className="stroke-muted"
                  />
                  <XAxis dataKey="month" className="text-xs" />
                  <YAxis className="text-xs" />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Bar
                    dataKey="credits"
                    fill="var(--color-chart-1)"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </ChartContainer>
          ) : (
            <div className="h-[300px] flex items-center justify-center text-muted-foreground">
              No verified credits data available yet.
            </div>
          )}
        </CardContent>
      </Card>

      {/* DANH SÁCH GẦN ĐÂY - DỮ LIỆU THẬT */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recent Journeys</CardTitle>
            <CardDescription>Your latest EV trips</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {journeys.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">
                  No journeys found.
                </p>
              ) : (
                journeys.slice(0, 3).map((journey) => (
                  <div
                    key={journey.id}
                    className="flex items-center justify-between border-b pb-3 last:border-0"
                  >
                    <div className="space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {journey.startLocation} → {journey.endLocation}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {journey.journeyDate}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium">
                        {journey.estimatedCredits?.toFixed(2)} tCO2
                      </p>
                      <p
                        className={`text-xs ${
                          journey.status === "APPROVED" ||
                          journey.status === "VERIFIED"
                            ? "text-emerald-600"
                            : journey.status === "PENDING"
                            ? "text-amber-600"
                            : "text-red-600"
                        }`}
                      >
                        {journey.status}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common tasks</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <Link
              href="/owner/upload"
              className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
            >
              <div className="flex items-center gap-3">
                <Upload className="h-5 w-5 text-emerald-600" />
                <span className="font-medium">Upload New Journey</span>
              </div>
            </Link>
            <Link
              href="/owner/credits"
              className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors"
            >
              <div className="flex items-center gap-3">
                <ShoppingCart className="h-5 w-5 text-emerald-600" />
                <span className="font-medium">List Credits for Sale</span>
              </div>
            </Link>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
