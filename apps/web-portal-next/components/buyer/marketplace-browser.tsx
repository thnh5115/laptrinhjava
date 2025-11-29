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
import { Badge } from "@/components/ui/badge";
import { Search, Loader2, AlertCircle, Zap } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import {
  getListings,
  purchaseCredit,
  quickBuyCredits, // [MỚI] Đừng quên import hàm này từ api/buyer
  type Listing,
} from "@/lib/api/buyer";

export function MarketplaceBrowser() {
  const { user } = useAuth();
  const { toast } = useToast();

  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);

  // --- State cho Mua Lẻ (Partial Buy) ---
  const [buyAmount, setBuyAmount] = useState<number>(1);
  const [isPurchasing, setIsPurchasing] = useState(false);
  const [openDialogId, setOpenDialogId] = useState<number | null>(null);

  // --- State cho Mua Nhanh (Quick Buy) ---
  const [quickBuyOpen, setQuickBuyOpen] = useState(false);
  const [wantedQty, setWantedQty] = useState(10);
  const [isQuickBuying, setIsQuickBuying] = useState(false);

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

  // Xử lý Mua Lẻ (Partial Buy)
  const handlePurchase = async (listing: Listing) => {
    if (!user) return;

    if (buyAmount <= 0 || buyAmount > listing.qty) {
      toast({
        title: "Invalid Quantity",
        description: `Please enter a quantity between 1 and ${listing.qty}`,
        variant: "destructive",
      });
      return;
    }

    setIsPurchasing(true);
    try {
      await purchaseCredit({
        buyerId: Number(user.id),
        listingId: listing.id,
        qty: buyAmount,
      });

      toast({
        title: "Success",
        description: `Successfully purchased ${buyAmount} credits!`,
      });
      setOpenDialogId(null);
      setBuyAmount(1);
      await fetchListings();
    } catch (error: any) {
      toast({
        title: "Transaction Failed",
        description:
          error?.response?.data?.message || "Could not complete purchase.",
        variant: "destructive",
      });
    } finally {
      setIsPurchasing(false);
    }
  };

  // Xử lý Mua Nhanh (Quick Buy)
  const handleQuickBuy = async () => {
    if (!user) return;

    if (wantedQty <= 0) {
      toast({
        title: "Invalid Amount",
        description: "Please enter a valid amount.",
        variant: "destructive",
      });
      return;
    }

    setIsQuickBuying(true);
    try {
      await quickBuyCredits({
        buyerId: Number(user.id),
        qty: wantedQty,
      });

      toast({
        title: "Quick Buy Complete",
        description: `Successfully aggregated and purchased ${wantedQty} credits!`,
      });
      setQuickBuyOpen(false);
      await fetchListings();
    } catch (error: any) {
      toast({
        title: "Quick Buy Failed",
        description:
          error?.response?.data?.message ||
          "Not enough credits available in the market.",
        variant: "destructive",
      });
    } finally {
      setIsQuickBuying(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* === PHẦN 1: QUICK BUY (MUA GỘP) === */}
      <Card className="bg-gradient-to-r from-emerald-50 to-teal-50 border-emerald-200 shadow-sm">
        <CardContent className="p-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-bold text-emerald-800 flex items-center gap-2">
              <Zap className="h-5 w-5 text-yellow-600 fill-yellow-500" />
              Quick Buy / Auto-Match
            </h3>
            <p className="text-sm text-emerald-600 mt-1">
              Need a large amount? We will automatically aggregate the cheapest
              listings for you.
            </p>
          </div>

          <Dialog open={quickBuyOpen} onOpenChange={setQuickBuyOpen}>
            <DialogTrigger asChild>
              <Button
                size="lg"
                className="bg-emerald-600 hover:bg-emerald-700 shadow-md min-w-[180px]"
              >
                Start Quick Buy
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Quick Buy Credits</DialogTitle>
                <DialogDescription>
                  Enter the total amount you need. The system will match
                  multiple listings starting from the lowest price.
                </DialogDescription>
              </DialogHeader>
              <div className="py-6 space-y-4">
                <div className="flex items-center gap-4 justify-center">
                  <Input
                    type="number"
                    min="1"
                    value={wantedQty}
                    onChange={(e) => setWantedQty(Number(e.target.value))}
                    className="text-3xl font-bold w-40 text-center h-16"
                  />
                  <span className="text-xl font-medium text-muted-foreground">
                    tCO2
                  </span>
                </div>
                <div className="bg-amber-50 p-3 rounded-md border border-amber-100 text-amber-800 text-sm flex gap-2">
                  <AlertCircle className="h-5 w-5 shrink-0" />
                  <p>
                    This will execute multiple transactions instantly until the
                    requested amount is filled.
                  </p>
                </div>
              </div>
              <DialogFooter>
                <Button
                  variant="outline"
                  onClick={() => setQuickBuyOpen(false)}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleQuickBuy}
                  disabled={isQuickBuying}
                  className="bg-emerald-600 hover:bg-emerald-700 text-white"
                >
                  {isQuickBuying ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />{" "}
                      Processing...
                    </>
                  ) : (
                    "Find & Buy Best Price"
                  )}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </CardContent>
      </Card>

      {/* === PHẦN 2: MARKETPLACE BROWSER (MUA LẺ) === */}
      <Card>
        <CardHeader>
          <CardTitle>Marketplace Listings</CardTitle>
          <CardDescription>
            Browse individual verified carbon credit listings
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by seller or ID..."
                className="pl-10"
              />
            </div>
            <Button variant="outline">Filter</Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {listings.length === 0 ? (
          <div className="col-span-full text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border-dashed border-2">
            <p>No active listings found.</p>
            <p className="text-sm mt-1">Check back later for new credits.</p>
          </div>
        ) : (
          listings.map((listing) => (
            <Card
              key={listing.id}
              className="flex flex-col hover:shadow-md transition-shadow"
            >
              <CardHeader className="pb-3">
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg">
                      Listing #{listing.id}
                    </CardTitle>
                    <CardDescription>
                      Seller ID: {listing.sellerId}
                    </CardDescription>
                  </div>
                  <Badge className="bg-emerald-100 text-emerald-900 hover:bg-emerald-200 border-0">
                    Verified
                  </Badge>
                </div>
              </CardHeader>

              <CardContent className="space-y-4 flex-1">
                <div className="grid grid-cols-2 gap-2">
                  <div className="p-3 bg-slate-50 dark:bg-slate-900 rounded-lg text-center">
                    <span className="block text-xs text-muted-foreground mb-1">
                      Price/Unit
                    </span>
                    <span className="text-lg font-bold text-emerald-600">
                      ${listing.pricePerUnit}
                    </span>
                  </div>
                  <div className="p-3 bg-slate-50 dark:bg-slate-900 rounded-lg text-center">
                    <span className="block text-xs text-muted-foreground mb-1">
                      Available
                    </span>
                    <span className="text-lg font-bold">
                      {listing.qty} tCO2
                    </span>
                  </div>
                </div>

                <Dialog
                  open={openDialogId === listing.id}
                  onOpenChange={(isOpen) => {
                    setOpenDialogId(isOpen ? listing.id : null);
                    if (isOpen) setBuyAmount(1);
                  }}
                >
                  <DialogTrigger asChild>
                    <Button className="w-full bg-slate-900 hover:bg-slate-800 text-white shadow-sm mt-2">
                      Buy Credits
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Purchase Credits</DialogTitle>
                      <DialogDescription>
                        Buying from Listing #{listing.id}
                      </DialogDescription>
                    </DialogHeader>

                    <div className="py-4 space-y-4">
                      <div className="space-y-2">
                        <Label>Quantity (Max: {listing.qty})</Label>
                        <div className="flex items-center gap-2">
                          <Input
                            type="number"
                            min="1"
                            max={listing.qty}
                            value={buyAmount}
                            onChange={(e) =>
                              setBuyAmount(Number(e.target.value))
                            }
                          />
                          <span className="text-sm font-medium">tCO2</span>
                        </div>
                      </div>

                      <div className="rounded-md bg-muted p-4 space-y-3">
                        <div className="flex justify-between text-sm">
                          <span className="text-muted-foreground">
                            Unit Price:
                          </span>
                          <span>${listing.pricePerUnit}</span>
                        </div>
                        <div className="border-t pt-3 flex justify-between items-center">
                          <span className="font-bold">Total Cost:</span>
                          <span className="text-xl font-bold text-emerald-600">
                            ${(buyAmount * listing.pricePerUnit).toFixed(2)}
                          </span>
                        </div>
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
                        disabled={
                          isPurchasing ||
                          buyAmount <= 0 ||
                          buyAmount > listing.qty
                        }
                        className="bg-emerald-600 hover:bg-emerald-700 text-white"
                      >
                        {isPurchasing ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />{" "}
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
