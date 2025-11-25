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
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Search, Eye, Check, X, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import {
  getListings,
  ListingStatus,
  type ListingSummary,
} from "@/lib/api/admin-listings";
import { ListingActions } from "./listing-actions";

export function ListingModerator() {
  const [listings, setListings] = useState<ListingSummary[]>([]);
  const [selectedListing, setSelectedListing] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isError, setIsError] = useState(false);
  const { toast } = useToast();

  // Debounce search term
  const [debouncedSearch, setDebouncedSearch] = useState(searchTerm);
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(searchTerm), 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Fetch listings
  const fetchListings = async () => {
    setIsLoading(true);
    setIsError(false);
    try {
      const params: any = {
        page,
        size: 20,
        sort: "createdAt,desc",
      };
      if (debouncedSearch) params.keyword = debouncedSearch;
      if (statusFilter !== "all") params.status = statusFilter.toUpperCase();

      const data = await getListings(params);
      setListings(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error: any) {
      setIsError(true);
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to load listings",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchListings();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, debouncedSearch, statusFilter]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-amber-100 text-amber-900 dark:bg-amber-900 dark:text-amber-100";
      case "APPROVED":
        return "bg-emerald-100 text-emerald-900 dark:bg-emerald-900 dark:text-emerald-100";
      case "REJECTED":
        return "bg-red-100 text-red-900 dark:bg-red-900 dark:text-red-100";
      case "DELISTED":
        return "bg-slate-200 text-slate-900 dark:bg-slate-900 dark:text-slate-100";
      case "SOLD":
        return "bg-blue-100 text-blue-900 dark:bg-blue-900 dark:text-blue-100";
      default:
        return "";
    }
  };

  const handleRefresh = () => {
    setPage(0);
    fetchListings();
  };

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Marketplace Listings</CardTitle>
              <CardDescription>
                Review and moderate carbon credit listings
              </CardDescription>
            </div>
            <div className="flex gap-2">
              <div className="relative w-64">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search listings..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-8"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-32">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="APPROVED">Approved</SelectItem>
                  <SelectItem value="REJECTED">Rejected</SelectItem>
                  <SelectItem value="DELISTED">Delisted</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading && (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          )}

          {isError && !isLoading && (
            <div className="text-center py-8">
              <p className="text-muted-foreground">Failed to load listings</p>
              <Button
                variant="outline"
                onClick={fetchListings}
                className="mt-4"
              >
                Retry
              </Button>
            </div>
          )}

          {!isLoading && !isError && (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Title</TableHead>
                    <TableHead>Owner</TableHead>
                    <TableHead>Credits</TableHead>
                    <TableHead>Price/Credit</TableHead>
                    <TableHead>Total</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {listings.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={9}
                        className="text-center text-muted-foreground"
                      >
                        No listings found
                      </TableCell>
                    </TableRow>
                  ) : (
                    listings.map((listing) => {
                      const credits = Number(listing.quantity ?? 0);
                      const pricePerCredit = Number(listing.price ?? 0);
                      const totalPrice = pricePerCredit * credits;
                      const ownerName =
                        listing.ownerFullName || listing.ownerEmail;
                      return (
                        <TableRow key={listing.id}>
                          <TableCell className="font-mono text-xs">
                            {listing.id}
                          </TableCell>
                          <TableCell
                            className="font-medium max-w-[200px] truncate"
                            title={listing.title}
                          >
                            {listing.title}
                          </TableCell>
                          <TableCell>
                            <div>
                              <p className="font-medium">{ownerName}</p>
                              <p className="text-xs text-muted-foreground">
                                {listing.ownerEmail}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell>
                            {credits} {listing.unit || "tCO2"}
                          </TableCell>
                          <TableCell>${pricePerCredit.toFixed(2)}</TableCell>
                          <TableCell className="font-medium">
                            ${totalPrice.toFixed(2)}
                          </TableCell>
                          <TableCell>
                            <Badge className={getStatusColor(listing.status)}>
                              {listing.status}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {new Date(listing.createdAt).toLocaleDateString()}
                          </TableCell>
                          <TableCell>
                            <div className="flex gap-1">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => setSelectedListing(listing.id)}
                              >
                                <Eye className="h-4 w-4 mr-1" />
                                View
                              </Button>
                              {listing.status === "PENDING" && (
                                <>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    className="text-emerald-600 hover:text-emerald-700"
                                    onClick={() =>
                                      setSelectedListing(listing.id)
                                    }
                                  >
                                    <Check className="h-4 w-4" />
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    className="text-red-600 hover:text-red-700"
                                    onClick={() =>
                                      setSelectedListing(listing.id)
                                    }
                                  >
                                    <X className="h-4 w-4" />
                                  </Button>
                                </>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>

              {/* Pagination */}
              <div className="flex items-center justify-between mt-4 pt-4 border-t">
                <p className="text-sm text-muted-foreground">
                  Showing {listings.length} of {totalElements} listings (Page{" "}
                  {page + 1} / {totalPages || 1})
                </p>
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0 || isLoading}
                  >
                    Previous
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setPage((p) => p + 1)}
                    disabled={page >= totalPages - 1 || isLoading}
                  >
                    Next
                  </Button>
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {selectedListing !== null && (
        <ListingActions
          listingId={selectedListing}
          onClose={() => setSelectedListing(null)}
          onSuccess={handleRefresh}
        />
      )}
    </>
  );
}
