"use client";

import { useToast } from "@/hooks/use-toast";
import { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
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
// Import API thật
import {
  getMyJourneys,
  createListing,
  type JourneyResponse,
} from "@/lib/api/owner";

export function CreditManagement() {
  // State lưu dữ liệu thật từ API
  const { toast } = useToast();
  const [journeys, setJourneys] = useState<JourneyResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [listingPrice, setListingPrice] = useState("25");
  const [isListing, setIsListing] = useState(false);

  // 1. Gọi API lấy danh sách hành trình thật
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const data = await getMyJourneys();
        // Đảm bảo data luôn là mảng
        setJourneys(Array.isArray(data) ? data : (data as any).content || []);
      } catch (error) {
        console.error("Failed to fetch journeys:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleListCredit = async () => {
    // 1. Validate dữ liệu
    if (!listingPrice || isNaN(Number(listingPrice))) {
      toast({
        title: "Error",
        description: "Please enter a valid price",
        variant: "destructive",
      });
      return;
    }
    if (totalAvailableCredits <= 0) {
      toast({
        title: "Error",
        description: "You have no credits to list",
        variant: "destructive",
      });
      return;
    }

    setIsListing(true);

    try {
      // 2. GỌI API THẬT (Đã thay thế đoạn code giả lập)
      await createListing({
        amount: totalAvailableCredits,
        pricePerCredit: Number(listingPrice),
      });

      // 3. Thông báo thành công
      toast({
        title: "Success",
        description: `Listed ${totalAvailableCredits.toFixed(
          2
        )} tCO2 successfully!`,
      });

      // 4. Đóng dialog và làm mới dữ liệu (nếu cần)
      setIsDialogOpen(false);

      // Mẹo: Bạn có thể gọi lại fetchJourneys() ở đây nếu muốn cập nhật lại số dư ngay lập tức
      // nhưng hiện tại cứ đóng dialog là được.
    } catch (error: any) {
      console.error("Listing failed:", error);
      toast({
        title: "Failed",
        description:
          error?.response?.data?.message ||
          "Could not list credits. Try again later.",
        variant: "destructive",
      });
    } finally {
      setIsListing(false);
    }
  };

  // 2. Logic tính toán dữ liệu thật (Thay thế mock data cũ)
  // Chỉ lấy các hành trình đã được duyệt (APPROVED hoặc VERIFIED)
  const verifiedJourneys = journeys.filter(
    (j) => j.status === "APPROVED" || j.status === "VERIFIED"
  );

  // Tính tổng tín chỉ: Cộng dồn trường estimatedCredits của các hành trình đã duyệt
  const totalAvailableCredits = verifiedJourneys.reduce(
    (sum, j) => sum + (j.estimatedCredits || 0),
    0
  );

  // Dữ liệu "Đã bán" tạm thời để 0 (Backend chưa hỗ trợ)
  const totalSoldAmount = 0;
  const totalSoldValue = 0;

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
        {/* Card 1: Tín chỉ khả dụng (REALTIME) */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Available Credits (Live)
            </CardTitle>
            <Tag className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {totalAvailableCredits.toFixed(2)} tCO2
            </div>
            <p className="text-xs text-muted-foreground">
              {verifiedJourneys.length} verified journeys
            </p>
          </CardContent>
        </Card>

        {/* Card 2: Tín chỉ đã bán */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Sold Credits</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {totalSoldAmount.toFixed(1)} tCO2
            </div>
            <p className="text-xs text-muted-foreground">
              ${totalSoldValue.toFixed(2)} earned
            </p>
          </CardContent>
        </Card>

        {/* Card 3: Giá trung bình */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Price</CardTitle>
            <DollarSign className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">$25.00</div>
            <p className="text-xs text-muted-foreground">Per tCO2</p>
          </CardContent>
        </Card>
      </div>

      {/* Danh sách chi tiết hành trình đã duyệt */}
      <Card>
        <CardHeader>
          <CardTitle>Verified Credit Sources</CardTitle>
          <CardDescription>
            Credits generated from your approved trips
          </CardDescription>
        </CardHeader>
        <CardContent>
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
                    <p className="text-lg font-bold text-emerald-600">
                      Verified
                    </p>
                    <Badge className="bg-emerald-100 text-emerald-900">
                      Ready to List
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
          <CardDescription>
            Set a price and list your verified credits for sale
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                <Tag className="mr-2 h-4 w-4" />
                List Credits for Sale
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>List Credits</DialogTitle>
                <DialogDescription>
                  Set your price per credit (tCO2) to list on the marketplace
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="price">Price per Credit (USD)</Label>
                  <Input
                    id="price"
                    type="number"
                    step="0.01"
                    value={listingPrice}
                    onChange={(e) => setListingPrice(e.target.value)}
                    placeholder="25.00"
                  />
                  <p className="text-xs text-muted-foreground">
                    Market average: $25.00 per tCO2
                  </p>
                </div>
                <div className="p-4 bg-muted rounded-lg">
                  <p className="text-sm font-medium mb-2">Available to List:</p>
                  <p className="text-2xl font-bold text-emerald-600">
                    {totalAvailableCredits.toFixed(2)} tCO2
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    From verified journeys
                  </p>
                </div>
              </div>
              <DialogFooter>
                <Button
                  onClick={handleListCredit}
                  disabled={isListing}
                  className="w-full bg-emerald-600 hover:bg-emerald-700"
                >
                  {isListing ? "Listing..." : "List Credits"}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>
    </div>
  );
}
