"use client";

import { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  DollarSign,
  TrendingUp,
  ArrowUpRight,
  Download,
  Loader2,
  History,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
// Import API thật
import {
  getWalletBalance,
  getWithdrawals,
  requestWithdrawal,
  type WalletBalance,
  type Payout,
} from "@/lib/api/owner";

export function WalletOverview() {
  const { toast } = useToast();

  // State dữ liệu thật
  const [balanceInfo, setBalanceInfo] = useState<WalletBalance | null>(null);
  const [payouts, setPayouts] = useState<Payout[]>([]);
  const [loading, setLoading] = useState(true);

  // State cho form rút tiền
  const [withdrawAmount, setWithdrawAmount] = useState("");
  const [bankAccount, setBankAccount] = useState("");
  const [isWithdrawing, setIsWithdrawing] = useState(false);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  // 1. Gọi API lấy dữ liệu ví
  const fetchData = async () => {
    try {
      setLoading(true);
      const [bal, hist] = await Promise.all([
        getWalletBalance(),
        getWithdrawals(),
      ]);
      setBalanceInfo(bal);
      setPayouts(hist);
    } catch (error) {
      console.error("Failed to load wallet:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // 2. Xử lý Rút tiền thật
  const handleWithdraw = async () => {
    if (
      !withdrawAmount ||
      isNaN(Number(withdrawAmount)) ||
      Number(withdrawAmount) <= 0
    ) {
      toast({ title: "Invalid Amount", variant: "destructive" });
      return;
    }
    if (!bankAccount) {
      toast({ title: "Missing Bank Account", variant: "destructive" });
      return;
    }

    setIsWithdrawing(true);
    try {
      await requestWithdrawal({
        amount: Number(withdrawAmount),
        paymentMethod: "BANK_TRANSFER",
        bankAccount: bankAccount,
        notes: "Web portal withdrawal",
      });

      toast({
        title: "Request Submitted",
        description: "Your withdrawal is pending approval.",
      });
      setIsDialogOpen(false);
      fetchData(); // Refresh số dư
    } catch (error: any) {
      toast({
        title: "Withdrawal Failed",
        description:
          error?.response?.data?.message || "Could not process request.",
        variant: "destructive",
      });
    } finally {
      setIsWithdrawing(false);
    }
  };

  if (loading)
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );

  return (
    <div className="space-y-6">
      {/* KPI Cards (Live Data) */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Earnings
            </CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${balanceInfo?.totalEarnings?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Lifetime revenue</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Available Balance
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-emerald-600">
              ${balanceInfo?.balance?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Ready to withdraw</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Pending Payouts
            </CardTitle>
            <ArrowUpRight className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${balanceInfo?.pendingWithdrawals?.toFixed(2) || "0.00"}
            </div>
            <p className="text-xs text-muted-foreground">Processing</p>
          </CardContent>
        </Card>
      </div>

      {/* Withdraw Form */}
      <Card>
        <CardHeader>
          <CardTitle>Funds Management</CardTitle>
          <CardDescription>Manage your earnings</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between p-4 bg-muted rounded-lg">
            <div>
              <p className="font-medium">Current Balance</p>
              <p className="text-2xl font-bold text-emerald-600">
                ${balanceInfo?.balance?.toFixed(2)}
              </p>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button>Withdraw Funds</Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Request Withdrawal</DialogTitle>
                </DialogHeader>
                <div className="space-y-4 py-4">
                  <div className="space-y-2">
                    <Label>Amount ($)</Label>
                    <Input
                      type="number"
                      value={withdrawAmount}
                      onChange={(e) => setWithdrawAmount(e.target.value)}
                      max={balanceInfo?.balance}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Bank Account / PayPal Email</Label>
                    <Input
                      value={bankAccount}
                      onChange={(e) => setBankAccount(e.target.value)}
                      placeholder="e.g. user@paypal.com"
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button onClick={handleWithdraw} disabled={isWithdrawing}>
                    {isWithdrawing ? "Processing..." : "Submit Request"}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </CardContent>
      </Card>

      {/* Withdrawal History (Dùng API thật /withdrawals) */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Withdrawal History</CardTitle>
              <CardDescription>Recent payout requests</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {payouts.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">
                No withdrawal history yet.
              </p>
            ) : (
              payouts.map((payout) => (
                <div
                  key={payout.id}
                  className="flex items-center justify-between border-b pb-4 last:border-0"
                >
                  <div className="space-y-1">
                    <p className="font-medium">
                      Withdrawal Request #{payout.id}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(payout.requestedAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-bold text-emerald-600">
                      ${payout.amount.toFixed(2)}
                    </p>
                    <Badge
                      variant={
                        payout.status === "COMPLETED" ? "default" : "secondary"
                      }
                    >
                      {payout.status}
                    </Badge>
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
