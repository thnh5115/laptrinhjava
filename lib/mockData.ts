// Mock data for the Carbon Credit Marketplace
import {
  User,
  EVJourneyData,
  CarbonCredit,
  Listing,
  Transaction,
  VerificationRequest,
  Certificate,
  Bid,
  Wallet,
  WalletTransaction,
  WithdrawalRequest,
  AdminTransaction,
  FinancialReport
} from '@/types';

// Mock Users
export const mockUsers: User[] = [
  {
    id: 'user-1',
    email: 'john.tesla@email.com',
    firstName: 'John',
    lastName: 'Tesla',
    role: 'ev-owner',
    createdAt: new Date('2024-01-15'),
    status: 'active',
    rating: 4.8,
    totalTransactions: 23
  },
  {
    id: 'user-2',
    email: 'sarah.green@company.com',
    firstName: 'Sarah',
    lastName: 'Green',
    role: 'buyer',
    createdAt: new Date('2024-02-01'),
    status: 'active',
    rating: 4.9,
    totalTransactions: 15
  },
  {
    id: 'user-3',
    email: 'mike.verify@audit.com',
    firstName: 'Mike',
    lastName: 'Verify',
    role: 'cva-auditor',
    createdAt: new Date('2024-01-01'),
    status: 'active',
    totalTransactions: 156
  },
  {
    id: 'user-4',
    email: 'admin@carboncredits.com',
    firstName: 'Admin',
    lastName: 'User',
    role: 'admin',
    createdAt: new Date('2023-12-01'),
    status: 'active',
    totalTransactions: 0
  },
  {
    id: 'user-5',
    email: 'lisa.eco@email.com',
    firstName: 'Lisa',
    lastName: 'Eco',
    role: 'ev-owner',
    createdAt: new Date('2024-01-20'),
    status: 'active',
    rating: 4.6,
    totalTransactions: 8
  }
];

// Mock EV Journey Data
export const mockEVJourneyData: EVJourneyData[] = [
  {
    id: 'journey-1',
    userId: 'user-1',
    date: new Date('2024-01-15'),
    distance: 45.2,
    energyUsed: 12.8,
    co2Saved: 8.5,
    location: 'San Francisco, CA',
    vehicleModel: 'Tesla Model 3'
  },
  {
    id: 'journey-2',
    userId: 'user-1',
    date: new Date('2024-01-16'),
    distance: 67.8,
    energyUsed: 18.9,
    co2Saved: 12.7,
    location: 'San Francisco, CA',
    vehicleModel: 'Tesla Model 3'
  },
  {
    id: 'journey-3',
    userId: 'user-5',
    date: new Date('2024-01-20'),
    distance: 32.1,
    energyUsed: 9.8,
    co2Saved: 6.2,
    location: 'Portland, OR',
    vehicleModel: 'Nissan Leaf'
  }
];

// Mock Carbon Credits
export const mockCarbonCredits: CarbonCredit[] = [
  {
    id: 'credit-1',
    ownerId: 'user-1',
    amount: 2.5,
    pricePerTonne: 85.00,
    totalPrice: 212.50,
    status: 'available',
    createdAt: new Date('2024-01-20'),
    verifiedAt: new Date('2024-01-22'),
    verificationStatus: 'approved',
    cvAuditorId: 'user-3',
    metadata: {
      location: 'San Francisco, CA',
      period: 'January 2024',
      vehicleInfo: 'Tesla Model 3',
      co2Calculation: 2.5
    }
  },
  {
    id: 'credit-2',
    ownerId: 'user-5',
    amount: 1.8,
    pricePerTonne: 78.00,
    totalPrice: 140.40,
    status: 'pending',
    createdAt: new Date('2024-01-25'),
    verificationStatus: 'pending',
    metadata: {
      location: 'Portland, OR',
      period: 'January 2024',
      vehicleInfo: 'Nissan Leaf',
      co2Calculation: 1.8
    }
  },
  {
    id: 'credit-3',
    ownerId: 'user-1',
    amount: 3.2,
    pricePerTonne: 90.00,
    totalPrice: 288.00,
    status: 'sold',
    createdAt: new Date('2024-01-10'),
    verifiedAt: new Date('2024-01-12'),
    verificationStatus: 'approved',
    cvAuditorId: 'user-3',
    metadata: {
      location: 'San Francisco, CA',
      period: 'December 2023',
      vehicleInfo: 'Tesla Model 3',
      co2Calculation: 3.2
    }
  }
];

