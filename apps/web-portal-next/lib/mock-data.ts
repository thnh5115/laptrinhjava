export type UserRole = "ev-owner" | "buyer" | "cva" | "admin"

export interface User {
  id: string
  email: string
  password: string
  role: UserRole
  name: string
  avatar?: string
}

export interface Journey {
  id: string
  userId: string
  date: string
  startLocation: string
  endLocation: string
  distance: number
  energyUsed: number
  creditsGenerated: number
  status: "pending" | "verified" | "rejected"
  verifiedBy?: string
  verifiedAt?: string
  rejectionReason?: string
}

export interface CarbonCredit {
  id: string
  journeyId: string
  ownerId: string
  amount: number
  pricePerCredit: number
  status: "available" | "sold" | "reserved"
  listedAt: string
  soldAt?: string
  buyerId?: string
}

export interface Transaction {
  id: string
  creditId: string
  buyerId: string
  sellerId: string
  amount: number
  pricePerCredit: number
  totalPrice: number
  timestamp: string
  status: "completed" | "pending" | "failed"
}

// Mock users
export const mockUsers: User[] = [
  {
    id: "1",
    email: "owner@example.com",
    password: "password",
    role: "ev-owner",
    name: "John Doe",
    avatar: "/man.jpg",
  },
  {
    id: "2",
    email: "buyer@example.com",
    password: "password",
    role: "buyer",
    name: "Jane Smith",
    avatar: "/diverse-woman-portrait.png",
  },
  {
    id: "3",
    email: "cva@example.com",
    password: "password",
    role: "cva",
    name: "Mike Verifier",
    avatar: "/inspector.png",
  },
  {
    id: "4",
    email: "admin@example.com",
    password: "password",
    role: "admin",
    name: "Sarah Admin",
    avatar: "/diverse-team-manager.png",
  },
]

// Mock journeys
export const mockJourneys: Journey[] = [
  {
    id: "j1",
    userId: "1",
    date: "2025-01-08",
    startLocation: "San Francisco, CA",
    endLocation: "San Jose, CA",
    distance: 48,
    energyUsed: 12.5,
    creditsGenerated: 15.2,
    status: "verified",
    verifiedBy: "3",
    verifiedAt: "2025-01-09T10:30:00Z",
  },
  {
    id: "j2",
    userId: "1",
    date: "2025-01-10",
    startLocation: "San Jose, CA",
    endLocation: "Palo Alto, CA",
    distance: 22,
    energyUsed: 5.8,
    creditsGenerated: 7.1,
    status: "pending",
  },
  {
    id: "j3",
    userId: "1",
    date: "2025-01-05",
    startLocation: "Oakland, CA",
    endLocation: "Berkeley, CA",
    distance: 12,
    energyUsed: 3.2,
    creditsGenerated: 3.8,
    status: "rejected",
    verifiedBy: "3",
    rejectionReason: "Insufficient documentation provided",
  },
]

// Mock carbon credits
export const mockCredits: CarbonCredit[] = [
  {
    id: "c1",
    journeyId: "j1",
    ownerId: "1",
    amount: 15.2,
    pricePerCredit: 25,
    status: "available",
    listedAt: "2025-01-09T12:00:00Z",
  },
  {
    id: "c2",
    journeyId: "j4",
    ownerId: "5",
    amount: 22.5,
    pricePerCredit: 23,
    status: "available",
    listedAt: "2025-01-08T09:00:00Z",
  },
  {
    id: "c3",
    journeyId: "j5",
    ownerId: "6",
    amount: 18.0,
    pricePerCredit: 24,
    status: "sold",
    listedAt: "2025-01-07T14:00:00Z",
    soldAt: "2025-01-08T16:30:00Z",
    buyerId: "2",
  },
]

// Mock transactions
export const mockTransactions: Transaction[] = [
  {
    id: "t1",
    creditId: "c3",
    buyerId: "2",
    sellerId: "6",
    amount: 18.0,
    pricePerCredit: 24,
    totalPrice: 432,
    timestamp: "2025-01-08T16:30:00Z",
    status: "completed",
  },
  {
    id: "t2",
    creditId: "c4",
    buyerId: "2",
    sellerId: "5",
    amount: 12.5,
    pricePerCredit: 22,
    totalPrice: 275,
    timestamp: "2025-01-06T11:15:00Z",
    status: "completed",
  },
]

export interface Payout {
  id: string
  userId: string
  amount: number
  status: "pending" | "approved" | "rejected" | "completed"
  requestedAt: string
  processedAt?: string
  processedBy?: string
  notes?: string
}

