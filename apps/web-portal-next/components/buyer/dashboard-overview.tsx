"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { TrendingUp, Leaf, DollarSign, Award, Loader2 } from "lucide-react";
import { useAuth } from "@/lib/contexts/AuthContext";
// Import API thật
import {
  getBuyerStats,
  getMyTransactions,
  type BuyerDashboardStats,
  type Transaction,
} from "@/lib/api/buyer";

export function BuyerDashboardOverview() {
  const { user } = useAuth();

  // State lưu dữ liệu thật
  const [stats, setStats] = useState<BuyerDashboardStats | null>(null);
  const [recentTx, setRecentTx] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    const fetchData = async () => {
      try {
        setLoading(true);
        const userId = Number(user.id);

        // Gọi song song 2 API lấy Stats và Lịch sử
        const [statsData, txData] = await Promise.all([
          getBuyerStats(userId),
          getMyTransactions(userId),
        ]);

        setStats(statsData);
        // Lấy 5 giao dịch mới nhất
        setRecentTx(txData.sort((a, b) => b.id - a.id).slice(0, 5));
      } catch (error) {
        console.error("Failed to load dashboard:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user]);

  if (loading)
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Real-time overview of your carbon portfolio.
        </p>
      </div>

      {/* KPI Cards - SỐ LIỆU THẬT */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
            <Leaf className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.totalOrders || 0}</div>
            <p className="text-xs text-muted-foreground">Transactions made</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Spent</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${stats?.totalSpent?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Lifetime investment</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completed</CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.completedTransactions || 0}
            </div>
            <p className="text-xs text-muted-foreground">Successful orders</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Pending</CardTitle>
            <Award className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.pendingTransactions || 0}
            </div>
            <p className="text-xs text-muted-foreground">Awaiting processing</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Transactions - DỮ LIỆU THẬT */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Purchases</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {recentTx.length === 0 ? (
              <p className="text-muted-foreground text-sm text-center py-4">
                No transactions yet.
              </p>
            ) : (
              recentTx.map((tx) => (
                <div
                  key={tx.id}
                  className="flex items-center justify-between border-b pb-3 last:border-0"
                >
                  <div className="space-y-1">
                    <p className="text-sm font-medium">Order #{tx.id}</p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(tx.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-emerald-600">
                      ${tx.amount.toFixed(2)}
                    </p>
                    <p
                      className={`text-xs ${
                        tx.status === "COMPLETED"
                          ? "text-emerald-600"
                          : "text-amber-600"
                      }`}
                    >
                      {tx.status}
                    </p>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
