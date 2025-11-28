"use client";

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
import { Badge } from "@/components/ui/badge";
import { Search, ShoppingCart, Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/lib/contexts/AuthContext";
import { getListings, purchaseCredit, type Listing } from "@/lib/api/buyer";

export function MarketplaceBrowser() {
  const { user } = useAuth();
  const { toast } = useToast();

  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  // [ĐÃ XÓA] const [purchaseAmount, setPurchaseAmount] = useState("1");
  const [isPurchasing, setIsPurchasing] = useState(false);
  const [openDialogId, setOpenDialogId] = useState<number | null>(null);

  const fetchListings = async () => {
    try {
      setLoading(true);
      const data = await getListings();
      setListings(data);
    } catch (error) {
      console.error("Failed to fetch listings:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchListings();
  }, []);

  const handlePurchase = async (listing: Listing) => {
    if (!user) return;

    setIsPurchasing(true);
    try {
      await purchaseCredit({
        buyerId: Number(user.id),
        listingId: listing.id,
        qty: listing.qty, // [FIX] Luôn mua toàn bộ số lượng của Listing
      });

      toast({ title: "Success", description: "Purchase successful!" });
      setOpenDialogId(null);
      await fetchListings();
    } catch (error: any) {
      toast({
        title: "Failed",
        description: error?.response?.data?.message || "Transaction failed.",
        variant: "destructive",
      });
    } finally {
      setIsPurchasing(false);
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
      <Card>
        <CardHeader>
          <CardTitle>Marketplace (Live)</CardTitle>
          <CardDescription>Verified credits from database</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input placeholder="Search listings..." className="pl-10" />
            </div>
            <Button variant="outline">Filter</Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {listings.length === 0 ? (
          <div className="col-span-full text-center py-12 text-muted-foreground">
            No active listings found. Waiting for Admin to approve listings.
          </div>
        ) : (
          listings.map((listing) => (
            <Card key={listing.id}>
              <CardHeader>
                <div className="flex justify-between">
                  <CardTitle className="text-lg">
                    Listing #{listing.id}
                  </CardTitle>
                  <Badge className="bg-emerald-100 text-emerald-900">
                    Verified
                  </Badge>
                </div>
                <CardDescription>Seller ID: {listing.sellerId}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Price</span>
                  <span className="font-bold text-emerald-600">
                    ${listing.pricePerUnit} / tCO2
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Available</span>
                  <span className="font-bold">{listing.qty} tCO2</span>
                </div>

                <Dialog
                  open={openDialogId === listing.id}
                  onOpenChange={(o) => setOpenDialogId(o ? listing.id : null)}
                >
                  <DialogTrigger asChild>
                    <Button className="w-full bg-emerald-600 hover:bg-emerald-700">
                      Buy Now
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Confirm Purchase</DialogTitle>
                      <DialogDescription>
                        You are buying the entire listing.
                      </DialogDescription>
                    </DialogHeader>

                    {/* [FIX] Thay thế ô Input bằng thông tin xác nhận */}
                    <div className="py-4 space-y-4 bg-muted/50 p-4 rounded-lg">
                      <div className="flex justify-between">
                        <span>Quantity:</span>
                        <span className="font-medium">{listing.qty} tCO2</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Price per Unit:</span>
                        <span>${listing.pricePerUnit}</span>
                      </div>
                      <div className="border-t pt-2 flex justify-between items-center">
                        <span className="font-bold">Total Price:</span>
                        <span className="text-xl font-bold text-emerald-600">
                          ${(listing.qty * listing.pricePerUnit).toFixed(2)}
                        </span>
                      </div>
                    </div>

                    <DialogFooter>
                      <Button
                        variant="outline"
                        onClick={() => setOpenDialogId(null)}
                      >
                        Cancel
                      </Button>
                      <Button
                        onClick={() => handlePurchase(listing)}
                        disabled={isPurchasing}
                        className="bg-emerald-600 hover:bg-emerald-700"
                      >
                        {isPurchasing ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            Processing...
                          </>
                        ) : (
                          "Confirm Purchase"
                        )}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}
