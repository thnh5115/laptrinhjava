'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAppContext } from '@/contexts/AppContext';
import { LoginForm } from '@/components/auth/LoginForm';

export default function Home() {
  const { state } = useAppContext();
  const router = useRouter();

  useEffect(() => {
    if (state.currentUser) {
      router.push('/dashboard');
    }
  }, [state.currentUser, router]);

  if (state.currentUser) {
    return null; // Will redirect
  }

  return <LoginForm />;
}