'use client';

/**
 * 403 Forbidden Page
 * Shown when user tries to access a page they don't have permission for
 */

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/lib/contexts/AuthContext";
import { useRouter } from "next/navigation";

export default function ForbiddenPage() {
  const { user, logout } = useAuth();
  const router = useRouter();

  const handleGoToDashboard = () => {
    if (user?.role === 'BUYER') {
      router.push('/buyer/dashboard');
    } else if (user?.role === 'OWNER') {
      router.push('/owner/dashboard');
    } else if (user?.role === 'CVA') {
      router.push('/cva/dashboard');
    } else if (user?.role === 'ADMIN') {
      router.push('/admin/dashboard');
    } else {
      router.push('/');
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-background">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl text-destructive">403 - Access Denied</CardTitle>
          <CardDescription>
            You don't have permission to access this page
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Your current role: <span className="font-semibold">{user?.role || 'Unknown'}</span>
          </p>
          <p className="text-sm text-muted-foreground">
            This page is restricted to specific user roles. Please contact your administrator if you believe this is an error.
          </p>
          <div className="flex flex-col gap-2">
            <Button
              onClick={handleGoToDashboard}
              className="w-full bg-emerald-600 hover:bg-emerald-700"
            >
              Go to My Dashboard
            </Button>
            <Button
              onClick={() => logout()}
              variant="outline"
              className="w-full"
            >
              Logout
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
