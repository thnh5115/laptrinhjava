'use client';

import { useState, useMemo } from 'react';
import { useAppContext } from '@/contexts/AppContext';
import { DashboardLayout } from '@/components/layout/DashboardLayout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Slider } from '@/components/ui/slider';
import { Search, Filter, Grid2x2 as Grid, List, Clock, MapPin, User, Star } from 'lucide-react';
import { MarketplaceFilters, Listing, CarbonCredit } from '@/types';
import { getUserById } from '@/lib/mockData';

export default function MarketplacePage() {
  const { state } = useAppContext();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<MarketplaceFilters>({
    priceMin: 0,
    priceMax: 200,
    sortBy: 'date',
    sortOrder: 'desc'
  });
  const [searchQuery, setSearchQuery] = useState('');

  // Get active listings with credit data
  const activeListings = useMemo(() => {
    return state.listings
      .filter(listing => listing.status === 'active')
      .map(listing => {
        const credit = state.credits.find(c => c.id === listing.creditId);
        const seller = getUserById(listing.sellerId);
        return { listing, credit, seller };
      })
      .filter((item): item is { listing: Listing; credit: CarbonCredit; seller: any } =>
        item.credit !== undefined && item.seller !== undefined
      );
  }, [state.listings, state.credits]);

  // Apply filters
  const filteredListings = useMemo(() => {
    let filtered = activeListings;

    // Search filter
    if (searchQuery) {
      filtered = filtered.filter(item =>
        item.credit?.metadata.location.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.seller?.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.seller?.lastName.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Price filter
    if (filters.priceMin !== undefined) {
      filtered = filtered.filter(item => item.listing.price >= filters.priceMin!);
    }
    if (filters.priceMax !== undefined) {
      filtered = filtered.filter(item => item.listing.price <= filters.priceMax!);
    }

    // Amount filter
    if (filters.amountMin !== undefined) {
      filtered = filtered.filter(item => item.credit!.amount >= filters.amountMin!);
    }
    if (filters.amountMax !== undefined) {
      filtered = filtered.filter(item => item.credit!.amount <= filters.amountMax!);
    }

    // Location filter
    if (filters.location) {
      filtered = filtered.filter(item =>
        item.credit?.metadata.location.toLowerCase().includes(filters.location!.toLowerCase())
      );
    }

    // Seller rating filter
    if (filters.sellerRating) {
      filtered = filtered.filter(item =>
        (item.seller?.rating || 0) >= filters.sellerRating!
      );
    }

    // Listing type filter
    if (filters.listingType && filters.listingType !== 'all') {
      filtered = filtered.filter(item => item.listing.listingType === filters.listingType);
    }

    // Sort
    filtered.sort((a, b) => {
      const multiplier = filters.sortOrder === 'asc' ? 1 : -1;
      switch (filters.sortBy) {
        case 'price':
          return (a.listing.price - b.listing.price) * multiplier;
        case 'amount':
          return (a.credit!.amount - b.credit!.amount) * multiplier;
        case 'rating':
          return ((a.seller?.rating || 0) - (b.seller?.rating || 0)) * multiplier;
        case 'date':
        default:
          return (new Date(a.listing.startDate).getTime() - new Date(b.listing.startDate).getTime()) * multiplier;
      }
    });

    return filtered;
  }, [activeListings, searchQuery, filters]);

  const handlePurchase = (listingId: string) => {
    // In a real app, this would open a purchase modal
    console.log('Purchase listing:', listingId);
  };

  const handleBid = (listingId: string) => {
    // In a real app, this would open a bidding modal
    console.log('Place bid on listing:', listingId);
  };

  const getTimeRemaining = (endDate: Date) => {
    const now = new Date();
    const diff = endDate.getTime() - now.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h`;
    return 'Ending soon';
  };

  const renderListingCard = (item: { listing: Listing; credit: CarbonCredit; seller: any }, index: number) => {
    const { listing, credit, seller } = item;
    const isAuction = listing.listingType === 'auction';
    const currentBid = listing.bids && listing.bids.length > 0 
      ? Math.max(...listing.bids.map(b => b.amount))
      : listing.price;

    if (viewMode === 'list') {
      return (
        <Card key={listing.id} className="mb-4">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div className="flex-1 grid grid-cols-1 md:grid-cols-5 gap-4 items-center">
                <div>
                  <h3 className="font-semibold">{credit.amount.toFixed(1)} tonnes CO₂</h3>
                  <p className="text-sm text-gray-500">{credit.metadata.location}</p>
                </div>
                <div className="flex items-center space-x-2">
                  <User className="w-4 h-4 text-gray-400" />
                  <span className="text-sm">{seller.firstName} {seller.lastName}</span>
                  <div className="flex items-center">
                    <Star className="w-4 h-4 text-yellow-400 fill-current" />
                    <span className="text-sm ml-1">{seller.rating}</span>
                  </div>
                </div>
                <div>
                  <Badge variant={isAuction ? "secondary" : "default"}>
                    {isAuction ? 'Auction' : 'Fixed Price'}
                  </Badge>
                </div>
                <div className="text-right">
                  <p className="font-bold text-lg">${currentBid.toFixed(2)}</p>
                  <p className="text-sm text-gray-500">per tonne</p>
                  {isAuction && listing.endDate && (
                    <p className="text-xs text-orange-600 flex items-center">
                      <Clock className="w-3 h-3 mr-1" />
                      {getTimeRemaining(listing.endDate)}
                    </p>
                  )}
                </div>
                <div>
                  {isAuction ? (
                    <Button onClick={() => handleBid(listing.id)} variant="outline">
                      Place Bid
                    </Button>
                  ) : (
                    <Button onClick={() => handlePurchase(listing.id)}>
                      Buy Now
                    </Button>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      );
    }

    return (
      <Card key={listing.id} className="group hover:shadow-lg transition-shadow">
        <CardContent className="p-6">
          <div className="flex justify-between items-start mb-4">
            <Badge variant={isAuction ? "secondary" : "default"}>
              {isAuction ? 'Auction' : 'Fixed Price'}
            </Badge>
            {isAuction && listing.endDate && (
              <div className="text-right">
                <p className="text-xs text-orange-600 flex items-center">
                  <Clock className="w-3 h-3 mr-1" />
                  {getTimeRemaining(listing.endDate)}
                </p>
              </div>
            )}
          </div>
          
          <div className="space-y-3">
            <div>
              <h3 className="text-xl font-bold">{credit.amount.toFixed(1)} tonnes CO₂</h3>
              <p className="text-gray-600 flex items-center">
                <MapPin className="w-4 h-4 mr-1" />
                {credit.metadata.location}
              </p>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <User className="w-4 h-4 text-gray-400" />
                <span className="text-sm">{seller.firstName} {seller.lastName}</span>
                <div className="flex items-center">
                  <Star className="w-4 h-4 text-yellow-400 fill-current" />
                  <span className="text-sm">{seller.rating}</span>
                </div>
              </div>
            </div>
            
            <div className="border-t pt-3">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-2xl font-bold">${currentBid.toFixed(2)}</p>
                  <p className="text-sm text-gray-500">per tonne</p>
                  {isAuction && listing.bids && listing.bids.length > 0 && (
                    <p className="text-xs text-green-600">{listing.bids.length} bids</p>
                  )}
                </div>
                {isAuction ? (
                  <Button onClick={() => handleBid(listing.id)} className="w-24">
                    Place Bid
                  </Button>
                ) : (
                  <Button onClick={() => handlePurchase(listing.id)} className="w-24">
                    Buy Now
                  </Button>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  };

  // Only show marketplace for buyers
  if (state.currentUser?.role !== 'buyer') {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h2>
          <p className="text-gray-600">This section is only available for buyers.</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Carbon Credit Marketplace</h1>
            <p className="text-gray-600">Browse and purchase verified carbon credits from EV owners</p>
          </div>
          
          <div className="flex items-center space-x-2">
            <Button
              variant={viewMode === 'grid' ? 'default' : 'outline'}
              size="icon"
              onClick={() => setViewMode('grid')}
            >
              <Grid className="w-4 h-4" />
            </Button>
            <Button
              variant={viewMode === 'list' ? 'default' : 'outline'}
              size="icon"
              onClick={() => setViewMode('list')}
            >
              <List className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Search and Filter Bar */}
        <Card>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <Input
                    placeholder="Search by location, seller..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10"
                  />
                </div>
              </div>
              
              <Button
                variant="outline"
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center space-x-2"
              >
                <Filter className="w-4 h-4" />
                <span>Filters</span>
              </Button>
            </div>

            {showFilters && (
              <div className="mt-6 pt-6 border-t">
                <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
                  <div className="space-y-2">
                    <Label>Price Range ($)</Label>
                    <div className="px-2">
                      <Slider
                        value={[filters.priceMin || 0, filters.priceMax || 200]}
                        onValueChange={([min, max]) => {
                          setFilters(prev => ({ ...prev, priceMin: min, priceMax: max }));
                        }}
                        max={200}
                        step={10}
                        className="w-full"
                      />
                      <div className="flex justify-between text-xs text-gray-500 mt-1">
                        <span>${filters.priceMin}</span>
                        <span>${filters.priceMax}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="location">Location</Label>
                    <Input
                      id="location"
                      placeholder="Any location"
                      value={filters.location || ''}
                      onChange={(e) => setFilters(prev => ({ ...prev, location: e.target.value }))}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="listingType">Type</Label>
                    <Select
                      value={filters.listingType || 'all'}
                      onValueChange={(value) => setFilters(prev => ({ ...prev, listingType: value as any }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Types</SelectItem>
                        <SelectItem value="fixed">Fixed Price</SelectItem>
                        <SelectItem value="auction">Auction</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="sortBy">Sort By</Label>
                    <Select
                      value={filters.sortBy}
                      onValueChange={(value) => setFilters(prev => ({ ...prev, sortBy: value as any }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="date">Date Listed</SelectItem>
                        <SelectItem value="price">Price</SelectItem>
                        <SelectItem value="amount">Amount</SelectItem>
                        <SelectItem value="rating">Seller Rating</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="sortOrder">Order</Label>
                    <Select
                      value={filters.sortOrder}
                      onValueChange={(value) => setFilters(prev => ({ ...prev, sortOrder: value as any }))}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="desc">High to Low</SelectItem>
                        <SelectItem value="asc">Low to High</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Results Summary */}
        <div className="flex justify-between items-center">
          <p className="text-gray-600">
            {filteredListings.length} credits available
          </p>
        </div>

        {/* Listings */}
        {viewMode === 'grid' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredListings.map((item, index) => renderListingCard(item, index))}
          </div>
        ) : (
          <div>
            {filteredListings.map((item, index) => renderListingCard(item, index))}
          </div>
        )}

        {filteredListings.length === 0 && (
          <Card>
            <CardContent className="text-center py-12">
              <h3 className="text-lg font-semibold text-gray-900 mb-2">No listings found</h3>
              <p className="text-gray-600">Try adjusting your search criteria or filters.</p>
            </CardContent>
          </Card>
        )}
      </div>
    </DashboardLayout>
  );
}