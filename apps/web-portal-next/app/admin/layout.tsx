'use client';

/**
 * Admin Layout - Unified Layout with Auth Guard + Sidebar
 * 1. Protects all /admin/* routes with authentication AND role verification
 * 2. Wraps all admin pages with DashboardLayout (sidebar + header)
 * 3. Uses centralized navigation from lib/config/admin-navigation.ts
 * 
 * Redirects to:
 * - /login if not authenticated
 * - /403 or appropriate dashboard if wrong role
 */

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';
import { DashboardLayout } from '@/components/layout/dashboard-layout';
import { adminNavigation } from '@/lib/config/admin-navigation';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { status, user } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const [mounted, setMounted] = useState(false);

  // Don't protect /login route
  const isLoginPage = pathname === '/login';

  // Only render on client-side to avoid hydration mismatch
  useEffect(() => {
    setMounted(true);
  }, []);

  // Handle redirects based on authentication and role
  useEffect(() => {
    console.log('[AdminLayout] Status:', status, 'User:', user?.email, 'Role:', user?.role, 'Path:', pathname);

    // Skip redirects for login page
    if (isLoginPage) {
      return;
    }

    // CRITICAL: Wait for auth check to complete
    // status='idle' means AuthContext is still checking localStorage
    // status='loading' means /me API call is in progress
    // Only redirect when status is definitively 'unauthenticated' or 'authenticated'
    if (status === 'idle' || status === 'loading') {
      console.log('[AdminLayout] Still checking auth, waiting...');
      return;
    }

    // Redirect to login if unauthenticated (after check completed)
    if (status === 'unauthenticated') {
      console.log('[AdminLayout] Not authenticated, redirecting to /login...');
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }

    // Check role once authenticated
    if (status === 'authenticated' && user) {
      if (user.role !== 'ADMIN') {
        console.warn('[AdminLayout] User is not ADMIN, role:', user.role);
        
        // Redirect to appropriate dashboard based on role
        if (user.role === 'BUYER') {
          router.replace('/buyer/dashboard');
        } else if (user.role === 'OWNER') {
          router.replace('/owner/dashboard');
        } else if (user.role === 'CVA') {
          router.replace('/cva/dashboard');
        } else {
          // Unknown role - redirect to forbidden page
          router.replace('/403');
        }
        return;
      }

      console.log('[AdminLayout] User authorized as ADMIN');
    }
  }, [status, user, router, isLoginPage, pathname]);

  // Don't render anything on server or until mounted
  if (!mounted) {
    return null;
  }

  // Show loading state while checking authentication (but not on login page)
  if ((status === 'idle' || status === 'loading') && !isLoginPage) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600 mx-auto mb-4"></div>
          <p className="text-muted-foreground">Verifying access...</p>
        </div>
      </div>
    );
  }

  // Only render children if authenticated with ADMIN role OR on login page
  if (isLoginPage || (status === 'authenticated' && user?.role === 'ADMIN')) {
    // Wrap with DashboardLayout to provide consistent sidebar across all admin pages
    return (
      <DashboardLayout navigation={adminNavigation}>
        {children}
      </DashboardLayout>
    );
  }

  // Show loading while redirecting
  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600 mx-auto mb-4"></div>
        <p className="text-muted-foreground">Redirecting...</p>
      </div>
    </div>
  );
}

