// Common types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// User models
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'BUYER' | 'OWNER' | 'CVA';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  updatedAt: string;
}

// Credit models
export interface CreditListing {
  id: string;
  ownerId: string;
  ownerName: string;
  title: string;
  description: string;
  quantity: number;
  availableQuantity: number;
  pricePerCredit: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SOLD_OUT';
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

// Transaction models
export interface Transaction {
  id: string;
  buyerId: string;
  buyerName: string;
  sellerId: string;
  sellerName: string;
  listingId: string;
  listingTitle: string;
  quantity: number;
  pricePerCredit: number;
  totalAmount: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  transactionDate: string;
  completedAt?: string;
}

// Certificate models
export interface Certificate {
  id: string;
  userId: string;
  transactionId: string;
  certificateNumber: string;
  creditsAmount: number;
  co2OffsetKg: number;
  issuedAt: string;
  downloadUrl: string;
}

// Verification models
export interface VerificationRequest {
  id: string;
  listingId: string;
  ownerId: string;
  ownerName: string;
  cvaId?: string;
  cvaName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'INFO_REQUESTED';
  submittedAt: string;
  reviewedAt?: string;
  comments?: string;
  rejectionReason?: string;
}

// Wallet models
export interface WalletBalance {
  userId: string;
  balance: number;
  currency: string;
  pendingBalance: number;
  lastUpdated: string;
}

// Analytics models
export interface AnalyticsData {
  totalUsers: number;
  totalTransactions: number;
  totalRevenue: number;
  totalCO2Offset: number;
  activeListings: number;
  pendingVerifications: number;
  conversionRate: number;
  averageTransactionValue: number;
}
