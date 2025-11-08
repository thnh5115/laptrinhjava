export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED"

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  sort?: string
}

export interface CreditIssuance {
  id: string
  co2ReducedKg: number
  creditsRaw: number
  creditsRounded: number
  idempotencyKey: string
  correlationId?: string | null
  createdAt: string
}

export interface VerificationRequest {
  id: string
  ownerId: string
  tripId: string
  distanceKm: number
  energyKwh: number
  checksum: string
  status: VerificationStatus
  createdAt: string
  verifiedAt?: string | null
  verifierId?: string | null
  notes?: string | null
  creditIssuance?: CreditIssuance | null
}

export interface VerificationQuery {
  status?: VerificationStatus
  ownerId?: string
  createdFrom?: string
  createdTo?: string
  search?: string
  page?: number
  size?: number
}

export interface AnalyticsDailyMetric {
  date: string
  submissions: number
  approvals: number
  rejections: number
  creditsIssued: number
}

export interface AnalyticsOverviewResponse {
  totalRequests: number
  pendingRequests: number
  approvedRequests: number
  rejectedRequests: number
  approvalRate: number
  rejectionRate: number
  totalCreditsIssued: number
  creditsIssuedInWindow: number
  requestsInWindow: number
  recentTrend: AnalyticsDailyMetric[]
}

export interface AnalyticsSummaryResponse {
  totalRequests: number
  pendingRequests: number
  approvedRequests: number
  rejectedRequests: number
  approvalRate: number
  rejectionRate: number
  totalCreditsIssued: number
}

export interface AnalyticsSeriesResponse {
  from: string
  to: string
  data: AnalyticsDailyMetric[]
}

export interface AnalyticsSeriesQuery {
  from?: string | Date
  to?: string | Date
}

const API_BASE_URL = process.env.NEXT_PUBLIC_CVA_API_URL ?? "http://localhost:8082"
const DEV_FALLBACK_TOKEN = "Basic Y3ZhX29mZmljZXI6cGFzc3dvcmQxMjM="

function getAuthHeader(): string | undefined {
  const token = process.env.NEXT_PUBLIC_CVA_BASIC_TOKEN?.trim()
  if (token && token.length > 0) {
    return token.startsWith("Basic ") ? token : `Basic ${token}`
  }

  if (process.env.NODE_ENV !== "production") {
    return DEV_FALLBACK_TOKEN
  }

  return undefined
}

