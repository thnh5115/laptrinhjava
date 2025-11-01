import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// Define protected routes by role
const roleBasedRoutes = {
  admin: ['/admin'],
  buyer: ['/buyer'],
  cva: ['/cva'],
  owner: ['/owner'],
};

// Public routes that don't require authentication
const publicRoutes = ['/login', '/register', '/'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Allow public routes
  if (publicRoutes.some(route => pathname.startsWith(route))) {
    return NextResponse.next();
  }

  // Check if user is authenticated (has JWT token)
  const token = request.cookies.get('access_token')?.value;

  if (!token) {
    // Redirect to login if not authenticated
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // TODO: Decode JWT to get user role
  // For now, we'll extract role from pathname
  // In production, you should decode the JWT token to get the actual user role
  
  // Check role-based access
  for (const [role, routes] of Object.entries(roleBasedRoutes)) {
    if (routes.some(route => pathname.startsWith(route))) {
      // This route belongs to a specific role
      // TODO: Verify user has this role from JWT
      // For now, we'll just check if they're authenticated
      return NextResponse.next();
    }
  }

  return NextResponse.next();
}

// Configure middleware matcher
export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public files (public folder)
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
};
