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