function generateUuid(): string {
  const globalCrypto = (globalThis as { crypto?: Crypto }).crypto
  if (globalCrypto && typeof globalCrypto.randomUUID === "function") {
    return globalCrypto.randomUUID()
  }
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (char) => {
    const r = (Math.random() * 16) | 0
    const v = char === "x" ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

async function fetchJson<T>(input: string, init?: RequestInit): Promise<T> {
  const url = input.startsWith("http") ? input : `${API_BASE_URL}${input}`
  const authHeader = getAuthHeader()
  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(init?.headers instanceof Headers ? Object.fromEntries(init.headers.entries()) : (init?.headers as Record<string, string> | undefined) ?? {}),
  }
  if (authHeader && !headers.Authorization) {
    headers.Authorization = authHeader
  }

  const response = await fetch(url, {
    cache: "no-store",
    ...init,
    headers,
  })

  if (!response.ok) {
    const detail = await safeParseError(response)
    throw new Error(detail)
  }

  return (await response.json()) as T
}

async function safeParseError(response: Response): Promise<string> {
  try {
    const payload = await response.json()
    if (payload?.message) {
      return `${response.status} ${response.statusText} - ${payload.message}`
    }
  } catch (err) {
    // ignore parse failure
  }
  return `${response.status} ${response.statusText}`
}

function normaliseDateInput(value: string | Date): string {
  if (value instanceof Date) {
    return value.toISOString().slice(0, 10)
  }
  if (value.length === 10 && value.includes("-")) {
    return value
  }
  return new Date(value).toISOString().slice(0, 10)
}

export async function getAnalyticsOverview(windowDays = 30): Promise<AnalyticsOverviewResponse> {
  const params = new URLSearchParams()
  if (typeof windowDays === "number" && !Number.isNaN(windowDays)) {
    params.set("windowDays", windowDays.toString())
  }

  const path = `/api/cva/analytics/overview${params.size > 0 ? `?${params.toString()}` : ""}`
  return fetchJson<AnalyticsOverviewResponse>(path)
}

export async function getAnalyticsSummary(): Promise<AnalyticsSummaryResponse> {
  return fetchJson<AnalyticsSummaryResponse>("/api/cva/analytics/summary")
}

export async function getAnalyticsSeries(query: AnalyticsSeriesQuery = {}): Promise<AnalyticsSeriesResponse> {
  const params = new URLSearchParams()
  if (query.from) {
    params.set("from", normaliseDateInput(query.from))
  }
  if (query.to) {
    params.set("to", normaliseDateInput(query.to))
  }

  const path = `/api/cva/analytics/series${params.size > 0 ? `?${params.toString()}` : ""}`
  return fetchJson<AnalyticsSeriesResponse>(path)
}

export async function listVerificationRequests(query: VerificationQuery = {}): Promise<PageResponse<VerificationRequest>> {
  const params = new URLSearchParams()
  if (query.status) params.set("status", query.status)
  if (query.ownerId) params.set("ownerId", query.ownerId)
  if (query.createdFrom) params.set("createdFrom", query.createdFrom)
  if (query.createdTo) params.set("createdTo", query.createdTo)
  if (query.search) params.set("search", query.search)
  if (typeof query.page === "number") params.set("page", query.page.toString())
  if (typeof query.size === "number") params.set("size", query.size.toString())

  const path = `/api/cva/requests${params.size > 0 ? `?${params.toString()}` : ""}`
  return fetchJson<PageResponse<VerificationRequest>>(path)
}

export async function getVerificationRequest(id: string): Promise<VerificationRequest> {
  return fetchJson<VerificationRequest>(`/api/cva/requests/${id}`)
}

interface ApprovePayload {
  verifierId: string
  notes?: string
  idempotencyKey?: string
  correlationId?: string
}

export async function approveVerificationRequest(id: string, payload: ApprovePayload): Promise<VerificationRequest> {
  const authHeader = getAuthHeader()
  const body = JSON.stringify({
    ...payload,
    idempotencyKey: payload.idempotencyKey ?? generateUuid(),
    correlationId: payload.correlationId ?? generateUuid(),
  })

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  }
  if (authHeader) {
    headers.Authorization = authHeader
  }

  const response = await fetch(`${API_BASE_URL}/api/cva/requests/${id}/approve`, {
    method: "PUT",
    cache: "no-store",
    headers,
    body,
  })

  if (!response.ok) {
    throw new Error(await safeParseError(response))
  }

  return (await response.json()) as VerificationRequest
}

interface RejectPayload {
  verifierId: string
  reason: string
  correlationId?: string
}

export async function rejectVerificationRequest(id: string, payload: RejectPayload): Promise<VerificationRequest> {
  const authHeader = getAuthHeader()
  const body = JSON.stringify({
    ...payload,
    correlationId: payload.correlationId ?? generateUuid(),
  })

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  }
  if (authHeader) {
    headers.Authorization = authHeader
  }

  const response = await fetch(`${API_BASE_URL}/api/cva/requests/${id}/reject`, {
    method: "PUT",
    cache: "no-store",
    headers,
    body,
  })

  if (!response.ok) {
    throw new Error(await safeParseError(response))
  }

  return (await response.json()) as VerificationRequest
}

export async function downloadReportPdf(id: string): Promise<Blob> {
  const authHeader = getAuthHeader()
  const headers: Record<string, string> = {}
  if (authHeader) {
    headers.Authorization = authHeader
  }

  const response = await fetch(`${API_BASE_URL}/api/cva/reports/${id}?format=pdf`, {
    method: "GET",
    cache: "no-store",
    headers,
  })

  if (!response.ok) {
    throw new Error(await safeParseError(response))
  }

  return await response.blob()
}
