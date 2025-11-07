import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Middleware - NO-OP for localStorage-based auth
 * 
 * CRITICAL: Middleware runs on server-side (Edge Runtime) and CANNOT read localStorage.
 * 
 * Authentication Strategy:
 * 1. ❌ Server-side middleware: NO-OP (cannot access localStorage)
 * 2. ✅ Client-side AuthContext: Reads token from localStorage on mount
 * 3. ✅ Client-side layout guards: Redirect to /login if no token
 * 
 * This middleware allows ALL requests through.
 * Protection happens purely client-side where localStorage is available.
 */
export function middleware(_request: NextRequest) {
  // NO-OP: Allow all requests through
  // Client-side guards in app/admin/layout.tsx will handle authentication
  return NextResponse.next();
}

// Optional: Only apply to API routes if needed for CORS/headers
// IMPORTANT: Do NOT match /admin/** - it breaks localStorage auth
export const config = {
  matcher: ['/api/:path*'], // Only API routes, NOT /admin/**
};

