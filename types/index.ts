// Types for the Carbon Credit Marketplace

export type UserRole = 'ev-owner' | 'buyer' | 'cva-auditor' | 'admin';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  profileImage?: string;
  createdAt: Date;
  status: 'active' | 'suspended' | 'pending';
  rating?: number;
  totalTransactions?: number;
}

export interface EVJourneyData {
  id: string;
  userId: string;
  date: Date;
  distance: number; // km
  energyUsed: number; // kWh
  co2Saved: number; // kg
  location: string;
  vehicleModel: string;
}

export interface CarbonCredit {
  id: string;
  ownerId: string;
  amount: number; // tonnes CO2
  pricePerTonne: number;
  totalPrice: number;
  status: 'available' | 'sold' | 'pending' | 'verified' | 'rejected';
  createdAt: Date;
  verifiedAt?: Date;
  verificationStatus: 'pending' | 'approved' | 'rejected';
  cvAuditorId?: string;
  metadata: {
    location: string;
    period: string;
    vehicleInfo: string;
    co2Calculation: number;
  };
}

export interface Listing {
  id: string;
  creditId: string;
  sellerId: string;
  listingType: 'fixed' | 'auction';
  price: number;
  reservePrice?: number;
  startDate: Date;
  endDate?: Date;
  status: 'active' | 'completed' | 'cancelled' | 'expired';
  bids?: Bid[];
  buyNowPrice?: number;
}

export interface Bid {
  id: string;
  listingId: string;
  bidderId: string;
  amount: number;
  timestamp: Date;
  status: 'active' | 'outbid' | 'winning' | 'won';
}

export interface Transaction {
  id: string;
  buyerId: string;
  sellerId: string;
  creditId: string;
  listingId: string;
  amount: number;
  pricePerTonne: number;
  totalPrice: number;
  status: 'completed' | 'pending' | 'failed' | 'refunded';
  timestamp: Date;
  paymentMethod: string;
  certificateId?: string;
}

export interface VerificationRequest {
  id: string;
  creditId: string;
  submittedBy: string;
  submittedAt: Date;
  status: 'pending' | 'approved' | 'rejected' | 'in-review';
  reviewedBy?: string;
  reviewedAt?: Date;
  notes?: string;
  documents: string[];
}

export interface Certificate {
  id: string;
  transactionId: string;
  buyerId: string;
  creditAmount: number;
  issueDate: Date;
  serialNumber: string;
  status: 'valid' | 'retired' | 'cancelled';
}

export interface DashboardStats {
  totalCredits: number;
  totalRevenue: number;
  totalCO2Offset: number;
  activeListings: number;
  pendingVerifications?: number;
  totalUsers?: number;
  monthlyGrowth: number;
}

export interface MarketplaceFilters {
  priceMin?: number;
  priceMax?: number;
  amountMin?: number;
  amountMax?: number;
  location?: string;
  sellerRating?: number;
  listingType?: 'fixed' | 'auction' | 'all';
  sortBy?: 'price' | 'amount' | 'date' | 'rating';
  sortOrder?: 'asc' | 'desc';
}

export interface Wallet {
  id: string;
  userId: string;
  balance: number;
  availableBalance: number;
  pendingBalance: number;
  totalEarnings: number;
  totalWithdrawn: number;
  currency: 'USD';
}

export interface WalletTransaction {
  id: string;
  walletId: string;
  userId: string;
  type: 'credit' | 'debit' | 'withdraw' | 'purchase' | 'sale';
  amount: number;
  balance: number;
  status: 'completed' | 'pending' | 'failed' | 'cancelled';
  description: string;
  relatedTransactionId?: string;
  timestamp: Date;
}

export interface WithdrawalRequest {
  id: string;
  userId: string;
  amount: number;
  status: 'pending' | 'approved' | 'rejected' | 'completed';
  requestedAt: Date;
  processedAt?: Date;
  processedBy?: string;
  bankDetails?: {
    accountName: string;
    accountNumber: string;
    routingNumber: string;
  };
  notes?: string;
}

export interface AdminTransaction {
  id: string;
  type: 'credit_sale' | 'credit_purchase' | 'withdrawal' | 'platform_fee' | 'refund';
  userId: string;
  userName: string;
  amount: number;
  status: 'completed' | 'pending' | 'failed' | 'cancelled';
  timestamp: Date;
  details: string;
  platformFee: number;
}

export interface FinancialReport {
  totalRevenue: number;
  totalTransactions: number;
  pendingPayouts: number;
  totalPlatformFees: number;
  totalCreditsTraded: number;
  averageTransactionValue: number;
  period: string;
}