// Mock Listings
export const mockListings: Listing[] = [
  {
    id: 'listing-1',
    creditId: 'credit-1',
    sellerId: 'user-1',
    listingType: 'fixed',
    price: 85.00,
    startDate: new Date('2024-01-22'),
    status: 'active'
  },
  {
    id: 'listing-2',
    creditId: 'credit-2',
    sellerId: 'user-5',
    listingType: 'auction',
    price: 70.00,
    reservePrice: 75.00,
    startDate: new Date('2024-01-25'),
    endDate: new Date('2024-02-01'),
    status: 'active',
    bids: [
      {
        id: 'bid-1',
        listingId: 'listing-2',
        bidderId: 'user-2',
        amount: 72.00,
        timestamp: new Date('2024-01-26'),
        status: 'winning'
      }
    ]
  }
];

// Mock Transactions
export const mockTransactions: Transaction[] = [
  {
    id: 'tx-1',
    buyerId: 'user-2',
    sellerId: 'user-1',
    creditId: 'credit-3',
    listingId: 'listing-3',
    amount: 3.2,
    pricePerTonne: 90.00,
    totalPrice: 288.00,
    status: 'completed',
    timestamp: new Date('2024-01-15'),
    paymentMethod: 'Credit Card',
    certificateId: 'cert-1'
  }
];

// Mock Verification Requests
export const mockVerificationRequests: VerificationRequest[] = [
  {
    id: 'verify-1',
    creditId: 'credit-2',
    submittedBy: 'user-5',
    submittedAt: new Date('2024-01-25'),
    status: 'pending',
    documents: ['journey-data.csv', 'vehicle-registration.pdf']
  },
  {
    id: 'verify-2',
    creditId: 'credit-1',
    submittedBy: 'user-1',
    submittedAt: new Date('2024-01-20'),
    status: 'approved',
    reviewedBy: 'user-3',
    reviewedAt: new Date('2024-01-22'),
    notes: 'All documentation verified successfully',
    documents: ['journey-data.csv', 'vehicle-registration.pdf', 'emissions-calculation.pdf']
  }
];

// Mock Certificates
export const mockCertificates: Certificate[] = [
  {
    id: 'cert-1',
    transactionId: 'tx-1',
    buyerId: 'user-2',
    creditAmount: 3.2,
    issueDate: new Date('2024-01-15'),
    serialNumber: 'CC-2024-001-3200',
    status: 'valid'
  }
];

// Helper functions
export const getUserById = (id: string): User | undefined => {
  return mockUsers.find(user => user.id === id);
};

export const getCreditsByOwnerId = (ownerId: string): CarbonCredit[] => {
  return mockCarbonCredits.filter(credit => credit.ownerId === ownerId);
};

export const getListingsBySellerId = (sellerId: string): Listing[] => {
  return mockListings.filter(listing => listing.sellerId === sellerId);
};

export const getTransactionsByUserId = (userId: string): Transaction[] => {
  return mockTransactions.filter(tx => tx.buyerId === userId || tx.sellerId === userId);
};

export const getJourneyDataByUserId = (userId: string): EVJourneyData[] => {
  return mockEVJourneyData.filter(journey => journey.userId === userId);
};

export const getVerificationRequestsByCVA = (auditorId: string): VerificationRequest[] => {
  return mockVerificationRequests.filter(req => req.reviewedBy === auditorId || req.status === 'pending');
};

// Mock Wallets
export const mockWallets: Wallet[] = [
  {
    id: 'wallet-1',
    userId: 'user-1',
    balance: 1245.50,
    availableBalance: 1200.50,
    pendingBalance: 45.00,
    totalEarnings: 2890.75,
    totalWithdrawn: 1645.25,
    currency: 'USD'
  },
  {
    id: 'wallet-2',
    userId: 'user-2',
    balance: 850.00,
    availableBalance: 850.00,
    pendingBalance: 0,
    totalEarnings: 0,
    totalWithdrawn: 0,
    currency: 'USD'
  },
  {
    id: 'wallet-5',
    userId: 'user-5',
    balance: 425.80,
    availableBalance: 285.80,
    pendingBalance: 140.00,
    totalEarnings: 625.80,
    totalWithdrawn: 200.00,
    currency: 'USD'
  }
];

