"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2 } from "lucide-react";
import { useAuth } from "@/lib/contexts/AuthContext";
import { getMyTransactions, type Transaction } from "@/lib/api/buyer";

export function TransactionHistory() {
  const { user } = useAuth();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    const fetchHistory = async () => {
      try {
        setLoading(true);
        const data = await getMyTransactions(Number(user.id));
        // Sắp xếp mới nhất lên đầu
        setTransactions(data.sort((a, b) => b.id - a.id));
      } catch (error) {
        console.error("Failed to fetch history:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, [user]);

  if (loading)
    return (
      <div className="flex justify-center p-8">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Transaction History</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {transactions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No transactions found. Start buying credits!
            </div>
          ) : (
            transactions.map((tx) => (
              <div
                key={tx.id}
                className="flex items-center justify-between border rounded-lg p-4 hover:bg-slate-50 transition-colors"
              >
                <div className="flex-1 space-y-2">
                  <div className="flex items-center gap-2">
                    <p className="font-medium">Order #{tx.id}</p>
                    <Badge
                      variant={
                        tx.status === "COMPLETED" ? "default" : "secondary"
                      }
                    >
                      {tx.status}
                    </Badge>
                  </div>
                  <div className="flex gap-4 text-sm text-muted-foreground">
                    <span>Listing ID: {tx.listingId}</span>
                    <span>Date: {new Date(tx.createdAt).toLocaleString()}</span>
                  </div>
                  <div className="flex gap-4 text-sm">
                    <span className="font-medium text-slate-700">
                      Quantity: {tx.qty} tCO2
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold text-emerald-600">
                    ${tx.amount.toFixed(2)}
                  </p>
                  <p className="text-xs text-muted-foreground">Total Paid</p>
                </div>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}
