'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useAppContext } from '@/contexts/AppContext';
import { LoadingSpinner } from '@/components/ui/loading-spinner';
import { Leaf } from 'lucide-react';

export function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login, state } = useAppContext();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    const success = await login(email, password);
    if (!success) {
      setError('Invalid credentials. Please try again.');
    }
  };

  const demoAccounts = [
    { email: 'john.tesla@email.com', role: 'EV Owner', description: 'Generate and sell carbon credits' },
    { email: 'sarah.green@company.com', role: 'Buyer', description: 'Purchase carbon credits' },
    { email: 'mike.verify@audit.com', role: 'CVA Auditor', description: 'Verify carbon credit authenticity' },
    { email: 'admin@carboncredits.com', role: 'Admin', description: 'System administration' },
  ];

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-blue-50 p-4">
      <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left side - Branding */}
        <div className="flex flex-col justify-center space-y-6">
          <div className="text-center lg:text-left">
            <div className="flex items-center justify-center lg:justify-start space-x-3 mb-6">
              <div className="w-12 h-12 bg-green-500 rounded-xl flex items-center justify-center">
                <Leaf className="w-8 h-8 text-white" />
              </div>
              <h1 className="text-3xl font-bold text-gray-900">CarbonCredit</h1>
            </div>
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Carbon Credit Marketplace for EV Owners
            </h2>
            <p className="text-lg text-gray-600 mb-8">
              Generate, verify, and trade carbon credits from your electric vehicle usage. 
              Join the sustainable transportation revolution.
            </p>
          </div>
          
          {/* Demo Accounts */}
          <div className="bg-white rounded-lg p-6 shadow-sm">
            <h3 className="font-semibold text-gray-900 mb-4">Demo Accounts</h3>
            <div className="space-y-3">
              {demoAccounts.map((account) => (
                <div key={account.email} className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-gray-900">{account.role}</p>
                    <p className="text-sm text-gray-500">{account.description}</p>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      setEmail(account.email);
                      setPassword('demo123');
                    }}
                  >
                    Use
                  </Button>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right side - Login Form */}
        <div className="flex items-center justify-center">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Welcome Back</CardTitle>
              <CardDescription>
                Sign in to your carbon credit marketplace account
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                {error && (
                  <Alert variant="destructive">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}
                <Button type="submit" className="w-full" disabled={state.isLoading}>
                  {state.isLoading ? (
                    <>
                      <LoadingSpinner size="sm" className="mr-2" />
                      Signing in...
                    </>
                  ) : (
                    'Sign In'
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}