// Mock Wallet Transactions
export const mockWalletTransactions: WalletTransaction[] = [
  {
    id: 'wtx-1',
    walletId: 'wallet-1',
    userId: 'user-1',
    type: 'sale',
    amount: 288.00,
    balance: 1245.50,
    status: 'completed',
    description: 'Carbon credit sale - 3.2 tonnes',
    relatedTransactionId: 'tx-1',
    timestamp: new Date('2024-01-15')
  },
  {
    id: 'wtx-2',
    walletId: 'wallet-1',
    userId: 'user-1',
    type: 'withdraw',
    amount: -500.00,
    balance: 957.50,
    status: 'completed',
    description: 'Withdrawal to bank account',
    timestamp: new Date('2024-01-10')
  },
  {
    id: 'wtx-3',
    walletId: 'wallet-1',
    userId: 'user-1',
    type: 'sale',
    amount: 212.50,
    balance: 1457.50,
    status: 'pending',
    description: 'Carbon credit sale - 2.5 tonnes',
    timestamp: new Date('2024-01-22')
  },
  {
    id: 'wtx-4',
    walletId: 'wallet-2',
    userId: 'user-2',
    type: 'purchase',
    amount: -288.00,
    balance: 850.00,
    status: 'completed',
    description: 'Carbon credit purchase - 3.2 tonnes',
    relatedTransactionId: 'tx-1',
    timestamp: new Date('2024-01-15')
  },
  {
    id: 'wtx-5',
    walletId: 'wallet-5',
    userId: 'user-5',
    type: 'sale',
    amount: 140.40,
    balance: 425.80,
    status: 'pending',
    description: 'Carbon credit sale - 1.8 tonnes',
    timestamp: new Date('2024-01-25')
  }
];

// Mock Withdrawal Requests
export const mockWithdrawalRequests: WithdrawalRequest[] = [
  {
    id: 'withdraw-1',
    userId: 'user-1',
    amount: 500.00,
    status: 'completed',
    requestedAt: new Date('2024-01-08'),
    processedAt: new Date('2024-01-10'),
    processedBy: 'user-4',
    bankDetails: {
      accountName: 'John Tesla',
      accountNumber: '****1234',
      routingNumber: '****5678'
    }
  },
  {
    id: 'withdraw-2',
    userId: 'user-5',
    amount: 200.00,
    status: 'pending',
    requestedAt: new Date('2024-01-26'),
    bankDetails: {
      accountName: 'Lisa Eco',
      accountNumber: '****5678',
      routingNumber: '****9012'
    }
  }
];

// Mock Admin Transactions
export const mockAdminTransactions: AdminTransaction[] = [
  {
    id: 'admin-tx-1',
    type: 'credit_sale',
    userId: 'user-1',
    userName: 'John Tesla',
    amount: 288.00,
    status: 'completed',
    timestamp: new Date('2024-01-15'),
    details: 'Carbon credit sale - 3.2 tonnes @ $90/tonne',
    platformFee: 14.40
  },
  {
    id: 'admin-tx-2',
    type: 'credit_purchase',
    userId: 'user-2',
    userName: 'Sarah Green',
    amount: 288.00,
    status: 'completed',
    timestamp: new Date('2024-01-15'),
    details: 'Carbon credit purchase - 3.2 tonnes @ $90/tonne',
    platformFee: 14.40
  },
  {
    id: 'admin-tx-3',
    type: 'withdrawal',
    userId: 'user-1',
    userName: 'John Tesla',
    amount: 500.00,
    status: 'completed',
    timestamp: new Date('2024-01-10'),
    details: 'Withdrawal to bank account ****1234',
    platformFee: 0
  },
  {
    id: 'admin-tx-4',
    type: 'credit_sale',
    userId: 'user-5',
    userName: 'Lisa Eco',
    amount: 140.40,
    status: 'pending',
    timestamp: new Date('2024-01-25'),
    details: 'Carbon credit sale - 1.8 tonnes @ $78/tonne',
    platformFee: 7.02
  },
  {
    id: 'admin-tx-5',
    type: 'withdrawal',
    userId: 'user-5',
    userName: 'Lisa Eco',
    amount: 200.00,
    status: 'pending',
    timestamp: new Date('2024-01-26'),
    details: 'Withdrawal to bank account ****5678',
    platformFee: 0
  }
];