export interface Dispute {
  id: string
  transactionId: string
  reportedBy: string
  reportedAgainst: string
  reason: string
  description: string
  evidence?: string[]
  status: "open" | "investigating" | "resolved" | "closed"
  createdAt: string
  resolvedAt?: string
  resolvedBy?: string
  resolution?: string
}

export interface Listing {
  id: string
  creditId: string
  sellerId: string
  amount: number
  pricePerCredit: number
  status: "active" | "flagged" | "removed" | "sold"
  createdAt: string
  flaggedReason?: string
  moderatedBy?: string
}

export interface Report {
  id: string
  type: "users" | "transactions" | "credits" | "revenue"
  generatedBy: string
  generatedAt: string
  dateRange: { start: string; end: string }
  format: "pdf" | "csv"
  fileUrl?: string
}

export interface UserActivity {
  id: string
  userId: string
  action: string
  details: string
  timestamp: string
  ipAddress?: string
}

export const mockPayouts: Payout[] = [
  {
    id: "p1",
    userId: "1",
    amount: 380,
    status: "pending",
    requestedAt: "2025-01-14T10:00:00Z",
  },
  {
    id: "p2",
    userId: "5",
    amount: 550,
    status: "approved",
    requestedAt: "2025-01-12T14:30:00Z",
    processedAt: "2025-01-13T09:15:00Z",
    processedBy: "4",
  },
  {
    id: "p3",
    userId: "6",
    amount: 432,
    status: "completed",
    requestedAt: "2025-01-10T11:00:00Z",
    processedAt: "2025-01-11T16:20:00Z",
    processedBy: "4",
  },
]

export const mockDisputes: Dispute[] = [
  {
    id: "d1",
    transactionId: "t1",
    reportedBy: "2",
    reportedAgainst: "6",
    reason: "Quality Issue",
    description: "The carbon credits do not match the journey documentation provided.",
    status: "open",
    createdAt: "2025-01-13T15:30:00Z",
  },
  {
    id: "d2",
    transactionId: "t2",
    reportedBy: "7",
    reportedAgainst: "5",
    reason: "Pricing Discrepancy",
    description: "Was charged more than the listed price per credit.",
    status: "investigating",
    createdAt: "2025-01-11T09:45:00Z",
  },
]

export const mockListings: Listing[] = [
  {
    id: "l1",
    creditId: "c1",
    sellerId: "1",
    amount: 15.2,
    pricePerCredit: 25,
    status: "active",
    createdAt: "2025-01-09T12:00:00Z",
  },
  {
    id: "l2",
    creditId: "c2",
    sellerId: "5",
    amount: 22.5,
    pricePerCredit: 23,
    status: "active",
    createdAt: "2025-01-08T09:00:00Z",
  },
  {
    id: "l3",
    creditId: "c5",
    sellerId: "8",
    amount: 10.0,
    pricePerCredit: 30,
    status: "flagged",
    createdAt: "2025-01-12T16:00:00Z",
    flaggedReason: "Suspicious pricing - significantly above market rate",
    moderatedBy: "4",
  },
]

export const mockReports: Report[] = [
  {
    id: "r1",
    type: "revenue",
    generatedBy: "4",
    generatedAt: "2025-01-01T10:00:00Z",
    dateRange: { start: "2024-12-01", end: "2024-12-31" },
    format: "pdf",
    fileUrl: "/reports/revenue-dec-2024.pdf",
  },
  {
    id: "r2",
    type: "users",
    generatedBy: "4",
    generatedAt: "2024-12-28T14:30:00Z",
    dateRange: { start: "2024-01-01", end: "2024-12-31" },
    format: "csv",
    fileUrl: "/reports/users-2024.csv",
  },
]

export const mockUserActivities: UserActivity[] = [
  {
    id: "a1",
    userId: "1",
    action: "Journey Uploaded",
    details: "Uploaded journey from San Francisco to San Jose",
    timestamp: "2025-01-14T09:30:00Z",
    ipAddress: "192.168.1.1",
  },
  {
    id: "a2",
    userId: "2",
    action: "Credit Purchased",
    details: "Purchased 18.0 tCO₂ for $432",
    timestamp: "2025-01-13T16:45:00Z",
    ipAddress: "192.168.1.2",
  },
  {
    id: "a3",
    userId: "1",
    action: "Profile Updated",
    details: "Changed email address",
    timestamp: "2025-01-12T11:20:00Z",
    ipAddress: "192.168.1.1",
  },
]
