'use client';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Wallet as WalletIcon, TrendingUp, DollarSign, Clock } from 'lucide-react';
import { Wallet } from '@/types';

interface WalletBalanceCardProps {
  wallet: Wallet;
}

export function WalletBalanceCard({ wallet }: WalletBalanceCardProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <Card className="bg-gradient-to-br from-green-500 to-green-600 text-white">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-medium">Total Balance</CardTitle>
            <WalletIcon className="w-5 h-5 opacity-80" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold">${wallet.balance.toFixed(2)}</div>
          <p className="text-sm opacity-90 mt-1">{wallet.currency}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-medium text-gray-900">Available</CardTitle>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">${wallet.availableBalance.toFixed(2)}</div>
          <p className="text-sm text-gray-500 mt-1">Ready to withdraw</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-medium text-gray-900">Pending</CardTitle>
            <Clock className="w-5 h-5 text-orange-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">${wallet.pendingBalance.toFixed(2)}</div>
          <p className="text-sm text-gray-500 mt-1">In processing</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg font-medium text-gray-900">Total Earnings</CardTitle>
            <TrendingUp className="w-5 h-5 text-blue-600" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold text-gray-900">${wallet.totalEarnings.toFixed(2)}</div>
          <p className="text-sm text-gray-500 mt-1">All-time revenue</p>
        </CardContent>
      </Card>
    </div>
  );
}