// Mock Financial Report
export const mockFinancialReport: FinancialReport = {
  totalRevenue: 428.40,
  totalTransactions: 5,
  pendingPayouts: 200.00,
  totalPlatformFees: 35.82,
  totalCreditsTraded: 7.2,
  averageTransactionValue: 209.40,
  period: 'January 2024'
};

// Wallet helper functions
export const getWalletByUserId = (userId: string): Wallet | undefined => {
  return mockWallets.find(wallet => wallet.userId === userId);
};

export const getWalletTransactionsByUserId = (userId: string): WalletTransaction[] => {
  return mockWalletTransactions.filter(tx => tx.userId === userId).sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
};

export const getWithdrawalRequestsByUserId = (userId: string): WithdrawalRequest[] => {
  return mockWithdrawalRequests.filter(req => req.userId === userId).sort((a, b) => b.requestedAt.getTime() - a.requestedAt.getTime());
};

export const getAllWithdrawalRequests = (): WithdrawalRequest[] => {
  return mockWithdrawalRequests.sort((a, b) => b.requestedAt.getTime() - a.requestedAt.getTime());
};

export const getAdminTransactions = (): AdminTransaction[] => {
  return mockAdminTransactions.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
};

export const getFinancialReport = (): FinancialReport => {
  return mockFinancialReport;
};

// Dashboard statistics
export const getDashboardStats = (userId: string, role: string) => {
  switch (role) {
    case 'ev-owner':
      const ownerCredits = getCreditsByOwnerId(userId);
      const ownerTransactions = getTransactionsByUserId(userId);
      return {
        totalCredits: ownerCredits.length,
        totalRevenue: ownerTransactions.reduce((sum, tx) => tx.sellerId === userId ? sum + tx.totalPrice : sum, 0),
        totalCO2Offset: ownerCredits.reduce((sum, credit) => sum + credit.amount, 0),
        activeListings: getListingsBySellerId(userId).filter(listing => listing.status === 'active').length,
        monthlyGrowth: 12.5
      };
    case 'buyer':
      const buyerTransactions = getTransactionsByUserId(userId);
      return {
        totalCredits: buyerTransactions.filter(tx => tx.buyerId === userId).reduce((sum, tx) => sum + tx.amount, 0),
        totalRevenue: buyerTransactions.filter(tx => tx.buyerId === userId).reduce((sum, tx) => sum + tx.totalPrice, 0),
        totalCO2Offset: buyerTransactions.filter(tx => tx.buyerId === userId).reduce((sum, tx) => sum + tx.amount, 0),
        activeListings: 0,
        monthlyGrowth: 8.3
      };
    case 'cva-auditor':
      const auditRequests = getVerificationRequestsByCVA(userId);
      return {
        totalCredits: auditRequests.length,
        totalRevenue: 0,
        totalCO2Offset: 0,
        activeListings: 0,
        pendingVerifications: auditRequests.filter(req => req.status === 'pending').length,
        monthlyGrowth: 15.2
      };
    case 'admin':
      return {
        totalCredits: mockCarbonCredits.length,
        totalRevenue: mockTransactions.reduce((sum, tx) => sum + tx.totalPrice, 0),
        totalCO2Offset: mockCarbonCredits.reduce((sum, credit) => sum + credit.amount, 0),
        activeListings: mockListings.filter(listing => listing.status === 'active').length,
        totalUsers: mockUsers.length,
        monthlyGrowth: 22.1
      };
    default:
      return {
        totalCredits: 0,
        totalRevenue: 0,
        totalCO2Offset: 0,
        activeListings: 0,
        monthlyGrowth: 0
      };
  }
};