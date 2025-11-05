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

const API_BASE_URL = process.env.NEXT_PUBLIC_CVA_API_URL ?? "http://localhost:8082"

function getAuthHeader(): string | undefined {
  const token = process.env.NEXT_PUBLIC_CVA_BASIC_TOKEN
  if (!token) {
    return undefined
  }
  return token.startsWith("Basic ") ? token : `Basic ${token}`
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
