"use client";

import { useToast } from "@/hooks/use-toast";
import { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { DollarSign, Tag, Loader2 } from "lucide-react";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog";

// [FIX] Import thêm getWalletBalance và WalletBalance
import {
  getMyJourneys,
  createListing,
  getWalletBalance,
  type JourneyResponse,
  type WalletBalance,
} from "@/lib/api/owner";

export function CreditManagement() {
  const { toast } = useToast();
  const [journeys, setJourneys] = useState<JourneyResponse[]>([]);

  // [FIX] Thêm state lưu số dư từ Backend
  const [walletInfo, setWalletInfo] = useState<WalletBalance | null>(null);

  const [loading, setLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [listingPrice, setListingPrice] = useState("25");
  const [isListing, setIsListing] = useState(false);

  // 1. Gọi API
  const fetchData = async () => {
    try {
      setLoading(true);

      // [FIX] Gọi song song: Lấy Journey (để hiện list) VÀ Lấy Wallet (để hiện số dư đúng)
      const [journeysData, walletData] = await Promise.all([
        getMyJourneys(),
        getWalletBalance(),
      ]);

      setJourneys(
        Array.isArray(journeysData)
          ? journeysData
          : (journeysData as any).content || []
      );
      setWalletInfo(walletData); // Lưu thông tin ví
    } catch (error) {
      console.error("Failed to fetch data:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // [FIX] Sử dụng số dư từ Backend (Nếu chưa load xong thì là 0)
  const availableCredits = walletInfo?.availableCredits || 0;

  const handleListCredit = async () => {
    if (!listingPrice || isNaN(Number(listingPrice))) {
      toast({
        title: "Error",
        description: "Invalid price",
        variant: "destructive",
      });
      return;
    }

    // [FIX] Check số dư dựa trên biến chuẩn từ Backend
    if (availableCredits <= 0) {
      toast({
        title: "Error",
        description: "You have no credits to list",
        variant: "destructive",
      });
      return;
    }

    setIsListing(true);
    try {
      await createListing({
        amount: availableCredits, // Bán hết số khả dụng
        pricePerCredit: Number(listingPrice),
      });

      toast({ title: "Success", description: "Listed successfully!" });
      setIsDialogOpen(false);

      // [FIX] Reload lại dữ liệu để cập nhật số dư mới (về 0)
      fetchData();
    } catch (error: any) {
      toast({
        title: "Failed",
        description: error?.response?.data?.message || "Error listing credits",
        variant: "destructive",
      });
    } finally {
      setIsListing(false);
    }
  };

  // Vẫn giữ biến này để hiển thị danh sách bên dưới (UI List)
  const verifiedJourneys = journeys.filter(
    (j) => j.status === "APPROVED" || j.status === "VERIFIED"
  );

  // [QUAN TRỌNG] XÓA DÒNG NÀY: const totalAvailableCredits = ...
  // Vì dòng này chính là nguyên nhân gây lỗi cộng ngược tiền.

  if (loading) {
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        {/* Card 1: Tín chỉ khả dụng */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Available Credits (Live)
            </CardTitle>
            <Tag className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {/* [FIX] Hiển thị biến chuẩn từ Backend */}
              {availableCredits.toFixed(2)} tCO2
            </div>
            <p className="text-xs text-muted-foreground">Ready to trade</p>
          </CardContent>
        </Card>

        {/* Card 2: Tín chỉ đang treo bán (Locked) */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Sold Credits</CardTitle>
            <DollarSign className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {/* [FIX] Lấy từ walletInfo.soldCredits */}
              {walletInfo?.soldCredits?.toFixed(2) || "0.00"} tCO2
            </div>
            <p className="text-xs text-muted-foreground">
              Completed sales
              {/* Hiển thị thêm số tiền kiếm được ở đây nếu muốn */}
              <span className="block mt-1 text-emerald-600 font-medium">
                +${walletInfo?.totalEarnings?.toFixed(2) || "0.00"} earned
              </span>
            </p>
          </CardContent>
        </Card>

        {/* Card 3: Tổng kiếm được */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Generated
            </CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {walletInfo?.totalCreditsGenerated?.toFixed(2) || "0.00"} tCO2
            </div>
            <p className="text-xs text-muted-foreground">Lifetime production</p>
          </CardContent>
        </Card>
      </div>

      {/* Phần danh sách Verified Journeys giữ nguyên để tham khảo */}
      <Card>
        <CardHeader>
          <CardTitle>Source History</CardTitle>
          <CardDescription>
            "Your verified journeys history (Reference only)
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* ... Giữ nguyên phần render list journeys ... */}
          <div className="space-y-4">
            {verifiedJourneys.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">
                No verified credits available yet.
              </p>
            ) : (
              verifiedJourneys.map((journey) => (
                <div
                  key={journey.id}
                  className="flex items-center justify-between border rounded-lg p-4"
                >
                  <div className="space-y-1">
                    <p className="font-medium">
                      {journey.estimatedCredits?.toFixed(2)} tCO2
                    </p>
                    <p className="text-sm text-muted-foreground">
                      Route: {journey.startLocation} → {journey.endLocation}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Date: {journey.journeyDate}
                    </p>
                  </div>
                  <div className="text-right">
                    <Badge className="bg-emerald-100 text-emerald-900">
                      Verified
                    </Badge>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* Form List Credits */}
      <Card>
        <CardHeader>
          <CardTitle>List New Credits</CardTitle>
          <CardDescription>Create a new sell order</CardDescription>
        </CardHeader>
        <CardContent>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                <Tag className="mr-2 h-4 w-4" />
                List All Available Credits
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Confirm Listing</DialogTitle>
                <DialogDescription>
                  You are listing all your available credits.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="p-4 bg-muted rounded-lg">
                  <div className="flex justify-between mb-2">
                    <span>Available to list:</span>
                    <span className="font-bold text-emerald-600">
                      {availableCredits.toFixed(2)} tCO2
                    </span>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="price">Price per Credit (USD)</Label>
                  <Input
                    id="price"
                    type="number"
                    step="0.01"
                    value={listingPrice}
                    onChange={(e) => setListingPrice(e.target.value)}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button
                  onClick={handleListCredit}
                  disabled={isListing || availableCredits <= 0}
                  className="w-full bg-emerald-600 hover:bg-emerald-700"
                >
                  {isListing ? "Processing..." : "Confirm Listing"}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>
    </div>
  );
}
